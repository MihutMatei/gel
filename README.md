# gel
Codul echipei gel pentru HackItAll 2026, proba jetbrains

## Civic Map MVP (web-first)

This MVP now boots a **MapLibre** map in the web target and uses a **MapTiler hosted style URL** as the vector/style provider.

### Architecture (current slice)
- `composeApp/src/commonMain`: Compose UI shell (`App`) and platform-agnostic app UI.
- `composeApp/src/jsMain`: web entrypoint (`main.kt`) + MapLibre bootstrap + 2.5D building layer setup.
- `composeApp/src/webMain/resources`: web host page and styles for map container + token injection.

### What is implemented
- Web map container initialization through Kotlin/JS runtime.
- Bucharest initial camera.
- Hosted style URL from MapTiler.
- 3D buildings layer using `fill-extrusion` after style load.

### MapTiler key handling
`MAPTILER_API_KEY` is a **public client token**. Restrict it by allowed domains/referrers in the MapTiler dashboard.

Create a local `.env` at repo root (template: `.env.example`):

```env
MAPTILER_API_KEY=YOUR_PUBLIC_MAPTILER_KEY
```

- Android reads it at build time via `resValue("maptiler_api_key", ...)`.
- Desktop receives it as JVM property `-DMAPTILER_API_KEY=...`.

Set it in `composeApp/src/webMain/resources/index.html`:
```html
window.MAPTILER_API_KEY = "YOUR_PUBLIC_MAPTILER_KEY";
```

### Run web target
```bash
cd /home/matei/Repositories/gel
./gradlew :composeApp:jsBrowserDevelopmentRun
```

### Next planned step
Add map pins + issue creation interactions on top of the current 2.5D map base.
