package com.litdev.orbimaze

import com.google.android.filament.MaterialInstance
import io.github.sceneview.collision.Vector3
import io.github.sceneview.math.Position
import io.github.sceneview.node.SphereNode

var nextJointId: Int = 0
class Joint(val pos: Vector3) {
    var id: Int = 0
    val tubes = mutableListOf<Tube>()
    lateinit var renderNode: SphereNode

    init {
        id = nextJointId++
        tubes.clear()
    }

    fun build(sceneView: MainSceneView,
              material: MaterialInstance,
              stacks: Int,
              slices: Int,
              radius: Float) {
        renderNode = SphereNode(
            engine = sceneView.engine,
            radius = radius,
            center = Position(x = pos.x, y = pos.y, z = pos.z),
            materialInstance = material,
            stacks = stacks,
            slices = slices
        )
        renderNode.name = id.toString()
        sceneView.addChildNode(renderNode)
    }
}