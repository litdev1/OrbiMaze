package com.litdev.orbimaze

import com.google.android.filament.Colors
import com.google.android.filament.EntityManager
import com.google.android.filament.LightManager
import com.google.android.filament.MaterialInstance
import io.github.sceneview.math.Position
import io.github.sceneview.node.LightNode
import io.github.sceneview.node.SphereNode
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red

class Orb {
    lateinit var sphere: SphereNode
    lateinit var lightNode: LightNode
    lateinit var tube: Tube
    var speed: Float = 0.0f
    var r: Float = 0.0f
    var dir: Int = 1

    fun build(sceneView: MainSceneView, material: MaterialInstance, color: Int, radius: Float, falloff: Float) {
        val R = color.red / 255.0f
        val G = color.green / 255.0f
        val B = color.blue / 255.0f
        material.setParameter("color", Colors.RgbType.SRGB, R, G, B)
        material.setParameter("metallic", 1.0f)
        material.setParameter("roughness", 0.0f)
        material.setParameter("reflectance", 1.0f)
        material.setParameter("emissive", Colors.RgbType.SRGB, 1.0f, 0.0f, 0.0f)

        sphere = SphereNode(
            engine = sceneView.engine,
            radius = radius,
            materialInstance = material
        )
        sceneView.addChildNode(sphere)

        val lightEntity = EntityManager.get().create()
        LightManager.Builder(LightManager.Type.POINT).apply {
            color(R, G, B)
            intensity(100_000_000.0f)
            falloff(falloff)
            castShadows(true)
            build(sceneView.engine, lightEntity)
        }
        lightNode = LightNode(sceneView.engine, lightEntity)
        sceneView.addChildNode(lightNode)
    }

    fun tubeSet(tube: Tube, dir: Int, speed: Float) {
        this.tube = tube
        this.dir = dir
        this.speed = speed
        r = if (dir == 1) 0.0f else 1.0f
        positionSet(tube.pointP(r))
    }

    fun positionSet(position: Position) {
        sphere.position = position
        lightNode.position = position
    }

    fun positionGet(): Position {
        return sphere.position
    }

    fun newTube() {
        val node = if (dir > 0) tube.node2 else tube.node1
        if (node.tubes.size == 1) {
            dir = -dir
        }
        else {
            var nextTube = tube
            while (nextTube == tube) {
                nextTube = node.tubes.random()
            }
            tube = nextTube
            dir = if (tube.node1 == node) 1 else -1
        }
        r = if (dir > 0) 0.0f else 1.0f
    }
}
