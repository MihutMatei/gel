package kronos.project

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import javafx.application.Platform
import javafx.embed.swing.JFXPanel
import javafx.scene.Scene
import javafx.scene.web.WebView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kronos.project.data.remote.dto.PinCommentDto
import kronos.project.map.MapDefaults
import kronos.project.map.MapMarker
import java.awt.BorderLayout
import java.awt.EventQueue
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import javax.swing.JPanel

@Composable
actual fun PlatformMapHost(
    modifier: Modifier,
    markers: List<MapMarker>,
    onMapClick: (Double, Double) -> Unit,
) {
    val mapTilerApiKey = remember { resolveDesktopMapTilerKey() }
    val scope = rememberCoroutineScope()
    val onMapClickState = rememberUpdatedState(onMapClick)
    val html = remember(markers, mapTilerApiKey) { desktopMapHtml(mapTilerApiKey, markers) }

    SwingPanel(
        modifier = modifier.fillMaxSize(),
        factory = {
            DesktopMapPanel().apply {
                setOnMapTapListener { lat, lng -> onMapClickState.value(lat, lng) }
                setOnSubmitCommentListener { pinId, content ->
                    scope.launch {
                        val userId = Dependencies.currentUserId.value
                        if (userId.isNullOrBlank()) {
                            pushCommentError("Please login first")
                            return@launch
                        }

                        val created = withContext(Dispatchers.IO) {
                            Dependencies.pinRepository.createPinComment(pinId, userId, content)
                        }

                        created
                            .onSuccess { createdComment ->
                                val comments = withContext(Dispatchers.IO) {
                                    Dependencies.pinRepository.fetchPinComments(pinId)
                                }.getOrElse { listOf(createdComment) }
                                pushCommentsToJs(pinId, comments)
                            }
                            .onFailure {
                                pushCommentError(it.message.orEmpty())
                            }
                    }
                }
                setOnLoadCommentsListener { pinId ->
                    scope.launch {
                        val comments = withContext(Dispatchers.IO) {
                            Dependencies.pinRepository.fetchPinComments(pinId)
                        }
                        comments.onSuccess { pushCommentsToJs(pinId, it) }
                    }
                }
                loadHtml(html)
            }
        },
        update = {
            it.setOnMapTapListener { lat, lng -> onMapClickState.value(lat, lng) }
            it.setOnSubmitCommentListener { pinId, content ->
                scope.launch {
                    val userId = Dependencies.currentUserId.value
                    if (userId.isNullOrBlank()) {
                        it.pushCommentError("Please login first")
                        return@launch
                    }

                    val created = withContext(Dispatchers.IO) {
                        Dependencies.pinRepository.createPinComment(pinId, userId, content)
                    }

                    created
                        .onSuccess { createdComment ->
                            val comments = withContext(Dispatchers.IO) {
                                Dependencies.pinRepository.fetchPinComments(pinId)
                            }.getOrElse { listOf(createdComment) }
                            it.pushCommentsToJs(pinId, comments)
                        }
                        .onFailure { throwable ->
                            it.pushCommentError(throwable.message.orEmpty())
                        }
                }
            }
            it.setOnLoadCommentsListener { pinId ->
                scope.launch {
                    val comments = withContext(Dispatchers.IO) {
                        Dependencies.pinRepository.fetchPinComments(pinId)
                    }
                    comments.onSuccess { loaded -> it.pushCommentsToJs(pinId, loaded) }
                }
            }
            it.loadHtml(html)
        },
    )

    DisposableEffect(Unit) {
        onDispose {
            Platform.runLater {
                Platform.setImplicitExit(false)
            }
        }
    }
}

@Composable
actual fun rememberLocationPermissionGranted(): Boolean = true

private class DesktopMapPanel : JPanel(BorderLayout()) {
    private val fxPanel = JFXPanel()
    private var webView: WebView? = null
    private var onMapTap: ((Double, Double) -> Unit)? = null
    private var onSubmitComment: ((String, String) -> Unit)? = null
    private var onLoadComments: ((String) -> Unit)? = null

    init {
        add(fxPanel, BorderLayout.CENTER)
        Platform.setImplicitExit(false)
        Platform.runLater {
            val view = WebView()
            view.engine.setOnAlert { event ->
                val payload = event.data ?: return@setOnAlert
                when {
                    payload.startsWith("GEL_MAP_TAP:") -> {
                        val coords = payload.removePrefix("GEL_MAP_TAP:").split(',')
                        if (coords.size != 2) return@setOnAlert

                        val lat = coords[0].toDoubleOrNull() ?: return@setOnAlert
                        val lng = coords[1].toDoubleOrNull() ?: return@setOnAlert
                        EventQueue.invokeLater {
                            onMapTap?.invoke(lat, lng)
                        }
                    }

                    payload.startsWith("GEL_COMMENT_SUBMIT:") -> {
                        val data = payload.removePrefix("GEL_COMMENT_SUBMIT:").split(':', limit = 2)
                        if (data.size != 2) return@setOnAlert
                        val pinId = URLDecoder.decode(data[0], StandardCharsets.UTF_8.name())
                        val content = URLDecoder.decode(data[1], StandardCharsets.UTF_8.name())
                        EventQueue.invokeLater {
                            onSubmitComment?.invoke(pinId, content)
                        }
                    }

                    payload.startsWith("GEL_COMMENT_LOAD:") -> {
                        val pinId = URLDecoder.decode(payload.removePrefix("GEL_COMMENT_LOAD:"), StandardCharsets.UTF_8.name())
                        EventQueue.invokeLater {
                            onLoadComments?.invoke(pinId)
                        }
                    }
                }
            }
            webView = view
            fxPanel.scene = Scene(view)
        }
    }

    fun setOnMapTapListener(listener: (Double, Double) -> Unit) {
        onMapTap = listener
    }

    fun setOnSubmitCommentListener(listener: (String, String) -> Unit) {
        onSubmitComment = listener
    }

    fun setOnLoadCommentsListener(listener: (String) -> Unit) {
        onLoadComments = listener
    }

    fun loadHtml(html: String) {
        Platform.runLater {
            webView?.engine?.loadContent(html)
        }
    }

    fun pushCommentsToJs(pinId: String, comments: List<PinCommentDto>) {
        val script = "window.onCommentsLoaded && window.onCommentsLoaded(\"${pinId.toJsStringLiteral()}\", ${comments.toJsCommentsLiteral()});"
        Platform.runLater {
            webView?.engine?.executeScript(script)
        }
    }

    fun pushCommentError(message: String) {
        val script = "window.onCommentPersistError && window.onCommentPersistError(\"${message.toJsStringLiteral()}\");"
        Platform.runLater {
            webView?.engine?.executeScript(script)
        }
    }
}

private fun resolveDesktopMapTilerKey(): String {
    val envKey = System.getenv("MAPTILER_API_KEY")?.trim().orEmpty()
    if (envKey.isNotEmpty()) return envKey

    return System.getProperty("MAPTILER_API_KEY")?.trim().orEmpty()
}

private fun desktopMapHtml(mapTilerApiKey: String, markers: List<MapMarker>): String {
    val markersJson = markers.joinToString(",", prefix = "[", postfix = "]") {
        val cardTitle = it.card?.title?.toJsStringLiteral().orEmpty()
        val mainAuthor = it.card?.mainPost?.author?.toJsStringLiteral().orEmpty()
        val mainContent = it.card?.mainPost?.content?.toJsStringLiteral().orEmpty()
        val mainVotes = it.card?.mainPost?.let { post -> "+${post.upvotes}/-${post.downvotes}" }?.toJsStringLiteral().orEmpty()
        val category = it.category.toJsStringLiteral()
        val commentsJson = it.card?.comments?.joinToString(",", prefix = "[", postfix = "]") { comment ->
            val author = comment.author.toJsStringLiteral()
            val content = comment.content.toJsStringLiteral()
            val votes = "+${comment.upvotes}/-${comment.downvotes}".toJsStringLiteral()
            "{author:\"$author\",content:\"$content\",votes:\"$votes\"}"
        } ?: "[]"

        "{id:\"${it.id.toJsStringLiteral()}\",lat:${it.latitude},lng:${it.longitude},title:\"${it.title.toJsStringLiteral()}\",category:\"$category\",cardTitle:\"$cardTitle\",mainAuthor:\"$mainAuthor\",mainContent:\"$mainContent\",mainVotes:\"$mainVotes\",comments:$commentsJson}"
    }

    return """
    <!doctype html>
    <html>
      <head>
        <meta charset="utf-8" />
        <meta name="viewport" content="width=device-width, initial-scale=1" />
        <link href="https://unpkg.com/maplibre-gl@4.4.1/dist/maplibre-gl.css" rel="stylesheet" />
        <style>
          html, body, #gel-map {
            margin: 0;
            width: 100%;
            height: 100%;
            overflow: hidden;
            background: #0f1720;
          }

          .maplibregl-ctrl-top-right {
            top: 16px;
            right: 16px;
          }

          .maplibregl-ctrl-group {
            border: 1px solid #2f3a46 !important;
            border-radius: 14px !important;
            overflow: hidden;
            box-shadow: 0 10px 24px rgba(0, 0, 0, 0.35) !important;
          }

          .maplibregl-ctrl-group button {
            width: 40px !important;
            height: 40px !important;
            background: #131a22 !important;
          }

          .maplibregl-ctrl-group button:hover {
            background: #1a2430 !important;
          }

          .maplibregl-ctrl-group button span {
            filter: brightness(1.25) contrast(1.05);
          }

          .maplibregl-ctrl-bottom-right,
          .maplibregl-ctrl-bottom-left {
            display: none !important;
          }

          #comment-modal-overlay {
            position: fixed;
            inset: 0;
            display: none;
            align-items: center;
            justify-content: center;
            background: rgba(0, 0, 0, 0.45);
            z-index: 1000;
          }

          #comment-modal {
            width: min(92vw, 360px);
            background: #ffffff;
            border-radius: 14px;
            box-shadow: 0 20px 40px rgba(0, 0, 0, 0.3);
            padding: 14px;
            box-sizing: border-box;
          }

          #comment-input {
            width: 100%;
            min-height: 90px;
            resize: vertical;
            border: 1px solid #d8dde3;
            border-radius: 10px;
            font-size: 13px;
            padding: 10px;
            box-sizing: border-box;
          }

          .comment-modal-actions {
            margin-top: 10px;
            display: flex;
            justify-content: flex-end;
            gap: 8px;
          }

          .comment-modal-btn {
            border: none;
            border-radius: 8px;
            padding: 8px 12px;
            font-size: 12px;
            cursor: pointer;
          }

          .comment-modal-btn.cancel {
            background: #eef1f4;
            color: #1f2937;
          }

          .comment-modal-btn.submit {
            background: #ff3d00;
            color: #ffffff;
          }
        </style>
      </head>
      <body>
        <div id="gel-map"></div>
        <div id="comment-modal-overlay">
          <div id="comment-modal">
            <div style="font-weight:700;font-size:14px;color:#1f2937;margin-bottom:8px;">Add Comment</div>
            <textarea id="comment-input" placeholder="Share more details..."></textarea>
            <div class="comment-modal-actions">
              <button class="comment-modal-btn cancel" onclick="closeCommentModal()">Cancel</button>
              <button class="comment-modal-btn submit" onclick="submitComment()">Submit</button>
            </div>
          </div>
        </div>
        <script src="https://unpkg.com/maplibre-gl@4.4.1/dist/maplibre-gl.js"></script>
        <script>
          const MAPTILER_API_KEY = "$mapTilerApiKey";
          const styleBase = "${MapDefaults.styleUrl}";
          const styleUrl = MAPTILER_API_KEY ? `${'$'}{styleBase}?key=${'$'}{MAPTILER_API_KEY}` : styleBase;

          const map = new maplibregl.Map({
            container: "gel-map",
            style: styleUrl,
            center: [${MapDefaults.centerLongitude}, ${MapDefaults.centerLatitude}],
            zoom: ${MapDefaults.zoom},
            pitch: ${MapDefaults.pitch},
            bearing: ${MapDefaults.bearing},
            antialias: true,
            attributionControl: false,
          });

          map.addControl(new maplibregl.NavigationControl({ showZoom: false, showCompass: false }), "top-right");

          const emitMapTap = (lat, lng) => {
            alert(`GEL_MAP_TAP:${'$'}{lat},${'$'}{lng}`);
          };

          const emitCommentSubmit = (pinId, content) => {
            alert(`GEL_COMMENT_SUBMIT:${'$'}{encodeURIComponent(pinId)}:${'$'}{encodeURIComponent(content)}`);
          };

          const emitCommentLoad = (pinId) => {
            alert(`GEL_COMMENT_LOAD:${'$'}{encodeURIComponent(pinId)}`);
          };

          const isMarkerTarget = (event) => {
            const originalEvent = event && event.originalEvent;
            const target = originalEvent && originalEvent.target;
            return !!(target && target.closest && target.closest(".maplibregl-marker"));
          };

          const markers = $markersJson;
          const markerById = {};
          const popupById = {};
          let activeCommentTargetId = null;

          markers.forEach((item) => {
            markerById[item.id] = item;
          });

          const commentModalOverlay = document.getElementById("comment-modal-overlay");
          const commentInput = document.getElementById("comment-input");

          const escapeHtml = (value) => String(value || "")
            .replace(/&/g, "&amp;")
            .replace(/</g, "&lt;")
            .replace(/>/g, "&gt;")
            .replace(/\"/g, "&quot;")
            .replace(/'/g, "&#39;");

          const categoryColor = (category) => {
            const key = String(category || "").toLowerCase();
            if (key === "utilities") return "#255f85";
            if (key === "public transport") return "#1f7a5f";
            if (key === "parking") return "#7250d4";
            if (key === "crime / safety") return "#8b2d2d";
            if (key === "commerce / store access") return "#7a5a1e";
            return "#3e4c5d";
          };

          const createMarkerElement = (category) => {
            const color = categoryColor(category);
            const marker = document.createElement("div");
            marker.style.width = "22px";
            marker.style.height = "22px";
            marker.style.borderRadius = "999px";
            marker.style.background = `linear-gradient(160deg, #ffffff 0%, ${'$'}{color} 36%, ${'$'}{color} 100%)`;
            marker.style.border = "2px solid #1f2937";
            marker.style.boxShadow = "0 5px 14px rgba(0,0,0,0.42)";
            marker.style.cursor = "pointer";
            return marker;
          };

          const closeCommentModal = () => {
            activeCommentTargetId = null;
            commentInput.value = "";
            commentModalOverlay.style.display = "none";
          };

          const openCommentModal = (pinId) => {
            activeCommentTargetId = pinId;
            commentInput.value = "";
            commentModalOverlay.style.display = "flex";
            setTimeout(() => commentInput.focus(), 0);
          };

          const submitComment = () => {
            const pinId = activeCommentTargetId;
            const text = String(commentInput.value || "").trim();
            if (!pinId || text.length < 2) return;

            if (typeof emitCommentSubmit === "function") {
              emitCommentSubmit(pinId, text);
            } else {
              const item = markerById[pinId];
              if (!item) return;
              if (!Array.isArray(item.comments)) item.comments = [];
              item.comments.push({ author: "you", content: text, votes: "+0/-0" });
              const popup = popupById[pinId];
              if (popup) popup.setHTML(buildCardHtml(item));
            }

            closeCommentModal();
          };

          const requestPinComments = (pinId) => {
            emitCommentLoad(pinId);
          };

          window.onCommentsLoaded = (pinId, comments) => {
            const item = markerById[pinId];
            if (!item) return;
            item.comments = Array.isArray(comments) ? comments : [];
            const popup = popupById[pinId];
            if (popup) popup.setHTML(buildCardHtml(item));
          };

          window.onCommentPersistError = (_message) => {
          };

          commentModalOverlay.addEventListener("click", (event) => {
            if (event.target === commentModalOverlay) {
              closeCommentModal();
            }
          });

          window.openCommentModal = openCommentModal;
          window.closeCommentModal = closeCommentModal;
          window.submitComment = submitComment;

          const buildCardHtml = (item) => {
            if (!item.cardTitle) return "";

            const title = `<div style=\"font-weight:700;font-size:14px;color:#202124;\">${'$'}{escapeHtml(item.cardTitle)}</div>`;
            const mainMeta = `<div style=\"font-size:11px;color:#5f6368;\">u/${'$'}{escapeHtml(item.mainAuthor)} ${'$'}{escapeHtml(item.mainVotes)}</div>`;
            const mainBody = `<div style=\"margin-top:4px;font-size:12px;color:#202124;line-height:1.4;\">${'$'}{escapeHtml(item.mainContent).replace(/\\n/g, "<br/>")}</div>`;

            const comments = Array.isArray(item.comments) ? item.comments : [];
            const commentsHtml = comments.map((comment) => {
              return `<div style=\"margin-top:6px;padding:7px 8px;border:1px solid #eceff1;border-radius:10px;background:#ffffff;\">`
                + `<div style=\"font-size:11px;color:#5f6368;\">u/${'$'}{escapeHtml(comment.author)} ${'$'}{escapeHtml(comment.votes)}</div>`
                + `<div style=\"margin-top:4px;font-size:12px;color:#202124;line-height:1.35;\">${'$'}{escapeHtml(comment.content).replace(/\\n/g, "<br/>")}</div>`
                + `</div>`;
            }).join("");

            const commentsSection = commentsHtml
              ? `<div style=\"margin-top:8px;max-height:142px;overflow-y:auto;padding-right:4px;\">${'$'}{commentsHtml}</div>`
              : "";

            const addCommentButton = `<button onclick=\"openCommentModal('${'$'}{escapeHtml(item.id)}')\" style=\"margin-top:10px;width:100%;border:none;border-radius:10px;padding:8px 10px;background:#ff3d00;color:#ffffff;font-size:12px;font-weight:600;cursor:pointer;\">Add Comment</button>`;

            return `<div style=\"min-width:250px;max-width:304px;box-sizing:border-box;padding:2px 4px;overflow:hidden;\">`
              + `${'$'}{title}`
              + `<div style=\"margin-top:8px;padding:8px;border:1px solid #eceff1;border-radius:10px;background:#f8f9fa;box-sizing:border-box;\">${'$'}{mainMeta}${'$'}{mainBody}</div>`
              + `${'$'}{commentsSection}`
              + `${'$'}{addCommentButton}`
              + `</div>`;
          };

          markers.forEach((item) => {
            const cardHtml = buildCardHtml(item);
            const popup = new maplibregl.Popup({ offset: 20 });
            if (cardHtml) {
              popup.setHTML(cardHtml);
            } else {
              popup.setText(item.title || "Reported issue");
            }
            popupById[item.id] = popup;
            popup.on("open", () => requestPinComments(item.id));

            new maplibregl.Marker({ element: createMarkerElement(item.category), anchor: "bottom" })
              .setLngLat([item.lng, item.lat])
              .setPopup(popup)
              .addTo(map);
          });

          map.on("click", (event) => {
            if (isMarkerTarget(event)) return;
            emitMapTap(event.lngLat.lat, event.lngLat.lng);
          });


          map.on("load", () => {
            const style = map.getStyle();
            const existing = (style.layers || []).find((layer) => layer.id === "${MapDefaults.buildingsLayerId}");
            if (existing) return;

            const buildingLayer = (style.layers || []).find((layer) => layer["source-layer"] === "${MapDefaults.buildingSourceLayer}");
            const sourceName = buildingLayer?.source || Object.keys(style.sources || {})[0];
            if (!sourceName) return;

            map.addLayer({
              id: "${MapDefaults.buildingsLayerId}",
              type: "fill-extrusion",
              source: sourceName,
              "source-layer": "${MapDefaults.buildingSourceLayer}",
              minzoom: ${MapDefaults.buildingsMinZoom},
              paint: {
                "fill-extrusion-color": "${MapDefaults.buildingsColor}",
                "fill-extrusion-height": ["coalesce", ["get", "height"], ["get", "render_height"], 0],
                "fill-extrusion-base": ["coalesce", ["get", "min_height"], ["get", "render_min_height"], 0],
                "fill-extrusion-opacity": ${MapDefaults.buildingsOpacity},
              },
            });
          });
        </script>
      </body>
    </html>
    """.trimIndent()
}

private fun String.toJsStringLiteral(): String = this
    .replace("\\", "\\\\")
    .replace("\"", "\\\"")
    .replace("\n", "\\n")
    .replace("\r", "")

private fun List<PinCommentDto>.toJsCommentsLiteral(): String = joinToString(
    separator = ",",
    prefix = "[",
    postfix = "]",
) { comment ->
    val author = comment.authorId.toJsStringLiteral()
    val content = comment.content.toJsStringLiteral()
    "{author:\"$author\",content:\"$content\",votes:\"+0/-0\"}"
}

