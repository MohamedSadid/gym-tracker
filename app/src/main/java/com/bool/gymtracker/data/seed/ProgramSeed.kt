package com.bool.gymtracker.data.seed

import com.bool.gymtracker.data.local.AppDatabase
import com.bool.gymtracker.data.local.WorkoutEntity
import com.bool.gymtracker.data.local.WorkoutExerciseEntity
import com.bool.gymtracker.domain.BuiltInProgram

object ProgramSeed {
    val programs: List<BuiltInProgram> = listOf(
        BuiltInProgram(
            key = "full_body",
            title = "Full Body program",
            blurb = "3 days per week. Hit each muscle 2–3 times with compounds.",
        ),
        BuiltInProgram(
            key = "ppl",
            title = "Push Pull Leg program",
            blurb = "6 days (or 3). Push, pull, then legs, then repeat.",
        ),
        BuiltInProgram(
            key = "upper_lower",
            title = "Upper Lower program",
            blurb = "4 days. Alternate upper and lower with A/B variation.",
        ),
        BuiltInProgram(
            key = "pro_split",
            title = "Pro Split program",
            blurb = "5-day body-part split built around compounds.",
        ),
    )

    fun definition(key: String): BuiltInProgram? = programs.find { it.key == key }

    data class Day(val name: String, val exercises: List<String>)

    fun days(key: String): List<Day> = when (key) {
        "full_body" -> listOf(
            Day(
                "Full Body — Day A",
                listOf(
                    "Back squat",
                    "Barbell bench press",
                    "Barbell row",
                    "Romanian deadlift",
                    "Overhead press",
                    "Plank",
                ),
            ),
            Day(
                "Full Body — Day B",
                listOf(
                    "Deadlift",
                    "Incline dumbbell press",
                    "Pull-up",
                    "Walking lunge",
                    "Lateral raise",
                    "Hanging leg raise",
                ),
            ),
            Day(
                "Full Body — Day C",
                listOf(
                    "Front squat",
                    "Dips",
                    "Seated cable row",
                    "Hip thrust",
                    "Face pull",
                    "Dumbbell curl",
                ),
            ),
        )
        "ppl" -> listOf(
            Day(
                "Push Pull Leg — Push",
                listOf(
                    "Barbell bench press",
                    "Overhead press",
                    "Incline dumbbell press",
                    "Lateral raise",
                    "Tricep pushdown",
                    "Skull crusher",
                ),
            ),
            Day(
                "Push Pull Leg — Pull",
                listOf(
                    "Deadlift",
                    "Pull-up",
                    "Barbell row",
                    "Face pull",
                    "Barbell curl",
                    "Hammer curl",
                ),
            ),
            Day(
                "Push Pull Leg — Legs",
                listOf(
                    "Back squat",
                    "Romanian deadlift",
                    "Leg press",
                    "Lying leg curl",
                    "Calf raise",
                    "Hanging leg raise",
                ),
            ),
        )
        "upper_lower" -> listOf(
            Day(
                "Upper Lower — Upper A",
                listOf(
                    "Barbell bench press",
                    "Barbell row",
                    "Overhead press",
                    "Lat pulldown",
                    "Lateral raise",
                    "Barbell curl",
                    "Tricep pushdown",
                ),
            ),
            Day(
                "Upper Lower — Lower A",
                listOf(
                    "Back squat",
                    "Romanian deadlift",
                    "Leg press",
                    "Lying leg curl",
                    "Calf raise",
                    "Plank",
                ),
            ),
            Day(
                "Upper Lower — Upper B",
                listOf(
                    "Incline dumbbell press",
                    "Seated cable row",
                    "Dips",
                    "Face pull",
                    "Rear delt fly",
                    "Hammer curl",
                    "Skull crusher",
                ),
            ),
            Day(
                "Upper Lower — Lower B",
                listOf(
                    "Deadlift",
                    "Front squat",
                    "Walking lunge",
                    "Hip thrust",
                    "Calf raise",
                    "Hanging leg raise",
                ),
            ),
        )
        "pro_split" -> listOf(
            Day(
                "Pro Split — Chest",
                listOf(
                    "Barbell bench press",
                    "Incline dumbbell press",
                    "Chest press machine",
                    "Pec deck fly",
                    "Dips",
                    "Push-up",
                ),
            ),
            Day(
                "Pro Split — Back",
                listOf(
                    "Deadlift",
                    "Pull-up",
                    "Barbell row",
                    "Lat pulldown",
                    "Face pull",
                ),
            ),
            Day(
                "Pro Split — Shoulders",
                listOf(
                    "Overhead press",
                    "Lateral raise",
                    "Rear delt fly",
                    "Front raise",
                    "Face pull",
                ),
            ),
            Day(
                "Pro Split — Arms",
                listOf(
                    "Barbell curl",
                    "Hammer curl",
                    "Close-grip bench press",
                    "Tricep pushdown",
                    "Skull crusher",
                ),
            ),
            Day(
                "Pro Split — Legs",
                listOf(
                    "Back squat",
                    "Romanian deadlift",
                    "Leg press",
                    "Lying leg curl",
                    "Calf raise",
                    "Hanging leg raise",
                ),
            ),
        )
        else -> emptyList()
    }

    suspend fun insertBuiltIns(db: AppDatabase) {
        val all = db.exerciseDao().listAll().associate { it.name to it.id }
        val now = System.currentTimeMillis()
        programs.forEach { program ->
            days(program.key).forEachIndexed { index, day ->
                val workoutId = db.workoutDao().insert(
                    WorkoutEntity(
                        name = day.name,
                        createdAt = now,
                        isBuiltIn = true,
                        programKey = program.key,
                        sortIndex = index,
                    ),
                )
                day.exercises.forEachIndexed { position, exerciseName ->
                    val exerciseId = all[exerciseName] ?: return@forEachIndexed
                    db.workoutExerciseDao().insert(
                        WorkoutExerciseEntity(
                            workoutId = workoutId,
                            exerciseId = exerciseId,
                            position = position,
                        ),
                    )
                }
            }
        }
    }

    suspend fun syncProSplitChest(db: AppDatabase) {
        val chest = days("pro_split").find { it.name == "Pro Split — Chest" } ?: return
        val workout = db.workoutDao().forProgram("pro_split").find { it.name == chest.name } ?: return
        val all = db.exerciseDao().listAll().associate { it.name to it.id }
        db.workoutExerciseDao().deleteForWorkout(workout.id)
        chest.exercises.forEachIndexed { position, exerciseName ->
            val exerciseId = all[exerciseName] ?: return@forEachIndexed
            db.workoutExerciseDao().insert(
                WorkoutExerciseEntity(
                    workoutId = workout.id,
                    exerciseId = exerciseId,
                    position = position,
                ),
            )
        }
    }
}
