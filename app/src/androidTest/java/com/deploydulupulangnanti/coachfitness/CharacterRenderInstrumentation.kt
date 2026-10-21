package com.deploydulupulangnanti.coachfitness

import android.app.Activity
import android.app.Instrumentation
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import kotlin.math.roundToInt

/** Pixel regression checks run against the actual Android Canvas renderer. */
class CharacterRenderInstrumentation : Instrumentation() {
    override fun onCreate(arguments: Bundle?) { super.onCreate(arguments); start() }
    override fun onStart() {
        val result=Bundle()
        try {
            val output=File(targetContext.getExternalFilesDir(null),"character-qa").apply { mkdirs() }
            val checks=JSONArray(); val failures=JSONArray()
            val size=800; val height=520; val scale=2f
            val bitmap=Bitmap.createBitmap(size,height,Bitmap.Config.ARGB_8888)
            val pixels=IntArray(size*height)
            val queue=IntArray(size*height)
            val phases=listOf(0f,.12f,.25f,.37f,.5f,.62f,.75f,.87f)
            for(spec in MovementModel.specifications.values) for(frame in phases.indices) {
                val pose=MovementModel.sample(spec.id,phases[frame])
                val sprites=PhotoCharacter.sprites(spec,pose)
                val body=sprites.single { it.part.startsWith("body_") }
                runOnMainSync {
                    bitmap.eraseColor(Color.TRANSPARENT)
                    val canvas=Canvas(bitmap); canvas.scale(scale,scale)
                    PhotoCharacterRenderer.draw(canvas,targetContext.resources,spec,pose)
                }
                bitmap.getPixels(pixels,0,size,0,0,size,height)
                fun opaque(index: Int)=index in pixels.indices && (pixels[index] ushr 24)>96
                fun nearest(point: MovementPoint): Int {
                    val x=(point.x*scale).roundToInt(); val y=(point.y*scale).roundToInt()
                    for(radius in 0..12) for(dy in -radius..radius) for(dx in -radius..radius) {
                        val px=x+dx; val py=y+dy
                        if(px in 0 until size && py in 0 until height && opaque(py*size+px)) return py*size+px
                    }
                    return -1
                }
                val side=body.part=="body_side"
                val head=nearest(body.point(if(side) .70f else .5f,.16f))
                val neck=nearest(body.point(if(side) .58f else .5f,if(side) .32f else .34f))
                val chest=nearest(body.point(if(side) .65f else .5f,.64f))
                val connected=BooleanArray(pixels.size)
                var read=0; var write=0
                if(head>=0) { queue[write++]=head; connected[head]=true }
                while(read<write) {
                    val index=queue[read++]; val x=index%size; val y=index/size
                    for(dy in -1..1) for(dx in -1..1) {
                        val px=x+dx; val py=y+dy
                        if(px in 0 until size && py in 0 until height) {
                            val candidate=py*size+px
                            if(!connected[candidate] && opaque(candidate)) { connected[candidate]=true; queue[write++]=candidate }
                        }
                    }
                }
                fun joined(index: Int)=index>=0 && connected[index]
                val neckJoined=joined(head) && joined(neck) && joined(chest)
                val detached=BodyJoint.entries.filter { it !in setOf(BodyJoint.HEAD,BodyJoint.NECK,BodyJoint.LT,BodyJoint.RT) }
                    .filter { !joined(nearest(pose[it])) }.map { it.name }
                val passed=neckJoined && detached.isEmpty()
                checks.put(JSONObject().put("exercise",spec.id).put("phase",phases[frame])
                    .put("head_neck_torso_connected",neckJoined).put("detached_joint_anchors",JSONArray(detached)).put("status",if(passed) "PASS" else "FAIL"))
                if(!passed) failures.put("${spec.id}/$frame: neck=$neckJoined, joints=$detached")
                File(output,"${spec.id}-$frame.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG,100,it) }
            }
            bitmap.recycle()
            val packageInfo=targetContext.packageManager.getPackageInfo(targetContext.packageName,0)
            File(output,"connectivity.json").writeText(JSONObject().put("renderer","Android Canvas drawBitmapMesh")
                .put("version_name",packageInfo.versionName).put("version_code",packageInfo.versionCode)
                .put("pose_count",checks.length()).put("status",if(failures.length()==0) "PASS" else "FAIL")
                .put("checks",checks).put("failures",failures).toString(2))
            result.putString("character_checks",if(failures.length()==0) "PASS" else "FAIL")
            result.putInt("pose_count",checks.length()); result.putString("failures",failures.toString())
            finish(if(failures.length()==0) Activity.RESULT_OK else Activity.RESULT_CANCELED,result)
        } catch(error: Throwable) {
            result.putString("character_checks","FAIL"); result.putString("error",error.stackTraceToString())
            finish(Activity.RESULT_CANCELED,result)
        }
    }
}
