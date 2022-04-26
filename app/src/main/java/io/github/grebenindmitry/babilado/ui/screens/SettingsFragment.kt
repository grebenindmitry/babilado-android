package io.github.grebenindmitry.babilado.ui.screens

import android.os.Bundle
import android.text.InputType
import androidx.preference.EditTextPreference
import androidx.preference.PreferenceFragmentCompat

class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        val context = preferenceManager.context
        val screen = preferenceManager.createPreferenceScreen(context)

        val serverPreference = EditTextPreference(context)
        serverPreference.key = "apiUrl"
        serverPreference.text = "https://babilado-backend.herokuapp.com/api"
        serverPreference.title = "API URL"
        serverPreference.summaryProvider = EditTextPreference.SimpleSummaryProvider.getInstance()
        serverPreference.setOnBindEditTextListener { editText ->
            editText.inputType = InputType.TYPE_TEXT_VARIATION_URI
            editText.isSingleLine = true
        }
        screen.addPreference(serverPreference)

        preferenceScreen = screen
    }
}