package com.hulloanson.hullowheel

import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.ceil

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

