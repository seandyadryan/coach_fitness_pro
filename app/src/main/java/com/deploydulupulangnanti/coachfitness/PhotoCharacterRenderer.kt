package com.deploydulupulangnanti.coachfitness

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.RectF

/** Shared decoded textures; a frame never decodes or allocates new bitmap data. */
object PhotoCharacterRenderer {
    private var textures: Map<String, Bitmap>? = null
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    private val farArm = ColorMatrixColorFilter(ColorMatrix(floatArrayOf(.8f,0f,0f,0f,0f,0f,.8f,0f,0f,0f,0f,0f,.8f,0f,0f,0f,0f,0f,1f,0f)))
    private val farLeg = ColorMatrixColorFilter(ColorMatrix(floatArrayOf(.78f,0f,0f,0f,0f,0f,.78f,0f,0f,0f,0f,0f,.78f,0f,0f,0f,0f,0f,1f,0f)))
    private val vertices=FloatArray(3*65*2)

    private fun textures(resources: Resources): Map<String,Bitmap> = textures ?: mapOf(
        "body_front" to R.drawable.athlete_body_front, "body_side" to R.drawable.athlete_body_side,
        "upper_arm" to R.drawable.athlete_upper_arm, "forearm" to R.drawable.athlete_forearm,
        "thigh" to R.drawable.athlete_thigh, "calf" to R.drawable.athlete_calf,
        "hand" to R.drawable.athlete_hand, "grip" to R.drawable.athlete_grip,
        "shoe_front" to R.drawable.athlete_shoe_front, "shoe_side" to R.drawable.athlete_shoe_side
    ).mapValues { (_,id) -> requireNotNull(BitmapFactory.decodeResource(resources,id,BitmapFactory.Options().apply { inScaled=false })) }.also { textures=it }

    fun draw(canvas: Canvas, resources: Resources, spec: MovementSpec, pose: MovementPose) {
        val images=textures(resources)
        PhotoCharacter.sprites(spec,pose).forEach { sprite ->
            val bitmap=images.getValue(sprite.part)
            canvas.save(); canvas.translate(sprite.x,sprite.y); canvas.rotate(sprite.angle)
            val left=-sprite.width*sprite.pivotX; val top=-sprite.height*sprite.pivotY
            paint.colorFilter=when { sprite.shade < .79f -> farLeg; sprite.shade < 1f -> farArm; else -> null }
            if (sprite.part.startsWith("body_")) {
                // A single mesh includes the head and neck: shared edges cannot separate.
                var index=0
                for(row in 0..64) for(column in 0..2) {
                    val point=sprite.localPoint(column/2f,row/64f)
                    vertices[index++]=point.x
                    vertices[index++]=point.y
                }
                canvas.drawBitmapMesh(bitmap,2,64,vertices,0,null,0,paint)
            } else canvas.drawBitmap(bitmap,null,RectF(left,top,left+sprite.width,top+sprite.height),paint)
            canvas.restore()
        }
        paint.colorFilter=null
    }
}
