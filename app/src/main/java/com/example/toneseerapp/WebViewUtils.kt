package com.example.toneseerapp

import android.webkit.WebView

// This is to toggle the play/pause and sync Tabs | Spotify
fun toggleTabsPlayPause(webView: WebView?, play: Boolean) {
    webView?.evaluateJavascript(
        """
        (function() {
            const btn = document.querySelector('#control-play');
            if (!btn) return;
            const isPlaying = btn.getAttribute('aria-pressed') === 'true';
            const shouldPlay = ${if (play) "true" else "false"};
            if (shouldPlay !== isPlaying) {
                btn.click();
            }
        })();
        """.trimIndent(),
        null
    )
}

