package com.deploydulupulangnanti.coachfitness

import kotlin.math.atan2
import kotlin.math.hypot

/** Texture placements shared by the native renderer and exported preview frames. */
data class CharacterSprite(
    val part: String, val x: Float, val y: Float, val width: Float, val height: Float,
    val angle: Float = 0f, val pivotX: Float = .5f, val pivotY: Float = 0f,
    val shade: Float = 1f, val bend: Float = 0f
)

object PhotoCharacter {
    val parts = setOf("head_front", "head_side", "torso_front", "torso_side", "upper_arm", "forearm", "thigh", "calf", "hand", "grip", "shoe_front", "shoe_side")

    fun sprites(spec: MovementSpec, pose: MovementPose): List<CharacterSprite> = buildList {
        val side = spec.viewLabel == "SIDE VIEW"
        fun angle(a: MovementPoint, b: MovementPoint) = Math.toDegrees(atan2((b.y-a.y).toDouble(), (b.x-a.x).toDouble())).toFloat()-90f
        fun bone(part: String, a: MovementPoint, b: MovementPoint, width: Float, shade: Float, overlap: Float = 2f) {
            val length = hypot(b.x-a.x,b.y-a.y).coerceAtLeast(1f)
            val height = length+overlap*2
            add(CharacterSprite(part,a.x,a.y,width,height,angle(a,b),pivotY=overlap/height,shade=shade))
        }
        fun leg(hip: BodyJoint, knee: BodyJoint, ankle: BodyJoint, toe: BodyJoint, near: Boolean) {
            val shade = if (near) 1f else .78f
            bone("thigh",pose[hip],pose[knee],if(side) 19f else 18f,shade,3f)
            bone("calf",pose[knee],pose[ankle],13f,shade)
            val a=pose[ankle]; val t=pose[toe]
            if(side) {
                // Ankle and toe are separate anchors; foot stays planted during a squat.
                val direction = Math.toDegrees(atan2((t.y-a.y).toDouble(),(t.x-a.x).toDouble())).toFloat()
                add(CharacterSprite("shoe_side",a.x,a.y,25f,17f,direction-18f,.28f,.35f,shade))
            } else add(CharacterSprite("shoe_front",a.x,a.y,13f,17f,0f,.5f,.3f,shade))
        }
        fun arm(shoulder: BodyJoint, elbow: BodyJoint, wrist: BodyJoint, near: Boolean) {
            val shade=if(near) 1f else .8f
            bone("upper_arm",pose[shoulder],pose[elbow],12f,shade,3f)
            bone("forearm",pose[elbow],pose[wrist],9f,shade)
            val hand=pose[wrist]
            val planted = hand.y > 219f && spec.id in setOf("wall","pushup","plank","bird","cat","child","bridge")
            val grip = spec.prop==MovementProp.DUMBBELLS || spec.prop==MovementProp.GOBLET || spec.id=="box"
            add(CharacterSprite(if(grip) "grip" else "hand",hand.x,hand.y,if(grip) 9f else 7f,if(grip) 11f else 12f,
                if(planted) -85f else angle(pose[elbow],hand),.5f,.12f,shade))
        }
        leg(BodyJoint.LH,BodyJoint.LK,BodyJoint.LA,BodyJoint.LT,false)
        leg(BodyJoint.RH,BodyJoint.RK,BodyJoint.RA,BodyJoint.RT,true)
        arm(BodyJoint.LS,BodyJoint.LE,BodyJoint.LW,false)
        val top=pose[BodyJoint.LS].mix(pose[BodyJoint.RS],.5f)
        val bottom=pose[BodyJoint.LH].mix(pose[BodyJoint.RH],.5f)
        val span=hypot(pose[BodyJoint.RS].x-pose[BodyJoint.LS].x,pose[BodyJoint.RS].y-pose[BodyJoint.LS].y)
        add(CharacterSprite(if(side) "torso_side" else "torso_front",top.x,top.y,
            if(side) 32f else (span+11f).coerceAtLeast(30f),hypot(bottom.x-top.x,bottom.y-top.y)/.70f,
            angle(top,bottom),.5f,.20f,bend=pose.spineCurve))
        val head=pose[BodyJoint.HEAD]
        add(CharacterSprite(if(side) "head_side" else "head_front",head.x,head.y,if(side) 32f else 29f,42f,
            angle(head,pose[BodyJoint.NECK]),if(side) .66f else .5f,.40f))
        arm(BodyJoint.RS,BodyJoint.RE,BodyJoint.RW,true)
    }
}
