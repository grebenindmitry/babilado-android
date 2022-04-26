package io.github.grebenindmitry.babilado

import android.content.SharedPreferences
import android.util.Log
import androidx.compose.material.SnackbarHostState
import io.github.grebenindmitry.babilado.structures.ErrorResponse
import io.github.grebenindmitry.babilado.structures.Session
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class LoginCallback(
    private val sharedPreferences: SharedPreferences,
    private val responseHandler: (Session) -> Unit,
    errorHandler: (Int, ErrorResponse) -> Unit,
    tag: String) : HttpCallback({
    try {
        val session = Json.decodeFromString<Session>(it.body!!.string())
        sharedPreferences.edit().putString("sessionId", session.id).putString("userId", session.user.id)
            .putString("username", session.user.username).putLong("sessionExpiry", session.expiryTime).apply()
        responseHandler(session)
    } catch (ex: Exception) {
        Log.e(tag, ex.toString())
    }
}, errorHandler, tag)