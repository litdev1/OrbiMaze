package com.litdev.orbimaze

import com.google.android.filament.Box
import com.google.android.filament.EntityManager
import com.google.android.filament.IndexBuffer
import com.google.android.filament.MaterialInstance
import com.google.android.filament.RenderableManager
import com.google.android.filament.VertexBuffer
import dev.romainguy.kotlin.math.Float3
import dev.romainguy.kotlin.math.cross
import dev.romainguy.kotlin.math.dot
import dev.romainguy.kotlin.math.normalize
import dev.romainguy.kotlin.math.pow
import io.github.sceneview.collision.Quaternion
import io.github.sceneview.collision.Vector3
import io.github.sceneview.math.Direction
import io.github.sceneview.math.Position
import io.github.sceneview.math.Transform
import io.github.sceneview.math.centerPosition
import io.github.sceneview.math.halfExtentSize
import io.github.sceneview.math.toFloat3
import java.nio.FloatBuffer
import java.nio.IntBuffer
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

var nextTubeId: Int = 0
class Tube(val joint1: Joint,
           val joint2: Joint) {
    var id: Int = 0
    var length: Float = 0.0f
    var startDirection: Vector3 = Vector3.zero()
    lateinit var coefA: Vector3
    lateinit var coefB: Vector3
    lateinit var coefC: Vector3
    lateinit var coefD: Vector3
    lateinit var renderNode: io.github.sceneview.node.RenderableNode

    init {
        id = nextTubeId++
        joint1.tubes.add(this)
        joint2.tubes.add(this)
    }

    fun build(sceneView: MainSceneView,
                  material: MaterialInstance,
                  segments: Int,
                  sides: Int,
                  radius: Float) {
        val vertices: FloatArray = FloatArray((segments + 1) * sides * 7)
        val indices: IntArray = IntArray(segments * sides * 2 * 3)
        val boundingBox = Box()

        val pos1 = joint1.pos
        val pos2 = joint2.pos
        if (startDirection.length() == 0.0f) {
            startDirection = Vector3.subtract(joint2.pos, joint1.pos)
            if (abs(startDirection.x) < abs(startDirection.y) ||
                abs(startDirection.x) < abs(startDirection.z)) startDirection.x = 0.0f
            if (abs(startDirection.y) < abs(startDirection.x) ||
                abs(startDirection.y) < abs(startDirection.z)) startDirection.y = 0.0f
            if (abs(startDirection.z) < abs(startDirection.x) ||
                abs(startDirection.z) < abs(startDirection.y)) startDirection.z = 0.0f
        }
        startDirection = startDirection.normalized()
        var tangent = Vector3.cross(startDirection, Vector3(0.0f, 1.0f, 0.0f))
        if (Vector3.dot(tangent, tangent) == 0.0f) {
            tangent = Vector3.cross(startDirection, Vector3(1.0f, 0.0f, 0.0f))
        }
        val dir1 = startDirection
        val dir2 = dir1
        coefA = pos1
        coefB = dir1
        coefC = Vector3(
            -2 * dir1.x - dir2.x + 3 * (pos2.x - pos1.x),
            -2 * dir1.y - dir2.y + 3 * (pos2.y - pos1.y),
            -2 * dir1.z - dir2.z + 3 * (pos2.z - pos1.z)
        )
        coefD = Vector3(
            dir1.x + dir2.x - 2 * (pos2.x - pos1.x),
            dir1.y + dir2.y - 2 * (pos2.y - pos1.y),
            dir1.z + dir2.z - 2 * (pos2.z - pos1.z)
        )
        length = Vector3.subtract(pos1, pos2).length() //Approximate length of tube, can't be bothered with integral

        //For bounding box
        val min = Vector3(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE)
        val max = Vector3(-Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE)

        for (i in 0..segments) {
            var r = i.toFloat() / segments //parametric distance along the curve
            val radScale = radius * (0.25f + 0.75f * pow(2 * abs(r - 0.5f), 2.0f))
            val pos = pointV(r) //position vector
            val dir = direction(r) //direction vector
            var rad = Vector3.cross(dir, tangent).normalized() //vector in radial direction
            if (Vector3.dot(rad, rad) == 0.0f) {
                TODO("Should never happen")
            }
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
            .material(0, material)
            .build(sceneView.engine, renderable)
        renderNode = io.github.sceneview.node.RenderableNode(sceneView.engine, renderable)
//        renderNode.collisionShape = boundingBox.toVector3Box()
        sceneView.addChildNode(renderNode)
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

    fun pointV(r: Float) : Vector3 {
        return Vector3(
            coefA.x + coefB.x * r + coefC.x * r * r + coefD.x * r * r * r,
            coefA.y + coefB.y * r + coefC.y * r * r + coefD.y * r * r * r,
            coefA.z + coefB.z * r + coefC.z * r * r + coefD.z * r * r * r
        )
    }

    fun direction(r: Float) : Vector3 {
        return Vector3(
            coefB.x + 2 * coefC.x * r + 3 * coefD.x * r * r,
            coefB.y + 2 * coefC.y * r + 3 * coefD.y * r * r,
            coefB.z + 2 * coefC.z * r + 3 * coefD.z * r * r
        ).normalized()
    }

    fun pointP(r: Float) : Position {
        return Position(
            coefA.x + coefB.x * r + coefC.x * r * r + coefD.x * r * r * r,
            coefA.y + coefB.y * r + coefC.y * r * r + coefD.y * r * r * r,
            coefA.z + coefB.z * r + coefC.z * r * r + coefD.z * r * r * r
        )
    }

    fun startDirectionSet(direction: Vector3)
    {
        startDirection = direction
    }
}