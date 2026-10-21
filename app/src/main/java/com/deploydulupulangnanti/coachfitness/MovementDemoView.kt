package com.deploydulupulangnanti.coachfitness

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.os.SystemClock
import android.view.View
import kotlin.math.min

/** Offline photographic character animation; never used to time or count a workout. */
class MovementDemoView(context: Context, val exercise: Exercise) : View(context) {
    private val spec = MovementModel.spec(exercise.id)
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val clip = Rect()
    private val lime = Color.rgb(194,248,106)
    private var elapsed = spec.cycleMs * .12
    private var lastFrame = 0L
    private var hostActive = false
    private var aggregatedVisible = false
    var requestedPlaying = true
        private set
    val motionReduced: Boolean get() = !ValueAnimator.areAnimatorsEnabled()
    private val frame = object : Runnable {
        override fun run() {
            if (!canAnimate()) { lastFrame = 0; return }
            val now = SystemClock.uptimeMillis()
            val inViewport = getGlobalVisibleRect(clip)
            if (inViewport) {
                if (lastFrame != 0L) elapsed += (now-lastFrame).coerceIn(0,100)
                lastFrame = now
                invalidate()
            } else lastFrame = 0
            postDelayed(this, if (inViewport) 33L else 250L)
        }
    }
    init {
        contentDescription = context.getString(R.string.movement_description, exercise.name, exercise.cues.first())
        importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_YES
        isFocusable = true
    }
    private fun canAnimate() = isAttachedToWindow && hostActive && aggregatedVisible && windowVisibility == VISIBLE && requestedPlaying && !motionReduced
    private fun updatePlayback() {
        removeCallbacks(frame); lastFrame = 0
        if (canAnimate()) postOnAnimation(frame)
        invalidate()
    }
    fun setHostActive(active: Boolean) { hostActive = active; updatePlayback() }
    fun setPlayback(play: Boolean) { requestedPlaying = play; updatePlayback() }
    fun nextPose() { elapsed += spec.cycleMs / 4.0; invalidate() }
    override fun onAttachedToWindow() { super.onAttachedToWindow(); updatePlayback() }
    override fun onDetachedFromWindow() { removeCallbacks(frame); lastFrame = 0; super.onDetachedFromWindow() }
    override fun onVisibilityAggregated(isVisible: Boolean) { super.onVisibilityAggregated(isVisible); aggregatedVisible = isVisible; updatePlayback() }
    override fun onWindowVisibilityChanged(visibility: Int) { super.onWindowVisibilityChanged(visibility); updatePlayback() }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val scale = min(width / 400f, height / 260f)
        if (scale <= 0) return
        canvas.save(); canvas.translate((width-400*scale)/2, (height-260*scale)/2); canvas.scale(scale,scale)
        val phase = ((elapsed % spec.cycleMs) / spec.cycleMs).toFloat()
        val pose = MovementModel.sample(exercise.id,phase)
        paint.shader = RadialGradient(200f,155f,142f,intArrayOf(0xFF293C2C.toInt(),0xFF18231C.toInt()),null,Shader.TileMode.CLAMP)
        paint.style = Paint.Style.FILL; canvas.drawRoundRect(0f,0f,400f,260f,20f,20f,paint); paint.shader = null
        paint.color = 0xFF819782.toInt(); paint.typeface = Typeface.create("sans-serif-medium",Typeface.NORMAL); paint.textSize=9f; paint.textAlign=Paint.Align.LEFT
        canvas.drawText(spec.viewLabel,18f,24f,paint)
        paint.color=0xFF405440.toInt(); paint.style=Paint.Style.STROKE; paint.strokeWidth=1.5f
        canvas.drawCircle(376f,20f,6f,paint); paint.color=lime
        canvas.drawArc(370f,14f,382f,26f,-90f,phase*360f,false,paint)
        paint.style=Paint.Style.FILL; paint.color=0xFF26392A.toInt()
        canvas.drawOval(97f,224f,305f,238f,paint)
        if (spec.prop==MovementProp.WALL) {
            paint.color=0xFF526B55.toInt(); canvas.drawRoundRect(300f,52f,305f,233f,2f,2f,paint)
            paint.color=0xFF344B38.toInt(); canvas.drawRect(305f,52f,315f,233f,paint)
        }
        paint.color=0xFF4A614B.toInt(); paint.strokeWidth=1f; canvas.drawLine(65f,233f,335f,233f,paint)
        PhotoCharacterRenderer.draw(canvas,resources,spec,pose)
        if (spec.prop==MovementProp.DUMBBELLS) { dumbbell(canvas,pose[BodyJoint.LW]); dumbbell(canvas,pose[BodyJoint.RW]) }
        if (spec.prop==MovementProp.GOBLET) {
            val hand=pose[BodyJoint.LW].mix(pose[BodyJoint.RW],.5f)
            paint.color=0xFFCAD9C3.toInt(); canvas.drawRoundRect(hand.x-3,hand.y-10,hand.x+3,hand.y+11,2f,2f,paint)
            paint.color=0xFF859F86.toInt(); canvas.drawRoundRect(hand.x-10,hand.y-15,hand.x+10,hand.y-6,3f,3f,paint); canvas.drawRoundRect(hand.x-9,hand.y+7,hand.x+9,hand.y+15,3f,3f,paint)
        }
        paint.color=0xFFBED0B7.toInt(); paint.textSize=10f; paint.typeface=Typeface.create("sans-serif",Typeface.NORMAL)
        paint.textAlign=Paint.Align.CENTER
        canvas.drawText(if(phase<.5f) spec.firstCue else spec.secondCue,200f,251f,paint)
        paint.textAlign=Paint.Align.LEFT
        canvas.restore()
    }
    private fun segment(canvas: Canvas,a: MovementPoint,b: MovementPoint,width: Float,color: Int) {
        paint.color=color; paint.style=Paint.Style.STROKE; paint.strokeWidth=width; paint.strokeCap=Paint.Cap.ROUND
        canvas.drawLine(a.x,a.y,b.x,b.y,paint); paint.style=Paint.Style.FILL
    }
    private fun dumbbell(c: Canvas,hand: MovementPoint) {
        segment(c,MovementPoint(hand.x-11,hand.y),MovementPoint(hand.x+11,hand.y),3.5f,0xFFDAE5D0.toInt())
        paint.color=0xFF94B383.toInt(); c.drawRoundRect(hand.x-14,hand.y-9,hand.x-8,hand.y+9,2f,2f,paint); c.drawRoundRect(hand.x+8,hand.y-9,hand.x+14,hand.y+9,2f,2f,paint)
    }
}
