package com.hulloanson.hullowheel

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceManager
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.*
import android.widget.Button
import com.bosphere.verticalslider.VerticalSlider
import kotlinx.coroutines.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.lang.Exception
import java.net.*
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*
import kotlin.math.*

const val BTN_CNT = 24

//private fun makeSlider(context: Context): VerticalSlider {
//
//}

class MainActivity() : AppCompatActivity(){

  /** Lifecycle functions **/

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
//    setContentView(R.layout.activity_main)
    setContentView(constructView(profile = getCurrentProfile(PreferenceManager.getDefaultSharedPreferences(this)), context = this))
  }

  private fun setImmersiveFullscreen() {
    window.decorView.apply {
      systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_IMMERSIVE
    }
  }

  override fun onResume() {
    super.onResume()
    setImmersiveFullscreen()
    window.decorView.setOnSystemUiVisibilityChangeListener { visibility ->
      if (visibility and View.SYSTEM_UI_FLAG_FULLSCREEN == 0) {
        Handler().postDelayed({
          setImmersiveFullscreen()
        }, 1000)
      }
    }
  }

  override fun onPause() {
    super.onPause()
  }

  /** Lifecycle functions end **/

}
