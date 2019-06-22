package com.hulloanson.vwheel

import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.Menu
import android.view.MenuItem
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity;

import kotlinx.android.synthetic.main.activity_settings.*
import java.lang.NullPointerException

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        supportFragmentManager
                .beginTransaction()
                .add(R.id.settings_container, SettingsFragment())
                .commit()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_settings, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item == null) return true
        if (item.itemId == R.id.action_start) {
            // Get set address
            val address = PreferenceManager.getDefaultSharedPreferences(this).getString("address", null)
                    ?: throw NullPointerException("unexpected: address is null")
            startActivity(Intent(this, MainActivity::class.java).apply {
                putExtra("address", address)
            })
        }
        return false
    }

}
