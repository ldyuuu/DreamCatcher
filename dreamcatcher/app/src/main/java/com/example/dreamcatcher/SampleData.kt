package com.example.dreamcatcher

val sampleDreams = listOf(
    Dream(
        userId = 1,
        title = "Dream 1",
        content = "I was flying over the ocean.",
        mood = """[{"label": "joy", "score": 0.7}, {"label": "fear", "score": 0.3}]""",
        createdAt = System.currentTimeMillis(),
        aiImageURL = ""
    ),
    Dream(
        userId = 1,
        title = "Dream 2",
        content = "I met a talking dog.",
        mood = """[{"label": "joy", "score": 0.8}, {"label": "surprise", "score": 0.2}]""",
        createdAt = System.currentTimeMillis(),
        aiImageURL = ""
    )
)
