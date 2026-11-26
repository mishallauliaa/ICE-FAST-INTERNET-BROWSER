# ICE BROWSER for Android

Android companion app for ICE BROWSER. Built with Kotlin and Android WebView.

## Features in this Android starter release
- Google search / address navigation
- Multiple tabs with tab picker and close active tab
- Back, forward, reload, home
- Local bookmarks
- Basic domain-based ad blocking
- Downloads through Android DownloadManager
- Clear browser cache, history, form data and cookies
- Cloudflare WARP launcher (requires the official WARP app installed and configured separately)

## Build APK with GitHub Actions
1. Upload the `Android` folder to the root of the repository.
2. Upload `.github/workflows/android-apk.yml` while preserving any existing workflows.
3. Open GitHub Actions.
4. Select **Build ICE BROWSER Android APK** and click **Run workflow**.
5. Download artifact **ICE-BROWSER-Android-1.0.0-debug**.
6. Extract the ZIP; install `app-debug.apk` on an Android device.

## Important limitations
- The WARP button opens the official Cloudflare WARP app. It does not embed a VPN service or control WARP connection state from inside ICE BROWSER.
- The current APK build is a debug-signed APK intended for testing. A release APK should be signed with a private keystore before public distribution.
- Android WebView uses Android's system WebView/Chromium provider and may differ from desktop Edge WebView2.
- Private browsing mode is not included in this first Android build; do not treat ordinary tabs as private.
