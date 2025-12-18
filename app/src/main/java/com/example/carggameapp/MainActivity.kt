package com.example.carggameapp

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Color
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.View
import android.view.animation.LinearInterpolator
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import java.util.Random
import com.example.carggameapp.R

// Data class for High Scores
data class HighScore(val name: String, val score: Int)

class MainActivity : AppCompatActivity() {

    // Views
    private lateinit var ivCar: ImageView
    private lateinit var tvScore: TextView
    private lateinit var rootLayout: ConstraintLayout
    private lateinit var obstacleContainer: FrameLayout
    private lateinit var livesContainer: LinearLayout
    private lateinit var heart1: ImageView
    private lateinit var heart2: ImageView
    private lateinit var heart3: ImageView
    private lateinit var btnLeft: Button
    private lateinit var btnRight: Button

    // Menus
    private lateinit var menuLayout: LinearLayout
    private lateinit var gameOverLayout: LinearLayout

    // Menu Inputs
    private lateinit var etPlayerName: EditText
    private lateinit var btnStart: Button
    private lateinit var btnExitApp: Button

    // Game Over Inputs
    private lateinit var tvHighScores: TextView
    private lateinit var btnPlayAgain: Button
    private lateinit var btnExitGame: Button

    // Game Logic Variables
    private var score = 0
    private var lives = 3
    private var currentLane = 1
    private var isRunning = false
    private var currentPlayerName = ""
    private val highScoresList = mutableListOf<HighScore>()

    // Handlers
    private val handler = Handler(Looper.getMainLooper())
    private val random = Random()
    private val activeObstacles = mutableListOf<ImageView>()
    private val laneBias = floatArrayOf(0.17f, 0.5f, 0.83f)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 1. Bind Views
        ivCar = findViewById(R.id.ivCar)
        tvScore = findViewById(R.id.tvScore)
        rootLayout = findViewById(R.id.rootLayout)
        obstacleContainer = findViewById(R.id.obstacleContainer)
        livesContainer = findViewById(R.id.livesContainer)
        heart1 = findViewById(R.id.heart1)
        heart2 = findViewById(R.id.heart2)
        heart3 = findViewById(R.id.heart3)
        btnLeft = findViewById(R.id.btnLeft)
        btnRight = findViewById(R.id.btnRight)

        // Menu Views
        menuLayout = findViewById(R.id.menuLayout)
        etPlayerName = findViewById(R.id.etPlayerName)
        btnStart = findViewById(R.id.btnStartGame)
        btnExitApp = findViewById(R.id.btnExitApp)

        // Game Over Views
        gameOverLayout = findViewById(R.id.gameOverLayout)
        tvHighScores = findViewById(R.id.tvHighScores)
        btnPlayAgain = findViewById(R.id.btnPlayAgain)
        btnExitGame = findViewById(R.id.btnExitGame)

        // 2. Button Listeners
        btnLeft.setOnClickListener { if (isRunning && currentLane > 0) { currentLane--; moveCar() } }
        btnRight.setOnClickListener { if (isRunning && currentLane < 2) { currentLane++; moveCar() } }

        // Start Screen Logic
        btnStart.setOnClickListener {
            val name = etPlayerName.text.toString().trim()
            if (name.isEmpty()) {
                Toast.makeText(this, "Enter a name to play!", Toast.LENGTH_SHORT).show()
            } else {
                currentPlayerName = name
                closeKeyboard()
                startGame()
            }
        }

        btnExitApp.setOnClickListener { finish() }

        // Game Over Screen Logic
        btnPlayAgain.setOnClickListener {
            startGame() // Replay with same name
        }

        btnExitGame.setOnClickListener {
            etPlayerName.setText("") // Clear the name box
            showStartMenu()          // Go back to name entry
        }

        // Initial State
        showStartMenu()
    }

    // --- SCREEN MANAGEMENT ---

    private fun showStartMenu() {
        menuLayout.visibility = View.VISIBLE
        gameOverLayout.visibility = View.GONE
        setGameControlsVisibility(false)
    }

    private fun showGameOverScreen() {
        menuLayout.visibility = View.GONE
        gameOverLayout.visibility = View.VISIBLE
        setGameControlsVisibility(false)
        updateHighScoreBoard()
    }

    private fun startGame() {
        menuLayout.visibility = View.GONE
        gameOverLayout.visibility = View.GONE
        setGameControlsVisibility(true)

        // Reset Logic
        score = 0
        lives = 3
        currentLane = 1
        isRunning = true

        activeObstacles.clear()
        obstacleContainer.removeAllViews()
        moveCar()
        updateHearts()
        tvScore.text = "SCORE: 0"

        // Start Loops
        handler.removeCallbacksAndMessages(null)
        handler.post(gameLoop)
        handler.post(spawnLoop)
    }

    private fun setGameControlsVisibility(visible: Boolean) {
        val v = if (visible) View.VISIBLE else View.INVISIBLE
        btnLeft.visibility = v
        btnRight.visibility = v
        ivCar.visibility = v
        livesContainer.visibility = v
        tvScore.visibility = v
    }

    private fun closeKeyboard() {
        val view = this.currentFocus
        if (view != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    // --- HIGH SCORE LOGIC ---

    private fun saveScore() {
        highScoresList.add(HighScore(currentPlayerName, score))
    }

    private fun updateHighScoreBoard() {
        if (highScoresList.isEmpty()) {
            tvHighScores.text = "No records yet..."
            return
        }
        // Sort descending and take top 5
        val top5 = highScoresList.sortedByDescending { it.score }.take(5)

        val sb = StringBuilder()
        for ((i, record) in top5.withIndex()) {
            sb.append("${i + 1}. ${record.name} : ${record.score}\n")
        }
        tvHighScores.text = sb.toString()
    }

    // --- GAME ENGINE ---

    private fun moveCar() {
        val set = ConstraintSet()
        set.clone(rootLayout)
        set.setHorizontalBias(R.id.ivCar, laneBias[currentLane])
        set.applyTo(rootLayout)
    }

    private val spawnLoop = object : Runnable {
        override fun run() {
            if (!isRunning) return
            spawnObstacle()
            val speed = if (score < 100) 1500L else 1000L
            handler.postDelayed(this, speed)
        }
    }

    private fun spawnObstacle() {
        val lane = random.nextInt(3)
        val obs = ImageView(this)

        // --- UPDATED LOGIC ---
        // Spawn Heart only if lives < 3 AND 8% chance (Rare!)
        val spawnHeart = (lives < 3 && random.nextInt(100) < 8)

        if (spawnHeart) {
            obs.setImageResource(R.drawable.ic_heart_life)
            obs.tag = "HEART" // Mark it as a heart
        } else {
            // Regular Rock
            val rockImages = listOf(R.drawable.ic_rock1, R.drawable.ic_rock2, R.drawable.ic_rock3)
            obs.setImageResource(rockImages.random())
            obs.tag = "ROCK"
        }

        val params = FrameLayout.LayoutParams(120, 120)
        val screenWidth = resources.displayMetrics.widthPixels

        if (lane == 0) params.leftMargin = (screenWidth * 0.17).toInt() - 60
        if (lane == 1) params.leftMargin = (screenWidth * 0.5).toInt() - 60
        if (lane == 2) params.leftMargin = (screenWidth * 0.83).toInt() - 60

        params.topMargin = -150
        obstacleContainer.addView(obs, params)
        activeObstacles.add(obs)

        val screenH = resources.displayMetrics.heightPixels.toFloat()
        val animator = ObjectAnimator.ofFloat(obs, "translationY", 0f, screenH + 200)
        animator.duration = 2500
        animator.interpolator = LinearInterpolator()
        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                // Score Logic: Only give points for avoiding ROCKS
                if (isRunning && activeObstacles.contains(obs)) {
                    if (obs.tag == "ROCK") {
                        score += 10
                        tvScore.text = "SCORE: $score"
                    }
                }
                activeObstacles.remove(obs)
                obstacleContainer.removeView(obs)
            }
        })
        animator.start()
    }

    private val gameLoop = object : Runnable {
        override fun run() {
            if (!isRunning) return

            val carRect = Rect()
            ivCar.getGlobalVisibleRect(carRect)
            carRect.inset(30, 30)

            val toRemove = mutableListOf<ImageView>()

            for (obs in activeObstacles) {
                val obsRect = Rect()
                obs.getGlobalVisibleRect(obsRect)
                obsRect.inset(20, 20)

                if (Rect.intersects(carRect, obsRect)) {
                    toRemove.add(obs)

                    // --- COLLISION LOGIC ---
                    if (obs.tag == "HEART") {
                        // Healed!
                        lives++
                        updateHearts()
                        Toast.makeText(this@MainActivity, "EXTRA LIFE!", Toast.LENGTH_SHORT).show()
                    } else {
                        // Crashed!
                        lives--
                        crash()
                    }
                }
            }

            for (obs in toRemove) {
                obs.visibility = View.INVISIBLE
                activeObstacles.remove(obs)
                obstacleContainer.removeView(obs)
            }

            if (lives <= 0) {
                isRunning = false
                handler.removeCallbacksAndMessages(null)
                saveScore()
                showGameOverScreen()
            } else {
                handler.postDelayed(this, 50)
            }
        }
    }

    private fun crash() {
        Toast.makeText(this, "CRASH! Watch out!", Toast.LENGTH_SHORT).show()

        val shake = ObjectAnimator.ofFloat(ivCar, "translationX", 0f, 25f, -25f, 25f, -25f, 15f, -15f, 0f)
        shake.duration = 500
        shake.start()

        updateHearts()

        val v = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= 26) {
            v.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            v.vibrate(200)
        }
    }

    private fun updateHearts() {
        heart1.visibility = if (lives >= 1) View.VISIBLE else View.INVISIBLE
        heart2.visibility = if (lives >= 2) View.VISIBLE else View.INVISIBLE
        heart3.visibility = if (lives >= 3) View.VISIBLE else View.INVISIBLE
    }
}