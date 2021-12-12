package com.hulloanson.hullowheel

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Space
import com.bosphere.verticalslider.VerticalSlider
import kotlinx.coroutines.*
import java.io.IOException
import java.lang.Exception
import java.lang.Math.PI
import java.net.*
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*
import kotlin.math.atan2
import kotlin.math.max
import kotlin.math.min

object Sender {
  private lateinit var sock: DatagramSocket

  fun send(bytes: ByteArray, dstAddress: InetAddress, dstPort: Int = 20000) {
    try {
      if (!::sock.isInitialized || sock.isClosed) {
        sock = DatagramSocket()
      }
      sock.send(DatagramPacket(bytes, bytes.size, dstAddress, dstPort))
      // TODO: catch these errors and give appropriate responses
    } catch (e: SecurityException) {
      // Possible reasons // TODO: what?
      throw e
    } catch (e: SocketException) {
      throw e
    } catch (e: Exception) {
      throw e
    }
  }
}

class MainActivity(
        private var btns: Array<Int> = Array(24, fun (_): Int { return 0 }), private var btnsAutoRelease: Array<UUID?> = Array(24, fun(_): UUID? { return null }))
  : AppCompatActivity(), SensorEventListener {
  private var wheel: Short = 0

  private var gas: Byte = 0

  private var brake: Byte = 0

  private lateinit var sensorManager: SensorManager

  private lateinit var sensor: Sensor

  private var send = true

  private val ROW_COUNT = 4

  private val COL_COUNT = 6

  private val SAMPLING_INTERVAL : Long = 50 // ms

  private lateinit var address: String

  private var port: Int = 0

  private var wheelTurnThreshold = 0 // deg

  private fun getInetAddress(): InetAddress {
    return InetAddress.getByName(address)
  }

  private fun packStates(): ByteArray {
    val buf = ByteBuffer.allocate(
            Short.SIZE_BYTES // wheel
                    + Byte.SIZE_BYTES * 2 // gas and brake
                    + 3 // 24 bits / 8
    )
    buf.order(ByteOrder.LITTLE_ENDIAN)
    buf.putShort(0, wheel)
    buf.put(2, gas)
    buf.put(3, brake)
    var btnOffset = 0
    while (btnOffset < 3) {
      var i = 0
      var n = 0
      while (i < 8) {
        n = n or (btns[btnOffset * 8 + i] shl i)
        i++
      }
      buf.put(4 + btnOffset, n.toByte())
      btnOffset++
    }
    return buf.array()
  }

  private fun startSending(): Job {
    return GlobalScope.launch {
      while (send) {
        try {
          Sender.send(packStates(), getInetAddress())
        } catch (e: IOException) {
          continue
        }
        delay(SAMPLING_INTERVAL)
      }
      println("Stopped sending")
    }
  }

  private fun listenToGyro() {
    sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
    sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    sensorManager.registerListener(this, sensor, 25000) // 25ms
  }

  private fun stopListeningToGyro() {
    sensorManager.unregisterListener(this)
  }

  private fun stopSending() {
    send = false
  }

  private fun constructButtonPad(): LinearLayout {
    val container = LinearLayout(this)
    container.orientation = LinearLayout.VERTICAL
    val ids = IntArray(ROW_COUNT)
    for (i in 0 until ROW_COUNT) {
      val row = makeRow()
      row.id = ViewCompat.generateViewId()
      ids[i] = row.id

      for (j in 0 until COL_COUNT) {
        val button = makeButton(i, j)
        val params = LinearLayout.LayoutParams(WRAP_CONTENT, MATCH_PARENT, 1.0f)
        row.addView(button, params)
      }
      container.addView(row, LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT, 1.0f))
    }
    return container
  }

  @SuppressLint("ClickableViewAccessibility")
  private fun constructVerticalBar(setValue: (Byte) -> Unit): LinearLayout {
    val container = LinearLayout(this)
    container.orientation = LinearLayout.VERTICAL
    val bar = VerticalSlider(this)
    bar.rotation = 180.0f
    val thickness = 100
    bar.setThumbRadiusPx((thickness * 1.2).toInt())
    bar.setTrackBgThicknessPx(thickness)
    bar.setTrackFgThicknessPx(thickness)
    bar.setOnSliderProgressChangeListener { progress -> setValue((progress * 120).toByte()) }
    bar.setOnTouchListener { _, motionEvent ->
      if (motionEvent.action == MotionEvent.ACTION_UP) {
        bar.setProgress(0.0f)
        setValue(0)
      }
      println("motionEvent.x: ${motionEvent.x}")
      println("motionEvent.y: ${motionEvent.y}")
      false
    }
    val barParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT, 1.0f)
    container.addView(Space(this), LinearLayout.LayoutParams(MATCH_PARENT, 20, 0.0f))
    container.addView(bar, barParams)
    container.addView(Space(this), LinearLayout.LayoutParams(MATCH_PARENT, 50, 0.0f))
    return container
  }

  private fun constructView() {
    val container = LinearLayout(this)
    container.orientation = LinearLayout.HORIZONTAL

//    container.addView(Space(this), LinearLayout.LayoutParams(100, MATCH_PARENT, 1.0f))
    val buttonPadParams = LinearLayout.LayoutParams(WRAP_CONTENT, MATCH_PARENT, 1.0f)
    container.addView(constructButtonPad(), buttonPadParams)

    container.addView(Space(this), LinearLayout.LayoutParams(50, MATCH_PARENT, 0.0f))
    val pedalParams = LinearLayout.LayoutParams(300, MATCH_PARENT, 0.0f)
    // Brake
    container.addView(constructVerticalBar{ v -> brake = v }, pedalParams)
    // space
//    container.addView(Space(this), LinearLayout.LayoutParams(100, MATCH_PARENT, 0.0f))
    // Gas
    container.addView(constructVerticalBar{ v -> gas = v }, pedalParams)
    container.addView(Space(this), LinearLayout.LayoutParams(350, MATCH_PARENT, 0.0f))

//    container.addView(Space(this), LinearLayout.LayoutParams(5, MATCH_PARENT, 1.0f))
    addContentView(container, ConstraintLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT))
  }

  private fun makeRow(): LinearLayout {
    val linLayout = LinearLayout(this)
    linLayout.minimumWidth = MATCH_PARENT
    return linLayout
  }

  @SuppressLint("ClickableViewAccessibility")
  private fun makeButton(row: Int, col: Int): Button {
    val buttId = row * COL_COUNT + col
    val button = Button(this)
    button.text = (buttId + 1).toString()
    button.setBackgroundColor(Color.GRAY)
    button.setOnClickListener{ _ ->
      Log.i("button onClick", "button $buttId clicked")
      btns[buttId] = 1
      GlobalScope.launch {
        // release only after a delay to make sure at least one sampling catches the click
        delay(SAMPLING_INTERVAL)
        btns[buttId] = 0
      }
    }
    return button
  }

  private fun start() {
    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    startSending()
    listenToGyro()
  }

  private fun cleanUp() {
    window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    stopListeningToGyro()
    stopSending()
  }

  private fun accelToXRotation(x: Float, y: Float): Short {
    var r = atan2(x.toDouble(), y.toDouble()) / (PI /180)
    Log.d("accelToXRotation", "raw rotation is $r")
    if (r in 90.0..180.0) {
      r = 90 - r
    } else if (r in -180.0..-90.0) {
      r = -270 - r
    } else if (r < 90 && r >= 0) {
      r = 90 - r
    } else if (r < 0 && r > -90) {
      r = 90 - r
    }
    r = max(min(r, 150.0), -150.0)
    Log.d("accelToXRotation", "normalized rotation is $r")
    return r.toInt().toShort()
  }

  /** Lifecycle functions **/

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    address = intent.getStringExtra("address")
    port = intent.getIntExtra("port", 0)
    setContentView(R.layout.activity_main)
    constructView()
  }

  private fun setImmersiveFullscreen() {
    window.decorView.apply {
      systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_IMMERSIVE
    }
  }

  override fun onResume() {
    super.onResume()
    start()
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
    cleanUp()
  }

  /** Lifecycle functions end **/

  /** Sensor callbacks **/

  override fun onAccuracyChanged(p0: Sensor?, p1: Int) {

  }

  override fun onSensorChanged(e: SensorEvent?) {
    if (e?.sensor == null) return
    if (e.sensor != sensor) return
    val x = e.values?.get(0)
    val y = e.values?.get(1)
    if (x == null || y == null) return
    val newVal = accelToXRotation(x, y)
    if (kotlin.math.abs(newVal - wheel) >= wheelTurnThreshold) {
      wheel = newVal
    }
  }

  /** Sensor callbacks end **/

}
