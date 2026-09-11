package com.bool.gymtracker.domain

data class ExerciseTargets(
    val primary: List<String>,
    val auxiliary: List<String>,
)

object ExerciseTargetMuscles {
    private val byName = mapOf(
        "Barbell bench press" to targets("Chest, front delts", "Triceps"),
        "Incline dumbbell press" to targets("Chest, front delts", "Triceps"),
        "Dumbbell bench press" to targets("Chest, front delts", "Triceps"),
        "Chest press machine" to targets("Chest", "Front delts, triceps"),
        "Dips" to targets("Chest, triceps", "Front delts"),
        "Push-up" to targets("Chest", "Front delts, triceps"),
        "Chest fly" to targets("Chest", "Front delts"),
        "Cable fly" to targets("Chest", "Front delts"),
        "Barbell row" to targets("Lats, mid-back", "Biceps, rear delts"),
        "Lat pulldown" to targets("Lats", "Biceps"),
        "Neutral-grip pulldown" to targets("Lats", "Biceps"),
        "Pull-up" to targets("Lats", "Biceps"),
        "Seated cable row" to targets("Mid-back, lats", "Biceps"),
        "Dumbbell row" to targets("Lats, mid-back", "Biceps"),
        "Chin-up" to targets("Lats, biceps", "Upper back"),
        "Back extension" to targets("Spinal erectors", "Glutes, hamstrings"),
        "Overhead press" to targets("Front delts", "Triceps, upper chest"),
        "Dumbbell shoulder press" to targets("Front delts", "Triceps"),
        "Lateral raise" to targets("Lateral delts", "Traps"),
        "Cable lateral raise" to targets("Lateral delts", "Traps"),
        "Front raise" to targets("Front delts", "Upper chest"),
        "Face pull" to targets("Rear delts", "Traps, rotator cuff"),
        "Rear delt fly" to targets("Rear delts", "Upper back"),
        "Machine rear delt" to targets("Rear delts", "Upper back"),
        "Tricep pushdown" to targets("Triceps", "Forearms"),
        "Rope tricep pushdown" to targets("Triceps", "Forearms"),
        "Skull crusher" to targets("Triceps", "Front delts"),
        "Close-grip bench press" to targets("Triceps", "Chest, front delts"),
        "Overhead tricep extension" to targets("Triceps", "Shoulders"),
        "Bench dip" to targets("Triceps", "Chest, front delts"),
        "Barbell curl" to targets("Biceps", "Forearms"),
        "Dumbbell curl" to targets("Biceps", "Forearms"),
        "Incline curl" to targets("Biceps", "Forearms"),
        "Cable curl" to targets("Biceps", "Forearms"),
        "Preacher curl" to targets("Biceps", "Forearms"),
        "Hammer curl" to targets("Brachialis, forearms", "Biceps"),
        "Wrist curl" to targets("Forearm flexors", "Grip"),
        "Reverse curl" to targets("Forearm extensors", "Brachialis"),
        "Wrist extension" to targets("Forearm extensors", "Grip"),
        "Deadlift" to targets("Hamstrings, glutes", "Back, quads"),
        "Romanian deadlift" to targets("Hamstrings, glutes", "Back"),
        "Stiff-leg deadlift" to targets("Hamstrings", "Glutes, back"),
        "Back squat" to targets("Quads, glutes", "Hamstrings, back"),
        "Front squat" to targets("Quads", "Glutes, upper back"),
        "Goblet squat" to targets("Quads, glutes", "Core"),
        "Hack squat" to targets("Quads", "Glutes"),
        "Leg press" to targets("Quads", "Glutes, hamstrings"),
        "Walking lunge" to targets("Quads, glutes", "Hamstrings"),
        "Bulgarian split squat" to targets("Quads, glutes", "Hamstrings"),
        "Leg extension" to targets("Quads", "—"),
        "Lying leg curl" to targets("Hamstrings", "Calves"),
        "Seated leg curl" to targets("Hamstrings", "Calves"),
        "Hip thrust" to targets("Glutes", "Hamstrings"),
        "Hip abduction machine" to targets("Glute medius", "Hip"),
        "Calf raise" to targets("Calves", "—"),
        "Seated calf raise" to targets("Soleus", "Calves"),
        "Standing calf raise" to targets("Calves", "—"),
        "Plank" to targets("Abs, core", "Shoulders"),
        "Side plank" to targets("Obliques", "Core"),
        "Hanging leg raise" to targets("Abs, hip flexors", "Forearms"),
        "Cable crunch" to targets("Abs", "Obliques"),
        "Crunch" to targets("Abs", "—"),
        "Ab wheel" to targets("Abs, core", "Lats, shoulders"),
        "Pallof press" to targets("Core, obliques", "Shoulders"),
        "Dead bug" to targets("Abs, core", "Hip flexors"),
        "Farmer carry" to targets("Grip, traps", "Core, legs"),
        "Shrug" to targets("Traps", "Forearms"),
    )

    fun forName(name: String, muscleGroup: MuscleGroup): ExerciseTargets =
        byName[name] ?: ExerciseTargets(listOf(muscleGroup.label), emptyList())

    fun listedNames(): Set<String> = byName.keys

    private fun targets(primary: String, auxiliary: String) = ExerciseTargets(
        primary = listOf(primary).filter { it != "—" },
        auxiliary = listOf(auxiliary).filter { it != "—" },
    )
}
