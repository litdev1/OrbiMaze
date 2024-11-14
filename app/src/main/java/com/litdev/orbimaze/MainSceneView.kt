package com.litdev.orbimaze

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import dev.romainguy.kotlin.math.Float2
import io.github.sceneview.SceneView
import io.github.sceneview.gesture.GestureDetector.OnGestureListener
import io.github.sceneview.gesture.MoveGestureDetector
import io.github.sceneview.gesture.RotateGestureDetector
import io.github.sceneview.gesture.ScaleGestureDetector
import io.github.sceneview.math.Position
import io.github.sceneview.node.Node

class MainSceneView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : SceneView(context, attrs, defStyleAttr) {
    init {
        setGestureListener()
    }

//    @SuppressLint("ClickableViewAccessibility")
//    override fun onTouchEvent(event: MotionEvent): Boolean {
//        if (!super.onTouchEvent(event)) {
//            when (event.action) {
////                MotionEvent.ACTION_DOWN -> return true
////                MotionEvent.ACTION_MOVE -> return true
////                MotionEvent.ACTION_UP -> return true
//            }
//        }
//        return false
//    }

    var lastTimeNanos: Long = 0
    var fps: Int = 0

    override fun onFrame(frameTimeNanos: Long) {
        //We want to handle all activity here based on recorded gestures
        super.onFrame(frameTimeNanos)
        val deltaTime = (frameTimeNanos - lastTimeNanos) / 1000000
        fps = (1000 / deltaTime).toInt()
        lastTimeNanos = frameTimeNanos
    }

    fun setGestureListener() {
        onGestureListener = object : OnGestureListener {
            override fun onContextClick(
                e: MotionEvent,
                node: Node?
            ) {

            }

            override fun onDoubleTap(
                e: MotionEvent,
                node: Node?
            ) {

            }

            override fun onDoubleTapEvent(
                e: MotionEvent,
                node: Node?
            ) {

            }

            override fun onDown(
                e: MotionEvent,
                node: Node?
            ) {
                cameraNode.position += Position(0f, 0f, -1f)
            }

            override fun onFling(
                e1: MotionEvent?,
                e2: MotionEvent,
                node: Node?,
                velocity: Float2
            ) {

            }

            override fun onLongPress(
                e: MotionEvent,
                node: Node?
            ) {

            }

            override fun onMove(
                detector: MoveGestureDetector,
                e: MotionEvent,
                node: Node?
            ) {

            }

            override fun onMoveBegin(
                detector: MoveGestureDetector,
                e: MotionEvent,
                node: Node?
            ) {

            }

            override fun onMoveEnd(
                detector: MoveGestureDetector,
                e: MotionEvent,
                node: Node?
            ) {

            }

            override fun onRotate(
                detector: RotateGestureDetector,
                e: MotionEvent,
                node: Node?
            ) {

            }

            override fun onRotateBegin(
                detector: RotateGestureDetector,
                e: MotionEvent,
                node: Node?
            ) {

            }

            override fun onRotateEnd(
                detector: RotateGestureDetector,
                e: MotionEvent,
                node: Node?
            ) {

            }

            override fun onScale(
                detector: ScaleGestureDetector,
                e: MotionEvent,
                node: Node?
            ) {

            }

            override fun onScaleBegin(
                detector: ScaleGestureDetector,
                e: MotionEvent,
                node: Node?
            ) {

            }

            override fun onScaleEnd(
                detector: ScaleGestureDetector,
                e: MotionEvent,
                node: Node?
            ) {

            }

            override fun onScroll(
                e1: MotionEvent?,
                e2: MotionEvent,
                node: Node?,
                distance: Float2
            ) {

            }

            override fun onShowPress(
                e: MotionEvent,
                node: Node?
            ) {

            }

            override fun onSingleTapConfirmed(
                e: MotionEvent,
                node: Node?
            ) {

            }

            override fun onSingleTapUp(
                e: MotionEvent,
                node: Node?
            ) {

            }
        }
    }
}