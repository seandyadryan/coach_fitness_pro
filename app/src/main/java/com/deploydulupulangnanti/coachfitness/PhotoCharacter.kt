package com.deploydulupulangnanti.coachfitness

import kotlin.math.atan2
import kotlin.math.hypot
import kotlin.math.cos
import kotlin.math.sin

/** Texture placements shared by the native renderer and exported preview frames. */
data class CharacterSprite(
    val part: String, val x: Float, val y: Float, val width: Float, val height: Float,
    val angle: Float = 0f, val pivotX: Float = .5f, val pivotY: Float = 0f,
    val shade: Float = 1f, val bend: Float = 0f,
    val headX: Float = 0f, val headY: Float = 0f, val headAngle: Float = 0f
) {
    fun opacity(v: Float): Float {
        if(!part.startsWith("body_")) return 1f
        val t=((v-.88f)/.12f).coerceIn(0f,1f)
        return 1-t*t*(3-2*t)
    }
    /** The neck and face stay rigid; only the torso rows bend below the shoulders. */
    fun localPoint(u: Float, v: Float): MovementPoint {
        val t=((v-.44f)/.50f).coerceIn(0f,1f)
        val smooth=t*t*(3-2*t)
        val centerShift=if(part=="body_side") width*.10f*smooth else 0f
        val curve=if(part.startsWith("body_")) -bend*4*t*(1-t) else 0f
        val body=MovementPoint((u-pivotX)*width-centerShift+curve,(v-pivotY)*height)
        if(!part.startsWith("body_") || v>=.44f) return body
        val headRadians=Math.toRadians(headAngle.toDouble())
        val hx=(u-if(part=="body_side") .70f else .5f)*width
        val hy=(v-.16f)*120f
        val gx=headX+(hx*cos(headRadians)-hy*sin(headRadians)).toFloat()-x
        val gy=headY+(hx*sin(headRadians)+hy*cos(headRadians)).toFloat()-y
        val inverse=Math.toRadians(-angle.toDouble())
        val head=MovementPoint((gx*cos(inverse)-gy*sin(inverse)).toFloat(),(gx*sin(inverse)+gy*cos(inverse)).toFloat())
        val blend=((v-.33f)/.11f).coerceIn(0f,1f)
        return head.mix(body,blend*blend*(3-2*blend))
    }
    fun point(u: Float,v: Float): MovementPoint {
        val p=localPoint(u,v); val radians=Math.toRadians(angle.toDouble())
        return MovementPoint(x+(p.x*cos(radians)-p.y*sin(radians)).toFloat(),y+(p.x*sin(radians)+p.y*cos(radians)).toFloat())
    }
}

object PhotoCharacter {
    val parts = setOf("body_front", "body_side", "upper_arm", "forearm", "thigh", "calf", "hand", "grip", "shoe_front", "shoe_side")

    fun sprites(spec: MovementSpec, pose: MovementPose): List<CharacterSprite> = buildList {
        val side = spec.viewLabel == "SIDE VIEW"
        fun angle(a: MovementPoint, b: MovementPoint) = Math.toDegrees(atan2((b.y-a.y).toDouble(), (b.x-a.x).toDouble())).toFloat()-90f
        val top=pose[BodyJoint.LS].mix(pose[BodyJoint.RS],.5f)
        val bottom=pose[BodyJoint.LH].mix(pose[BodyJoint.RH],.5f)
        val rawHead=pose[BodyJoint.HEAD]
        val headDistance=hypot(rawHead.x-top.x,rawHead.y-top.y).coerceAtLeast(1f)
        val visualHead=top.mix(rawHead,(28f/headDistance).coerceAtMost(1f))
        val body=CharacterSprite(if(side) "body_side" else "body_front",top.x,top.y,
            if(side) 42f else 43f,hypot(bottom.x-top.x,bottom.y-top.y)/.50f,
            angle(top,bottom),if(side) .40f else .5f,.44f,bend=pose.spineCurve,
            headX=visualHead.x,headY=visualHead.y,headAngle=angle(rawHead,pose[BodyJoint.NECK]))
        fun bone(part: String, a: MovementPoint, b: MovementPoint, width: Float, shade: Float, overlap: Float = 2f) {
            val length = hypot(b.x-a.x,b.y-a.y).coerceAtLeast(1f)
            val height = length+overlap*2
            add(CharacterSprite(part,a.x,a.y,width,height,angle(a,b),pivotY=overlap/height,shade=shade))
        }
        fun leg(hip: BodyJoint, knee: BodyJoint, ankle: BodyJoint, toe: BodyJoint, near: Boolean) {
            val shade = if (near) 1f else .78f
            val root=body.point(if(side) { if(near) .54f else .44f } else { if(near) .72f else .28f },.93f)
            bone("thigh",root,pose[knee],if(side) 19f else 18f,shade,5f)
            bone("calf",pose[knee],pose[ankle],13f,shade,3f)
            val a=pose[ankle]; val t=pose[toe]
            if(side) {
                // Ankle and toe are separate anchors; foot stays planted during a squat.
                val direction = Math.toDegrees(atan2((t.y-a.y).toDouble(),(t.x-a.x).toDouble())).toFloat()
                add(CharacterSprite("shoe_side",a.x,a.y,25f,17f,direction-18f,.28f,.35f,shade))
            } else add(CharacterSprite("shoe_front",a.x,a.y,13f,17f,0f,.5f,.3f,shade))
        }
        fun arm(shoulder: BodyJoint, elbow: BodyJoint, wrist: BodyJoint, near: Boolean) {
            val shade=if(near) 1f else .8f
            val root=body.point(if(side) { if(near) .40f else .30f } else { if(near) .84f else .16f },if(side) .47f else .45f)
            bone("upper_arm",root,pose[elbow],12f,shade,6f)
            bone("forearm",pose[elbow],pose[wrist],9f,shade,4f)
            val hand=pose[wrist]
            val planted = hand.y > 219f && spec.id in setOf("wall","pushup","plank","bird","cat","child","bridge")
            val grip = spec.prop==MovementProp.DUMBBELLS || spec.prop==MovementProp.GOBLET || spec.id=="box"
            add(CharacterSprite(if(grip) "grip" else "hand",hand.x,hand.y,if(grip) 9f else 7f,if(grip) 11f else 12f,
                if(planted) -85f else angle(pose[elbow],hand),.5f,.12f,shade))
        }
        leg(BodyJoint.LH,BodyJoint.LK,BodyJoint.LA,BodyJoint.LT,false)
        leg(BodyJoint.RH,BodyJoint.RK,BodyJoint.RA,BodyJoint.RT,true)
        arm(BodyJoint.LS,BodyJoint.LE,BodyJoint.LW,false)
        // One connected photograph owns the head, neck, shoulders and pelvis.
        // No independent head transform can expose a cut edge at the neck.
        add(body)
        arm(BodyJoint.RS,BodyJoint.RE,BodyJoint.RW,true)
    }
}
