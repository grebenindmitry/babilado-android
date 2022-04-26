package io.github.grebenindmitry.babilado.ui.screens

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.preference.PreferenceManager
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import io.github.grebenindmitry.babilado.structures.User
import io.github.grebenindmitry.babilado.ui.theme.BabiladoTheme
import io.github.grebenindmitry.babilado.BabiladoViewModel
import java.util.*

@ExperimentalComposeUiApi
@ExperimentalMaterialApi
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
            val isSystemDark = isSystemInDarkTheme()
            val isDark = remember { mutableStateOf(sharedPreferences.getBoolean("isDark", isSystemDark)) }
            BabiladoTheme(darkTheme = isDark.value) {
                val systemUiController = rememberSystemUiController()

                SideEffect { systemUiController.setStatusBarColor(Color.Transparent, !isDark.value) }

                val navController = rememberNavController()
                val viewModel = BabiladoViewModel(this, navController)

                NavHost(navController, "chatList") {
                    composable("chatList") { ChatListComposable(viewModel, isDark) }

                    composable("register") { RegisterComposable(viewModel) }

                    composable("chat?userId={userId}&username={username}") {
                        ChatComposable(viewModel, navController,
                            User(it.arguments?.getString("userId")!!, it.arguments?.getString("username")!!))
                    }
                }
            }
        }
    }
}