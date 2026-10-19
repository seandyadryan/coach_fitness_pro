package com.deploydulupulangnanti.coachfitness

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/** Device-independent illustration geometry. Coordinates use a 400 Ã— 260 stage. */
data class MovementPoint(val x: Float, val y: Float) {
    fun mix(other: MovementPoint, amount: Float) = MovementPoint(x + (other.x - x) * amount, y + (other.y - y) * amount)
}

enum class BodyJoint { HEAD, NECK, LS, LE, LW, RS, RE, RW, LH, LK, LA, LT, RH, RK, RA, RT }

data class MovementPose(val joints: Map<BodyJoint, MovementPoint>, val spineCurve: Float = 0f) {
    operator fun get(joint: BodyJoint): MovementPoint = joints.getValue(joint)
    fun change(vararg points: Pair<BodyJoint, MovementPoint>, curve: Float = spineCurve) = copy(joints = joints + points, spineCurve = curve)
    fun mix(other: MovementPose, amount: Float) = MovementPose(BodyJoint.entries.associateWith { this[it].mix(other[it], amount) }, spineCurve + (other.spineCurve - spineCurve) * amount)
    fun mirrored() = MovementPose(BodyJoint.entries.associateWith { joint ->
        val paired = when (joint) {
            BodyJoint.LS -> BodyJoint.RS; BodyJoint.LE -> BodyJoint.RE; BodyJoint.LW -> BodyJoint.RW
            BodyJoint.RS -> BodyJoint.LS; BodyJoint.RE -> BodyJoint.LE; BodyJoint.RW -> BodyJoint.LW
            BodyJoint.LH -> BodyJoint.RH; BodyJoint.LK -> BodyJoint.RK; BodyJoint.LA -> BodyJoint.RA; BodyJoint.LT -> BodyJoint.RT
            BodyJoint.RH -> BodyJoint.LH; BodyJoint.RK -> BodyJoint.LK; BodyJoint.RA -> BodyJoint.LA; BodyJoint.RT -> BodyJoint.LT
            else -> joint
        }
        this[paired].let { MovementPoint(400f - it.x, it.y) }
    }, spineCurve)
    fun swapLimbs() = copy(joints = joints + listOf(BodyJoint.LS to BodyJoint.RS, BodyJoint.LE to BodyJoint.RE, BodyJoint.LW to BodyJoint.RW, BodyJoint.LH to BodyJoint.RH, BodyJoint.LK to BodyJoint.RK, BodyJoint.LA to BodyJoint.RA, BodyJoint.LT to BodyJoint.RT).flatMap { (l, r) -> listOf(l to this[r], r to this[l]) })
}

enum class MovementProp { NONE, WALL, DUMBBELLS, GOBLET }
data class MovementSpec(val id: String, val cycleMs: Long, val viewLabel: String, val firstCue: String, val secondCue: String, val prop: MovementProp = MovementProp.NONE, val hold: Boolean = false)

/** Deliberately schematic movement examples; written form cues remain authoritative. */
object MovementModel {
    private fun p(x: Number, y: Number) = MovementPoint(x.toFloat(), y.toFloat())
    private fun pose(vararg coordinates: MovementPoint) = MovementPose(BodyJoint.entries.zip(coordinates.toList()).toMap()).also { require(coordinates.size == BodyJoint.entries.size) }
    private val front = pose(p(200,60),p(200,84),p(184,92),p(175,122),p(176,154),p(216,92),p(225,122),p(224,154),p(190,150),p(187,187),p(184,224),p(174,230),p(210,150),p(213,187),p(216,224),p(228,230))
    private val side = pose(p(210,60),p(202,84),p(196,92),p(192,122),p(193,154),p(205,92),p(210,123),p(213,154),p(194,150),p(194,187),p(194,224),p(211,230),p(203,150),p(204,187),p(204,224),p(222,230))
    private val squat = side.change(BodyJoint.HEAD to p(211,99),BodyJoint.NECK to p(202,123),BodyJoint.LS to p(194,132),BodyJoint.LE to p(221,141),BodyJoint.LW to p(249,141),BodyJoint.RS to p(204,132),BodyJoint.RE to p(231,140),BodyJoint.RW to p(259,140),BodyJoint.LH to p(171,185),BodyJoint.LK to p(211,185),BodyJoint.RH to p(180,185),BodyJoint.RK to p(220,186))
    private val lunge = side.change(BodyJoint.HEAD to p(213,86),BodyJoint.NECK to p(204,110),BodyJoint.LS to p(196,118),BodyJoint.LE to p(187,148),BodyJoint.LW to p(195,177),BodyJoint.RS to p(205,118),BodyJoint.RE to p(217,147),BodyJoint.RW to p(225,176),BodyJoint.LH to p(183,175),BodyJoint.LK to p(160,204),BodyJoint.LA to p(125,225),BodyJoint.LT to p(142,230),BodyJoint.RH to p(192,175),BodyJoint.RK to p(229,186),BodyJoint.RA to p(229,224),BodyJoint.RT to p(247,230))
    private val highPlank = pose(p(279,125),p(257,138),p(239,145),p(240,184),p(246,225),p(249,146),p(252,185),p(258,225),p(165,166),p(120,193),p(80,222),p(68,230),p(174,169),p(129,196),p(89,222),p(77,230))
    private val lowPushup = highPlank.change(BodyJoint.HEAD to p(280,170),BodyJoint.NECK to p(257,183),BodyJoint.LS to p(238,190),BodyJoint.LE to p(212,211),BodyJoint.RS to p(248,190),BodyJoint.RE to p(221,213),BodyJoint.LH to p(165,199),BodyJoint.RH to p(174,202),BodyJoint.LK to p(121,211),BodyJoint.RK to p(130,214))
    private val quadruped = pose(p(272,140),p(249,144),p(222,150),p(224,187),p(229,225),p(231,151),p(234,188),p(239,225),p(153,159),p(153,217),p(117,222),p(100,230),p(163,160),p(163,218),p(127,223),p(110,230))
    private val back = pose(p(92,208),p(114,213),p(124,215),p(150,222),p(177,224),p(132,218),p(157,225),p(183,226),p(190,219),p(230,185),p(264,225),p(282,230),p(199,221),p(239,188),p(273,225),p(291,230))
    private val tableBack = back.change(BodyJoint.LE to p(127,180),BodyJoint.LW to p(129,145),BodyJoint.RE to p(138,184),BodyJoint.RW to p(142,149),BodyJoint.LK to p(203,176),BodyJoint.LA to p(244,179),BodyJoint.LT to p(258,178),BodyJoint.RK to p(213,180),BodyJoint.RA to p(254,183),BodyJoint.RT to p(268,182))
    val specifications = listOf(
        MovementSpec("march",4000,"FRONT VIEW","Lift one knee","Alternate sides"),
        MovementSpec("squat",4800,"SIDE VIEW","Sit back with control","Press through your feet"),
        MovementSpec("wall",4600,"SIDE VIEW","Bend your elbows slowly","Push away from the wall",MovementProp.WALL),
        MovementSpec("pushup",4800,"SIDE VIEW","Lower with control","Press up as one unit"),
        MovementSpec("bridge",4800,"SIDE VIEW","Lift through your hips","Lower your hips slowly"),
        MovementSpec("bird",6400,"SIDE VIEW","Reach opposite arm and leg","Return and alternate"),
        MovementSpec("plank",6000,"SIDE VIEW","Hold a steady body line","Keep breathing",hold=true),
        MovementSpec("lunge",6400,"SIDE VIEW","Step back and lower","Return, then alternate"),
        MovementSpec("calf",4000,"SIDE VIEW","Lift your heels","Lower with control"),
        MovementSpec("deadbug",6400,"SIDE VIEW","Extend opposite arm and leg","Return and alternate"),
        MovementSpec("step",4000,"FRONT VIEW","Step gently to the side","Return and alternate"),
        MovementSpec("knees",4400,"FRONT VIEW","Bring knee toward opposite hand","Alternate sides"),
        MovementSpec("jack",4800,"FRONT VIEW","Step out and raise your arms","Return without jumping"),
        MovementSpec("box",4000,"FRONT VIEW","Reach forward without locking","Return to your guard"),
        MovementSpec("cat",6000,"SIDE VIEW","Round your back gently","Lengthen through your chest"),
        MovementSpec("child",6000,"SIDE VIEW","Reach forward comfortably","Breathe without forcing",hold=true),
        MovementSpec("rotation",6000,"FRONT VIEW","Turn your torso gently","Return through the center"),
        MovementSpec("shoulder",4800,"FRONT VIEW","Roll shoulders gently","Keep your neck relaxed"),
        MovementSpec("curl",4800,"FRONT VIEW","Curl without swinging","Lower the weights slowly",MovementProp.DUMBBELLS),
        MovementSpec("row",4800,"SIDE VIEW","Draw elbows toward your hips","Lower the weights slowly",MovementProp.DUMBBELLS),
        MovementSpec("goblet",4800,"SIDE VIEW","Keep the weight close","Stand smoothly",MovementProp.GOBLET)
    ).associateBy { it.id }
    fun spec(id: String) = requireNotNull(specifications[id]) { "Missing movement illustration: $id" }
    private fun cycle(vararg poses: MovementPose, phase: Float): MovementPose {
        val position = phase * poses.size
        val index = position.toInt().coerceAtMost(poses.lastIndex)
        val fraction = position - index
        val eased = (1f - cos(fraction * PI.toFloat())) * .5f
        return poses[index].mix(poses[(index + 1) % poses.size], eased)
    }
    fun sample(id: String, phase: Float): MovementPose {
        spec(id)
        require(phase.isFinite())
        val t = ((phase % 1f) + 1f) % 1f
        val wave = (sin(t * 2 * PI).toFloat())
        return when (id) {
            "squat" -> cycle(side, squat, phase=t)
            "goblet" -> cycle(goblet(side),goblet(squat),phase=t)
            "march", "knees" -> {
                val ready=front.change(BodyJoint.LE to p(172,122),BodyJoint.LW to p(183,101),BodyJoint.RE to p(228,122),BodyJoint.RW to p(217,101))
                val raised = ready.change(BodyJoint.LK to p(if(id=="knees") 158 else 174,if(id=="knees") 160 else 177),BodyJoint.LA to p(if(id=="knees") 165 else 169,if(id=="knees") 197 else 212),BodyJoint.LT to p(if(id=="knees") 180 else 183,if(id=="knees") 202 else 216),BodyJoint.RE to p(206,125),BodyJoint.RW to p(if(id=="knees") 177 else 199,if(id=="knees") 158 else 101),BodyJoint.LE to p(171,125),BodyJoint.LW to p(165,104))
                cycle(ready,raised,ready,raised.mirrored(),phase=t)
            }
            "wall" -> {
                val start=side.change(BodyJoint.HEAD to p(251,78),BodyJoint.NECK to p(241,102),BodyJoint.LS to p(230,111),BodyJoint.LE to p(262,117),BodyJoint.LW to p(295,122),BodyJoint.RS to p(239,111),BodyJoint.RE to p(269,117),BodyJoint.RW to p(297,122),BodyJoint.LH to p(201,169),BodyJoint.RH to p(210,170),BodyJoint.LK to p(186,195),BodyJoint.RK to p(195,196),BodyJoint.LA to p(172,224),BodyJoint.LT to p(190,230),BodyJoint.RA to p(181,224),BodyJoint.RT to p(199,230))
                val low=start.change(BodyJoint.HEAD to p(282,86),BodyJoint.NECK to p(271,109),BodyJoint.LS to p(259,118),BodyJoint.LE to p(274,145),BodyJoint.RS to p(268,118),BodyJoint.RE to p(283,145),BodyJoint.LH to p(222,177),BodyJoint.RH to p(231,178),BodyJoint.LK to p(198,201),BodyJoint.RK to p(207,202))
                cycle(start,low,phase=t)
            }
            "pushup" -> cycle(highPlank,lowPushup,phase=t)
            "bridge" -> cycle(back,back.change(BodyJoint.LH to p(187,172),BodyJoint.RH to p(196,175)),phase=t)
            "bird" -> {
                val reach=quadruped.change(BodyJoint.LS to p(222,149),BodyJoint.LE to p(265,145),BodyJoint.LW to p(308,142),BodyJoint.RK to p(117,159),BodyJoint.RA to p(72,157),BodyJoint.RT to p(55,157))
                cycle(quadruped,reach,quadruped,reach.swapLimbs(),phase=t)
            }
            "plank" -> {
                val hold=highPlank.change(BodyJoint.LE to p(240,219),BodyJoint.LW to p(282,225),BodyJoint.RE to p(250,220),BodyJoint.RW to p(292,226))
                hold.change(BodyJoint.HEAD to p(279,125+wave),BodyJoint.NECK to p(257,138+wave),BodyJoint.LH to p(165,166+wave),BodyJoint.RH to p(174,169+wave))
            }
            "lunge" -> cycle(side,lunge,side,lunge.swapLimbs(),phase=t)
            "calf" -> {
                val raised=side.copy(joints=side.joints.mapValues { (joint,point) -> if(joint==BodyJoint.LT||joint==BodyJoint.RT) point else p(point.x,point.y-9) })
                cycle(side,raised,phase=t)
            }
            "deadbug" -> {
                val reach=tableBack.change(BodyJoint.LE to p(88,211),BodyJoint.LW to p(51,213),BodyJoint.RK to p(238,218),BodyJoint.RA to p(280,219),BodyJoint.RT to p(297,223))
                cycle(tableBack,reach,tableBack,reach.swapLimbs(),phase=t)
            }
            "step" -> {
                val step=front.copy(joints=front.joints.mapValues { (_,point)->p(point.x+15,point.y) }).change(BodyJoint.RK to p(238,187),BodyJoint.RA to p(257,224),BodyJoint.RT to p(270,230),BodyJoint.LE to p(178,122),BodyJoint.LW to p(159,146),BodyJoint.RE to p(246,122),BodyJoint.RW to p(263,146))
                cycle(front,step,front,step.mirrored(),phase=t)
            }
            "jack" -> {
                val open=front.change(BodyJoint.LE to p(162,65),BodyJoint.LW to p(178,30),BodyJoint.RE to p(238,65),BodyJoint.RW to p(222,30),BodyJoint.RK to p(234,186),BodyJoint.RA to p(254,224),BodyJoint.RT to p(267,230))
                val legs=cycle(front,open,front,open.mirrored(),phase=t)
                val amount=(1-cos(t*4*PI).toFloat())*.5f
                val angle=PI.toFloat()*.5f+amount*2.65f
                val elbow=p(184+cos(angle)*34,92+sin(angle)*34)
                val wrist=p(elbow.x+cos(angle+.06f)*34,elbow.y+sin(angle+.06f)*34)
                legs.change(BodyJoint.LE to elbow,BodyJoint.LW to wrist,BodyJoint.RE to p(400-elbow.x,elbow.y),BodyJoint.RW to p(400-wrist.x,wrist.y))
            }
            "box" -> {
                val guard=front.change(BodyJoint.LE to p(164,121),BodyJoint.LW to p(181,93),BodyJoint.RE to p(236,121),BodyJoint.RW to p(221,93),BodyJoint.LA to p(170,224),BodyJoint.LT to p(156,230),BodyJoint.RA to p(231,224),BodyJoint.RT to p(246,230))
                val jab=guard.change(BodyJoint.RS to p(221,96),BodyJoint.RE to p(256,103),BodyJoint.RW to p(289,107))
                cycle(guard,jab,guard,jab.mirrored(),phase=t)
            }
            "cat" -> {
                val rounded=quadruped.change(BodyJoint.HEAD to p(262,173),BodyJoint.NECK to p(244,154),BodyJoint.LS to p(222,145),BodyJoint.RS to p(231,146),curve=-17f)
                val lengthened=quadruped.change(BodyJoint.HEAD to p(270,126),BodyJoint.NECK to p(248,144),BodyJoint.LS to p(222,160),BodyJoint.RS to p(231,161),curve=13f)
                cycle(rounded,lengthened,phase=t)
            }
            "child" -> pose(p(243,208+wave),p(222,196+wave),p(200,194+wave),p(244,212),p(286,225),p(209,197+wave),p(253,215),p(295,226),p(124,203+wave),p(175,224),p(127,222),p(110,230),p(133,205+wave),p(184,225),p(136,223),p(119,230))
            "rotation" -> {
                val turn=wave*8
                front.change(BodyJoint.HEAD to p(200+turn,60),BodyJoint.NECK to p(200+turn,84),BodyJoint.LS to p(184+turn,92+wave*2),BodyJoint.RS to p(216+turn,92-wave*2),BodyJoint.LE to p(173+turn,124),BodyJoint.LW to p(198+turn,123),BodyJoint.RE to p(227+turn,124),BodyJoint.RW to p(205+turn,123))
            }
            "shoulder" -> {
                val dx=cos(t*2*PI).toFloat()*3; val dy=wave*4
                front.change(BodyJoint.LS to p(184+dx,92+dy),BodyJoint.LE to p(175+dx,122+dy),BodyJoint.LW to p(176+dx,154+dy),BodyJoint.RS to p(216-dx,92+dy),BodyJoint.RE to p(225-dx,122+dy),BodyJoint.RW to p(224-dx,154+dy))
            }
            "curl" -> {
                val low=front.change(BodyJoint.LE to p(174,125),BodyJoint.LW to p(174,158),BodyJoint.RE to p(226,125),BodyJoint.RW to p(226,158))
                val amount=(1-cos(t*2*PI).toFloat())*.5f
                val angle=PI.toFloat()*.5f-amount*2.62f
                val wrist=p(174+cos(angle)*33,125+sin(angle)*33)
                low.change(BodyJoint.LW to wrist,BodyJoint.RW to p(400-wrist.x,wrist.y))
            }
            "row" -> {
                val low=side.change(BodyJoint.HEAD to p(266,90),BodyJoint.NECK to p(246,103),BodyJoint.LS to p(226,114),BodyJoint.LE to p(226,152),BodyJoint.LW to p(224,187),BodyJoint.RS to p(235,116),BodyJoint.RE to p(237,154),BodyJoint.RW to p(235,189),BodyJoint.LH to p(175,157),BodyJoint.RH to p(184,159),BodyJoint.LK to p(192,188),BodyJoint.RK to p(202,190))
                cycle(low,low.change(BodyJoint.LE to p(194,134),BodyJoint.LW to p(201,165),BodyJoint.RE to p(204,136),BodyJoint.RW to p(211,167)),phase=t)
            }
            else -> error("Unimplemented movement: $id")
        }
    }
    private fun goblet(base: MovementPose): MovementPose {
        val chest=base[BodyJoint.RS]
        return base.change(BodyJoint.LE to p(chest.x+20,chest.y+29),BodyJoint.LW to p(chest.x+13,chest.y+12),BodyJoint.RE to p(chest.x+30,chest.y+29),BodyJoint.RW to p(chest.x+18,chest.y+12))
    }
}
