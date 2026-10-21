package com.deploydulupulangnanti.coachfitness

import java.io.File

/** Export the production geometry so illustration QA never reimplements poses. */
object MovementFrameExporter {
    @JvmStatic fun main(args: Array<String>) {
        fun quote(s: String)="\""+s.replace("\\","\\\\").replace("\"","\\\"")+"\""
        val json=MovementModel.specifications.values.joinToString(",",prefix="[",postfix="]") { spec ->
            val frames=(0 until 48).joinToString(",",prefix="[",postfix="]") { index ->
                val pose=MovementModel.sample(spec.id,index/48f)
                val sprites=PhotoCharacter.sprites(spec,pose).joinToString(",",prefix="[",postfix="]") { s ->
                    "{\"part\":${quote(s.part)},\"x\":${s.x},\"y\":${s.y},\"width\":${s.width},\"height\":${s.height},\"angle\":${s.angle},\"pivotX\":${s.pivotX},\"pivotY\":${s.pivotY},\"shade\":${s.shade},\"bend\":${s.bend}}"
                }
                "{\"curve\":${pose.spineCurve},\"sprites\":$sprites,\"joints\":{"+pose.joints.entries.joinToString(",") { (joint,p) -> "${quote(joint.name)}:[${p.x},${p.y}]" }+"}}"
            }
            "{\"id\":${quote(spec.id)},\"name\":${quote(Catalog.exercises.first{it.id==spec.id}.name)},\"cycleMs\":${spec.cycleMs},\"view\":${quote(spec.viewLabel)},\"prop\":${quote(spec.prop.name)},\"firstCue\":${quote(spec.firstCue)},\"secondCue\":${quote(spec.secondCue)},\"frames\":$frames}"
        }
        File(args.single()).apply { parentFile?.mkdirs(); writeText(json) }
        println("Exported ${MovementModel.specifications.size} production movement illustrations.")
    }
}
