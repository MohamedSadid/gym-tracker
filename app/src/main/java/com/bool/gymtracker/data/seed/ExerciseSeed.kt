package com.bool.gymtracker.data.seed

import com.bool.gymtracker.data.local.ExerciseEntity
import com.bool.gymtracker.domain.MuscleGroup

object ExerciseSeed {
    fun builtIn(): List<ExerciseEntity> = listOf(
        "Barbell bench press" to MuscleGroup.PUSH,
        "Incline dumbbell press" to MuscleGroup.PUSH,
        "Overhead press" to MuscleGroup.PUSH,
        "Dumbbell shoulder press" to MuscleGroup.PUSH,
        "Dips" to MuscleGroup.PUSH,
        "Push-up" to MuscleGroup.PUSH,
        "Chest fly" to MuscleGroup.PUSH,
        "Lateral raise" to MuscleGroup.PUSH,
        "Tricep pushdown" to MuscleGroup.PUSH,
        "Skull crusher" to MuscleGroup.PUSH,
        "Close-grip bench press" to MuscleGroup.PUSH,
        "Front raise" to MuscleGroup.PUSH,
        "Barbell row" to MuscleGroup.PULL,
        "Lat pulldown" to MuscleGroup.PULL,
        "Pull-up" to MuscleGroup.PULL,
        "Seated cable row" to MuscleGroup.PULL,
        "Dumbbell row" to MuscleGroup.PULL,
        "Face pull" to MuscleGroup.PULL,
        "Barbell curl" to MuscleGroup.PULL,
        "Dumbbell curl" to MuscleGroup.PULL,
        "Hammer curl" to MuscleGroup.PULL,
        "Rear delt fly" to MuscleGroup.PULL,
        "Chin-up" to MuscleGroup.PULL,
        "Deadlift" to MuscleGroup.LEGS,
        "Back squat" to MuscleGroup.LEGS,
        "Front squat" to MuscleGroup.LEGS,
        "Romanian deadlift" to MuscleGroup.LEGS,
        "Leg press" to MuscleGroup.LEGS,
        "Walking lunge" to MuscleGroup.LEGS,
        "Bulgarian split squat" to MuscleGroup.LEGS,
        "Leg extension" to MuscleGroup.LEGS,
        "Lying leg curl" to MuscleGroup.LEGS,
        "Hip thrust" to MuscleGroup.LEGS,
        "Calf raise" to MuscleGroup.LEGS,
        "Goblet squat" to MuscleGroup.LEGS,
        "Plank" to MuscleGroup.CORE,
        "Hanging leg raise" to MuscleGroup.CORE,
        "Cable crunch" to MuscleGroup.CORE,
        "Ab wheel" to MuscleGroup.CORE,
        "Pallof press" to MuscleGroup.CORE,
        "Farmer carry" to MuscleGroup.OTHER,
        "Shrug" to MuscleGroup.OTHER,
    ).map { (name, group) ->
        ExerciseEntity(name = name, muscleGroup = group.name, isCustom = false)
    }
}
