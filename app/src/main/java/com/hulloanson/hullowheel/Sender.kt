package com.hulloanson.hullowheel

import java.lang.Exception
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.SocketException

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

