package io.github.grebenindmitry.babilado.ui.screens

import android.content.Intent
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.AutofillNode
import androidx.compose.ui.autofill.AutofillType
import androidx.compose.ui.composed
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalAutofill
import androidx.compose.ui.platform.LocalAutofillTree
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.preference.PreferenceManager
import io.github.grebenindmitry.babilado.*
import io.github.grebenindmitry.babilado.R
import kotlinx.coroutines.launch
import okhttp3.Request

private const val tag = "register"

@ExperimentalComposeUiApi
fun Modifier.autofill(
    autofillTypes: List<AutofillType>,
    onFill: ((String) -> Unit),
) = composed {
    val autofill = LocalAutofill.current
    val autofillNode = AutofillNode(onFill = onFill, autofillTypes = autofillTypes)
    LocalAutofillTree.current += autofillNode

    this.onGloballyPositioned {
        autofillNode.boundingBox = it.boundsInWindow()
    }.onFocusChanged { focusState ->
        autofill?.run {
            if (focusState.isFocused) {
                requestAutofillForNode(autofillNode)
            } else {
                cancelAutofillForNode(autofillNode)
            }
        }
    }
}

@ExperimentalComposeUiApi
@Composable
fun RegisterComposable(viewModel: BabiladoViewModel) {
    var username by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    var usernameError by rememberSaveable { mutableStateOf(false) }
    var passwordError by rememberSaveable { mutableStateOf(false) }

    var passwordVisibility by rememberSaveable { mutableStateOf(false) }
    var loginLoading by rememberSaveable { mutableStateOf(false) }
    var registerLoading by rememberSaveable { mutableStateOf(false) }

    val passwordRegex = Regex("[^A-Za-z0-9 !@#\$%&*\\-_=+]")

    val scaffoldState = rememberScaffoldState()
    val coroutineScope = rememberCoroutineScope()
    val activity = LocalContext.current as ComponentActivity
    val keyboardController = LocalSoftwareKeyboardController.current

    val usernameAutofillNode = AutofillNode(autofillTypes = listOf(AutofillType.NewUsername, AutofillType.Username),
        onFill = { username = it })

    val passwordAutofillNode = AutofillNode(autofillTypes = listOf(AutofillType.Password, AutofillType.NewPassword),
        onFill = { password = it })

    LocalAutofillTree.current += usernameAutofillNode
    LocalAutofillTree.current += passwordAutofillNode

    fun checkFields(): Boolean {
        if (username == "") usernameError = true
        if (password == "") passwordError = true

        if (username != "" && password != "") return true
        return false
    }

    val invalidCredentialsMsg = stringResource(R.string.invalid_cred)

    fun login() {
        if (!checkFields()) {
            return
        }

        loginLoading = true
        viewModel.login(username, password, onSuccess = {
            loginLoading = false
            coroutineScope.launch {
                viewModel.navController.popBackStack()
                viewModel.navController.navigate("chatList")
            }
        }, onError = { code, err ->
            loginLoading = false
            when (code) {
                401 -> coroutineScope.launch { scaffoldState.snackbarHostState.showSnackbar(invalidCredentialsMsg) }
                else -> Log.e(tag, err.message)
            }
        })
    }

    val userExistsMsg = stringResource(R.string.user_exists, username)

    fun register() {
        if (!checkFields()) {
            return
        }

        registerLoading = true
        viewModel.register(username, password, {
            registerLoading = false
            login()
        }, { code, err ->
            registerLoading = false
            when (code) {
                409 -> coroutineScope.launch { scaffoldState.snackbarHostState.showSnackbar(userExistsMsg) }
                else -> Log.e(tag, err.message)
            }
        })
    }

    Scaffold(scaffoldState = scaffoldState, topBar = {
        IconButton(onClick = { activity.startActivity(Intent(activity, SettingsActivity::class.java)) }) {
            Icon(Icons.Outlined.Settings, "Settings")
        }
    }) {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.SpaceAround) {
                Text(text = stringResource(R.string.login_register_prompt), style = MaterialTheme.typography.h3,
                    modifier = Modifier.padding(16.dp))

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    AutofillNode(autofillTypes = listOf(AutofillType.Username), onFill = { username = it })
                    OutlinedTextField(value = username, onValueChange = {
                        usernameError = false
                        username = passwordRegex.replace(it, "")
                    }, modifier = Modifier.padding(8.dp)
                        .autofill(listOf(AutofillType.Username, AutofillType.NewUsername)) { username = it },
                        singleLine = true, isError = usernameError,
                        keyboardOptions = KeyboardOptions(autoCorrect = false, imeAction = ImeAction.Next),
                        label = { Text(text = stringResource(R.string.username)) })
                    OutlinedTextField(value = password, onValueChange = {
                        passwordError = false
                        if (it.contains(Char(10))) if (checkFields()) login()
                        password = Regex("[^A-Za-z0-9 !@#\$%&*\\-_=+]").replace(it, "")
                    }, modifier = Modifier.padding(8.dp)
                        .autofill(listOf(AutofillType.Password, AutofillType.NewPassword)) { password = it },
                        singleLine = true, isError = passwordError,
                        keyboardOptions = KeyboardOptions(autoCorrect = false, imeAction = ImeAction.Done,
                            keyboardType = KeyboardType.Password), keyboardActions = KeyboardActions(onDone = {
                            keyboardController?.hide()
                            if (checkFields()) login()
                        }),
                        visualTransformation = if (passwordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
                        label = { Text(text = stringResource(R.string.password)) }, trailingIcon = {
                            IconButton(onClick = { passwordVisibility = !passwordVisibility }) {
                                Icon(imageVector = if (passwordVisibility) Icons.Filled.Visibility
                                else Icons.Filled.VisibilityOff, "")
                            }
                        })
                    Row {
                        Box(Modifier.padding(8.dp)) {
                            OutlinedButton({ register() }) {
                                Text(stringResource(R.string.register))
                            }
                            if (registerLoading) {
                                CircularProgressIndicator(color = MaterialTheme.colors.secondary,
                                    modifier = Modifier.align(Alignment.Center))
                            }
                        }
                        Box(Modifier.padding(8.dp)) {
                            Button({ if (checkFields()) login() }) { Text(text = stringResource(R.string.login)) }
                            if (loginLoading) {
                                CircularProgressIndicator(color = MaterialTheme.colors.secondary,
                                    modifier = Modifier.align(Alignment.Center))
                            }
                        }
                    }
                }
            }
        }
    }
}