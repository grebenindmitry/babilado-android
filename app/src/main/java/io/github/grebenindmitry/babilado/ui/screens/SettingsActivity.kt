package io.github.grebenindmitry.babilado.ui.screens

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import io.github.grebenindmitry.babilado.R

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val toolbar = findViewById<MaterialToolbar>(R.id.topAppBar)
        toolbar.setNavigationOnClickListener { finish() }

        val fragmentCompat = SettingsFragment()
        supportFragmentManager.beginTransaction().replace(R.id.settings_container, fragmentCompat).commit()
    }
}