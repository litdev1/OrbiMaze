package com.litdev.orbimaze

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.core.content.ContextCompat
import com.google.android.filament.Material
import com.google.android.filament.utils.Manipulator
import dev.romainguy.kotlin.math.Float2
import io.github.sceneview.SceneView
import io.github.sceneview.gesture.GestureDetector.OnGestureListener
import io.github.sceneview.gesture.MoveGestureDetector
import io.github.sceneview.gesture.RotateGestureDetector
import io.github.sceneview.gesture.ScaleGestureDetector
import io.github.sceneview.math.Position
import io.github.sceneview.node.Node
import java.nio.ByteBuffer

class MainSceneView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : SceneView(context, attrs, defStyleAttr) {
    val nodes = mutableListOf<com.litdev.orbimaze.Node>()
    val tubes = mutableListOf<Tube>()
    val player: Orb = Orb()
    var lastTimeNanos: Long = 0
    var fps: Int = 0

    init {
        setGestureListener()
        initialiseScene()
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

    private fun initialiseScene() {
//        val bloomOptions = com.google.android.filament.View.BloomOptions()
//        bloomOptions.enabled = true
//        sceneView.view.bloomOptions = bloomOptions
//        val fogOptions = com.google.android.filament.View.FogOptions()
//        fogOptions.enabled = true
//        sceneView.view.fogOptions = fogOptions

        mainLightNode.apply {
            this?.intensity = 10000.0f
        }
        indirectLight.apply {
            this?.intensity = 10000.0f
        }
        cameraNode.apply {
            position = Position(x = 3.0f, y = 3.0f, z = 10.0f)
            focalLength = 28.0
            near = 0.01f
            far = 30.0f
        }

        val manipulator = Manipulator.Builder()
            .viewport(width, height)
            .mapMinDistance(0f)
            .targetPosition(3.0f, 3.0f, 3.0f)
            .orbitHomePosition(3.0f, 3.0f, 10.0f)
            .zoomSpeed(0.03f)
            .orbitSpeed(0.004f, 0.004f)
            .farPlane(100.0f)
            .flightMaxMoveSpeed(1.0f)
            .flightPanSpeed(0.001f, 0.001f)
            .flightSpeedSteps(80)
            .flightMoveDamping(15.0f)
            .flightStartPosition(3.0f,3.0f,10.0f)
            .flightStartOrientation(0.0f, 0.0f)
            .fovDegrees(33.00f)
            .fovDirection(Manipulator.Fov.VERTICAL)
            .mapMinDistance(0.0f)
            .mapExtent(10.0f, 10.0f)
            .build(Manipulator.Mode.ORBIT)
        cameraManipulator = manipulator

        val gold = ContextCompat.getColor(context, R.color.gold)
        val silver = ContextCompat.getColor(context, R.color.silver)

        val materialLoader = materialLoader
        val nodeMaterial = materialLoader.createColorInstance(color = silver,
            metallic = 1.0f,
            roughness = 0.1f,
            reflectance = 0.8f
        )
        val tubeMaterial = materialLoader.createColorInstance(color = gold,
            metallic = 1.0f,
            roughness = 0.1f,
            reflectance = 0.8f
        )

//        Generate(nodes, tubes).simple()
//        Generate(nodes, tubes).random(100)
        Generate(nodes, tubes).cube(7, 7, 7, 0.5f, 0.7f)

        for (tube in tubes) {
            tube.build(this, tubeMaterial, 12, 20, 0.05f)
        }
        for (node in nodes) {
            node.build(this, nodeMaterial, 24, 24, 0.0625f)
        }

        val buffer = readAsset("materials/emissive_colored.filamat")
        val material = Material.Builder().payload(buffer, buffer.remaining()).build(engine)
        val materialInstance = material.createInstance()
        player.build(this, materialInstance, Color.RED, 0.1f, 2.0f)
        player.positionSet(Position(x = 3.0f, y = 3.0f, z = 3.0f))
        player.tubeSet(tubes[0], 1, 1.0f)
    }

    override fun onFrame(frameTimeNanos: Long) {
        //We want to handle all activity here based on recorded gestures
        super.onFrame(frameTimeNanos)
        val deltaTime = (frameTimeNanos - lastTimeNanos) / 1000000
        fps = (1000 / deltaTime).toInt()
        lastTimeNanos = frameTimeNanos

        update(deltaTime.toFloat()/1000.0f)
    }

    private fun readAsset(assetName: String): ByteBuffer {
        val input = context.assets.open(assetName)
        val bytes = ByteArray(input.available())
        input.read(bytes)
        return ByteBuffer.wrap(bytes)
    }

    fun update(dt: Float) {
        if (dt > 1.0f) return
        player.r += player.dir * player.speed * dt / player.tube.length
        if (player.r < 0 || player.r > 1)
        {
            player.newTube()
        }
        player.positionSet(player.tube.pointP(player.r))
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
                if (node != null) {

                }
            }
        }
    }
}