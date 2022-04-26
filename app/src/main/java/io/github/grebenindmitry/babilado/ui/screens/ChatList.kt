package io.github.grebenindmitry.babilado.ui.screens

import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.preference.PreferenceManager
import io.github.grebenindmitry.babilado.BabiladoViewModel
import io.github.grebenindmitry.babilado.R
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

private const val tag = "ChatList"

@ExperimentalMaterialApi
@Composable
fun ChatListComposable(viewModel: BabiladoViewModel, isDark: MutableState<Boolean>) {
    val activity = LocalContext.current as ComponentActivity
    val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity)
    val scaffoldState = rememberScaffoldState()
    val coroutineScope = rememberCoroutineScope()

    var newMessageDialogOpen by remember { mutableStateOf(false) }
    var newChatUsername by rememberSaveable { mutableStateOf("") }

    val conversationList = remember { viewModel.conversationList }

    LaunchedEffect(true) {
        viewModel.loadChats()
    }

    if (newMessageDialogOpen) {
        AlertDialog({ newMessageDialogOpen = false },
            title = { Text(stringResource(R.string.new_message_dialog_title), style = MaterialTheme.typography.h6) },
            text = { OutlinedTextField(newChatUsername, { newChatUsername = it }, textStyle = TextStyle.Default) },
            confirmButton = {
                Button({
                    newMessageDialogOpen = false
                    viewModel.newChat(newChatUsername)
                }) {
                    Text(stringResource(R.string.ok))
                }
            })
    }

    @Composable
    fun topAppBar() {
        TopAppBar({ Text(stringResource(R.string.app_name)) }, navigationIcon = {
            IconButton({ coroutineScope.launch { scaffoldState.drawerState.open() } }) {
                Icon(Icons.Outlined.Menu, null)
            }
        }, actions = {
            IconButton({ /* TODO */ }) { Icon(Icons.Outlined.Search, stringResource(R.string.search)) }
        })
    }

    @Composable
    fun drawer() {
        Box(Modifier.fillMaxSize()) {
            //Bottom bar
            Box(Modifier.align(Alignment.BottomStart).fillMaxWidth()) {
                IconButton({ activity.startActivity(Intent(activity, SettingsActivity::class.java)) }) {
                    Icon(Icons.Outlined.Settings, stringResource(R.string.settings))
                }
                IconButton({
                    sharedPreferences.edit().putBoolean("isDark", !isDark.value).apply()
                    isDark.value = !isDark.value
                }, Modifier.align(Alignment.BottomEnd)) {
                    Icon(if (isDark.value) Icons.Outlined.LightMode else Icons.Outlined.DarkMode,
                        stringResource(R.string.alt_dark_mode))
                }
            }
            Column {
                //Top user card
                Surface(Modifier.fillMaxWidth(), color = MaterialTheme.colors.primary, elevation = 10.dp) {
                    Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                        Image(ImageBitmap.imageResource(R.drawable.boopsnoot), stringResource(R.string.alt_avatar),
                            Modifier.size(80.dp).clip(CircleShape).align(Alignment.CenterHorizontally))
                        Text(viewModel.user.username, style = MaterialTheme.typography.h4, textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth())
                        Button({ viewModel.logOut() },
                            colors = ButtonDefaults.buttonColors(MaterialTheme.colors.secondary)) {
                            Text(stringResource(R.string.log_out))
                        }
                    }
                }
                //Action list
                Column(Modifier.fillMaxWidth()) {
                    Row(Modifier.clickable { }.padding(8.dp).fillMaxWidth()) {
                        Icon(Icons.Outlined.Edit, null, Modifier.padding(8.dp))
                        Text(stringResource(R.string.new_msg), Modifier.align(Alignment.CenterVertically))
                    }
                }
            }
        }
    }

    @Composable
    fun chatList() {
        LazyColumn {
            conversationList.forEach { conversation ->
                item {
                    ListItem(text = { Text(conversation.user.username) }, icon = {
                        Image(ImageBitmap.imageResource(R.drawable.boopsnoot), stringResource(R.string.alt_avatar),
                            Modifier.size(60.dp).clip(CircleShape))
                    }, secondaryText = {
                        Text(if (conversation.lastMsg.type == 0) conversation.lastMsg.data else "WIP")
                    }, trailing = {
                        Text(
                            SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(conversation.lastMsg.time_sent)))
                    }, modifier = Modifier.clickable {
                        viewModel.navController.navigate(
                            "chat?userId=${conversation.user.id}&username=${conversation.user.username}")
                    })
                    Divider()
                }
            }
        }
    }


    Scaffold(scaffoldState = scaffoldState, topBar = { topAppBar() }, floatingActionButton = {
        FloatingActionButton({ newMessageDialogOpen = true }) { Icon(Icons.Outlined.Add, null) }
    }, drawerContent = { drawer() }) {
        when {
            conversationList.isEmpty() -> Box(Modifier.fillMaxSize()) {
                Text(stringResource(R.string.no_chats_msg), style = MaterialTheme.typography.h5,
                    textAlign = TextAlign.Center, modifier = Modifier.align(Alignment.Center).padding(16.dp))
            }
            else -> chatList()
        }
    }
}