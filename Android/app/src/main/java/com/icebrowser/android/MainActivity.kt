package com.icebrowser.android

import android.app.Activity
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.webkit.CookieManager
import android.webkit.DownloadListener
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import java.net.URL
import java.util.Locale

class MainActivity : Activity() {
    private data class BrowserTab(val web: WebView, var title: String = "Tab baru")

    private val tabs = mutableListOf<BrowserTab>()
    private var activeIndex = -1
    private lateinit var root: LinearLayout
    private lateinit var address: EditText
    private lateinit var tabCount: TextView
    private lateinit var pageTitle: TextView
    private lateinit var webContainer: LinearLayout
    private var adBlockEnabled = true

    private val blockedHosts = setOf(
        "doubleclick.net", "googlesyndication.com", "googleadservices.com",
        "adservice.google.com", "adnxs.com", "taboola.com", "outbrain.com",
        "criteo.com", "popads.net", "popcash.net"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.rgb(16, 28, 46)
        window.navigationBarColor = Color.rgb(16, 28, 46)
        buildInterface()
        newTab(HOME_URL)
    }

    private fun buildInterface() {
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.rgb(16, 28, 46))
        }

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(12), dp(10), dp(12), dp(8))
        }
        val brand = TextView(this).apply {
            text = "❄  ICE BROWSER"
            textSize = 18f
            setTextColor(Color.WHITE)
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        header.addView(brand, LinearLayout.LayoutParams(0, dp(42), 1f))
        tabCount = TextView(this).apply {
            text = "1 tab"
            textSize = 13f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setPadding(dp(10), 0, dp(10), 0)
        }
        header.addView(tabCount)
        header.addView(actionButton("＋") { newTab(HOME_URL) })
        root.addView(header)

        val addressRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(10), dp(4), dp(10), dp(8))
        }
        address = EditText(this).apply {
            singleLine = true
            textSize = 14f
            setTextColor(Color.WHITE)
            setHintTextColor(Color.LTGRAY)
            hint = "Cari atau masukkan alamat"
            setPadding(dp(14), 0, dp(14), 0)
            setBackgroundColor(Color.rgb(35, 52, 75))
            imeOptions = EditorInfo.IME_ACTION_GO
            setOnEditorActionListener { _, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_GO || event?.keyCode == KeyEvent.KEYCODE_ENTER) {
                    navigateFromAddress()
                    true
                } else false
            }
        }
        addressRow.addView(address, LinearLayout.LayoutParams(0, dp(44), 1f))
        addressRow.addView(actionButton("GO") { navigateFromAddress() })
        root.addView(addressRow)

        val controls = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(dp(6), 0, dp(6), dp(6))
        }
        controls.addView(actionButton("‹") { goBack() }, weightParams())
        controls.addView(actionButton("›") { goForward() }, weightParams())
        controls.addView(actionButton("↻") { currentWeb()?.reload() }, weightParams())
        controls.addView(actionButton("⌂") { loadUrl(HOME_URL) }, weightParams())
        controls.addView(actionButton("☆") { saveBookmark() }, weightParams())
        controls.addView(actionButton("VPN") { openWarp() }, weightParams())
        controls.addView(actionButton("⋮") { showMenu() }, weightParams())
        root.addView(controls)

        pageTitle = TextView(this).apply {
            text = "Siap menjelajah"
            textSize = 11f
            setTextColor(Color.LTGRAY)
            setPadding(dp(12), dp(3), dp(12), dp(5))
            maxLines = 1
        }
        root.addView(pageTitle)

        webContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.WHITE)
        }
        root.addView(webContainer, LinearLayout.LayoutParams(-1, 0, 1f))
        setContentView(root)
    }

    private fun actionButton(label: String, action: () -> Unit): Button = Button(this).apply {
        text = label
        textSize = 13f
        isAllCaps = false
        setTextColor(Color.WHITE)
        background = android.graphics.drawable.GradientDrawable().apply {
            setColor(Color.rgb(31, 65, 99))
            cornerRadius = dp(10).toFloat()
        }
        setPadding(dp(8), 0, dp(8), 0)
        minWidth = dp(42)
        minHeight = dp(40)
        setOnClickListener { action() }
    }

    private fun weightParams() = LinearLayout.LayoutParams(0, dp(42), 1f).apply {
        marginStart = dp(2); marginEnd = dp(2)
    }

    private fun newTab(url: String) {
        val web = WebView(this)
        web.setBackgroundColor(Color.WHITE)
        web.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            loadsImagesAutomatically = true
            mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
            javaScriptCanOpenWindowsAutomatically = false
            setSupportMultipleWindows(false)
            cacheMode = WebSettings.LOAD_DEFAULT
            builtInZoomControls = true
            displayZoomControls = false
            userAgentString = userAgentString + " ICEBrowser/1.0"
        }
        web.webChromeClient = object : WebChromeClient() {
            override fun onReceivedTitle(view: WebView?, title: String?) {
                val tab = tabs.firstOrNull { it.web === web }
                if (tab != null) tab.title = title?.takeIf { it.isNotBlank() } ?: "Tab baru"
                if (currentWeb() === web) pageTitle.text = tab?.title ?: "ICE BROWSER"
            }
        }
        web.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val uri = request?.url ?: return false
                return uri.scheme !in listOf("http", "https")
            }

            override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse? {
                if (!adBlockEnabled) return null
                return try {
                    val host = request?.url?.host?.lowercase(Locale.ROOT) ?: return null
                    val blocked = blockedHosts.any { host == it || host.endsWith(".$it") }
                    if (blocked) WebResourceResponse("text/plain", "UTF-8", 403, "Blocked by ICE AdBlock", emptyMap(), "".byteInputStream()) else null
                } catch (_: Exception) { null }
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                if (currentWeb() === web) {
                    address.setText(url ?: "")
                    pageTitle.text = "Memuat halaman…"
                }
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                if (currentWeb() === web) {
                    address.setText(url ?: "")
                    pageTitle.text = web.title ?: "Selesai"
                }
            }
        }
        web.setDownloadListener(DownloadListener { downloadUrl, userAgent, contentDisposition, mimeType, _ ->
            try {
                val request = DownloadManager.Request(Uri.parse(downloadUrl)).apply {
                    setMimeType(mimeType)
                    addRequestHeader("User-Agent", userAgent)
                    setTitle(URL(downloadUrl).path.substringAfterLast('/').ifBlank { "ICE-BROWSER-download" })
                    setDescription("Mengunduh melalui ICE BROWSER")
                    setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, URL(downloadUrl).path.substringAfterLast('/').ifBlank { "download" })
                    allowScanningByMediaScanner()
                }
                (getSystemService(DOWNLOAD_SERVICE) as DownloadManager).enqueue(request)
                Toast.makeText(this, "Unduhan dimulai", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this, "Gagal mengunduh: ${e.message}", Toast.LENGTH_LONG).show()
            }
        })
        tabs.add(BrowserTab(web))
        activeIndex = tabs.lastIndex
        webContainer.addView(web, LinearLayout.LayoutParams(-1, -1))
        updateTabs()
        web.loadUrl(url)
    }

    private fun currentWeb(): WebView? = tabs.getOrNull(activeIndex)?.web

    private fun switchTab(index: Int) {
        if (index !in tabs.indices) return
        tabs.forEachIndexed { i, tab -> tab.web.visibility = if (i == index) View.VISIBLE else View.GONE }
        activeIndex = index
        currentWeb()?.let {
            address.setText(it.url ?: "")
            pageTitle.text = it.title ?: "ICE BROWSER"
        }
        updateTabs()
    }

    private fun updateTabs() {
        tabCount.text = "${tabs.size} tab"
    }

    private fun navigateFromAddress() {
        val input = address.text.toString().trim()
        if (input.isBlank()) return
        val target = when {
            input.startsWith("https://", true) || input.startsWith("http://", true) -> input
            input.contains(' ') || !input.contains('.') -> "https://www.google.com/search?q=${Uri.encode(input)}"
            else -> "https://$input"
        }
        loadUrl(target)
        (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
            .hideSoftInputFromWindow(address.windowToken, 0)
        address.clearFocus()
    }

    private fun loadUrl(url: String) { currentWeb()?.loadUrl(url) }
    private fun goBack() { currentWeb()?.let { if (it.canGoBack()) it.goBack() } }
    private fun goForward() { currentWeb()?.let { if (it.canGoForward()) it.goForward() } }

    private fun saveBookmark() {
        val url = currentWeb()?.url ?: return
        val prefs = getSharedPreferences("ice_browser", MODE_PRIVATE)
        val old = prefs.getStringSet("bookmarks", emptySet())?.toMutableSet() ?: mutableSetOf()
        old.add(url)
        prefs.edit().putStringSet("bookmarks", old).apply()
        Toast.makeText(this, "Bookmark disimpan", Toast.LENGTH_SHORT).show()
    }

    private fun showMenu() {
        val options = arrayOf("Tab baru", "Daftar tab", "Bookmark", "AdBlock: ${if (adBlockEnabled) "ON" else "OFF"}", "Unduhan", "Buka Cloudflare WARP", "Hapus data browser")
        android.app.AlertDialog.Builder(this)
            .setTitle("ICE BROWSER")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> newTab(HOME_URL)
                    1 -> showTabs()
                    2 -> showBookmarks()
                    3 -> { adBlockEnabled = !adBlockEnabled; Toast.makeText(this, "AdBlock ${if (adBlockEnabled) "aktif" else "nonaktif"}", Toast.LENGTH_SHORT).show() }
                    4 -> openDownloads()
                    5 -> openWarp()
                    6 -> clearBrowserData()
                }
            }.show()
    }

    private fun showTabs() {
        val labels = tabs.mapIndexed { index, tab -> "${index + 1}. ${tab.title}" }.toTypedArray()
        android.app.AlertDialog.Builder(this).setTitle("Pilih tab")
            .setItems(labels) { _, which -> switchTab(which) }
            .setNeutralButton("Tab baru") { _, _ -> newTab(HOME_URL) }
            .setNegativeButton("Tutup tab aktif") { _, _ -> closeCurrentTab() }
            .show()
    }

    private fun closeCurrentTab() {
        if (tabs.isEmpty()) return
        val removed = tabs.removeAt(activeIndex)
        webContainer.removeView(removed.web)
        removed.web.stopLoading()
        removed.web.destroy()
        if (tabs.isEmpty()) newTab(HOME_URL) else switchTab((activeIndex - 1).coerceAtLeast(0))
    }

    private fun showBookmarks() {
        val items = getSharedPreferences("ice_browser", MODE_PRIVATE).getStringSet("bookmarks", emptySet())?.toList().orEmpty()
        if (items.isEmpty()) {
            Toast.makeText(this, "Belum ada bookmark", Toast.LENGTH_SHORT).show(); return
        }
        android.app.AlertDialog.Builder(this).setTitle("Bookmark")
            .setItems(items.toTypedArray()) { _, which -> loadUrl(items[which]) }.show()
    }

    private fun openDownloads() {
        try {
            val intent = Intent(DownloadManager.ACTION_VIEW_DOWNLOADS)
            startActivity(intent)
        } catch (_: Exception) { Toast.makeText(this, "Aplikasi unduhan tidak tersedia", Toast.LENGTH_SHORT).show() }
    }

    private fun openWarp() {
        val packageNames = listOf("com.cloudflare.onedotonedotonedotone", "com.cloudflare.oneagent")
        val launch = packageNames.firstNotNullOfOrNull { packageName -> packageManager.getLaunchIntentForPackage(packageName) }
        if (launch != null) startActivity(launch)
        else {
            try { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.cloudflare.onedotonedotonedotone"))) }
            catch (_: Exception) { Toast.makeText(this, "Cloudflare WARP tidak ditemukan", Toast.LENGTH_SHORT).show() }
        }
    }

    private fun clearBrowserData() {
        android.app.AlertDialog.Builder(this)
            .setTitle("Hapus data browser?")
            .setMessage("Cookie, cache, dan data situs pada WebView akan dihapus.")
            .setNegativeButton("Batal", null)
            .setPositiveButton("Hapus") { _, _ ->
                tabs.forEach { it.web.clearCache(true); it.web.clearHistory(); it.web.clearFormData() }
                CookieManager.getInstance().removeAllCookies { }
                CookieManager.getInstance().flush()
                Toast.makeText(this, "Data browser dibersihkan", Toast.LENGTH_SHORT).show()
            }.show()
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    override fun onBackPressed() {
        val web = currentWeb()
        if (web?.canGoBack() == true) web.goBack() else super.onBackPressed()
    }

    override fun onDestroy() {
        tabs.forEach { it.web.stopLoading(); it.web.destroy() }
        tabs.clear()
        super.onDestroy()
    }

    companion object { private const val HOME_URL = "https://www.google.com" }
}
