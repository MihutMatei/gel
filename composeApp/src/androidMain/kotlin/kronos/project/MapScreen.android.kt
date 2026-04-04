package kronos.project

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import kronos.project.map.MapDefaults
import kronos.project.map.MapMarker

@Composable
@SuppressLint("SetJavaScriptEnabled")
actual fun PlatformMapHost(
    modifier: Modifier,
    markers: List<MapMarker>,
    onMapClick: (Double, Double) -> Unit,
) {
    val webViewState = remember { mutableStateOf<WebView?>(null) }
    val onMapClickState by rememberUpdatedState(onMapClick)
    val mapTilerApiKey = stringResource(R.string.maptiler_api_key)
    val html = remember(markers, mapTilerApiKey) { androidMapHtml(mapTilerApiKey, markers) }

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = {
                WebView(it).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                    )
                    webViewClient = WebViewClient()
                    addJavascriptInterface(
                        AndroidMapBridge { lat, lng -> onMapClickState(lat, lng) },
                        "AndroidMapBridge",
                    )
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    loadDataWithBaseURL(
                        "https://localhost/",
                        html,
                        "text/html",
                        "UTF-8",
                        null,
                    )
                    tag = html
                    webViewState.value = this
                }
            },
            update = { webView ->
                if (webView.tag != html) {
                    webView.loadDataWithBaseURL("https://localhost/", html, "text/html", "UTF-8", null)
                    webView.tag = html
                }
            },
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            webViewState.value?.apply {
                stopLoading()
                loadUrl("about:blank")
                destroy()
            }
            webViewState.value = null
        }
    }
}


@Composable
actual fun rememberLocationPermissionGranted(): Boolean {
    val context = LocalContext.current
    var granted by remember { mutableStateOf(false) }

    LaunchedEffect(context) {
        val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        granted = fine || coarse
    }

    return granted
}

private fun androidMapHtml(mapTilerApiKey: String, markers: List<MapMarker>): String {
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
            const bridge = window.AndroidMapBridge;
            if (bridge && typeof bridge.onMapTap === "function") {
              bridge.onMapTap(lat, lng);
            }
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

            const title = `<div style=\"font-weight:700;font-size:14px;color:#202124;line-height:1.25;overflow-wrap:anywhere;\">${'$'}{escapeHtml(item.cardTitle)}</div>`;
            const mainMeta = `<div style=\"font-size:11px;color:#5f6368;\">u/${'$'}{escapeHtml(item.mainAuthor)} ${'$'}{escapeHtml(item.mainVotes)}</div>`;
            const mainBody = `<div style=\"margin-top:4px;font-size:12px;color:#202124;line-height:1.4;overflow-wrap:anywhere;\">${'$'}{escapeHtml(item.mainContent).replace(/\\n/g, "<br/>")}</div>`;

            const comments = Array.isArray(item.comments) ? item.comments : [];
            const commentsHtml = comments.map((comment) => {
              return `<div style=\"margin-top:6px;padding:7px 8px;width:100%;box-sizing:border-box;border:1px solid #eceff1;border-radius:10px;background:#ffffff;\">`
                + `<div style=\"font-size:11px;color:#5f6368;\">u/${'$'}{escapeHtml(comment.author)} ${'$'}{escapeHtml(comment.votes)}</div>`
                + `<div style=\"margin-top:4px;font-size:12px;color:#202124;line-height:1.35;overflow-wrap:anywhere;\">${'$'}{escapeHtml(comment.content).replace(/\\n/g, "<br/>")}</div>`
                + `</div>`;
            }).join("");

            const commentsSection = commentsHtml
              ? `<div style=\"margin-top:8px;max-height:142px;overflow-y:auto;overflow-x:hidden;padding-right:4px;box-sizing:border-box;\">${'$'}{commentsHtml}</div>`
              : "";

            return `<div style=\"width:100%;max-width:280px;box-sizing:border-box;padding:2px 4px;overflow:hidden;\">`
              + `${'$'}{title}`
              + `<div style=\"margin-top:8px;padding:8px;width:100%;box-sizing:border-box;border:1px solid #eceff1;border-radius:10px;background:#f8f9fa;\">${'$'}{mainMeta}${'$'}{mainBody}</div>`
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

private class AndroidMapBridge(
    private val onMapTapCallback: (Double, Double) -> Unit,
) {
    private val mainHandler = Handler(Looper.getMainLooper())

    @JavascriptInterface
    fun onMapTap(lat: Double, lng: Double) {
        mainHandler.post {
            onMapTapCallback(lat, lng)
        }
    }
}

