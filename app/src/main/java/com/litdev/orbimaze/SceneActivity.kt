package com.litdev.orbimaze

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.google.android.filament.Material
import com.google.android.filament.utils.Manipulator
import io.github.sceneview.math.Position
import io.github.sceneview.math.Size
import io.github.sceneview.node.LightNode
import java.nio.ByteBuffer

lateinit var sceneActivity: SceneActivity

class SceneActivity : AppCompatActivity() {
    lateinit var sceneView: MainSceneView
    lateinit var textView: TextView
    val nodes = mutableListOf<Node>()
    val tubes = mutableListOf<Tube>()
    val player: Orb = Orb()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //enableEdgeToEdge()
        setContentView(R.layout.activity_scene)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        sceneActivity = this
        setFullScreen(
            findViewById(R.id.main)
        )

        sceneView = findViewById<MainSceneView>(R.id.sceneView)
        textView = findViewById<TextView>(R.id.fpsView)

        initialiseScene()

        val handler = Handler(Looper.getMainLooper())
        val updateRunnable = object : Runnable {
            override fun run() {
                updateUI()
                handler.postDelayed(this, 100)
            }
        }
        handler.postDelayed(updateRunnable, 0)
    }

    private fun initialiseScene() {
//        val bloomOptions = com.google.android.filament.View.BloomOptions()
//        bloomOptions.enabled = true
//        sceneView.view.bloomOptions = bloomOptions
//        val fogOptions = com.google.android.filament.View.FogOptions()
//        fogOptions.enabled = true
//        sceneView.view.fogOptions = fogOptions

        sceneView.mainLightNode.apply {
            this?.intensity = 10000.0f
        }
        sceneView.indirectLight.apply {
            this?.intensity = 10000.0f
        }
        sceneView.cameraNode.apply {
            position = Position(x = 3.0f, y = 3.0f, z = 10.0f)
            focalLength = 28.0
            near = 0.01f
            far = 30.0f
        }

//        val lightEntity1 = EntityManager.get().create()
//        LightManager.Builder(LightManager.Type.DIRECTIONAL).apply {
//            color(1.0f, 1.0f, 1.0f)
//            intensity(100_000.0f)
//            direction(1.0f, -1.0f, 0.0f)
//            castShadows(true)
//            build(sceneView.engine, lightEntity1)
//        }
//        val lightNode1 = LightNode(sceneView.engine, lightEntity1)
//        sceneView.addChildNode(lightNode1)

//        val lightEntity2 = EntityManager.get().create()
//        LightManager.Builder(LightManager.Type.SPOT).apply {
//            color(1.0f, 1.0f, 1.0f)
//            intensity(100_000.0f)
//            direction(-1.0f, 0.0f, 0.0f)
//            position(2.0f, 0.0f, 0.0f)
//            falloff(10.0f)
//            spotLightCone(0.5f, 1.0f)
//            castShadows(true)
//            build(sceneView.engine, lightEntity2)
//        }
//        val lightNode2 = LightNode(sceneView.engine, lightEntity2)
//        sceneView.addChildNode(lightNode2)

        val manipulator = Manipulator.Builder()
            .viewport(sceneView.width, sceneView.height)
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
        sceneView.cameraManipulator = manipulator

        val gold = ContextCompat.getColor(this, R.color.gold)
        val silver = ContextCompat.getColor(this, R.color.silver)

        val materialLoader = sceneView.materialLoader
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
            tube.build(sceneView, tubeMaterial, 12, 20, 0.05f)
        }
        for (node in nodes) {
            node.build(sceneView, nodeMaterial, 24, 24, 0.0625f)
        }

        val buffer = readAsset("materials/emissive_colored.filamat")
        val material = Material.Builder().payload(buffer, buffer.remaining()).build(sceneView.engine)
        val materialInstance = material.createInstance()
        player.build(sceneView, materialInstance, Color.RED, 0.1f, 2.0f)
        player.positionSet(Position(x = 3.0f, y = 3.0f, z = 3.0f))
        player.tubeSet(tubes[0], 1, 1.0f)
    }

    @SuppressLint("DefaultLocale")
    private fun updateUI() {
        textView.text = String.format("%d fps", sceneView.fps)
    }

    private fun Activity.setFullScreen(
        rootView: View,
        fullScreen: Boolean = true,
        hideSystemBars: Boolean = true,
        fitsSystemWindows: Boolean = true
    ) {
        rootView.viewTreeObserver?.addOnWindowFocusChangeListener { hasFocus ->
            if (hasFocus) {
                WindowCompat.setDecorFitsSystemWindows(window, fitsSystemWindows)
                WindowInsetsControllerCompat(window, rootView).apply {
                    if (hideSystemBars) {
                        if (fullScreen) {
                            hide(
                                WindowInsetsCompat.Type.systemBars()
                            )
                        } else {
                            show(
                                WindowInsetsCompat.Type.systemBars()
                            )
                        }
                        systemBarsBehavior =
                            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                    }
                }
            }
        }
    }

    private fun readAsset(assetName: String): ByteBuffer {
        val input = assets.open(assetName)
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
}


