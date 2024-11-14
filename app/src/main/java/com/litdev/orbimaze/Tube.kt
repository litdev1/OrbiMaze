package com.litdev.orbimaze

class Tube(val node1: Node,
           val node2: Node) {
    init {
        node1.tubes.add(this)
        node2.tubes.add(this)
    }
}