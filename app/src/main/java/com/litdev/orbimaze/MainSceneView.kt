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
import io.github.sceneview.collision.Quaternion
import io.github.sceneview.collision.Vector3
import io.github.sceneview.gesture.GestureDetector.OnGestureListener
import io.github.sceneview.gesture.MoveGestureDetector
import io.github.sceneview.gesture.RotateGestureDetector
import io.github.sceneview.gesture.ScaleGestureDetector
import io.github.sceneview.math.Position
import io.github.sceneview.math.toFloat3
import io.github.sceneview.math.toVector3
import io.github.sceneview.node.Node
import java.nio.ByteBuffer
import kotlin.math.abs

class MainSceneView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : SceneView(context, attrs, defStyleAttr) {
    val joints = mutableListOf<Joint>()
    val tubes = mutableListOf<Tube>()

    var viewMode = 0
    var level = 0
    var numEnemy = 0
    var numPill = 0
    val nextJoints = mutableListOf<Joint>()
    var nextJoint = 0
    var wait = false
    var cameraCatchup = 0.025f
    var cameraDist = 10.0f
    var scaleFactor = 0.02f
    val cameraMinDist = 0.5f
    val cameraMaxDist = 20.0f
    var cameraRot = 0.0f
    var rotationFactor = 1.0f
    val cameraDirStart = Vector3(0.0f, -0.5f, -1.0f).normalized()
    var cameraDir = cameraDirStart
    var speedMultiplier = 1.0f
    var speedFactor = 0.0025f
    val speedMinMultiplier = 0.1f
    val speedMaxMultiplier = 5.0f

    var lastDistanceX = 0.0f
    var lastDistanceY = 0.0f
    var flingCount = 0
    var pressCount = 0
    var moveCount = 0
    var scaleCount = 0
    var tapCount = 0
    var modeCount = 0

    val player: Orb = Orb()
    val highlight: Orb = Orb()
    val secondaryHighlights = mutableListOf<Orb>()
    val enemies = mutableListOf<Orb>()
    val pills = mutableListOf<Orb>()
    var lastTimeNanos: Long = Long.MAX_VALUE
    var fps: Int = 0
    val upDir = Vector3(0.0f, 1.0f, 0.0f)
    val rand = java.util.Random(System.currentTimeMillis())
    var gameState = -1

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
//        view.bloomOptions = bloomOptions
//        val fogOptions = com.google.android.filament.View.FogOptions()
//        fogOptions.enabled = true
//        view.fogOptions = fogOptions

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
            far = cameraMaxDist*1.5f
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
        cameraManipulator = null

        level = 1 //ApplicationClass.instance.level
        levelSet()
    }

    fun levelSet() {
        val children = childNodes.toList()
        this.removeChildNodes(children)

        lastDistanceX = 0.0f
        lastDistanceY = 0.0f
        flingCount = 0
        pressCount = 0
        moveCount = 0
        scaleCount = 0
        tapCount = 0
        modeCount = 0

        numEnemy = 0
        numPill = 0
        var enemySpeed = 0.0f
        var pillSpeed = 0.0f
        when(level)
        {
            1 -> {
                Generate(joints, tubes).cube(2, 2, 2, 0.0f, 0.0f)
            }
            2 -> {
                Generate(joints, tubes).cube(3, 3, 3, 0.1f, 0.2f)
                numEnemy = 2
                numPill = 1
                enemySpeed = 0.0f
                pillSpeed = 0.0f
            }
            3 -> {
                Generate(joints, tubes).cube(5, 1, 5, 0.0f, 1.5f)
                numEnemy = 3
                numPill = 3
                enemySpeed = 0.25f
                pillSpeed = 0.1f
            }
            4 -> {
                Generate(joints, tubes).cube(4, 4, 4, 0.1f, 0.2f)
                numEnemy = 3
                numPill = 3
                enemySpeed = 0.25f
                pillSpeed = 0.0f
            }
            5 -> {
                Generate(joints, tubes).random(50, 0.5f, 10.0f)
                        numEnemy = 3
                numPill = 3
                enemySpeed = 0.0f
                pillSpeed = 0.0f
            }
            6 -> {
                Generate(joints, tubes).cube(4, 4, 4, 0.1f, 0.2f)
                        numEnemy = 3
                numPill = 3
                enemySpeed = 0.25f
                pillSpeed = 0.0f
            }
            7 -> {
                Generate(joints, tubes).cube(5, 5, 5, 0.2f, 0.5f)
                numEnemy = 5
                numPill = 5
                enemySpeed = 0.25f
                pillSpeed = 0.1f
            }
            8 -> {
                Generate(joints, tubes).cube(6, 6, 6, 0.4f, 0.6f)
                numEnemy = 5
                numPill = 5
                enemySpeed = 0.25f
                pillSpeed = 0.0f
            }
            9 -> {
                Generate(joints, tubes).cube(7, 7, 7, 0.6f, 0.7f)
                numEnemy = 5
                numPill = 5
                enemySpeed = 0.25f
                pillSpeed = 0.1f
            }
            10 -> {
                Generate(joints, tubes).random(500, 0.5f, 3.0f)
                numEnemy = 5
                numPill = 5
                enemySpeed = 0.0f
                pillSpeed = 0.0f
            }
        }

        val gold = ContextCompat.getColor(context, R.color.gold)
        val silver = ContextCompat.getColor(context, R.color.silver)

        val materialLoader = materialLoader
        val jointMaterial = materialLoader.createColorInstance(
            color = gold,
            metallic = 1.0f,
            roughness = 0.1f,
            reflectance = 0.8f
        )
        val tubeMaterial = materialLoader.createColorInstance(
            color = silver,
            metallic = 1.0f,
            roughness = 0.1f,
            reflectance = 0.8f
        )

        for (tube in tubes) {
            tube.build(this, tubeMaterial, 12, 20, 0.05f)
        }
        for (joint in joints) {
            joint.build(this, jointMaterial, 24, 24, 0.07f)
        }

        val buffer = readAsset("materials/emissive_colored.filamat")
        val material = Material.Builder().payload(buffer, buffer.remaining()).build(engine)

        enemies.clear()
        for (i in 0..< numEnemy) {
            val enemy = Orb()
            enemy.build(this, material.createInstance(), Color.RED, 0.1f, 2.0f)
            enemy.tubeSet(tubes.random(), 1, enemySpeed)
            enemies.add(enemy)
        }
        pills.clear()
        for (i in 0..< numPill) {
            val pill = Orb()
            pill.build(this, material.createInstance(), Color.BLUE, 0.1f, 2.0f)
            pill.tubeSet(tubes.random(), 1, pillSpeed)
            pills.add(pill)
        }

        player.build(this, material.createInstance(), Color.MAGENTA, 0.125f, 2.0f)
        player.tubeSet(tubes.random(), 1, 0.3f)
        updateNextJoints()

        val buffer1 = readAsset("materials/emissive_colored.filamat")
        val material1 = Material.Builder().payload(buffer1, buffer1.remaining()).build(engine)
        highlight.build(this, material1.createInstance(), Color.YELLOW, 0.125f, 1.0f)

        secondaryHighlights.clear()
        for (i in 0..< 10)
        {
            val orb = Orb()
            orb.build(this, material1.createInstance(), Color.YELLOW, 0.075f, 1.0f)
            secondaryHighlights.add(orb)
        }

        gameState = 0
    }

    override fun onFrame(frameTimeNanos: Long) {
        super.onFrame(frameTimeNanos)
        if (gameState != 0) return

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
        if (dt < 0.0f || dt > 1.0f) return

        updateSprites(dt)
        updateCamera()
        updateNextJoint()
        updateGame()

        if (gameState != 0) {
            levelSet()
        }
    }

    fun updateGame() {
        if (level == 1) {
            if (flingCount > 1 &&
                pressCount > 1 &&
                moveCount > 2 &&
                scaleCount > 1 &&
                tapCount > 4) {
                gameState = 1
                level++
                if (ApplicationClass.instance.level < level) {
                    ApplicationClass.instance.level = level
                    ApplicationClass.instance.save()
                }
            }
        } else {
            for (enemy in enemies) {
                if (enemy.tube == player.tube) {
                    val dist = (enemy.positionGet() - player.positionGet()).toVector3().length()
                    if (dist < enemy.sphere.geometry.radius + player.sphere.geometry.radius) {
                        gameState = -1
                    }
                }
            }
            val markForDelete = mutableListOf<Orb>()
            for (pill in pills) {
                if (pill.tube == player.tube) {
                    val dist = (pill.positionGet() - player.positionGet()).toVector3().length()
                    if (dist < pill.sphere.geometry.radius + player.sphere.geometry.radius) {
                        markForDelete.add(pill)
                    }
                }
            }
            for (pill in markForDelete) {
                removeChildNode(pill.sphere)
                removeChildNode(pill.lightNode)
                pills.remove(pill)
            }
            if (pills.isEmpty()) {
                gameState = 1
                level++
                if (level > ApplicationClass.instance.maxLevel) {
                    gameState = 2 //Game completed!
                    level = ApplicationClass.instance.maxLevel
                }
                if (ApplicationClass.instance.level < level) {
                    ApplicationClass.instance.level = level
                    ApplicationClass.instance.save()
                }
            }
        }
    }

    fun updateSprites(dt: Float) {
        for (enemy in enemies) {
            enemy.r += enemy.dir * enemy.speed * dt / enemy.tube.length
            if (enemy.r < 0 || enemy.r > 1)
            {
                enemy.newTube()
            }
            enemy.positionSet(enemy.tube.pointP(enemy.r))
        }

        for (pill in pills) {
            pill.r += pill.dir * pill.speed * dt / pill.tube.length
            if (pill.r < 0 || pill.r > 1)
            {
                pill.newTube()
            }
            pill.positionSet(pill.tube.pointP(pill.r))
        }

        if (!wait) {
            player.r += player.dir * player.speed * speedMultiplier * dt / player.tube.length
            if (player.r < 0 || player.r > 1) {
                player.newTube(nextJoints[nextJoint])
                updateNextJoints()
            }
            player.positionSet(player.tube.pointP(player.r))
        }
    }

    fun updateCamera() {
        val playerPos = player.tube.pointV(player.r)
        val playerDir = player.tube.direction(player.r)
        playerDir.x *= player.dir
        playerDir.y *= player.dir
        playerDir.z *= player.dir

        if (viewMode == 0) {
            val rot = Quaternion.axisAngle(upDir, -cameraRot) //rotation about vertical direction
            cameraDir = Quaternion.rotateVector(rot, cameraDir)
        } else {
            val cameraDirLast = cameraDir.normalized() //For a smooth transition
            cameraDir = playerDir
            cameraDir.x -= 0.1f
            cameraDir.y -= 0.1f
            cameraDir.z -= 0.1f
            if (abs(cameraDir.y) > 0.99) { //Issue when view direction is same as up direction
                cameraDir.x = 0.01f
                cameraDir = cameraDir.normalized()
            }
            cameraDir.x = (1.0f-cameraCatchup)*cameraDirLast.x + cameraCatchup*cameraDir.x
            cameraDir.y = (1.0f-cameraCatchup)*cameraDirLast.y + cameraCatchup*cameraDir.y
            cameraDir.z = (1.0f-cameraCatchup)*cameraDirLast.z + cameraCatchup*cameraDir.z
        }
        val cameraPos = Vector3(
            playerPos.x - cameraDist * cameraDir.x,
            playerPos.y - cameraDist * cameraDir.y,
            playerPos.z - cameraDist * cameraDir.z)
        val cameraLookAt = playerPos //Vector3.add(playerPos, cameraDir)
        cameraNode.position = cameraPos.toFloat3()
        cameraNode.lookAt(cameraLookAt.toFloat3(), upDir.toFloat3())
        if (cameraNode.position.x.isNaN()) {
            TODO("Camera failure")
        }
    }

    fun updateNextJoints() {
        nextJoints.clear()
        val endJoint = if (player.dir > 0) player.tube.joint2 else player.tube.joint1
        for (tube in endJoint.tubes) {
            if (tube == player.tube) continue
            if (endJoint == tube.joint1) {
                nextJoints.add(tube.joint2)
            } else {
                nextJoints.add(tube.joint1)
            }
        }
        nextJoint = rand.nextInt(nextJoints.size)
    }

    fun updateNextJoint() {
        highlight.positionSet(nextJoints[nextJoint].pos.toFloat3())
        for (orb in secondaryHighlights) {
            orb.positionSet(nextJoints[nextJoint].pos.toFloat3())
        }
        var i = 0
        for (joint in nextJoints) {
            if (joint != nextJoints[nextJoint]) {
                secondaryHighlights[i++].positionSet(joint.pos.toFloat3())
            }
        }
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
                wait = false
                flingCount++
            }

            override fun onLongPress(
                e: MotionEvent,
                node: Node?
            ) {
                wait = true
                pressCount++
            }

            override fun onMove(
                detector: MoveGestureDetector,
                e: MotionEvent,
                node: Node?
            ) {
                cameraRot = (detector.lastDistanceX!!-lastDistanceX)*rotationFactor
                speedMultiplier -= (detector.lastDistanceY!!-lastDistanceY)*speedFactor
                if (speedMultiplier < speedMinMultiplier) speedMultiplier = speedMinMultiplier
                if (speedMultiplier > speedMaxMultiplier) speedMultiplier = speedMaxMultiplier
                lastDistanceX = detector.lastDistanceX!!
                lastDistanceY = detector.lastDistanceY!!
            }

            override fun onMoveBegin(
                detector: MoveGestureDetector,
                e: MotionEvent,
                node: Node?
            ) {
                lastDistanceX = detector.lastDistanceX!!
                lastDistanceY = detector.lastDistanceY!!
            }

            override fun onMoveEnd(
                detector: MoveGestureDetector,
                e: MotionEvent,
                node: Node?
            ) {
                cameraRot = 0.0f
                moveCount++
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
                cameraDist *= 1.0f - (detector.scaleFactor-1.0f)*scaleFactor
                if (cameraDist < cameraMinDist) cameraDist = cameraMinDist
                if (cameraDist > cameraMaxDist) cameraDist = cameraMaxDist
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
                scaleCount++
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
                nextJoint = (nextJoint + 1) % nextJoints.size
                tapCount++
            }

            override fun onSingleTapUp(
                e: MotionEvent,
                node: Node?
            ) {

            }
        }
    }
}