package com.litdev.orbimaze

import android.graphics.Color
import com.google.android.filament.Box
import com.google.android.filament.Colors
import com.google.android.filament.EntityManager
import com.google.android.filament.IndexBuffer
import com.google.android.filament.Material
import com.google.android.filament.MaterialInstance
import com.google.android.filament.RenderableManager
import com.google.android.filament.VertexBuffer
import dev.romainguy.kotlin.math.Float3
import dev.romainguy.kotlin.math.Mat4
import dev.romainguy.kotlin.math.cross
import dev.romainguy.kotlin.math.dot
import dev.romainguy.kotlin.math.normalize
import dev.romainguy.kotlin.math.pow
import io.github.sceneview.collision.Vector3
import io.github.sceneview.collision.Quaternion
import io.github.sceneview.math.Position
import java.nio.FloatBuffer
import dev.romainguy.kotlin.math.rotation
import io.github.sceneview.math.Direction
import io.github.sceneview.math.Transform
import io.github.sceneview.math.centerPosition
import io.github.sceneview.math.halfExtentSize
import io.github.sceneview.math.toFloat3
import io.github.sceneview.node.SphereNode
import java.nio.ByteBuffer
import java.nio.IntBuffer
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sign

class CubicTube(sceneView: MainSceneView, tubeMaterial : MaterialInstance, nodeMaterial : MaterialInstance, radius: Float, sides: Int, segments: Int) {
    val sceneView = sceneView
    val tubeMaterial = tubeMaterial
    val nodeMaterial = nodeMaterial
    val radius = radius
    val sides = sides
    val segments = segments

    val vertices: FloatArray = FloatArray((segments + 1) * sides * 7)
    val indices: IntArray = IntArray(segments * sides * 2 * 3)
    val boundingBox = Box()

    fun buildTube(tube: Tube) {
        val pos1 = tube.node1.pos
        val pos2 = tube.node2.pos
        var dirX = sign(pos2.x - pos1.x)
        var dirY = 0.0f
        var dirZ = sign(pos2.z - pos1.z)
        if (abs(pos2.x - pos1.x) >= abs(pos2.z - pos1.z)) {
            dirZ = 0.0f;
        } else {
            dirX = 0.0f;
        }
        val dir1 = Vector3(dirX, dirY, dirZ).normalized()
        val dir2 = dir1
        val A = pos1
        val B = dir1
        val C = Vector3(
            -2 * dir1.x - dir2.x + 3 * (pos2.x - pos1.x),
            -2 * dir1.y - dir2.y + 3 * (pos2.y - pos1.y),
            -2 * dir1.z - dir2.z + 3 * (pos2.z - pos1.z)
        )
        val D = Vector3(
            dir1.x + dir2.x - 2 * (pos2.x - pos1.x),
            dir1.y + dir2.y - 2 * (pos2.y - pos1.y),
            dir1.z + dir2.z - 2 * (pos2.z - pos1.z)
        )

        val up = Vector3(0.0f, 1.0f, 0.0f)
        val min = Vector3(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE)
        val max = Vector3(-Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE)

        for (i in 0..segments) {
            var r = i.toFloat() / segments //parametric distance along the curve
            val radScale = radius * (0.25f + 0.75f * pow(2 * abs(r - 0.5f), 2.0f))
            val pos = Vector3(
                A.x + B.x * r + C.x * r * r + D.x * r * r * r,
                A.y + B.y * r + C.y * r * r + D.y * r * r * r,
                A.z + B.z * r + C.z * r * r + D.z * r * r * r
            ) //point
            val dir = Vector3(
                B.x + 2 * C.x * r + 3 * D.x * r * r,
                B.y + 2 * C.y * r + 3 * D.y * r * r,
                B.z + 2 * C.z * r + 3 * D.z * r * r
            ).normalized() //direction vector
            val rad = Vector3.cross(dir, up) //vector in radial direction
            for (j in 0..sides - 1) {
                val angle = j.toFloat() / sides * 360
                val rot = Quaternion.axisAngle(dir, angle) //rotation about direction
                val radDir = Quaternion.rotateVector(rot, rad)
                    .normalized() //vector in radial direction after rotation
                val x = pos.x + radScale * radDir.x
                val y = pos.y + radScale * radDir.y
                val z = pos.z + radScale * radDir.z
                vertices[i * sides * 7 + j * 7 + 0] = x
                vertices[i * sides * 7 + j * 7 + 1] = y
                vertices[i * sides * 7 + j * 7 + 2] = z
                min.x = min(min.x, x)
                min.y = min(min.y, y)
                min.z = min(min.z, z)
                max.x = max(max.x, x)
                max.y = max(max.y, y)
                max.z = max(max.z, z)
                val quaternion = normalToTangent(radDir.toFloat3())
                vertices[i * sides * 7 + j * 7 + 3] = quaternion.x
                vertices[i * sides * 7 + j * 7 + 4] = quaternion.y
                vertices[i * sides * 7 + j * 7 + 5] = quaternion.z
                vertices[i * sides * 7 + j * 7 + 6] = quaternion.w

                if (i < segments) {
                    indices[i * sides * 2 * 3 + j * 2 * 3 + 0] = i * sides + j
                    indices[i * sides * 2 * 3 + j * 2 * 3 + 1] = i * sides + (j + 1) % sides
                    indices[i * sides * 2 * 3 + j * 2 * 3 + 2] = (i + 1) * sides + j
                    indices[i * sides * 2 * 3 + j * 2 * 3 + 3] = i * sides + (j + 1) % sides
                    indices[i * sides * 2 * 3 + j * 2 * 3 + 4] = (i + 1) * sides + (j + 1) % sides
                    indices[i * sides * 2 * 3 + j * 2 * 3 + 5] = (i + 1) * sides + j
                }
            }
        }
        boundingBox.centerPosition =
            Float3((min.x + max.x) / 2.0f, (min.y + max.y) / 2.0f, (min.z + max.z) / 2.0f)
        boundingBox.halfExtentSize =
            Float3((max.x - min.x) / 2.0f, (max.y - min.y) / 2.0f, (max.z - min.z) / 2.0f)

        val vertexBuffer = VertexBuffer.Builder()
            .bufferCount(1)
            .vertexCount(vertices.size)
            .attribute(
                VertexBuffer.VertexAttribute.POSITION,
                0,
                VertexBuffer.AttributeType.FLOAT3,
                0,
                28
            )
            .attribute(
                VertexBuffer.VertexAttribute.TANGENTS,
                0,
                VertexBuffer.AttributeType.FLOAT4,
                12,
                28
            )
            .build(sceneView.engine)
        val indexBuffer = IndexBuffer.Builder()
            .indexCount(indices.size)
            .bufferType(IndexBuffer.Builder.IndexType.UINT)
            .build(sceneView.engine)
        vertexBuffer.setBufferAt(sceneView.engine, 0, FloatBuffer.wrap(vertices))
        indexBuffer.setBuffer(sceneView.engine, IntBuffer.wrap(indices))

        val renderable = EntityManager.get().create()
        RenderableManager.Builder(1)
            .boundingBox(boundingBox)
            .geometry(0, RenderableManager.PrimitiveType.TRIANGLES, vertexBuffer, indexBuffer)
            .material(0, tubeMaterial)
            .build(sceneView.engine, renderable)

        sceneView.scene.addEntity(renderable)
    }

    fun buildNode(node: Node) {
        val node = SphereNode(
            engine = sceneView.engine,
            radius = 1.25f*radius,
            center = Position(x = node.pos.x, y = node.pos.y, z = node.pos.z),
            materialInstance = nodeMaterial,
            stacks = 2*sides,
            slices = 2*sides
        )
        sceneView.addChildNode(node)
    }

    fun normalToTangent(normal: Float3): dev.romainguy.kotlin.math.Quaternion {
        var tangent: Float3
        val bitangent: Float3

        // Calculate basis vectors (+x = tangent, +y = bitangent, +z = normal).
        tangent = cross(Direction(y = 1.0f), normal)

        // Uses almostEqualRelativeAndAbs for equality checks that account for float inaccuracy.
        if (dot(tangent, tangent) == 0.0f) {
            bitangent = normalize(cross(normal, Direction(x = 1.0f)))
            tangent = normalize(cross(bitangent, normal))
        } else {
            tangent = normalize(tangent)
            bitangent = normalize(cross(normal, tangent))
        }
        // Rotation of a 4x4 Transformation Matrix is represented by the top-left 3x3 elements.
        return Transform(right = tangent, up = bitangent, forward = normal).toQuaternion()
    }
}