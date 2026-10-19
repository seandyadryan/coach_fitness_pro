package com.deploydulupulangnanti.coachfitness

import org.junit.Assert.*
import org.junit.Test
import kotlin.math.hypot

class MovementTest {
    @Test fun everyCatalogExerciseHasACompleteFiniteIllustration() {
        assertEquals(Catalog.exercises.map { it.id }.toSet(),MovementModel.specifications.keys)
        MovementModel.specifications.keys.forEach { id ->
            (0..120).forEach { frame ->
                val pose=MovementModel.sample(id,frame/120f)
                assertEquals(BodyJoint.entries.toSet(),pose.joints.keys)
                pose.joints.forEach { (joint,point) ->
                    assertTrue("$id/$joint x=${point.x}",point.x.isFinite() && point.x in 20f..380f)
                    assertTrue("$id/$joint y=${point.y}",point.y.isFinite() && point.y in 15f..235f)
                }
            }
        }
    }
    @Test fun loopsJoinContinuouslyAndEachMovementChangesPose() {
        MovementModel.specifications.forEach { (id,spec) ->
            val start=MovementModel.sample(id,0f)
            val end=MovementModel.sample(id,.99999f)
            BodyJoint.entries.forEach { joint -> assertTrue("$id loop jumps at $joint",hypot(start[joint].x-end[joint].x,start[joint].y-end[joint].y)<.1f) }
            val maxChange=(1..15).maxOf { frame -> val pose=MovementModel.sample(id,frame/16f); BodyJoint.entries.sumOf { joint -> hypot(pose[joint].x-start[joint].x,pose[joint].y-start[joint].y).toDouble() } }
            assertTrue("$id is static",maxChange>if(spec.hold) .5 else 5.0)
        }
    }
    @Test fun curlAndJackKeepTheirForearmsAtFullLengthThroughoutTheArc() {
        listOf("curl","jack").forEach { id -> (0..60).forEach { frame ->
            val pose=MovementModel.sample(id,frame/60f)
            val wrist=pose[BodyJoint.LW]; val elbow=pose[BodyJoint.LE]
            assertEquals(if(id=="curl") 33f else 34f,hypot(wrist.x-elbow.x,wrist.y-elbow.y),.02f)
        } }
    }
    @Test fun unsupportedMovementsAreNeverSilentlyReplaced() {
        assertThrows(IllegalArgumentException::class.java) { MovementModel.sample("unknown",0f) }
        assertThrows(IllegalArgumentException::class.java) { MovementModel.sample("squat",Float.NaN) }
    }
    @Test fun staticHoldsKeepSupportPointsOnTheGround() {
        listOf("plank","child").forEach { id ->
            val first=MovementModel.sample(id,0f)
            (0..20).forEach { frame ->
                val pose=MovementModel.sample(id,frame/20f)
                listOf(BodyJoint.LW,BodyJoint.RW,BodyJoint.LA,BodyJoint.RA).forEach { assertEquals("$id support moved",first[it],pose[it]) }
            }
        }
    }
    @Test fun forearmPlankPlacesElbowsBelowShouldersWithoutLengtheningTheUpperArm() {
        val pose=MovementModel.sample("plank",.25f)
        assertTrue(kotlin.math.abs(pose[BodyJoint.RS].x-pose[BodyJoint.RE].x)<5)
        assertTrue(hypot(pose[BodyJoint.RS].x-pose[BodyJoint.RE].x,pose[BodyJoint.RS].y-pose[BodyJoint.RE].y) in 25f..40f)
    }
}
