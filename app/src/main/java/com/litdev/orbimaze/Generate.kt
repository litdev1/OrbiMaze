package com.litdev.orbimaze

import io.github.sceneview.collision.Vector3
import dev.romainguy.kotlin.math.pow
import kotlin.random.Random

class Generate(val joints: MutableList<Joint>,
               val tubes: MutableList<Tube>) {

    val rand: Random = Random(System.currentTimeMillis())

    fun reset() {
        nextJointId = 0
        nextTubeId = 0
        joints.clear()
        tubes.clear()
    }

    fun simple() {
        reset()

        joints.add(Joint(Vector3(0.0f, 1.0f, 0.0f)))
        joints.add(Joint(Vector3(1.0f, 2.0f, 1.0f)))
        joints.add(Joint(Vector3(2.0f, 1.0f, 1.0f)))
        tubes.add(Tube(joints[0], joints[1]))
        tubes.add(Tube(joints[1], joints[2]))
        tubes.add(Tube(joints[2], joints[0]))

        tidy()
    }

    fun random(count: Int, minLen: Float, maxLen: Float, tubeNumber: Float) {
        reset()
        val scale = 1.0f;

        val length = scale*pow(count.toFloat(), 1 / 3.0f)

        for (i in 0 until count) {
            joints.add(
                Joint(
                    Vector3(
                        length * rand.nextFloat(),
                        length * rand.nextFloat(),
                        length * rand.nextFloat()
                    )
                )
            )
        }
        var i = 0
        while (tubes.size < tubeNumber*count && i < 100*count) {
            i++
            val joint1 = joints[rand.nextInt(count)]
            val joint2 = joints[rand.nextInt(count)]
            if (joint1 == joint2) continue
            val dist = Vector3.subtract(joint1.pos, joint2.pos).length()
            if (dist < scale*minLen || dist > scale*maxLen) continue
            var duplicate = false
            for (tube in joint1.tubes) {
                if (tube.joint1 == joint2 || tube.joint2 == joint2) {
                    duplicate = true
                    break
                }
            }
            if (!duplicate) tubes.add(Tube(joint1, joint2))
        }

        tidy()
    }

    fun cube(nx: Int, ny: Int, nz: Int, removeFraction: Float, moveDistance: Float) {
        reset()

        for (k in 0 until nz) {
            for (j in 0 until ny) {
                for (i in 0 until nx) {
                    joints.add(Joint(Vector3(i.toFloat(), j.toFloat(), k.toFloat())))
                }
            }
        }
        moveRandom(moveDistance)
        for (i in 0 until nx) {
            for (j in 0 until ny) {
                for (k in 0 until nz) {
                    val ijk = k * ny * nz + j * nz + i
                    if (rand.nextFloat() < removeFraction) continue
                    val joint = joints[ijk]
                    if (i < nx - 1) {
                        val jointX = joints[ijk + 1]
                        val tube = Tube(joint, jointX)
                        tube.startDirectionSet(Vector3(1.0f, 0.0f, 0.0f))
                        tubes.add(tube)
                    }
                    if (j < ny - 1) {
                        val jointY = joints[ijk + ny]
                        val tube = Tube(joint, jointY)
                        tube.startDirectionSet(Vector3(0.0f, 1.0f, 0.0f))
                        tubes.add(tube)

                    }
                    if (k < nz - 1) {
                        val jointZ = joints[ijk + ny * nz]
                        val tube = Tube(joint, jointZ)
                        tube.startDirectionSet(Vector3(0.0f, 0.0f, 1.0f))
                        tubes.add(tube)
                    }
                }
            }
        }

        tidy()
    }

    fun tidy()
    {
        val jointsToRemove = mutableListOf<Joint>()
        val tubesToRemove = mutableListOf<Tube>()
        var first = true
        while (first || jointsToRemove.isNotEmpty() || tubesToRemove.isNotEmpty()) {
            first = false
            jointsToRemove.clear()
            tubesToRemove.clear()
            for (joint in joints) {
                if (joint.tubes.isEmpty()) {
                    jointsToRemove.add(joint)
                } else if (joint.tubes.size == 1) {
                    jointsToRemove.add(joint)
                    val tube = joint.tubes[0]
                    tubesToRemove.add(tube)
                    tube.joint1.tubes.remove(tube)
                    tube.joint2.tubes.remove(tube)
                }
            }
            for (joint in jointsToRemove) {
                joints.remove(joint)
            }
            for (tube in tubesToRemove) {
                tubes.remove(tube)
            }
        }
    }

    fun moveRandom(dist: Float) {
        for (joint in joints) {
            joint.pos.x += dist * (rand.nextFloat() - 0.5f)
            joint.pos.y += dist * (rand.nextFloat() - 0.5f)
            joint.pos.z += dist * (rand.nextFloat() - 0.5f)
        }
    }
}