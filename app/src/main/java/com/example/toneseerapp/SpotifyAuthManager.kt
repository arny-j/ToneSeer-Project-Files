package com.example.toneseerapp

import android.content.Context
import android.graphics.Bitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import androidx.core.graphics.createBitmap
import androidx.core.graphics.set

object SpotifyAuthManager {
    // Uses codeVerifier to create code to exchange for the auth code
    private var codeVerifier: String? = null
    private var sessionId: String? = null
    // Main func that starts the login process
    fun startLogin(context: Context, onQrGenerated: (Bitmap) -> Unit) {
        sessionId = java.util.UUID.randomUUID().toString()
        // Endpoint with sessionId attached for QR code
        val loginUrl = "https://tone-seer-spotify-login.vercel.app/?session_id=$sessionId"

        // QR Builder
        val writer = QRCodeWriter() // start the writer
        val bitMatrix = writer.encode(loginUrl, BarcodeFormat.QR_CODE, 512, 512) // encode the URL
        val width = bitMatrix.width // size attributes
        val height = bitMatrix.height
        val bmp = createBitmap(width, height, android.graphics.Bitmap.Config.RGB_565) // Create the QR code
        for (x in 0 until width) {
            for (y in 0 until height) {
                bmp[x, y] = if (bitMatrix.get(x, y)) android.graphics.Color.BLACK
                else android.graphics.Color.WHITE
            }
        }
        // Trigger and generate the QR code
        onQrGenerated(bmp)
    }
    fun getCodeVerifier(): String? = codeVerifier
    fun getSessionId(): String? = sessionId
}
