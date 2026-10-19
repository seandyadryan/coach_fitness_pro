package com.deploydulupulangnanti.coachfitness

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.*
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.*
import android.view.*
import android.widget.*
import org.json.JSONObject
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID
import kotlin.math.*

class MainActivity : Activity() {
    private val bg = Color.rgb(16, 20, 17)
    private val surface = Color.rgb(27, 33, 28)
    private val ink = Color.rgb(243, 245, 236)
    private val muted = Color.rgb(162, 173, 162)
    private val lime = Color.rgb(194, 248, 106)
    private lateinit var store: LocalStore
    private lateinit var root: LinearLayout
    private lateinit var body: LinearLayout
    private val handler = Handler(Looper.getMainLooper())
    private var screen = "Today"
    private var selected = "foundation"
    private var filter = "All"
    private var sessionId = ""
    private var index = 0
    private var rest = false
    private var remaining = 0L
    private var totalMs = 0L
    private var running = false
    private var lastTick = 0L
    private var timerText: TextView? = null
    private var timerBar: ProgressBar? = null
    private var pauseButton: TextView? = null
    private val movementDemos = mutableListOf<MovementDemoView>()
    private var sessionDemo: MovementDemoView? = null
    private var demosActive = false
    private val ticker = object : Runnable {
        override fun run() {
            if (running && screen == "Session") {
                val now = SystemClock.elapsedRealtime()
                val elapsed = (now - lastTick).coerceAtLeast(0)
                lastTick = now
                totalMs += elapsed
                remaining -= elapsed
                if (remaining <= 0L) advance() else refreshClock()
            }
            handler.postDelayed(this, 200)
        }
    }

    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        store = LocalStore(this)
        window.decorView.systemUiVisibility = 0
        window.navigationBarColor = bg
        window.statusBarColor = bg
        screen = state?.getString("screen") ?: "Today"
        selected = state?.getString("selected") ?: "foundation"
        if (screen == "Session") restoreSession()
        if (screen == "Complete") screen = "Progress"
        if (screen == "Session" && store.active == null) screen = "Today"
        render()
        handler.post(ticker)
        if (Build.VERSION.SDK_INT >= 33) onBackInvokedDispatcher.registerOnBackInvokedCallback(android.window.OnBackInvokedDispatcher.PRIORITY_DEFAULT) { goBack() }
    }
    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString("screen", screen); outState.putString("selected", selected)
        super.onSaveInstanceState(outState)
    }
    override fun onResume() { super.onResume(); demosActive = true; movementDemos.forEach { it.setHostActive(true) } }
    override fun onPause() { demosActive = false; movementDemos.forEach { it.setHostActive(false) }; pause(); super.onPause() }
    override fun onDestroy() { handler.removeCallbacks(ticker); movementDemos.forEach { it.setHostActive(false) }; movementDemos.clear(); super.onDestroy() }
    // Legacy button navigation on API 26–32. API 33+ uses the native
    // OnBackInvokedDispatcher callback registered in onCreate above.
    @android.annotation.SuppressLint("GestureBackNavigation")
    @Deprecated("Handled with predictive back on Android 13+")
    override fun onBackPressed() { goBack() }
    private fun goBack() {
        when (screen) {
            "Today" -> finish()
            "Session" -> { pause(); showDiscard() }
            "Exercise" -> navigate("Library")
            "Detail" -> navigate("Workouts")
            else -> navigate("Today")
        }
    }
    private fun dp(n: Int) = (n * resources.displayMetrics.density).roundToInt()
    private fun text(value: String, size: Int = 16, color: Int = ink, bold: Boolean = false): TextView = TextView(this).apply {
        text = value; textSize = size.toFloat(); setTextColor(color); includeFontPadding = false
        typeface = Typeface.create(if (bold) "sans-serif-medium" else "sans-serif", if (bold) Typeface.BOLD else Typeface.NORMAL)
        setLineSpacing(dp(3).toFloat(), 1f)
    }
    private fun s(id: Int) = getString(id)
    private fun label(value: String) = text(value, 11, muted, true).apply { letterSpacing = .14f }
    private fun shape(color: Int, radius: Int = 20, border: Int = Color.TRANSPARENT) = GradientDrawable().apply {
        setColor(color); cornerRadius = dp(radius).toFloat(); if (border != Color.TRANSPARENT) setStroke(dp(1), border)
    }
    private fun column() = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
    private fun row() = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
    private fun LinearLayout.gap(n: Int = 16) { addView(Space(this@MainActivity), LinearLayout.LayoutParams(1, dp(n))) }
    private fun LinearLayout.add(view: View, height: Int = -2) { addView(view, LinearLayout.LayoutParams(-1, if (height < 0) height else dp(height))) }
    private fun card(): LinearLayout = column().apply { background = shape(surface); setPadding(dp(20), dp(20), dp(20), dp(20)) }
    private fun action(title: String, primary: Boolean = true, onClick: () -> Unit): TextView = text(title, 15, if (primary) bg else ink, true).apply {
        gravity = Gravity.CENTER; minHeight = dp(52); setPadding(dp(18), dp(15), dp(18), dp(15))
        background = shape(if (primary) lime else Color.rgb(42, 51, 42), 16)
        isClickable = true; isFocusable = true; contentDescription = title; setOnClickListener { onClick() }
    }
    private fun navigate(destination: String) { if (screen == "Session") pause(); screen = destination; render() }
    private fun render() {
        movementDemos.forEach { it.setHostActive(false) }; movementDemos.clear(); sessionDemo = null
        if (screen == "Session") window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        else window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        timerText = null; timerBar = null; pauseButton = null
        root = column().apply { setBackgroundColor(bg) }
        if (Build.VERSION.SDK_INT >= 30) {
            root.setOnApplyWindowInsetsListener { view, insets ->
                val bars = insets.getInsets(WindowInsets.Type.systemBars() or WindowInsets.Type.displayCutout())
                view.setPadding(bars.left, bars.top, bars.right, bars.bottom); insets
            }
        } else {
            root.setOnApplyWindowInsetsListener { view, insets ->
                @Suppress("DEPRECATION")
                view.setPadding(insets.systemWindowInsetLeft, insets.systemWindowInsetTop, insets.systemWindowInsetRight, insets.systemWindowInsetBottom)
                insets
            }
        }
        setContentView(root)
        val frame = FrameLayout(this)
        val scroll = ScrollView(this).apply { isFillViewport = true; clipToPadding = false }
        body = column().apply { setPadding(dp(24), dp(24), dp(24), dp(24)) }
        scroll.addView(body)
        val maxWidth = min(resources.displayMetrics.widthPixels, dp(960))
        frame.addView(scroll, FrameLayout.LayoutParams(maxWidth, -1, Gravity.CENTER_HORIZONTAL))
        root.addView(frame, LinearLayout.LayoutParams(-1, 0, 1f))
        when (screen) {
            "Today" -> today()
            "Workouts" -> workouts()
            "Progress" -> progress()
            "Profile" -> profile()
            "Library" -> library()
            "Exercise" -> exerciseDetail()
            "Detail" -> detail()
            "Session" -> session()
            "Complete" -> complete()
        }
        if (screen in listOf("Today", "Workouts", "Progress", "Profile")) navigation()
        root.requestApplyInsets()
    }
    private fun header() {
        val top = row()
        top.addView(ImageView(this).apply { setImageResource(R.drawable.ic_mark); contentDescription = s(R.string.app_name) }, LinearLayout.LayoutParams(dp(38), dp(38)))
        top.addView(text("  COACH FITNESS", 16, ink, true), LinearLayout.LayoutParams(0, -2, 1f))
        top.addView(text("PRO", 11, lime, true).apply { background = shape(0xFF293726.toInt(), 8); setPadding(dp(10), dp(6), dp(10), dp(6)) })
        body.add(top); body.gap(30)
    }
    private fun title(eyebrow: String, heading: String, subtitle: String? = null) {
        body.add(label(eyebrow)); body.gap(10); body.add(text(heading, 34, ink, true))
        subtitle?.let { body.gap(10); body.add(text(it, 15, muted)) }; body.gap(24)
    }
    private fun back() { body.add(text("‹  ${s(R.string.back)}", 15, lime).apply { minHeight = dp(44); gravity = Gravity.CENTER_VERTICAL; setOnClickListener { goBack() }; isFocusable = true }); body.gap(14) }
    private fun navigation() {
        val nav = row().apply { setPadding(dp(12), dp(10), dp(12), dp(8)); background = shape(surface, 0) }
        listOf("Today" to R.string.today, "Workouts" to R.string.workouts, "Progress" to R.string.progress, "Profile" to R.string.profile).forEachIndexed { i, (key, id) ->
            val item = column().apply { gravity = Gravity.CENTER; minimumHeight = dp(54); setOnClickListener { navigate(key) }; isFocusable = true; contentDescription = s(id) }
            item.addView(Glyph(this, i, if (screen == key) lime else muted), LinearLayout.LayoutParams(dp(23), dp(23)))
            item.gap(5); item.addView(text(s(id), 11, if (screen == key) lime else muted, screen == key).apply { gravity = Gravity.CENTER })
            nav.addView(item, LinearLayout.LayoutParams(0, -2, 1f))
        }; root.add(nav)
    }
    private fun today() {
        header(); title(s(R.string.greeting), s(R.string.headline))
        if (store.active != null) { body.add(action(s(R.string.resume_session)) { restoreSession(); navigate("Session") }); body.gap(18) }
        val hero = FrameLayout(this).apply { background = shape(surface, 24); clipToOutline = true }
        val pictureId = resources.getIdentifier("training_hero", "drawable", packageName)
        if (pictureId != 0) hero.addView(ImageView(this).apply { setImageResource(pictureId); scaleType = ImageView.ScaleType.CENTER_CROP; importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO }, FrameLayout.LayoutParams(-1, -1))
        hero.addView(View(this).apply { background = GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, intArrayOf(0xF20F1811.toInt(), 0x22101811)) }, FrameLayout.LayoutParams(-1, -1))
        val copy = column().apply { setPadding(dp(22), dp(24), dp(22), dp(22)) }
        copy.add(label(s(R.string.today_plan)).apply { setTextColor(lime) }); copy.gap(16)
        copy.add(text("Full Body\nFoundation", 29, ink, true)); copy.gap(12)
        copy.add(text("${Catalog.workouts.first().minutes()} MIN  ·  BODYWEIGHT", 11, ink, true)); copy.gap(22)
        copy.addView(action(s(R.string.start_workout)) { selected = "foundation"; navigate("Detail") }, LinearLayout.LayoutParams(dp(175), -2))
        hero.addView(copy, FrameLayout.LayoutParams(-1, -1)); body.add(hero, 280); body.gap(24)
        val sessions = store.sessions(); val count = TrainingStats.weekCount(sessions, LocalDate.now())
        val week = card(); val heading = row(); heading.addView(text(s(R.string.week_activity), 18, ink, true), LinearLayout.LayoutParams(0, -2, 1f)); heading.addView(text(getString(R.string.weekly_progress, count, store.weeklyGoal), 12, lime))
        week.add(heading); week.gap(20)
        val days = row(); val monday = LocalDate.now().minusDays((LocalDate.now().dayOfWeek.value - 1).toLong())
        (0..6).forEach { day ->
            val date = monday.plusDays(day.toLong()); val trained = sessions.any { it.date == date.toString() }
            val cell = column().apply { gravity = Gravity.CENTER }
            cell.addView(text(date.dayOfWeek.getDisplayName(java.time.format.TextStyle.NARROW, Locale.getDefault()), 11, muted))
            cell.gap(9); cell.addView(text(if (trained) "✓" else date.dayOfMonth.toString(), 14, if (trained) bg else ink, true).apply {
                gravity = Gravity.CENTER; background = shape(if (trained) lime else bg, 22, if (date == LocalDate.now()) lime else Color.TRANSPARENT)
                contentDescription = "$date ${if (trained) s(R.string.completed) else ""}"
            }, LinearLayout.LayoutParams(dp(34), dp(34)))
            days.addView(cell, LinearLayout.LayoutParams(0, -2, 1f))
        }; week.add(days); body.add(week); body.gap(18)
        val hydration = card(); hydration.add(label("DAILY RITUAL")); hydration.gap(9); hydration.add(text(s(R.string.water_title), 21, ink, true)); hydration.gap(8)
        hydration.add(text(getString(R.string.water_progress, store.water(), store.waterGoal), 15, muted)); hydration.gap(14)
        hydration.add(progressBar(store.water(), store.waterGoal)); hydration.gap(18)
        hydration.add(action(s(R.string.add_water), false) { store.setWater(store.water() + 250); render() }); body.add(hydration); body.gap(18)
        body.add(action(s(R.string.library), false) { navigate("Library") }); body.gap(18)
        body.add(text(s(R.string.safety_text), 12, muted))
    }
    private fun progressBar(value: Int, max: Int): ProgressBar = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply {
        this.max = max; progress = value; progressTintList = android.content.res.ColorStateList.valueOf(lime)
        progressBackgroundTintList = android.content.res.ColorStateList.valueOf(0xFF3A453A.toInt()); minimumHeight = dp(6)
    }
    private fun workouts() {
        header(); title("BUILT FOR YOUR REAL LIFE", s(R.string.workout_title), s(R.string.workout_subtitle))
        val filters = row(); listOf("All" to R.string.all, "Strength" to R.string.strength, "Cardio" to R.string.cardio, "Mobility" to R.string.mobility).forEach { (key, id) ->
            val chip = text(s(id), 12, if (key == filter) bg else muted, true).apply { gravity = Gravity.CENTER; setPadding(dp(11), dp(12), dp(11), dp(12)); background = shape(if (key == filter) lime else surface, 30); setOnClickListener { filter = key; render() }; isFocusable = true }
            filters.addView(chip, LinearLayout.LayoutParams(0, -2, 1f).apply { marginEnd = dp(5) })
        }; body.add(filters); body.gap(22)
        val cards = Catalog.workouts.filter { filter == "All" || it.category == filter }.map { workoutCard(it) }
        grid(cards)
        body.gap(8); body.add(action(s(R.string.library), false) { navigate("Library") })
    }
    private fun grid(views: List<View>) {
        val wide = resources.configuration.screenWidthDp >= 700
        if (!wide) views.forEach { body.add(it); body.gap(14) }
        else views.chunked(2).forEach { group ->
            val line = row().apply { gravity = Gravity.TOP }
            group.forEach { line.addView(it, LinearLayout.LayoutParams(0, -2, 1f).apply { marginEnd = dp(12) }) }
            if (group.size == 1) line.addView(Space(this), LinearLayout.LayoutParams(0, 1, 1f))
            body.add(line); body.gap(14)
        }
    }
    private fun workoutCard(w: Workout): View = card().apply {
        val top = row(); top.addView(label(w.category.uppercase()).apply { setTextColor(lime) }, LinearLayout.LayoutParams(0, -2, 1f)); top.addView(text("↗", 23, lime)); add(top); gap(12)
        add(text(w.name, 23, ink, true)); gap(9); add(text("${w.minutes()} min  ·  ${w.level}  ·  ${if (w.id == "dumbbell") "Dumbbells" else "No equipment"}", 12, muted)); gap(15)
        add(progressBar(w.exercises.size, 9)); setOnClickListener { selected = w.id; navigate("Detail") }; isFocusable = true; contentDescription = "${w.name}, ${w.minutes()} minutes, ${w.level}"
    }
    private fun detail() {
        back(); val w = workout(); title(w.category.uppercase(), w.name, w.description)
        val meta = card(); meta.add(text("${w.minutes()} MIN  /  ${w.rounds} ROUNDS  /  ${w.level.uppercase()}", 13, lime, true)); meta.gap(10); meta.add(text(s(R.string.rest_between), 13, muted)); body.add(meta); body.gap(22)
        body.add(label(s(R.string.routine))); body.gap(14)
        w.exercises.forEachIndexed { i, id ->
            val e = Catalog.exercises.first { it.id == id }; val item = card(); val line = row()
            line.addView(text(String.format(Locale.US, "%02d", i + 1), 20, lime, true), LinearLayout.LayoutParams(dp(43), -2))
            val copy = column(); copy.add(text(e.name, 16, ink, true)); copy.gap(5); copy.add(text("${e.seconds} sec  ·  ${e.equipment}", 12, muted))
            line.addView(copy, LinearLayout.LayoutParams(0, -2, 1f)); line.addView(text("›", 23, muted)); item.add(line)
            item.setOnClickListener { showExercise(e) }; item.isFocusable = true; body.add(item); body.gap(10)
        }
        body.gap(10); body.add(action(if (store.active == null) s(R.string.start_workout) else s(R.string.resume_session)) {
            if (store.active == null) { AlertDialog.Builder(this).setTitle(s(R.string.safety_title)).setMessage(s(R.string.safety_text)).setNegativeButton(s(R.string.cancel), null).setPositiveButton(s(R.string.start_workout)) { _, _ -> begin(w) }.show() }
            else { restoreSession(); navigate("Session") }
        }); body.gap(12); body.add(text(s(R.string.safety_text), 12, muted))
    }
    private fun workout() = Catalog.workouts.firstOrNull { it.id == selected } ?: Catalog.workouts.first()
    private fun library() {
        back(); title("MOVE WITH CONFIDENCE", s(R.string.library), s(R.string.library_subtitle))
        grid(Catalog.exercises.map { e -> card().apply { add(text(e.name, 19, ink, true)); gap(7); add(text(e.area, 13, muted)); setOnClickListener { selected = e.id; navigate("Exercise") }; isFocusable = true } })
    }
    private fun exerciseDetail() { back(); val e = Catalog.exercises.firstOrNull { it.id == selected } ?: Catalog.exercises.first(); title(e.area.uppercase(), e.name); body.add(movementPanel(e)); body.gap(18); val notes = card(); exerciseNotes(notes, e); body.add(notes) }
    private fun movementView(e: Exercise) = MovementDemoView(this,e).also { demo ->
        demo.id = R.id.movement_demo
        movementDemos.add(demo); demo.setHostActive(demosActive)
    }
    private fun movementPanel(e: Exercise): View = card().apply {
        add(label(s(R.string.movement_demo)).apply { setTextColor(lime) }); gap(12)
        val demo = movementView(e); add(demo,224); gap(12)
        lateinit var control: TextView
        fun controlLabel() = s(if(demo.motionReduced) R.string.next_pose else if(demo.requestedPlaying) R.string.pause_demo else R.string.play_demo)
        control = action(controlLabel(),false) {
            if(demo.motionReduced) demo.nextPose() else demo.setPlayback(!demo.requestedPlaying)
            val value=controlLabel(); control.text=value; control.contentDescription=value
        }
        add(control); gap(12); add(text(s(R.string.movement_note),12,muted))
    }
    private fun exerciseNotes(container: LinearLayout, e: Exercise) {
        container.add(label(s(R.string.form_notes))); container.gap(18)
        e.cues.forEachIndexed { i, cue -> container.add(text("${i + 1}.  $cue", 17, ink)); container.gap(18) }
        container.add(text("${s(R.string.equipment)}: ${e.equipment}", 13, lime)); container.gap(12); container.add(text(s(R.string.safety_text), 12, muted))
    }
    private fun showExercise(e: Exercise) {
        val before=movementDemos.toSet()
        val content=column().apply { setPadding(dp(16),dp(12),dp(16),dp(16)); add(movementPanel(e)); gap(16); e.cues.forEachIndexed { i,cue -> add(text("${i+1}. $cue",15)); gap(14) } }
        val scroll=ScrollView(this).apply { addView(content) }
        AlertDialog.Builder(this).setTitle(e.name).setView(scroll).setPositiveButton(android.R.string.ok,null).create().apply {
            setOnDismissListener { val removed=movementDemos.filter { it !in before }; removed.forEach { it.setHostActive(false) }; movementDemos.removeAll(removed.toSet()) }
        }.show()
    }
    private fun progress() {
        header(); title("YOUR EFFORT, MADE VISIBLE", s(R.string.progress_title), s(R.string.progress_subtitle))
        val sessions = store.sessions(); val stats = row()
        listOf(sessions.size.toString() to R.string.sessions, (sessions.sumOf { it.seconds } / 60).toString() to R.string.minutes, TrainingStats.streak(sessions.mapNotNull { runCatching { LocalDate.parse(it.date) }.getOrNull() }.toSet(), LocalDate.now()).toString() to R.string.streak).forEach { (value, label) ->
            val tile = card().apply { setPadding(dp(10), dp(20), dp(10), dp(20)); gravity = Gravity.CENTER; addView(text(value, 31, lime, true)); gap(9); addView(text(s(label), 11, muted)) }
            stats.addView(tile, LinearLayout.LayoutParams(0, -2, 1f).apply { marginEnd = dp(7) })
        }; body.add(stats); body.gap(24)
        val chart = card(); chart.add(text(s(R.string.week_activity), 20, ink, true)); chart.gap(10); chart.add(text(getString(R.string.weekly_progress, TrainingStats.weekCount(sessions, LocalDate.now()), store.weeklyGoal), 13, muted)); chart.gap(15)
        chart.add(WeekChart(this, sessions), 155); body.add(chart); body.gap(28); body.add(label(s(R.string.history))); body.gap(16)
        if (sessions.isEmpty()) { val empty = card(); empty.add(text(s(R.string.empty_history), 22, ink, true)); empty.gap(10); empty.add(text(s(R.string.empty_subtitle), 14, muted)); empty.gap(20); empty.add(action(s(R.string.explore)) { navigate("Workouts") }); body.add(empty) }
        sessions.take(100).forEach { session -> val item = card(); item.add(text(Catalog.workouts.firstOrNull { it.id == session.workout }?.name ?: session.workout, 19, ink, true)); item.gap(8); item.add(text("${session.date}  ·  ${session.seconds / 60}m ${session.seconds % 60}s", 13, muted)); body.add(item); body.gap(10) }
    }
    private fun profile() {
        header(); title("DESIGNED AROUND YOU", s(R.string.profile_title), s(R.string.profile_subtitle))
        val settings = card(); settings.add(label(s(R.string.weekly_goal))); settings.gap(12)
        val goals = row(); (1..7).forEach { n -> goals.addView(text("$n", 15, if (n == store.weeklyGoal) bg else muted, true).apply { gravity = Gravity.CENTER; minHeight = dp(44); background = shape(if (n == store.weeklyGoal) lime else bg, 12); setOnClickListener { store.weeklyGoal = n; render() }; contentDescription = "Weekly goal $n"; isFocusable = true }, LinearLayout.LayoutParams(0, dp(44), 1f).apply { marginEnd = dp(5) }) }; settings.add(goals); settings.gap(22)
        settings.add(label(s(R.string.experience))); settings.gap(12)
        val levels = arrayOf(s(R.string.beginner), s(R.string.intermediate), s(R.string.advanced))
        settings.add(action(levels[store.level], false) { AlertDialog.Builder(this).setTitle(s(R.string.experience)).setSingleChoiceItems(levels, store.level) { dialog, which -> store.level = which; dialog.dismiss(); render() }.setNegativeButton(s(R.string.cancel), null).show() }); settings.gap(22)
        settings.add(label(s(R.string.water_goal))); settings.gap(12)
        settings.add(action("${store.waterGoal} ml", false) { val values = (500..6000 step 250).map { "$it ml" }.toTypedArray(); AlertDialog.Builder(this).setTitle(s(R.string.water_goal)).setSingleChoiceItems(values, (store.waterGoal - 500) / 250) { dialog, which -> store.waterGoal = 500 + which * 250; dialog.dismiss(); render() }.setNegativeButton(s(R.string.cancel), null).show() }); settings.gap(10); settings.add(text(s(R.string.water_note), 12, muted)); body.add(settings); body.gap(18)
        val privacy = card(); privacy.add(label(s(R.string.offline_note)).apply { setTextColor(lime) }); privacy.gap(12); privacy.add(text(s(R.string.offline_details), 15, muted)); body.add(privacy); body.gap(18)
        body.add(action(s(R.string.privacy), false) { AlertDialog.Builder(this).setTitle(s(R.string.privacy)).setMessage(s(R.string.privacy_in_app)).setPositiveButton(android.R.string.ok, null).setNeutralButton(s(R.string.view_web_policy)) { _, _ -> openUrl("https://seandyadryan.github.io/coach_fitness_pro/") }.show() }); body.gap(10)
        body.add(action(s(R.string.support), false) { try { startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:seandyadryan2@gmail.com?subject=Coach%20Fitness%20PRO%20support"))) } catch (_: Exception) { Toast.makeText(this, s(R.string.no_email), Toast.LENGTH_LONG).show() } }); body.gap(10)
        body.add(action(s(R.string.reset_water), false) { store.setWater(0); render() }); body.gap(10)
        body.add(action(s(R.string.delete_data), false) { AlertDialog.Builder(this).setTitle(s(R.string.delete_data)).setMessage(s(R.string.delete_message)).setNegativeButton(s(R.string.cancel), null).setPositiveButton(s(R.string.delete)) { _, _ -> store.clear(); running = false; render() }.show() }); body.gap(22)
        body.add(text("${s(R.string.app_name)} · 1.1.0\n${s(R.string.developer)}", 12, muted)); body.gap(15); body.add(text(s(R.string.safety_text), 12, muted))
    }
    private fun openUrl(url: String) { try { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) } catch (_: Exception) { Toast.makeText(this, s(R.string.no_browser), Toast.LENGTH_LONG).show() } }
    private fun begin(w: Workout) {
        selected = w.id; sessionId = UUID.randomUUID().toString(); index = 0; rest = false; totalMs = 0; remaining = w.movements().first().seconds * 1000L; running = false
        saveSession(); navigate("Session"); running = true; lastTick = SystemClock.elapsedRealtime(); updatePauseButton()
    }
    private fun restoreSession() {
        store.active?.let { saved ->
            selected = saved.optString("workout", "foundation"); sessionId = saved.optString("id", UUID.randomUUID().toString())
            index = saved.optInt("index", 0).coerceIn(0, workout().movements().lastIndex); rest = saved.optBoolean("rest", false)
            remaining = saved.optLong("remaining", 30000L).coerceAtLeast(1); totalMs = saved.optLong("total", 0).coerceAtLeast(0); running = false
        }
    }
    private fun saveSession() { if (sessionId.isNotEmpty()) store.active = JSONObject().put("id", sessionId).put("workout", selected).put("index", index).put("rest", rest).put("remaining", remaining.coerceAtLeast(1)).put("total", totalMs) }
    private fun pause() {
        if (screen != "Session") return
        if (running) { val delta = (SystemClock.elapsedRealtime() - lastTick).coerceAtLeast(0); remaining = (remaining - delta).coerceAtLeast(1); totalMs += delta; running = false }
        saveSession(); updatePauseButton()
    }
    private fun updatePauseButton() {
        val title = s(if (running) R.string.pause else R.string.resume)
        pauseButton?.apply { text = title; contentDescription = title }
        sessionDemo?.setPlayback(running)
    }
    private fun refreshClock() {
        val secs = (remaining.coerceAtLeast(0) + 999) / 1000
        timerText?.text = String.format(Locale.US, "%02d:%02d", secs / 60, secs % 60)
        val maximum = if (rest) 30000 else workout().movements()[index].seconds * 1000
        timerBar?.progress = (maximum - remaining.toInt()).coerceIn(0, maximum)
    }
    private fun advance() {
        val wasRunning = running
        if (!rest && index == workout().movements().lastIndex) { finishSession(); return }
        if (rest) { index++; rest = false; remaining = workout().movements()[index].seconds * 1000L } else { rest = true; remaining = 30000L }
        saveSession(); render(); running = wasRunning; lastTick = SystemClock.elapsedRealtime(); updatePauseButton()
    }
    private fun session() {
        val w = workout(); val exercise = w.movements()[index]
        body.add(text(w.name.uppercase(), 12, lime, true)); body.gap(25)
        title(getString(R.string.session_counter, index + 1, w.movements().size), if (rest) s(R.string.rest) else exercise.name, if (rest) s(R.string.rest_hint) else exercise.area)
        val timer = card().apply { gravity = Gravity.CENTER_HORIZONTAL; setPadding(dp(24), dp(30), dp(24), dp(30)) }
        timer.addView(text(if (rest) "RECOVER" else "MOVE AT YOUR PACE", 11, lime, true).apply { letterSpacing = .12f })
        if(!rest) { timer.gap(12); sessionDemo=movementView(exercise).apply { setPlayback(running) }; timer.add(sessionDemo!!,156) }
        timer.gap(12); timerText = text("", 56, ink, true).apply { gravity = Gravity.CENTER; typeface = Typeface.create("sans-serif-light", Typeface.NORMAL) }; timer.add(timerText!!); timer.gap(12)
        timerBar = progressBar(0, if (rest) 30000 else exercise.seconds * 1000); timer.add(timerBar!!); timer.gap(18)
        pauseButton = action(s(if (running) R.string.pause else R.string.resume)) { if (running) pause() else { running = true; lastTick = SystemClock.elapsedRealtime(); updatePauseButton() } }; timer.add(pauseButton!!); timer.gap(12)
        timer.add(action(s(if (rest) R.string.skip_rest else if (index == w.movements().lastIndex) R.string.finish else R.string.next), false) { if (running) { val delta = SystemClock.elapsedRealtime() - lastTick; totalMs += delta; lastTick = SystemClock.elapsedRealtime() }; advance() }); body.add(timer); body.gap(20)
        if (!rest) { val notes = card(); notes.add(label(s(R.string.form_notes))); notes.gap(14); exercise.cues.forEach { notes.add(text("•  $it", 15, ink)); notes.gap(12) }; body.add(notes); body.gap(15) }
        body.add(action(s(R.string.end_session), false) { pause(); showDiscard() }); refreshClock()
    }
    private fun showDiscard() { AlertDialog.Builder(this).setTitle(s(R.string.end_session)).setMessage(s(R.string.abandon_message)).setNegativeButton(s(R.string.resume)) { _, _ -> if (screen == "Session") { running = true; lastTick = SystemClock.elapsedRealtime(); updatePauseButton() } }.setPositiveButton(s(R.string.end_session)) { _, _ -> running = false; store.active = null; sessionId = ""; navigate("Today") }.show() }
    private fun finishSession() {
        running = false
        store.add(Session(sessionId, selected, LocalDate.now().toString(), (totalMs / 1000).toInt().coerceAtLeast(1)))
        screen = "Complete"; render(); sessionId = ""
    }
    private fun complete() {
        body.gap(35); body.add(text("✓", 80, lime, true).apply { gravity = Gravity.CENTER }); body.gap(24)
        title(s(R.string.session_saved).uppercase(), s(R.string.complete_title), s(R.string.complete_subtitle))
        val summary = card(); summary.add(text(workout().name, 24, ink, true)); summary.gap(10); summary.add(text("${totalMs / 60000}m ${(totalMs / 1000) % 60}s  ·  ${LocalDate.now().format(DateTimeFormatter.ofPattern("d MMM yyyy"))}", 15, muted)); body.add(summary); body.gap(24)
        body.add(action(s(R.string.done)) { navigate("Today") }); body.gap(12); body.add(action(s(R.string.progress), false) { navigate("Progress") })
    }
    private inner class Glyph(context: android.content.Context, private val kind: Int, private val color: Int) : View(context) {
        override fun onDraw(canvas: Canvas) {
            val p = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = this@Glyph.color; style = Paint.Style.STROKE; strokeWidth = 1.8f; strokeCap = Paint.Cap.ROUND; strokeJoin = Paint.Join.ROUND }
            canvas.save(); canvas.scale(width / 24f, height / 24f)
            when (kind) {
                0 -> { val path = Path(); path.moveTo(3f, 11f); path.lineTo(12f, 3f); path.lineTo(21f, 11f); path.moveTo(6f, 10f); path.lineTo(6f, 21f); path.lineTo(18f, 21f); path.lineTo(18f, 10f); canvas.drawPath(path, p) }
                1 -> { canvas.drawLine(3f, 12f, 21f, 12f, p); canvas.drawLine(6f, 6f, 6f, 18f, p); canvas.drawLine(18f, 6f, 18f, 18f, p); canvas.drawLine(3f, 9f, 3f, 15f, p); canvas.drawLine(21f, 9f, 21f, 15f, p) }
                2 -> { canvas.drawLine(4f, 20f, 21f, 20f, p); canvas.drawLine(6f, 16f, 6f, 11f, p); canvas.drawLine(12f, 16f, 12f, 7f, p); canvas.drawLine(18f, 16f, 18f, 3f, p) }
                3 -> { canvas.drawCircle(12f, 7f, 4f, p); canvas.drawArc(4f, 14f, 20f, 27f, 180f, 180f, false, p) }
            }; canvas.restore()
        }
    }
    private inner class WeekChart(context: android.content.Context, private val sessions: List<Session>) : View(context) {
        override fun onDraw(canvas: Canvas) {
            val p = Paint(Paint.ANTI_ALIAS_FLAG); val today = LocalDate.now(); val monday = today.minusDays((today.dayOfWeek.value - 1).toLong())
            val values = (0..6).map { d -> sessions.filter { it.date == monday.plusDays(d.toLong()).toString() }.sumOf { it.seconds } / 60f }
            val max = max(20f, values.maxOrNull() ?: 0f); val col = width / 7f; val baseline = height - dp(25).toFloat()
            (0..6).forEach { i ->
                val left = col * i + col * .22f; val right = col * i + col * .78f
                p.color = 0xFF303B30.toInt(); canvas.drawRoundRect(left, dp(12).toFloat(), right, baseline, dp(5).toFloat(), dp(5).toFloat(), p)
                if (values[i] > 0) { p.color = lime; canvas.drawRoundRect(left, baseline - (baseline - dp(12)) * values[i] / max, right, baseline, dp(5).toFloat(), dp(5).toFloat(), p) }
                p.color = muted; p.textSize = dp(11).toFloat(); p.textAlign = Paint.Align.CENTER
                canvas.drawText(monday.plusDays(i.toLong()).dayOfWeek.getDisplayName(java.time.format.TextStyle.SHORT, Locale.getDefault()).take(2), col * (i + .5f), height - dp(5).toFloat(), p)
            }
            contentDescription = values.mapIndexed { i, v -> "${monday.plusDays(i.toLong())}: ${v.toInt()} minutes" }.joinToString(", ")
        }
    }
}
