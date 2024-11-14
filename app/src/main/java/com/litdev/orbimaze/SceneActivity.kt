package com.litdev.orbimaze

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
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
import dev.romainguy.kotlin.math.Float3
import io.github.sceneview.collision.Vector3
import io.github.sceneview.node.CubeNode
import io.github.sceneview.node.CylinderNode
import io.github.sceneview.node.LightNode
import io.github.sceneview.node.SphereNode
import java.nio.ByteBuffer
import java.nio.FloatBuffer
import java.nio.ShortBuffer
import io.github.sceneview.math.Position
import io.github.sceneview.math.Size
import java.nio.IntBuffer

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

        //makeCube()

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
        textView.text = sceneView.fps.toString() + " fps"
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

    private fun makeCube() {
        val vertexBuffer = VertexBuffer.Builder()
            .bufferCount(1)
            .vertexCount(8)
            .attribute(VertexBuffer.VertexAttribute.POSITION, 0, VertexBuffer.AttributeType.FLOAT3, 0, 12)
            .build(sceneView.engine)

        val indexBuffer = IndexBuffer.Builder()
            .indexCount(36)
            .bufferType(IndexBuffer.Builder.IndexType.USHORT)
            .build(sceneView.engine)

        val vertices = floatArrayOf(
            // positions for the 8 vertices of the cube
            -1.0f, -1.0f, -1.0f,
            1.0f, -1.0f, -1.0f,
            1.0f,  1.0f, -1.0f,
            -1.0f,  1.0f, -1.0f,
            -1.0f, -1.0f,  1.0f,
            1.0f, -1.0f,  1.0f,
            1.0f,  1.0f,  1.0f,
            -1.0f,  1.0f,  1.0f
        )

        val indices = shortArrayOf(
            // indices for the 12 triangles of the cube
            0, 1, 2, 0, 2, 3,
            4, 5, 6, 4, 6, 7,
            0, 1, 5, 0, 5, 4,
            2, 3, 7, 2, 7, 6,
            0, 3, 7, 0, 7, 4,
            1, 2, 6, 1, 6, 5
        )

        vertexBuffer.setBufferAt(sceneView.engine, 0, FloatBuffer.wrap(vertices))
        indexBuffer.setBuffer(sceneView.engine, ShortBuffer.wrap(indices))

        val buffer = readAsset("materials/opaque_colored.filamat")
        val material = Material.Builder().payload(buffer, buffer.remaining()).build(sceneView.engine)
        val materialInstance = material.createInstance()
        materialInstance.setParameter("color", Colors.RgbType.SRGB, 1.0f, 0.85f, 0.57f)
        materialInstance.setParameter("metallic", 0.0f)
        materialInstance.setParameter("roughness", 0.3f)
        materialInstance.setParameter("reflectance", 0.3f)

        val renderable = EntityManager.get().create()
        RenderableManager.Builder(1)
            .boundingBox(Box(0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f))
            .geometry(0, RenderableManager.PrimitiveType.TRIANGLES, vertexBuffer, indexBuffer)
            .material(0, materialInstance)
            .build(sceneView.engine, renderable)

        sceneView.scene.addEntity(renderable)
    }

    private fun readAsset(assetName: String): ByteBuffer {
        val input = assets.open(assetName)
        val bytes = ByteArray(input.available())
        input.read(bytes)
        return ByteBuffer.wrap(bytes)
    }
}