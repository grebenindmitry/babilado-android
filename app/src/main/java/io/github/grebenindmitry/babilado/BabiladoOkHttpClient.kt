package io.github.grebenindmitry.babilado

import okhttp3.Call
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

object BabiladoOkHttpClient : OkHttpClient() {
    fun newCall(url: String, body: String? = null): Call {
        return if (body == null) {
            super.newCall(Request.Builder().url(url).get().build())
        } else {
            super.newCall(
                Request.Builder().url(url).post(body.toRequestBody("application/json; charset=utf-8".toMediaType()))
                    .build())
        }
    }

    fun newSessionProtectedCall(userId: String, sessionId: String, url: String, body: String? = null): Call {
        return if (body == null) {
            super.newCall(
                Request.Builder().header("userId", userId).header("sessionId", sessionId).url(url).get().build())
        } else {
            super.newCall(Request.Builder().header("userId", userId).header("sessionId", sessionId).url(url)
                .post(body.toRequestBody("application/json; charset=utf-8".toMediaType())).build())
        }
    }
}