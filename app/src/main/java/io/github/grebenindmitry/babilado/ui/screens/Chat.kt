package io.github.grebenindmitry.babilado.ui.screens

import android.graphics.Typeface
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.setPadding
import androidx.core.widget.doOnTextChanged
import androidx.navigation.NavController
import com.blacksquircle.ui.editorkit.plugin.autoindent.autoIndentation
import com.blacksquircle.ui.editorkit.plugin.base.PluginSupplier
import com.blacksquircle.ui.editorkit.utils.EditorTheme
import com.blacksquircle.ui.editorkit.widget.TextProcessor
import io.github.grebenindmitry.babilado.BabiladoViewModel
import io.github.grebenindmitry.babilado.R
import io.github.grebenindmitry.babilado.structures.Message
import io.github.grebenindmitry.babilado.structures.User
import io.github.grebenindmitry.babilado.ui.theme.BabiladoTheme
import java.text.SimpleDateFormat
import java.util.*

private const val tag = "Chat"

@Composable
fun ChatComposable(viewModel: BabiladoViewModel, navController: NavController, recipient: User) {
    var messageText by rememberSaveable { mutableStateOf("") }
    val messageList = remember { viewModel.getMessageLiveData(recipient) }
    var msgType by remember { mutableStateOf(0) }
    var textProcessor: TextProcessor? = null

    LaunchedEffect(true) {
        viewModel.loadMessages(recipient)
    }

    fun openUserInfo() {
        /* TODO */
    }

    fun sendMessage() {
        viewModel.sendMessage(messageText, msgType, recipient)
        messageText = ""
        textProcessor?.clearText()
    }

    @Composable
    fun MessageTimestamp(timestamp: Long) {
        Surface(modifier = Modifier.padding(8.dp), color = MaterialTheme.colors.surface,
            shape = MaterialTheme.shapes.small) {
            Text(SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestamp)), Modifier.padding(4.dp))
        }
    }

    @Composable
    fun MessageRow(msg: Message) {
        LazyRow(verticalAlignment = Alignment.Bottom, modifier = Modifier.fillMaxWidth(),
            reverseLayout = msg.sender == recipient.id,
            horizontalArrangement = if (msg.sender == recipient.id) Arrangement.Start else Arrangement.End) {
            item { MessageTimestamp(timestamp = msg.time_sent) }
            item {
                Card(shape = MaterialTheme.shapes.medium,
                    backgroundColor = if (msg.sender == recipient.id) MaterialTheme.colors.surface else MaterialTheme.colors.primary,
                    modifier = Modifier.widthIn(max = 275.dp), elevation = 2.dp) {
                    SelectionContainer {
                        Text(text = msg.data, modifier = Modifier.padding(8.dp),
                            fontFamily = if (msg.type == 1) FontFamily.Monospace else FontFamily.Default)
                    }
                }
            }
        }
    }

    @Composable
    fun SendMessageBar() {
        Surface(color = MaterialTheme.colors.surface, modifier = Modifier.wrapContentHeight()) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Button(onClick = { if (msgType == 0) msgType = 1 else msgType = 0 }, shape = MaterialTheme.shapes.small,
                    modifier = Modifier.padding(8.dp, 8.dp, 0.dp, 8.dp).size(50.dp)) {
                    Icon(if (msgType == 0) Icons.Outlined.Code else Icons.Outlined.Article, "Switch between code mode")
                }

                when (msgType) {
                    0 -> {
                        OutlinedTextField(messageText, { messageText = it },
                            label = { Text(stringResource(R.string.msg)) }, singleLine = false,
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                            keyboardActions = KeyboardActions(onSend = { sendMessage() }),
                            shape = MaterialTheme.shapes.medium,
                            modifier = Modifier.padding(8.dp, 0.dp, 8.dp, 8.dp).weight(4f, true))
                    }
                    1 -> {
                        Card(modifier = Modifier.padding(8.dp).weight(4f, true),
                            border = BorderStroke(1.dp, MaterialTheme.colors.onSurface)) {
                            AndroidView(factory = {
                                textProcessor = TextProcessor(it)
                                textProcessor!!.apply {
                                    setPadding(16)
                                    typeface = Typeface.MONOSPACE
                                    colorScheme = EditorTheme.MONOKAI
                                    plugins(PluginSupplier.create {
                                        autoIndentation {
                                            autoCloseBrackets = true
                                            autoCloseQuotes = true
                                            autoIndentLines = true
                                        }
                                    })

                                    doOnTextChanged { text, _, _, _ ->
                                        messageText = text.toString()
                                    }
                                }
                            })
                        }
                    }
                }
                Button(onClick = { sendMessage() }, shape = MaterialTheme.shapes.small,
                    modifier = Modifier.padding(0.dp, 8.dp, 8.dp, 8.dp).size(50.dp)) {
                    Icon(Icons.Outlined.Send, stringResource(R.string.send))
                }
            }
        }
    }

    BabiladoTheme {
        Scaffold(topBar = {
            TopAppBar(backgroundColor = MaterialTheme.colors.background, title = {
                Row(modifier = Modifier.clip(MaterialTheme.shapes.small).clickable { openUserInfo() }.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    if (!messageList.isNullOrEmpty()) {
                        Image(ImageBitmap.imageResource(R.drawable.boopsnoot), stringResource(R.string.alt_avatar),
                            Modifier.padding(4.dp, 0.dp).clip(CircleShape))
                    } else CircularProgressIndicator()
                    Text(if (messageList.isNullOrEmpty()) stringResource(R.string.loading) else recipient.username,
                        Modifier.padding(4.dp, 0.dp))
                }
            }, navigationIcon = {
                IconButton(onClick = { navController.navigateUp() },
                    content = { Icon(Icons.Outlined.ArrowBack, stringResource(R.string.back)) })
            }, modifier = Modifier.height(75.dp), elevation = 4.dp)
        }) {
            Column(verticalArrangement = Arrangement.Bottom) {
                LazyColumn(contentPadding = PaddingValues(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.weight(5f, true), reverseLayout = true) {
                    if (messageList.isNotEmpty()) {
                        items(messageList.sortedByDescending { it.time_sent }) { message -> MessageRow(message) }
                    }
                }
                SendMessageBar()
            }

        }
    }
}