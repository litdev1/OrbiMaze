package com.litdev.orbimaze

import io.github.sceneview.collision.Vector3

class Node(val pos: Vector3) {
    val tubes = mutableListOf<Tube>()

    init {
        tubes.clear()
    }
}