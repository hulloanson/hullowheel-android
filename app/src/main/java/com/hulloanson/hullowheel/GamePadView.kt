package com.hulloanson.hullowheel

import android.annotation.SuppressLint
import android.content.Context
import android.opengl.GLSurfaceView
import android.view.MotionEvent



class GamePadView(context: Context?) : GLSurfaceView(context) {
  @SuppressLint("ClickableViewAccessibility")
  override fun onTouchEvent(event: MotionEvent?): Boolean {
    State.brake =
  }
}