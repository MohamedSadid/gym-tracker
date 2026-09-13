package com.bool.gymtracker.domain

data class ExerciseDemo(
    val position1Asset: String,
    val position2Asset: String,
    val tips: List<String>,
)

object ExerciseDemos {
    private val byName = mapOf(
        "Barbell bench press" to ExerciseDemo(
            position1Asset = "exercises/barbell_bench_press_position_1.png",
            position2Asset = "exercises/barbell_bench_press_position_2.png",
            tips = listOf(
                "Lie on a flat bench, feet on the floor, bar over mid-chest.",
                "Lower the bar to the chest with wrists stacked over the elbows.",
                "Press until the elbows are straight; do not bounce the bar off the chest.",
            ),
        ),
        "Dumbbell bench press" to ExerciseDemo(
            position1Asset = "exercises/dumbbell_bench_press_position_1.png",
            position2Asset = "exercises/dumbbell_bench_press_position_2.png",
            tips = listOf(
                "Lie on a flat bench, feet on the floor, dumbbells over the chest.",
                "Lower until the bells are beside the chest, elbows about 45° from the torso.",
                "Press until the elbows are straight; do not bang the dumbbells together.",
            ),
        ),
        "Smith bench press" to ExerciseDemo(
            position1Asset = "exercises/smith_bench_press_position_1.png",
            position2Asset = "exercises/smith_bench_press_position_2.png",
            tips = listOf(
                "Lie on a flat bench inside the Smith machine, bar over mid-chest.",
                "Unrack, then lower the guided bar until it touches the chest, elbows about 45°.",
                "Press along the rails until the elbows are straight; do not bounce the bar.",
            ),
        ),
        "Incline barbell press" to ExerciseDemo(
            position1Asset = "exercises/incline_barbell_press_position_1.png",
            position2Asset = "exercises/incline_barbell_press_position_2.png",
            tips = listOf(
                "Set a moderate incline. Bar starts over the upper chest.",
                "Lower the bar to the chest with elbows about 45° from the torso.",
                "Press until the elbows are fully straight; do not bounce the bar.",
            ),
        ),
        "Incline Smith bench press" to ExerciseDemo(
            position1Asset = "exercises/incline_smith_bench_press_position_1.png",
            position2Asset = "exercises/incline_smith_bench_press_position_2.png",
            tips = listOf(
                "Set a moderate incline inside the Smith machine, bar over mid-chest.",
                "Unrack, then lower the guided bar until it touches mid-chest, elbows about 45°.",
                "Press along the rails until the elbows are straight; do not bounce the bar.",
            ),
        ),
        "Incline dumbbell press" to ExerciseDemo(
            position1Asset = "exercises/incline_dumbbell_press_position_1.png",
            position2Asset = "exercises/incline_dumbbell_press_position_2.png",
            tips = listOf(
                "Set a moderate incline. Dumbbells start over the upper chest.",
                "Lower to the upper chest with wrists stacked over the elbows.",
                "Press until the elbows are straight without flaring the elbows wide.",
            ),
        ),
        "Pec deck fly" to ExerciseDemo(
            position1Asset = "exercises/pec_deck_fly_position_1.png",
            position2Asset = "exercises/pec_deck_fly_position_2.png",
            tips = listOf(
                "Sit tall against the pad. Hold the vertical handles, elbows about shoulder height.",
                "Open until you feel a stretch across mid-chest; keep a slight elbow bend.",
                "Bring the handles together in front of the chest with elbows straight. Do not slam them.",
            ),
        ),
        "Cable fly" to ExerciseDemo(
            position1Asset = "exercises/cable_fly_position_1.png",
            position2Asset = "exercises/cable_fly_position_2.png",
            tips = listOf(
                "Sit tall against the 90° back pad between the cable stacks. Handles at about mid-chest height.",
                "Open the arms wide to the sides with a slight elbow bend until you feel a chest stretch.",
                "Bring the handles together in front of the chest with both elbows fully straight. Grip the D-handles in the hands. Do not slam them or shrug.",
            ),
        ),
        "Chest press machine" to ExerciseDemo(
            position1Asset = "exercises/chest_press_machine_position_1.png",
            position2Asset = "exercises/chest_press_machine_position_2.png",
            tips = listOf(
                "Sit tall against the 90° back pad. Grip both horizontal press handles at the chest.",
                "This is the settled stretch: elbows bent, machine down.",
                "Press the handles forward against the weight until both elbows are fully straight, then return to the settle position.",
            ),
        ),
        "Push-up" to ExerciseDemo(
            position1Asset = "exercises/push_up_position_1.png",
            position2Asset = "exercises/push_up_position_2.png",
            tips = listOf(
                "Hands under the chest, body in a straight line from head to heels.",
                "Lower until the chest is close to the floor, elbows about 45° from the torso.",
                "Press until both elbows are fully straight. Do not sag or pike the hips.",
            ),
        ),
    )

    fun forName(name: String): ExerciseDemo? = byName[name]
}
