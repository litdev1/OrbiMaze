package com.litdev.orbimaze

import io.github.sceneview.collision.Vector3
import dev.romainguy.kotlin.math.pow
import kotlin.random.Random

class Generate(val nodes: MutableList<Node>,
               val tubes: MutableList<Tube>) {

    val rand: Random = Random(System.currentTimeMillis())

    fun simple() {
        nodes.clear()
        tubes.clear()

        nodes.add(Node(Vector3(0.0f, 1.0f, 0.0f)))
        nodes.add(Node(Vector3(1.0f, 2.0f, 1.0f)))
        nodes.add(Node(Vector3(2.0f, 1.0f, 1.0f)))
        tubes.add(Tube(nodes[0], nodes[1]))
        tubes.add(Tube(nodes[1], nodes[2]))
        tubes.add(Tube(nodes[2], nodes[0]))
    }

    fun random(count: Int) {
        nodes.clear()
        tubes.clear()

        val length = pow(count.toFloat(), 1 / 3.0f)

        for (i in 0 until count) {
            nodes.add(
                Node(
                    Vector3(
                        length * rand.nextFloat(),
                        length * rand.nextFloat(),
                        length * rand.nextFloat()
                    )
                )
            )
        }
        var i = 0
        while (tubes.size < count && i < 5*count) {
            i++
            val node1 = nodes[rand.nextInt(count)]
            val node2 = nodes[rand.nextInt(count)]
            if (node1 == node2) continue
            val dist = Vector3.subtract(node1.pos, node2.pos).length()
            if (dist < 0.5 || dist > length/3.0f) continue
            var duplicate = false
            for (tube in node1.tubes) {
                if (tube.node1 == node2 || tube.node2 == node2) {
                    duplicate = true
                    continue
                }
            }
            if (!duplicate) tubes.add(Tube(node1, node2))
        }
        val nodesToRemove = mutableListOf<Node>()
        for (node in nodes) {
            if (node.tubes.isEmpty()) {
                nodesToRemove.add(node)
            }
        }
        for (node in nodesToRemove) {
            nodes.remove(node)
        }
    }

    fun cube(nx: Int, ny: Int, nz: Int, removeFraction: Float) {
        nodes.clear()
        tubes.clear()

        for (k in 0 until nz) {
            for (j in 0 until ny) {
                for (i in 0 until nx) {
                    nodes.add(Node(Vector3(i.toFloat(), j.toFloat(), k.toFloat())))
                }
            }
        }
        for (i in 0 until nx) {
            for (j in 0 until ny) {
                for (k in 0 until nz) {
                    val ijk = k * ny * nz + j * nz + i
                    if (rand.nextFloat() < removeFraction) continue
                    val node = nodes[ijk]
                    if (i < nx - 1) {
                        val nodeX = nodes[ijk + 1]
                        tubes.add(Tube(node, nodeX))
                    }
                    if (j < ny - 1) {
                        val nodeY = nodes[ijk + ny]
                        tubes.add(Tube(node, nodeY))
                    }
                    if (k < nz - 1) {
                        val nodeZ = nodes[ijk + ny * nz]
                        tubes.add(Tube(node, nodeZ))
                    }
                }
            }
        }
        val nodesToRemove = mutableListOf<Node>()
        val tubesToRemove = mutableListOf<Tube>()
        var first = true
        while (first || nodesToRemove.size > 0 || tubesToRemove.size > 0) {
            first = false
            nodesToRemove.clear()
            tubesToRemove.clear()
            for (node in nodes) {
                if (node.tubes.isEmpty()) {
                    nodesToRemove.add(node)
                } else if (node.tubes.size == 1) {
                    nodesToRemove.add(node)
                    val tube = node.tubes[0]
                    tubesToRemove.add(tube)
                    tube.node1.tubes.remove(tube)
                    tube.node2.tubes.remove(tube)
                }
            }
            for (node in nodesToRemove) {
                nodes.remove(node)
            }
            for (tube in tubesToRemove) {
                tubes.remove(tube)
            }
        }
    }
}