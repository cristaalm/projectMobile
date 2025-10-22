package com.renova.mobile.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.renova.mobile.MainActivity
import com.renova.mobile.R
import com.renova.mobile.network.ApiClient
import com.renova.mobile.network.RegisterFcmTokenRequest
import com.renova.mobile.utils.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RenovaMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Handle FCM messages here.
        Log.d(TAG, "From: ${remoteMessage.from}")

        // Check if message contains a data payload.
        remoteMessage.data.isNotEmpty().let {
            Log.d(TAG, "Message data payload: " + remoteMessage.data)
            sendNotification(remoteMessage.data["title"], remoteMessage.data["body"])
        }

        // Check if message contains a notification payload.
        remoteMessage.notification?.let {
            Log.d(TAG, "Message Notification Body: ${it.body}")
            sendNotification(it.title, it.body)
        }
    }

    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")
        // Guardar localmente para poder registrarlo tras login si aún no hay sesión
        try {
            val sessionManager = SessionManager(applicationContext)
            sessionManager.saveFcmToken(token)
        } catch (e: Exception) {
            Log.e(TAG, "Error guardando token FCM", e)
        }
        // Enviar al servidor si hay sesión activa
        sendRegistrationToServer(token)
    }

    private fun sendRegistrationToServer(token: String?) {
        if (token.isNullOrBlank()) {
            Log.w(TAG, "Token FCM vacío, no se envía")
            return
        }
        val context = applicationContext
        val sessionManager = SessionManager(context)
        val userId = sessionManager.getUser()?.id
        if (userId == null || !sessionManager.isLoggedIn()) {
            Log.i(TAG, "No hay usuario logueado; se registrará cuando haya sesión")
            return
        }
        // Asegurar ApiClient
        try {
            ApiClient.init(context)
        } catch (e: Exception) {
            Log.e(TAG, "Error inicializando ApiClient", e)
        }
        // Llamada de red en background
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ApiClient.apiService.registerFcmToken(
                    RegisterFcmTokenRequest(userId = userId, token = token)
                )
                if (response.isSuccessful && response.body()?.success == true) {
                    Log.d(TAG, "FCM token registrado para userId=$userId")
                } else {
                    val msg = response.body()?.message ?: "Error ${response.code()}"
                    Log.e(TAG, "Fallo al registrar token FCM: $msg")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Excepción registrando token FCM", e)
            }
        }
        Log.d(TAG, "sendRegistrationTokenToServer($token)")
    }

    private fun sendNotification(title: String?, messageBody: String?) {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE)

        val channelId = getString(R.string.default_notification_channel_id)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId,
                "Channel human readable title",
                NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build())
    }

    companion object {
        private const val TAG = "RenovaMessagingService"
    }
}