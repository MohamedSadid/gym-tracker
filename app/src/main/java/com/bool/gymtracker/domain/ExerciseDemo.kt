package com.bool.gymtracker.domain

data class ExerciseDemo(
    val lockoutAsset: String,
    val bottomAsset: String,
    val tips: List<String>,
)

object ExerciseDemos {
    private val byName = mapOf(
        "Barbell bench press" to ExerciseDemo(
            lockoutAsset = "exercises/barbell_bench_press_lockout.png",
            bottomAsset = "exercises/barbell_bench_press_bottom.png",
            tips = listOf(
                "Lie on a flat bench, feet on the floor, bar over mid-chest.",
                "Lower the bar to the chest with wrists stacked over the elbows.",
                "Press to lockout; do not bounce the bar off the chest.",
            ),
        ),
        "Dumbbell bench press" to ExerciseDemo(
            lockoutAsset = "exercises/dumbbell_bench_press_lockout.png",
            bottomAsset = "exercises/dumbbell_bench_press_bottom.png",
            tips = listOf(
                "Lie on a flat bench, feet on the floor, dumbbells over the chest.",
                "Lower until the bells are beside the chest, elbows about 45° from the torso.",
                "Press to lockout; do not bang the dumbbells together.",
            ),
        ),
        "Smith bench press" to ExerciseDemo(
            lockoutAsset = "exercises/smith_bench_press_lockout.png",
            bottomAsset = "exercises/smith_bench_press_bottom.png",
            tips = listOf(
                "Lie on a flat bench inside the Smith machine, bar over mid-chest.",
                "Unrack, then lower the guided bar until it touches the chest, elbows about 45°.",
                "Press to lockout along the rails; do not bounce the bar.",
            ),
        ),
        "Incline barbell press" to ExerciseDemo(
            lockoutAsset = "exercises/incline_barbell_press_lockout.png",
            bottomAsset = "exercises/incline_barbell_press_bottom.png",
            tips = listOf(
                "Set a moderate incline. Bar starts over the upper chest.",
                "Lower the bar to the chest with elbows about 45° from the torso.",
                "Press to lockout with elbows fully straight; do not bounce the bar.",
            ),
        ),
        "Incline dumbbell press" to ExerciseDemo(
            lockoutAsset = "exercises/incline_dumbbell_press_lockout.png",
            bottomAsset = "exercises/incline_dumbbell_press_bottom.png",
            tips = listOf(
                "Set a moderate incline. Dumbbells start over the upper chest.",
                "Lower to the upper chest with wrists stacked over the elbows.",
                "Press to lockout without flaring the elbows wide.",
            ),
        ),
    )

    fun forName(name: String): ExerciseDemo? = byName[name]
}
