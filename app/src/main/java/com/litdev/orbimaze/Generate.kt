package com.litdev.orbimaze

import io.github.sceneview.collision.Vector3

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

    fun random() {
        nodes.clear()
        tubes.clear()
    }
}