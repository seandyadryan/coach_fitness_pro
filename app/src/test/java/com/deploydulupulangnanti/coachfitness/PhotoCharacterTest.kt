package com.deploydulupulangnanti.coachfitness

import org.junit.Assert.*
import org.junit.Test
import kotlin.math.cos
import kotlin.math.sin

class PhotoCharacterTest {
    @Test fun everyPoseUsesKnownPhotographicPartsAndFitsTheStage() {
        val used=mutableSetOf<String>()
        for(spec in MovementModel.specifications.values) for(frame in 0..120) {
            val sprites=PhotoCharacter.sprites(spec,MovementModel.sample(spec.id,frame/120f))
            assertTrue(sprites.any { it.part.startsWith("head_") })
            assertTrue(sprites.any { it.part.startsWith("torso_") })
            sprites.forEach { sprite ->
                used.add(sprite.part); assertTrue(sprite.part in PhotoCharacter.parts)
                assertTrue(listOf(sprite.x,sprite.y,sprite.width,sprite.height,sprite.angle,sprite.bend).all { it.isFinite() })
                assertTrue(sprite.width>0 && sprite.height>0)
                val angle=Math.toRadians(sprite.angle.toDouble())
                for(u in listOf(0f,1f)) for(v in listOf(0f,1f)) {
                    val x=(u-sprite.pivotX)*sprite.width
                    val y=(v-sprite.pivotY)*sprite.height
                    val stageX=sprite.x+x*cos(angle)-y*sin(angle)
                    val stageY=sprite.y+x*sin(angle)+y*cos(angle)
                    assertTrue("${spec.id} ${sprite.part} clipped: $stageX,$stageY",stageX in 0.0..400.0 && stageY in 0.0..260.0)
                }
            }
        }
        assertEquals(PhotoCharacter.parts,used)
    }
}
