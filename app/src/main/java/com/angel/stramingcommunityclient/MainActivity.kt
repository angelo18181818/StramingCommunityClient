package com.angel.stramingcommunityclient

import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.SystemClock
import android.text.InputType
import android.view.Gravity
import android.view.KeyEvent
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import org.mozilla.geckoview.GeckoRuntime
import org.mozilla.geckoview.GeckoSession
import org.mozilla.geckoview.GeckoView

private const val DEFAULT_URL = "https://streamingcommunityz.partners/it/watch/4615"
private const val CONFIG_NAME = "viewer_config"
private const val CONFIG_URL_KEY = "site_url"
private const val URL_CHORD_WINDOW_MS = 350L

class MainActivity : Activity() {
    private lateinit var geckoSession: GeckoSession
    private var upPressed = false
    private var downPressed = false
    private var lastVerticalKey = KeyEvent.KEYCODE_UNKNOWN
    private var lastVerticalDownAt = 0L
    private var urlDialog: Dialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val geckoView = findViewById<GeckoView>(R.id.gecko_view)
        val geckoRuntime = GeckoRuntime.create(applicationContext)
        geckoSession = GeckoSession()
        geckoSession.open(geckoRuntime)
        geckoView.setSession(geckoSession)
        geckoSession.load(GeckoSession.Loader().uri(configuredUrl()))
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        val isVerticalKey = event.keyCode == KeyEvent.KEYCODE_DPAD_UP ||
            event.keyCode == KeyEvent.KEYCODE_DPAD_DOWN

        if (!isVerticalKey) {
            return super.dispatchKeyEvent(event)
        }

        if (event.action == KeyEvent.ACTION_UP) {
            if (event.keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                upPressed = false
            } else {
                downPressed = false
            }
            return super.dispatchKeyEvent(event)
        }

        if (event.action == KeyEvent.ACTION_DOWN && event.repeatCount == 0) {
            val now = SystemClock.uptimeMillis()
            val oppositeKeyWasJustPressed =
                lastVerticalKey != KeyEvent.KEYCODE_UNKNOWN &&
                    lastVerticalKey != event.keyCode &&
                    now - lastVerticalDownAt <= URL_CHORD_WINDOW_MS

            if (event.keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                upPressed = true
            } else {
                downPressed = true
            }

            lastVerticalKey = event.keyCode
            lastVerticalDownAt = now

            if ((upPressed && downPressed) || oppositeKeyWasJustPressed) {
                upPressed = false
                downPressed = false
                lastVerticalKey = KeyEvent.KEYCODE_UNKNOWN
                showUrlDialog()
                return true
            }
        }

        return super.dispatchKeyEvent(event)
    }

    private fun configuredUrl(): String =
        getSharedPreferences(CONFIG_NAME, MODE_PRIVATE)
            .getString(CONFIG_URL_KEY, DEFAULT_URL)
            ?.let(::normalizeUrl)
            ?: DEFAULT_URL

    private fun showUrlDialog() {
        if (urlDialog?.isShowing == true) {
            return
        }

        val input = EditText(this).apply {
            setText(configuredUrl())
            setSelectAllOnFocus(true)
            setSingleLine(true)
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_URI
            imeOptions = EditorInfo.IME_ACTION_DONE
            setTextColor(Color.BLACK)
            setHintTextColor(Color.DKGRAY)
            setBackgroundColor(Color.WHITE)
            setPadding(dp(16), dp(12), dp(16), dp(12))
            hint = "https://sito.example/"
        }

        val container = FrameLayout(this).apply {
            setPadding(dp(24), dp(24), dp(24), dp(24))
            addView(
                input,
                FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    Gravity.CENTER
                )
            )
        }

        val dialog = Dialog(this).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(container)
            setCanceledOnTouchOutside(false)
        }
        urlDialog = dialog

        fun saveUrl(): Boolean {
            val newUrl = normalizeUrl(input.text.toString())
            if (newUrl == null) {
                input.error = "Inserisci un URL http:// o https:// valido"
                return false
            }

            getSharedPreferences(CONFIG_NAME, MODE_PRIVATE)
                .edit()
                .putString(CONFIG_URL_KEY, newUrl)
                .apply()
            dialog.dismiss()
            geckoSession.load(GeckoSession.Loader().uri(newUrl))
            return true
        }

        input.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) saveUrl() else false
        }
        input.setOnKeyListener { _, keyCode, event ->
            if (
                event.action == KeyEvent.ACTION_UP &&
                (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_DPAD_CENTER)
            ) {
                saveUrl()
            } else {
                false
            }
        }
        dialog.setOnDismissListener { urlDialog = null }
        dialog.show()
        dialog.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            attributes = attributes.apply { dimAmount = 0.6f }
            setLayout(
                (resources.displayMetrics.widthPixels * 0.82f).toInt(),
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        input.post {
            input.requestFocus()
            (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager)
                .showSoftInput(input, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    private fun normalizeUrl(rawUrl: String): String? {
        val trimmed = rawUrl.trim()
        if (trimmed.isEmpty()) {
            return null
        }

        val candidate = if (trimmed.contains("://")) trimmed else "https://$trimmed"
        val uri = try {
            Uri.parse(candidate)
        } catch (_: RuntimeException) {
            return null
        }
        val isWebUrl = uri.scheme.equals("http", ignoreCase = true) ||
            uri.scheme.equals("https", ignoreCase = true)
        return candidate.takeIf { isWebUrl && !uri.host.isNullOrBlank() }
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()
}
