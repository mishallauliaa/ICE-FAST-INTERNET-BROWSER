# ICE BROWSER Android v1.1.0

Android browser companion for ICE BROWSER. Built with Kotlin and the Android System WebView.

## Compatibility
- Minimum Android: Android 5.0 (Lollipop), API 21.
- Target SDK: API 28 for a conservative compatibility profile on older Android devices.
- Requires a working Android System WebView or Chrome WebView provider supplied by the device.
- On very old devices, modern websites may still fail if their system WebView, TLS support, or root certificates are outdated.

## Features
- ICE BROWSER launcher icon and in-app logo.
- Google search and address navigation.
- Multiple tabs with tab picker and close active tab.
- Back, forward, reload, and home.
- Local bookmarks.
- Basic domain-based ad blocking.
- Downloads through Android DownloadManager.
- Clear cache, history, form data, and cookies.
- Cloudflare WARP launcher (requires official WARP app installed separately; WARP availability depends on Android version and device support).

## Build APK with GitHub Actions
1. Upload the `Android` folder to the repository root, preserving its folder name.
2. Upload `.github/workflows/android-apk.yml` and keep any existing Windows workflow.
3. Open GitHub Actions.
4. Select **Build ICE BROWSER Android APK** and click **Run workflow**.
5. Download artifact **ICE-BROWSER-Android-1.1.0-debug**.
6. Extract the ZIP and install `app-debug.apk`.

## Important notes
- The workflow creates a debug-signed APK for testing/sideloading. A Play Store/public release needs a separately signed release build and keystore.
- VPN is not embedded in ICE BROWSER. The WARP button opens the separate official Cloudflare WARP app.
- AdBlock is a basic domain filter and does not guarantee blocking every advertisement.
- This project has not been compiled on a physical Android device in this environment.
