package com.example.carggameapp

class GameManager(private val lifeCount: Int = 3) {

    var score: Int = 0 // Tracks Points (Coins)
        private set

    var distance: Int = 0 // Tracks Odometer (Meters)
        private set

    var lives: Int = lifeCount
        private set

    var isGameRunning: Boolean = false
        private set

    // Game Speed Delay (Lower is faster)
    var gameSpeed: Long = 1000L

    fun addScore(points: Int) {
        score += points
    }

    fun addDistance(meters: Int) {
        distance += meters
    }

    fun reduceLife() {
        if (lives > 0) lives--
    }

    fun addLife() {
        if (lives < 3) lives++
    }

    fun isDead(): Boolean {
        return lives <= 0
    }

    fun startGame() {
        isGameRunning = true
        score = 0
        distance = 0
        lives = 3
    }

    fun stopGame() {
        isGameRunning = false
    }
}