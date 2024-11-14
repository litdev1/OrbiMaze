package com.litdev.orbimaze

import io.github.sceneview.collision.Vector3
import dev.romainguy.kotlin.math.pow
import kotlin.random.Random

class Generate(val nodes: MutableList<Node>,
               val tubes: MutableList<Tube>) {

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
        val rand: Random = Random(System.currentTimeMillis())

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
}