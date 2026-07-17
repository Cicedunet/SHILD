package com.example.shielmind.service

import android.content.Context
import android.util.Log
import java.util.Properties
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

object EmailSender {
    private const val TAG = "ShieldMind_EmailSender"

    fun sendEmail(
        context: Context,
        recipientEmail: String,
        subject: String,
        bodyText: String,
        onSuccess: () -> Unit = {},
        onFailure: (Exception) -> Unit = {}
    ) {
        // Retrieve SMTP settings from SharedPreferences with hardcoded fallbacks
        val prefs = context.getSharedPreferences("shieldmind_prefs", Context.MODE_PRIVATE)
        val smtpHost = prefs.getString("smtp_host", "smtp.gmail.com") ?: "smtp.gmail.com"
        val smtpPort = prefs.getString("smtp_port", "587") ?: "587"
        val smtpUser = prefs.getString("smtp_user", "").let {
            if (it.isNullOrBlank()) "fgghh8202@gmail.com" else it
        }
        val smtpPass = prefs.getString("smtp_password", "").let {
            if (it.isNullOrBlank()) "vbcg dgle grcc xgab" else it
        }

        if (smtpUser.isBlank() || smtpPass.isBlank()) {
            val err = Exception("SMTP Sender Email or Password is empty.")
            Log.e(TAG, "Cannot send email: configuration incomplete.", err)
            onFailure(err)
            return
        }

        Thread {
            try {
                val props = Properties().apply {
                    put("mail.smtp.auth", "true")
                    put("mail.smtp.starttls.enable", "true")
                    put("mail.smtp.host", smtpHost)
                    put("mail.smtp.port", smtpPort)
                }

                val session = Session.getInstance(props, object : Authenticator() {
                    override fun getPasswordAuthentication(): PasswordAuthentication {
                        return PasswordAuthentication(smtpUser, smtpPass)
                    }
                })

                val message = MimeMessage(session).apply {
                    setFrom(InternetAddress(smtpUser))
                    setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail))
                    setSubject(subject, "UTF-8")
                    setText(bodyText, "UTF-8")
                }

                Transport.send(message)
                Log.d(TAG, "Email sent successfully to $recipientEmail")
                onSuccess()
            } catch (e: Exception) {
                Log.e(TAG, "Error sending email via SMTP", e)
                onFailure(e)
            }
        }.start()
    }
}
