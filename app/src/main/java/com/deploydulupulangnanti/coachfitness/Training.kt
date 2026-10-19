package com.deploydulupulangnanti.coachfitness

import java.time.LocalDate

data class Exercise(val id: String, val name: String, val area: String, val seconds: Int, val cues: List<String>, val equipment: String = "Bodyweight")
data class Workout(val id: String, val name: String, val category: String, val level: String, val description: String, val exercises: List<String>, val rounds: Int = 2) {
    fun movements() = (1..rounds).flatMap { exercises.map { id -> Catalog.exercises.first { it.id == id } } }
    fun minutes() = (movements().sumOf { it.seconds + 30 } - 30 + 59) / 60
}
data class Session(val id: String, val workout: String, val date: String, val seconds: Int)

object TrainingStats {
    fun streak(dates: Set<LocalDate>, today: LocalDate): Int {
        var cursor = if (today in dates) today else today.minusDays(1)
        var count = 0
        while (cursor in dates) { count++; cursor = cursor.minusDays(1) }
        return count
    }
    fun weekCount(sessions: List<Session>, today: LocalDate): Int {
        val monday = today.minusDays((today.dayOfWeek.value - 1).toLong())
        return sessions.count { runCatching { LocalDate.parse(it.date) in monday..today }.getOrDefault(false) }
    }
}

object Catalog {
    val exercises = listOf(
        Exercise("march", "March in place", "Warm-up · Full body", 45, listOf("Stand tall with relaxed shoulders.", "Lift alternating knees to a comfortable height.", "Swing your arms gently and breathe evenly.")),
        Exercise("squat", "Bodyweight squat", "Strength · Lower body", 40, listOf("Place feet about shoulder-width apart.", "Send your hips back and bend your knees comfortably.", "Keep your knees tracking over your toes; stand tall.")),
        Exercise("wall", "Wall push-up", "Strength · Upper body", 40, listOf("Place palms on a wall at chest height.", "Step back and keep your body in a straight line.", "Bend your elbows slowly, then push away from the wall.")),
        Exercise("pushup", "Push-up", "Strength · Upper body", 35, listOf("Place hands slightly wider than shoulders.", "Brace your middle and lower your chest with control.", "Press up without locking your elbows; use knees if needed.")),
        Exercise("bridge", "Glute bridge", "Strength · Glutes", 40, listOf("Lie on your back with knees bent, feet flat.", "Press through your heels and lift your hips.", "Keep ribs down; lower slowly without arching your back.")),
        Exercise("bird", "Bird dog", "Strength · Core", 40, listOf("Start on hands and knees with your spine neutral.", "Extend the opposite arm and leg without twisting.", "Return slowly and alternate sides.")),
        Exercise("plank", "Forearm plank", "Strength · Core", 30, listOf("Place elbows beneath shoulders and extend your legs.", "Brace your core, keeping hips in line with shoulders.", "Breathe steadily; lower knees to make it easier.")),
        Exercise("lunge", "Reverse lunge", "Strength · Lower body", 40, listOf("Stand tall with feet hip-width apart.", "Step back and bend both knees within a comfortable range.", "Press through your front foot to return; alternate sides.")),
        Exercise("calf", "Calf raise", "Strength · Lower legs", 40, listOf("Stand near a wall for balance.", "Lift your heels slowly and pause at the top.", "Lower with control; keep ankles aligned.")),
        Exercise("deadbug", "Dead bug", "Strength · Core", 40, listOf("Lie on your back, arms up, knees bent at 90 degrees.", "Slowly lower one heel and the opposite arm.", "Keep your back comfortable against the floor; alternate.")),
        Exercise("step", "Side step", "Cardio · Full body", 45, listOf("Stand with soft knees and feet together.", "Step from side to side and move arms naturally.", "Keep the pace comfortable; avoid bouncing your joints.")),
        Exercise("knees", "Standing knee drive", "Cardio · Full body", 40, listOf("Stand tall and brace your middle.", "Bring one knee toward your opposite hand.", "Alternate sides at a pace you can maintain.")),
        Exercise("jack", "Step jack", "Cardio · Full body", 40, listOf("Step one foot out while raising both arms.", "Return to the center and repeat on the other side.", "Keep this low impact; stay within a pain-free range.")),
        Exercise("box", "Shadow boxing", "Cardio · Upper body", 40, listOf("Keep knees soft and hands near your chin.", "Punch forward gently, alternating hands.", "Rotate through the torso without locking elbows.")),
        Exercise("cat", "Cat-cow", "Mobility · Spine", 45, listOf("Start on hands and knees.", "Inhale and gently lengthen through your chest.", "Exhale and round your spine slowly; do not force range.")),
        Exercise("child", "Child’s pose", "Mobility · Back", 40, listOf("Kneel on a comfortable surface.", "Sit your hips toward your heels and reach arms forward.", "Breathe gently; use a cushion or reduce the bend as needed.")),
        Exercise("rotation", "Standing trunk rotation", "Mobility · Torso", 40, listOf("Stand with feet hip-width apart and soft knees.", "Rotate your torso gently to one side, then the other.", "Keep the movement slow and your hips comfortable.")),
        Exercise("shoulder", "Shoulder circles", "Mobility · Shoulders", 40, listOf("Stand tall and relax your neck.", "Make small controlled circles with your shoulders.", "Reverse direction halfway; avoid shrugging forcefully.")),
        Exercise("curl", "Dumbbell curl", "Strength · Arms", 40, listOf("Choose a light weight you can control.", "Keep elbows near your sides and curl without swinging.", "Lower slowly; keep wrists neutral."), "Dumbbells"),
        Exercise("row", "Dumbbell bent-over row", "Strength · Back", 40, listOf("Hinge at your hips with a neutral back and soft knees.", "Pull light dumbbells toward your hips, elbows close.", "Lower slowly and avoid rounding your back."), "Dumbbells"),
        Exercise("goblet", "Goblet squat", "Strength · Lower body", 40, listOf("Hold one light dumbbell close to your chest.", "Sit back into a comfortable squat, heels on the floor.", "Stand smoothly; reduce depth if you lose control."), "Dumbbells")
    )
    val workouts = listOf(
        Workout("foundation", "Full Body Foundation", "Strength", "Beginner", "Build a steady base with simple, controlled movements. No equipment needed, just a little space and a fresh start.", listOf("march", "squat", "wall", "bridge", "bird", "calf")),
        Workout("core", "Core & Control", "Strength", "Beginner", "Slow down and connect with your core. Focus on control and steady breathing throughout.", listOf("march", "deadbug", "bird", "plank", "bridge")),
        Workout("energy", "Everyday Energy", "Cardio", "Beginner", "A low-impact way to get moving. Find a pace that lets you stay in control.", listOf("march", "step", "knees", "jack", "box", "step")),
        Workout("mobility", "Reset & Recharge", "Mobility", "Beginner", "Make room for movement with gentle mobility. Keep every stretch comfortable and unforced.", listOf("march", "shoulder", "rotation", "cat", "child")),
        Workout("dumbbell", "Dumbbell Essentials", "Strength", "Intermediate", "A focused session for your home gym. Choose light weights and make every repetition deliberate.", listOf("march", "goblet", "row", "curl", "bridge", "plank")),
        Workout("lower", "Lower Body Builder", "Strength", "Intermediate", "Train your legs and glutes with a balanced bodyweight session. Quality comes before speed.", listOf("march", "squat", "lunge", "bridge", "calf", "squat")),
        Workout("upper", "Upper Body Focus", "Strength", "Intermediate", "Give your upper body and core a purposeful session, with plenty of space to move at your own pace.", listOf("shoulder", "pushup", "bird", "plank", "wall")),
        Workout("quick", "The 6-Minute Reset", "Mobility", "Beginner", "A short pause for your body. Ease into your day or unwind after sitting with gentle movement.", listOf("march", "shoulder", "rotation", "cat", "child"), 1)
    )
}
