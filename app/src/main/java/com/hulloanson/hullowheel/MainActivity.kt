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
import android.widget.Button
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.Space
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

object State {
  var wheel: Short = 0

  var gas: Byte = 0

  var brake: Byte = 0

  var btns: Array<Int> = Array(BTN_CNT, fun (_): Int { return 0 })

  fun packStates(): ByteArray {
    val buf = ByteBuffer.allocate(
            Short.SIZE_BYTES // wheel
                    + Byte.SIZE_BYTES * 2 // gas and brake
                    + ceil(BTN_CNT / 8.0).toInt() // 1 button consumes 1 bit
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
}

fun setLayoutParam(view: View, position: Position) {
  val layoutParams = GridLayout.LayoutParams()
  layoutParams.columnSpec = GridLayout.spec(position.nthColumn, position.width)
  layoutParams.rowSpec = GridLayout.spec(position.nthRow, position.height)
  layoutParams.width = MATCH_PARENT
  layoutParams.height = MATCH_PARENT
  view.layoutParams = layoutParams
}

private fun makeButton(context: Context, label: String, onClick: View.OnClickListener): Button {
  val button = Button(context)
  button.text = label
  button.setBackgroundColor(Color.GRAY)
  button.setOnClickListener(onClick)
  return button
}

//private fun makeSlider(context: Context): VerticalSlider {
//
//}

@SuppressLint("ClickableViewAccessibility")
private fun makeVerticalBar(setValue: (Byte) -> Unit, context: Context): VerticalSlider {
  val bar = VerticalSlider(context)
  bar.rotation = 180.0f
  bar.setOnSliderProgressChangeListener { progress -> setValue((progress * 120).toInt().toByte()) }
  bar.setOnTouchListener { _, motionEvent ->
    if (motionEvent.action == MotionEvent.ACTION_UP) {
      bar.setProgress(0.0f)
      setValue(0)
    }
    false
  }
  return bar
}

fun constructView(profile: UIProfile, context: Context) : GridLayout {
  val grid = GridLayout(context)
  val layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT)
  grid.rowCount = profile.rowCount
  grid.columnCount = profile.columnCount
  grid.layoutParams = layoutParams
  for (i in 0 until grid.rowCount) {
    for (j in 0 until grid.columnCount) {
//      val e = makeButton(context, "1", onClick = {})
      val e = Space(context)
      setLayoutParam(e, Position(j, i, 1, 1))
      grid.addView(e)
    }
  }

//  for (e in profile.gamePadElements) {
//    if (e is Slider) {
//      // draw slider in gridlayout
//      val slider = makeVerticalBar(setValue = {_ -> }, context = context)
//      setLayoutParam(slider, e.position)
//      grid.addView(slider)
//    } else if (e is com.hulloanson.hullowheel.Button) {
//      // draw button in gridlayout
//      val button = makeButton(context, e.label, onClick = {})
//      setLayoutParam(button, e.position)
//      grid.addView(button)
//    } // ignore if it's other stuff
//  }
  return grid
}

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
