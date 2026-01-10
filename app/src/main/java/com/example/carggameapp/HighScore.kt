package com.example.carggameapp

data class HighScore(
    val name: String,
    val score: Int,
    val lat: Double = 0.0,
    val lon: Double = 0.0
)