package com.deploydulupulangnanti.coachfitness

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate

class LocalStore(context: Context) {
    private val prefs = context.getSharedPreferences("coach_fitness", Context.MODE_PRIVATE)
    var weeklyGoal: Int
        get() = prefs.getInt("weekly_goal", 3)
        set(v) { prefs.edit().putInt("weekly_goal", v.coerceIn(1, 7)).apply() }
    var waterGoal: Int
        get() = prefs.getInt("water_goal", 2000)
        set(v) { prefs.edit().putInt("water_goal", v.coerceIn(500, 6000)).apply() }
    var level: Int
        get() = prefs.getInt("level", 0)
        set(v) { prefs.edit().putInt("level", v.coerceIn(0, 2)).apply() }
    fun water(): Int = if (prefs.getString("water_date", "") == LocalDate.now().toString()) prefs.getInt("water", 0) else 0
    fun setWater(ml: Int) { prefs.edit().putString("water_date", LocalDate.now().toString()).putInt("water", ml.coerceIn(0, 20000)).apply() }
    fun sessions(): List<Session> = runCatching {
        val rows = JSONArray(prefs.getString("sessions", "[]"))
        (0 until rows.length()).map { i -> rows.getJSONObject(i).let { Session(it.getString("id"), it.getString("workout"), it.getString("date"), it.getInt("seconds")) } }
    }.getOrDefault(emptyList())
    fun add(session: Session) {
        if (sessions().any { it.id == session.id }) return
        val rows = JSONArray()
        (listOf(session) + sessions()).forEach { rows.put(JSONObject().put("id", it.id).put("workout", it.workout).put("date", it.date).put("seconds", it.seconds)) }
        prefs.edit().putString("sessions", rows.toString()).remove("active").commit()
    }
    var active: JSONObject?
        get() = runCatching { prefs.getString("active", null)?.let { JSONObject(it) } }.getOrNull()
        set(value) { prefs.edit().apply { if (value == null) remove("active") else putString("active", value.toString()) }.commit() }
    fun clear() { prefs.edit().clear().commit() }
}
