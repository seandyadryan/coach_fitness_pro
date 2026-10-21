package com.deploydulupulangnanti.coachfitness

import org.junit.Assert.*
import org.junit.Test

class PhotoCharacterTest {
    @Test fun everyPoseUsesKnownPhotographicPartsAndFitsTheStage() {
        val used=mutableSetOf<String>()
        for(spec in MovementModel.specifications.values) for(frame in 0..120) {
            val sprites=PhotoCharacter.sprites(spec,MovementModel.sample(spec.id,frame/120f))
            assertEquals("Head, neck and torso must share one connected texture",1,sprites.count { it.part.startsWith("body_") })
            assertFalse("Separate head textures can expose the cut neck edge",sprites.any { it.part.startsWith("head_") || it.part.startsWith("torso_") })
            sprites.forEach { sprite ->
                used.add(sprite.part); assertTrue(sprite.part in PhotoCharacter.parts)
                assertTrue(listOf(sprite.x,sprite.y,sprite.width,sprite.height,sprite.angle,sprite.bend).all { it.isFinite() })
                assertTrue(sprite.width>0 && sprite.height>0)
                val rows=if(sprite.part.startsWith("body_")) (0..64).map { it/64f } else listOf(0f,1f)
                for(u in listOf(0f,.5f,1f)) for(v in rows) {
                    val point=sprite.point(u,v)
                    assertTrue("${spec.id} ${sprite.part} clipped: $point",point.x in 0f..400f && point.y in 0f..260f)
                }
            }
        }
        assertEquals(PhotoCharacter.parts,used)
    }
}
