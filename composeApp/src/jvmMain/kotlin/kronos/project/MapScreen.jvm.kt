package kronos.project

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import javafx.embed.swing.JFXPanel
import javafx.scene.Scene
import javafx.scene.web.WebView
import kronos.project.map.MapDefaults
import java.awt.BorderLayout
import javax.swing.JPanel
import javafx.application.Platform
import kronos.project.map.MapMarker

@Composable
actual fun PlatformMapHost(modifier: Modifier, markers: List<MapMarker>) {
    val mapTilerApiKey = remember { resolveDesktopMapTilerKey() }
    val html = remember(markers, mapTilerApiKey) { desktopMapHtml(mapTilerApiKey, markers) }

    SwingPanel(
        modifier = modifier.fillMaxSize(),
        factory = {
            DesktopMapPanel().apply {
                loadHtml(html)
            }
        },
        update = {
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

    init {
        add(fxPanel, BorderLayout.CENTER)
        Platform.setImplicitExit(false)
        Platform.runLater {
            val view = WebView()
            webView = view
            fxPanel.scene = Scene(view)
        }
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
        "{id:\"${it.id}\",lat:${it.latitude},lng:${it.longitude},title:\"${it.title.replace("\"", "\\\"")}\"}"
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

          map.addControl(new maplibregl.NavigationControl(), "top-right");

          const markers = $markersJson;
          markers.forEach((item) => {
            const popup = new maplibregl.Popup({ offset: 20 }).setText(item.title || "Reported issue");
            new maplibregl.Marker({ color: "#FF3D00" })
              .setLngLat([item.lng, item.lat])
              .setPopup(popup)
              .addTo(map);
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

