package kronos.project

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import javafx.application.Platform
import javafx.embed.swing.JFXPanel
import javafx.scene.Scene
import javafx.scene.web.WebView
import kronos.project.map.MapDefaults
import kronos.project.map.MapMarker
import java.awt.BorderLayout
import java.awt.EventQueue
import javax.swing.JPanel

@Composable
actual fun PlatformMapHost(
    modifier: Modifier,
    markers: List<MapMarker>,
    onMapClick: (Double, Double) -> Unit,
) {
    val mapTilerApiKey = remember { resolveDesktopMapTilerKey() }
    val onMapClickState = rememberUpdatedState(onMapClick)
    val html = remember(markers, mapTilerApiKey) { desktopMapHtml(mapTilerApiKey, markers) }

    SwingPanel(
        modifier = modifier.fillMaxSize(),
        factory = {
            DesktopMapPanel().apply {
                setOnMapTapListener { lat, lng -> onMapClickState.value(lat, lng) }
                loadHtml(html)
            }
        },
        update = {
            it.setOnMapTapListener { lat, lng -> onMapClickState.value(lat, lng) }
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

    init {
        add(fxPanel, BorderLayout.CENTER)
        Platform.setImplicitExit(false)
        Platform.runLater {
            val view = WebView()
            view.engine.setOnAlert { event ->
                val payload = event.data ?: return@setOnAlert
                if (!payload.startsWith("GEL_MAP_TAP:")) return@setOnAlert

                val coords = payload.removePrefix("GEL_MAP_TAP:").split(',')
                if (coords.size != 2) return@setOnAlert

                val lat = coords[0].toDoubleOrNull() ?: return@setOnAlert
                val lng = coords[1].toDoubleOrNull() ?: return@setOnAlert
                EventQueue.invokeLater {
                    onMapTap?.invoke(lat, lng)
                }
            }
            webView = view
            fxPanel.scene = Scene(view)
        }
    }

    fun setOnMapTapListener(listener: (Double, Double) -> Unit) {
        onMapTap = listener
    }

    fun loadHtml(html: String) {
        Platform.runLater {
            webView?.engine?.loadContent(html)
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
        val commentsJson = it.card?.comments?.joinToString(",", prefix = "[", postfix = "]") { comment ->
            val author = comment.author.toJsStringLiteral()
            val content = comment.content.toJsStringLiteral()
            val votes = "+${comment.upvotes}/-${comment.downvotes}".toJsStringLiteral()
            "{author:\"$author\",content:\"$content\",votes:\"$votes\"}"
        } ?: "[]"

        "{id:\"${it.id.toJsStringLiteral()}\",lat:${it.latitude},lng:${it.longitude},title:\"${it.title.toJsStringLiteral()}\",cardTitle:\"$cardTitle\",mainAuthor:\"$mainAuthor\",mainContent:\"$mainContent\",mainVotes:\"$mainVotes\",comments:$commentsJson}"
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
        </style>
      </head>
      <body>
        <div id="gel-map"></div>
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

          const isMarkerTarget = (event) => {
            const originalEvent = event && event.originalEvent;
            const target = originalEvent && originalEvent.target;
            return !!(target && target.closest && target.closest(".maplibregl-marker"));
          };

          const markers = $markersJson;
          const escapeHtml = (value) => String(value || "")
            .replace(/&/g, "&amp;")
            .replace(/</g, "&lt;")
            .replace(/>/g, "&gt;")
            .replace(/\"/g, "&quot;")
            .replace(/'/g, "&#39;");

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

            return `<div style=\"min-width:250px;max-width:304px;box-sizing:border-box;padding:2px 4px;overflow:hidden;\">`
              + `${'$'}{title}`
              + `<div style=\"margin-top:8px;padding:8px;border:1px solid #eceff1;border-radius:10px;background:#f8f9fa;box-sizing:border-box;\">${'$'}{mainMeta}${'$'}{mainBody}</div>`
              + `${'$'}{commentsSection}`
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

            new maplibregl.Marker({ color: "#FF3D00" })
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

