package com.hulloanson.vwheel

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.v4.view.ViewCompat
import android.view.MotionEvent
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.Button
import android.widget.LinearLayout
import android.widget.SeekBar
import com.bosphere.verticalslider.VerticalSlider
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.*
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.zip.GZIPOutputStream

class MainActivity : AppCompatActivity(), SensorEventListener {
  private var sock: DatagramSocket? = null

  private lateinit var states: ByteArray

  private lateinit var mutex: Mutex

  private lateinit var sensorManager: SensorManager

  private lateinit var sensor: Sensor

  private var send = true

  private val ROW_COUNT = 4

  private val COL_COUNT = 6

  private fun getInetAddress(): InetAddress {
    return InetAddress.getByAddress(byteArrayOf(192.toByte(), 168.toByte(), 43.toByte(), 66.toByte()))
  }

  private fun initStore() {
    states = ByteArray(36, fun(_): Byte { return 0 })
    mutex = Mutex()
  }

  private fun readStateAsync(): Deferred<ByteArray> {
    return GlobalScope.async {
      mutex.withLock {
        states.clone()
      }
    }
  }

  private fun writeStateAsync(position: Int, vararg values: Byte) {
    GlobalScope.async {
      mutex.withLock {
        for ((index, value) in values.withIndex()) {
            states[position + index] = value
        }
      }
    }
  }

  private fun writeStateAsync(position: Int, value: Byte) {
    GlobalScope.async {
      mutex.withLock {
        states[position] = value
      }
    }
  }

  private fun compress(raw: ByteArray) : ByteArray {
    val bStream = ByteArrayOutputStream(raw.size)
    val gStream = GZIPOutputStream(bStream)
    gStream.write(raw)
    gStream.close()
    val compressed = bStream.toByteArray()
    bStream.reset()
    return compressed
  }

  private fun prefixSize(raw: ByteArray): ByteArray {
      System.out.println("output size: ${raw.size.toByte()}")
      return byteArrayOf(raw.size.toByte()).plus(raw)
  }

  private fun startSending(): Job {
    sock = DatagramSocket()
    return GlobalScope.launch {
      sock!!.connect(getInetAddress(), 20000)
      while (send) {
        try {
          val compressedState = compress(readStateAsync().await())
          sock!!.send(DatagramPacket(compressedState, compressedState.size))
        } catch (e: IOException) {

        }
        delay(50)
      }
      System.out.println("Stopped sending")
      sock!!.disconnect()
      sock = null
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
  private fun constructVerticalBar(offset: Int): LinearLayout {
    val container = LinearLayout(this)
    val bar = VerticalSlider(this)
    bar.rotation = 180.0f
    bar.setPadding(50, 50, 50 , 50)
    bar.setOnSliderProgressChangeListener { progress ->
      writeStateAsync(offset, *floatToBytes(progress * 1000))
    }
    bar.setOnTouchListener { _, motionEvent ->
      if (motionEvent.action == MotionEvent.ACTION_UP) {
        bar.setProgress(0.0f)
        writeStateAsync(offset, *floatToBytes(0.0f))
      }
      false
    }
    container.addView(bar, MATCH_PARENT, MATCH_PARENT)
    return container
  }

  private fun constructView() {
    val container = LinearLayout(this)
    container.orientation = LinearLayout.HORIZONTAL

    val buttonPadParams = LinearLayout.LayoutParams(WRAP_CONTENT, MATCH_PARENT, 1.0f)
    container.addView(constructButtonPad(), buttonPadParams)

    val pedalParams = LinearLayout.LayoutParams(WRAP_CONTENT, MATCH_PARENT, 1.0f)
    // Brake
    container.addView(constructVerticalBar(8), pedalParams)
    // Gas
    container.addView(constructVerticalBar(4), pedalParams)

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
    button.setOnTouchListener { _, event: MotionEvent ->
      GlobalScope.launch {
        if (event.action == MotionEvent.ACTION_DOWN) {
          writeStateAsync(buttId + 12, 1)
        } else if (event.action == MotionEvent.ACTION_UP) {
          writeStateAsync(buttId + 12, 0)
        }
      }
      true
    }
    return button
  }

  private fun start() {
    initStore()
    startSending()
    listenToGyro()
  }

  private fun cleanUp() {
    stopListeningToGyro()
    stopSending()
  }

  private fun floatToBytes(f: Float): ByteArray {
    return ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putFloat(f).array()
  }

  private fun accelToXRotation(x: Float, y: Float): Float {
    return Math.abs(
            Math.atan2(x.toDouble(), y.toDouble()) / (Math.PI/180)
    ).toFloat()
  }

  /* Lifecycle functions */

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    constructView()
  }

  override fun onResume() {
    super.onResume()
    start()
  }

  override fun onPause() {
    super.onPause()
    cleanUp()
  }

  /* Lifecycle functions end*/

  /* Sensor callbacks */

  override fun onAccuracyChanged(p0: Sensor?, p1: Int) {

  }

  override fun onSensorChanged(e: SensorEvent?) {
    if (e?.sensor == null) return
    if (e.sensor != sensor) return
    val x = e.values?.get(0)
    val y = e.values?.get(1)
    if (x == null || y == null) return
    writeStateAsync(0, *floatToBytes(accelToXRotation(x, y)))
  }

  /* Sensor callbacks end */

}

class SeekBarChangedListener: SeekBar.OnSeekBarChangeListener {
  override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
  }

  override fun onStartTrackingTouch(p0: SeekBar?) {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun onStopTrackingTouch(p0: SeekBar?) {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

}