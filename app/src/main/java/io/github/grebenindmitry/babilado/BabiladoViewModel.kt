package io.github.grebenindmitry.babilado

import android.util.Log
import androidx.activity.ComponentActivity
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import androidx.preference.PreferenceManager
import io.github.grebenindmitry.babilado.room.BabiladoDatabase
import io.github.grebenindmitry.babilado.structures.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import okhttp3.*
import java.util.*
import java.util.concurrent.TimeUnit

class BabiladoViewModel(activity: ComponentActivity, val navController: NavController) : ViewModel() {

    private val messageDAO = BabiladoDatabase.getInstance(activity).messageDAO()
    private val conversationDAO = BabiladoDatabase.getInstance(activity).conversationDao()

    private val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity)

    private val tag = "babiladoVM"

    private var loggedOut = false
    var user: User = User(sharedPreferences.getString("userId", "")!!, sharedPreferences.getString("username", "")!!)
        get() {
            return if (field.id == "") {
                if (!loggedOut) logOut(); loggedOut = true
                field
            } else field
        }
    private var sessionId = sharedPreferences.getString("sessionId", "")!!
        get() {
            return if (field == "") {
                if (!loggedOut) logOut(); loggedOut = true
                field
            } else field
        }
    private val apiUrl = sharedPreferences.getString("apiUrl", "https://babilado-backend.herokuapp.com/api")!!

    var conversationList = mutableStateListOf<Conversation>()
        private set
    private val messagesByRecipient = mutableMapOf<String, SnapshotStateList<Message>>()

    private var ws: WebSocket? = null
    private val wsOkHttp = OkHttpClient.Builder().pingInterval(10, TimeUnit.SECONDS).build()
    private var wstemp = ""
        set(newtemp: String) {
            handleWSMsg(newtemp)
            field = newtemp
        }

    private val json = Json {
        serializersModule = SerializersModule {
            polymorphic(WSMessage::class) {
                subclass(WSNewMessage::class)
            }
        }
    }

    init {
        if (user.id != "" && sessionId != "") {
            connectWS()
        }
    }

    //Public:
    //  General:

    fun login(
        username: String, password: String, onSuccess: () -> Unit, onError: (code: Int, err: ErrorResponse) -> Unit) {

        BabiladoOkHttpClient.newCall(
            Request.Builder().url("$apiUrl/session").header("username", username).header("password", password).get()
                .build()).enqueue(LoginCallback(sharedPreferences, { session ->
            user = session.user
            sessionId = session.id
            connectWS()
            loadChats()
            onSuccess()
        }, { code, err -> onError(code, err) }, tag))
    }

    fun register(
        username: String, password: String, onSuccess: () -> Unit, onError: (code: Int, err: ErrorResponse) -> Unit) {
        val requestJson = """{ "username": "$username", "password": "$password" }"""
        BabiladoOkHttpClient.newCall("$apiUrl/users", requestJson)
            .enqueue(HttpCallback({ onSuccess() }, { code, err -> onError(code, err) }, tag))

    }

    fun logOut() {
        user = User("", "")
        sharedPreferences.edit().remove("sessionId").remove("userId").remove("sessionExpiry").remove("password")
            .remove("username").apply()
        try {
            navController.navigate("register")
        } catch (ex: Exception) {
        }
    }

    fun loadChats() {
        viewModelScope.launch(Dispatchers.IO) {
            val dbChats = conversationDAO.getAll()
            if (dbChats.isNotEmpty()) {
                conversationList.clear()
                conversationList.addAll(dbChats)
            }
        }

        getRemoteChats()
    }

    //  Chat.kt:

    fun getMessageLiveData(recipient: User): SnapshotStateList<Message> {
        if (messagesByRecipient[recipient.id] == null) messagesByRecipient[recipient.id] = mutableStateListOf()
        return messagesByRecipient[recipient.id]!!
    }

    fun sendMessage(messageText: String, messageType: Int, recipient: User) {
        val message = Message("", "", recipient.id, messageText, messageType, Date().time)
        val wsNewMessage = WSNewMessage(message)

        if (ws?.send(json.encodeToString(wsNewMessage)) != true) {
            BabiladoOkHttpClient.newSessionProtectedCall(user.id, sessionId, "$apiUrl/message",
                json.encodeToString(message)).enqueue(HttpCallback({}, { _, err -> Log.e(tag, err.message) }, tag))
        }
    }

    fun loadMessages(recipient: User) {
        viewModelScope.launch(Dispatchers.IO) {
            val dbMessages = messageDAO.getMessagesByRecipient(recipient.id)

            if (dbMessages.isNotEmpty()) {
                messagesByRecipient[recipient.id]!!.clear()
                messagesByRecipient[recipient.id]!!.addAll(dbMessages)

                checkNewMessages(recipient)
            } else getRemoteMessages(recipient)
        }
    }

    //  ChatList.kt:

    fun newChat(username: String) {
        BabiladoOkHttpClient.newSessionProtectedCall(user.id, sessionId, "$apiUrl/users/$username")
            .enqueue(HttpCallback({
                try {
                    val recipient = json.decodeFromString<User>(it.body!!.string())

                    navController.navigate("chat?userId=${recipient.id}&username=${recipient.username}")
                } catch (ex: Exception) {
                    Log.e(tag, ex.toString())
                }
            }, { code, err ->
                when (code) {
                    401 -> logOut()
                    else -> Log.e(tag, err.message)
                }
            }, tag))
    }

    //Private:
    //  General:

    private fun connectWS() {
        ws = wsOkHttp.newWebSocket(
            Request.Builder().header("sessionId", sessionId).header("userId", user.id).url(apiUrl).build(),
            object : WebSocketListener() {
                override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                    super.onClosed(webSocket, code, reason)
                }

                override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                    super.onFailure(webSocket, t, response)
                    Log.e(tag, "ws failed: $t")
                }

                override fun onMessage(webSocket: WebSocket, text: String) {
                    super.onMessage(webSocket, text)
                    wstemp = text
                }

                override fun onOpen(webSocket: WebSocket, response: Response) {
                    super.onOpen(webSocket, response)
                }
            })
    }

    private fun handleWSMsg(text: String) {
        val wsMessage = json.decodeFromString<WSMessage>(text)

        if (wsMessage is WSNewMessage) {
            val newMessage = wsMessage.newMessage
            val recipientId = if (newMessage.sender == user.id) newMessage.recipient else newMessage.sender

            if (messagesByRecipient[recipientId] != null) {
                if (messagesByRecipient[recipientId]!!.none { it.id == newMessage.id }) {
                    messagesByRecipient[recipientId]!!.add(newMessage)
                }
            }
        }
    }

    //  Chat.kt:

    private fun checkNewMessages(recipient: User) {
        val messages = messagesByRecipient[recipient.id]!!
        BabiladoOkHttpClient.newSessionProtectedCall(user.id, sessionId,
            "$apiUrl/messages/last?recipient=${recipient.id}").enqueue(HttpCallback({
            try {
                if (!messages.isNullOrEmpty()) {
                    val lastLocalMsg = messages.sortedByDescending { message -> message.time_sent }[0]
                    val lastRemoteMsg = json.decodeFromString<Message>(it.body!!.string())

                    if (lastLocalMsg.id == lastRemoteMsg.id) return@HttpCallback
                }

                getRemoteMessages(recipient)
            } catch (e: Exception) {
                Log.e(tag, e.toString())
            }
        }, { code, err ->
            when (code) {
                401 -> logOut()
                else -> Log.e(tag, err.message)
            }
        }, tag))
    }

    private fun getRemoteMessages(recipient: User) {
        BabiladoOkHttpClient.newSessionProtectedCall(user.id, sessionId, "$apiUrl/messages?recipient=${recipient.id}")
            .enqueue(HttpCallback({
                try {
                    val newMessages = Json.decodeFromString<List<Message>>(it.body!!.string())
                    messagesByRecipient[recipient.id]!!.clear()
                    messagesByRecipient[recipient.id]!!.addAll(newMessages)
                    viewModelScope.launch(Dispatchers.IO) {
                        messageDAO.insertMessages(newMessages)
                    }
                } catch (e: Exception) {
                    Log.e(tag, e.toString())
                }
            }, { code, err ->
                when (code) {
                    401 -> logOut()
                    else -> Log.e(tag, err.message)
                }
            }, tag))
    }

//  ChatList.kt:

    private fun getRemoteChats() {
        BabiladoOkHttpClient.newSessionProtectedCall(user.id, sessionId, "$apiUrl/users/${user.id}/conversations")
            .enqueue(HttpCallback({
                try {
                    val newConversations = json.decodeFromString<List<Conversation>>(it.body!!.string())
                    viewModelScope.launch {
                        conversationList.clear()
                        conversationList.addAll(newConversations)
                    }
                    viewModelScope.launch(Dispatchers.IO) {
                        conversationDAO.insertConversations(newConversations)
                    }
                } catch (ex: Exception) {
                    Log.e(tag, ex.toString())
                }
            }, { code, err ->
                when (code) {
                    401 -> logOut()
                    404 -> conversationList.clear()
                    else -> Log.e(tag, err.message)
                }
            }, tag))
    }
}