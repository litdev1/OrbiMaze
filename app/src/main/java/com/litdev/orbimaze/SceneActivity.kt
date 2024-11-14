package com.litdev.orbimaze

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.google.android.filament.Box
import com.google.android.filament.Colors
import com.google.android.filament.EntityManager
import com.google.android.filament.IndexBuffer
import com.google.android.filament.LightManager
import com.google.android.filament.Material
import com.google.android.filament.RenderableManager
import com.google.android.filament.VertexBuffer
import com.google.android.filament.utils.Manipulator
import io.github.sceneview.math.Position
import io.github.sceneview.math.Size
import io.github.sceneview.node.CubeNode
import io.github.sceneview.node.CylinderNode
import io.github.sceneview.node.LightNode
import io.github.sceneview.node.SphereNode
import java.nio.ByteBuffer
import java.nio.FloatBuffer
import java.nio.ShortBuffer

class SceneActivity : AppCompatActivity() {
    lateinit var sceneView: MainSceneView
    lateinit var textView: TextView
    val nodes = mutableListOf<Node>()
    val tubes = mutableListOf<Tube>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //enableEdgeToEdge()
        setContentView(R.layout.activity_scene)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

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
        sceneView.mainLightNode.apply {
            this?.intensity = 100000.0f
        }
        sceneView.indirectLight.apply {
            this?.intensity = 100000.0f
        }
        sceneView.cameraNode.apply {
            position = Position(z = 10.0f)
        }

        val lightEntity1 = EntityManager.get().create()
        LightManager.Builder(LightManager.Type.DIRECTIONAL).apply {
            color(1.0f, 1.0f, 1.0f)
            intensity(100_000.0f)
            direction(1.0f, -1.0f, 0.0f)
            castShadows(true)
            build(sceneView.engine, lightEntity1)
        }
        val lightNode1 = LightNode(sceneView.engine, lightEntity1)
        sceneView.addChildNode(lightNode1)

        val lightEntity2 = EntityManager.get().create()
        LightManager.Builder(LightManager.Type.SPOT).apply {
            color(1.0f, 1.0f, 1.0f)
            intensity(100_000.0f)
            direction(-1.0f, 0.0f, 0.0f)
            position(2.0f, 0.0f, 0.0f)
            falloff(10.0f)
            spotLightCone(0.5f, 1.0f)
            castShadows(true)
            build(sceneView.engine, lightEntity2)
        }
        val lightNode2 = LightNode(sceneView.engine, lightEntity2)
        sceneView.addChildNode(lightNode2)

        val manipulator = Manipulator.Builder()
            .viewport(sceneView.width, sceneView.height)
            .mapMinDistance(-10f)
            .targetPosition(0.0f, 1.0f, 0.0f)
            .orbitHomePosition(0.0f, 0.0f, 10.0f)
            .zoomSpeed(0.03f)
            .orbitSpeed(0.004f, 0.004f)
            .farPlane(100.0f)
            .flightMaxMoveSpeed(1.0f)
            .flightPanSpeed(0.001f, 0.001f)
            .flightSpeedSteps(80)
            .flightMoveDamping(15.0f)
            .flightStartPosition(0.0f,0.0f,10.0f)
            .flightStartOrientation(0.0f, 0.0f)
            .fovDegrees(33.00f)
            .fovDirection(Manipulator.Fov.VERTICAL)
            .mapMinDistance(0.0f)
            .mapExtent(10.0f, 10.0f)
            .build(Manipulator.Mode.ORBIT)
        sceneView.cameraManipulator = manipulator

        val materialLoader = sceneView.materialLoader

        val cylinder = CylinderNode(
            engine = sceneView.engine,
            radius = 0.2f,
            height = 2.0f,
            materialInstance = materialLoader.createColorInstance(color = Color.RED,
                metallic = 0.5f,
                roughness = 0.2f,
                reflectance = 0.4f
            )
        )
        cylinder.position = Position(x = -1.0f, y = 1.0f, z = 0.0f)
        sceneView.addChildNode(cylinder)

        val sphere = SphereNode(
            engine = sceneView.engine,
            radius = 0.2f,
            center = Position(x = 0.0f, y = 0.0f, z = 0.0f),
            materialInstance = materialLoader.createColorInstance(color = Color.GREEN,
                metallic = 0.5f,
                roughness = 0.2f,
                reflectance = 0.4f
            )
        )
        sceneView.addChildNode(sphere)

        val cube = CubeNode(
            engine = sceneView.engine,
            size = Size(1.0f, 1.0f, 1.0f),
            center = Position(x = 1.0f, y = -1.0f, z = 0.0f),
            materialInstance = materialLoader.createColorInstance(color = Color.BLUE,
                metallic = 1.0f,
                roughness = 0.0f,
                reflectance = 1.0f
            )
        )
        sceneView.addChildNode(cube)

        val buffer = readAsset("materials/opaque_colored.filamat")
        val material = Material.Builder().payload(buffer, buffer.remaining()).build(sceneView.engine)
        val tubeMaterial = material.createInstance()
        tubeMaterial.setParameter("color", Colors.RgbType.SRGB, 0.8f, 0.7f, 0.5f)
        tubeMaterial.setParameter("metallic", 1.0f)
        tubeMaterial.setParameter("roughness", 0.1f)
        tubeMaterial.setParameter("reflectance", 0.8f)
        val nodeMaterial = materialLoader.createColorInstance(color = Color.LTGRAY,
            metallic = 1.0f,
            roughness = 0.1f,
            reflectance = 0.8f
        )

        Generate(nodes, tubes).simple()

        val cubicTube: CubicTube = CubicTube(sceneView, tubeMaterial, nodeMaterial, 0.05f, 12, 20)
        for (tube in tubes) {
            cubicTube.buildTube(tube)
        }
        for (node in nodes) {
            cubicTube.buildNode(node)
        }
    }

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
                                WindowInsetsCompat.Type.statusBars() or
                                        WindowInsetsCompat.Type.navigationBars()
                            )
                        } else {
                            show(
                                WindowInsetsCompat.Type.statusBars() or
                                        WindowInsetsCompat.Type.navigationBars()
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
}