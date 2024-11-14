package com.litdev.orbimaze

import io.github.sceneview.collision.Vector3

class Node(pos: Vector3) {
    val pos = pos
    val tubes = mutableListOf<Tube>()
    init {
        tubes.clear()
    }
}