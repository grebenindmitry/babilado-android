package io.github.grebenindmitry.babilado

import android.util.Log
import androidx.compose.material.SnackbarHostState
import io.github.grebenindmitry.babilado.structures.ErrorResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException

open class HttpCallback(
    private val responseHandler: (Response) -> Unit,
    private val errorHandler: (Int, ErrorResponse) -> Unit,
    private val tag: String) : Callback {

    override fun onFailure(call: Call, e: IOException) {
        Log.e(tag, e.toString())
    }

    override fun onResponse(call: Call, response: Response) {
        if (response.code == 200) responseHandler(response)
        else {
            try {
                val result = Json.decodeFromString<ErrorResponse>(response.body!!.string())
                errorHandler(response.code, result)
            } catch (ex: Exception) {
                Log.e(tag, ex.toString())
            }
        }
        response.close()
    }
}