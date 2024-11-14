package com.litdev.orbimaze

class Tube(node1: Node, node2: Node) {
    val node1 = node1
    val node2 = node2
    init {
        node1.tubes.add(this)
        node2.tubes.add(this)
    }
}