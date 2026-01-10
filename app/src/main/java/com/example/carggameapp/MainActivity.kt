package com.example.carggameapp

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.LinearInterpolator
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import java.util.Random

class MainActivity : AppCompatActivity() {

    // --- MANAGERS ---
    private lateinit var gameManager: GameManager
    private lateinit var tiltDetector: TiltDetector
    private lateinit var scoreManager: ScoreManager
    private lateinit var locationManager: GameLocationManager

    // --- FRAGMENTS ---
    private val fragmentList = FragmentList()
    private val fragmentMap = FragmentMap()

    // UI Components
    private lateinit var ivCar: ImageView
    private lateinit var tvScore: TextView
    private lateinit var tvOdometer: TextView
    private lateinit var rootLayout: ConstraintLayout
    private lateinit var obstacleContainer: FrameLayout
    private lateinit var livesContainer: LinearLayout
    private lateinit var heart1: ImageView
    private lateinit var heart2: ImageView
    private lateinit var heart3: ImageView
    private lateinit var btnLeft: Button
    private lateinit var btnRight: Button

    private lateinit var menuLayout: LinearLayout
    private lateinit var gameOverLayout: LinearLayout
    private lateinit var highScoreLayout: LinearLayout // NEW

    private lateinit var etPlayerName: EditText

    private lateinit var btnModeSlow: Button
    private lateinit var btnModeFast: Button
    private lateinit var btnModeSensor: Button
    private lateinit var btnHighScores: Button // NEW

    private lateinit var btnExitApp: Button
    private lateinit var btnPlayAgain: Button
    private lateinit var btnExitGame: Button
    private lateinit var btnCloseScores: Button // NEW

    private val handler = Handler(Looper.getMainLooper())
    private val random = Random()
    private val activeObstacles = mutableListOf<ImageView>()
    private val laneBias = floatArrayOf(0.1f, 0.3f, 0.5f, 0.7f, 0.9f)
    private var currentLane = 2
    private var currentPlayerName = ""
    private var isSensorMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        SignalManager.init(this)
        gameManager = GameManager()
        scoreManager = ScoreManager(this)
        locationManager = GameLocationManager(this)

        requestLocationPermission()

        tiltDetector = TiltDetector(this, object : TiltDetector.TiltCallback {
            override fun tiltLeft() { if (gameManager.isGameRunning && isSensorMode) moveLeft() }
            override fun tiltRight() { if (gameManager.isGameRunning && isSensorMode) moveRight() }
        })

        initViews()
        initListeners()

        // --- CALLBACK: When clicking a name in the list, zoom the map ---
        fragmentList.listener = { lat, lon ->
            fragmentMap.zoom(lat, lon)
        }

        showStartMenu()
    }

    private fun requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
                if (isGranted) SignalManager.getInstance().toast("GPS Enabled")
            }
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    override fun onPause() {
        super.onPause()
        stopTimers()
        if (isSensorMode) tiltDetector.stop()
    }

    override fun onResume() {
        super.onResume()
        if (gameManager.isGameRunning) {
            startTimers()
            if (isSensorMode) tiltDetector.start()
        }
    }

    private fun initViews() {
        ivCar = findViewById(R.id.ivCar)
        tvScore = findViewById(R.id.tvScore)
        tvOdometer = findViewById(R.id.tvOdometer)
        rootLayout = findViewById(R.id.rootLayout)
        obstacleContainer = findViewById(R.id.obstacleContainer)
        livesContainer = findViewById(R.id.livesContainer)
        heart1 = findViewById(R.id.heart1)
        heart2 = findViewById(R.id.heart2)
        heart3 = findViewById(R.id.heart3)
        btnLeft = findViewById(R.id.btnLeft)
        btnRight = findViewById(R.id.btnRight)

        menuLayout = findViewById(R.id.menuLayout)
        highScoreLayout = findViewById(R.id.highScoreLayout) // NEW
        etPlayerName = findViewById(R.id.etPlayerName)

        btnModeSlow = findViewById(R.id.btnModeSlow)
        btnModeFast = findViewById(R.id.btnModeFast)
        btnModeSensor = findViewById(R.id.btnModeSensor)
        btnHighScores = findViewById(R.id.btnHighScores) // NEW

        btnExitApp = findViewById(R.id.btnExitApp)

        gameOverLayout = findViewById(R.id.gameOverLayout)
        btnPlayAgain = findViewById(R.id.btnPlayAgain)
        btnExitGame = findViewById(R.id.btnExitGame)
        btnCloseScores = findViewById(R.id.btnCloseScores) // NEW
    }

    private fun initListeners() {
        btnLeft.setOnClickListener { moveLeft() }
        btnRight.setOnClickListener { moveRight() }

        btnModeSlow.setOnClickListener { startGame(sensorMode = false, speed = 1500L) }
        btnModeFast.setOnClickListener { startGame(sensorMode = false, speed = 800L) }
        btnModeSensor.setOnClickListener { startGame(sensorMode = true, speed = 1200L) }

        // NEW: Open High Scores
        btnHighScores.setOnClickListener {
            showHighScores()
        }

        // NEW: Close High Scores
        btnCloseScores.setOnClickListener {
            highScoreLayout.visibility = View.GONE
            menuLayout.visibility = View.VISIBLE
        }

        btnExitApp.setOnClickListener { finish() }
        btnPlayAgain.setOnClickListener { showStartMenu() }
        btnExitGame.setOnClickListener { showStartMenu() }
    }

    // --- FRAGMENT LOGIC ---
    private fun showHighScores() {
        menuLayout.visibility = View.GONE
        highScoreLayout.visibility = View.VISIBLE

        // Load the fragments into the frames
        supportFragmentManager.beginTransaction()
            .replace(R.id.frame_list, fragmentList)
            .replace(R.id.frame_map, fragmentMap)
            .commit()

        // Load data into them
        handler.postDelayed({
            val scores = scoreManager.getAllScores()
            fragmentList.updateList(scores)
            fragmentMap.addMarkers(scores)
        }, 200)
    }

    private fun moveLeft() { if (currentLane > 0) { currentLane--; updateCarPosition() } }
    private fun moveRight() { if (currentLane < 4) { currentLane++; updateCarPosition() } }

    private fun startGame(sensorMode: Boolean, speed: Long) {
        val name = etPlayerName.text.toString().trim()
        if (name.isEmpty()) { SignalManager.getInstance().toast("Enter Name!"); return }
        currentPlayerName = name
        closeKeyboard()
        isSensorMode = sensorMode
        gameManager.gameSpeed = speed

        menuLayout.visibility = View.GONE
        gameOverLayout.visibility = View.GONE
        highScoreLayout.visibility = View.GONE

        if (isSensorMode) {
            btnLeft.visibility = View.INVISIBLE
            btnRight.visibility = View.INVISIBLE
            tiltDetector.start()
            SignalManager.getInstance().toast("Tilt Enabled!")
        } else {
            btnLeft.visibility = View.VISIBLE
            btnRight.visibility = View.VISIBLE
            tiltDetector.stop()
        }

        ivCar.visibility = View.VISIBLE
        livesContainer.visibility = View.VISIBLE
        tvScore.visibility = View.VISIBLE
        tvOdometer.visibility = View.VISIBLE

        gameManager.startGame()
        updateUI()
        activeObstacles.forEach { obstacleContainer.removeView(it) }
        activeObstacles.clear()
        currentLane = 2
        updateCarPosition()
        startTimers()
    }

    private fun startTimers() {
        handler.post(gameLoop)
        handler.post(spawnLoop)
        handler.post(odometerLoop)
    }

    private fun stopTimers() {
        handler.removeCallbacks(gameLoop)
        handler.removeCallbacks(spawnLoop)
        handler.removeCallbacks(odometerLoop)
    }

    private fun updateCarPosition() {
        val set = ConstraintSet()
        set.clone(rootLayout)
        set.setHorizontalBias(R.id.ivCar, laneBias[currentLane])
        set.applyTo(rootLayout)
    }

    private val spawnLoop = object : Runnable {
        override fun run() {
            if (!gameManager.isGameRunning) return
            spawnObstacle()
            handler.postDelayed(this, gameManager.gameSpeed)
        }
    }

    private val odometerLoop = object : Runnable {
        override fun run() {
            if (!gameManager.isGameRunning) return
            gameManager.addDistance(10)
            updateUI()
            handler.postDelayed(this, 1000)
        }
    }

    private val gameLoop = object : Runnable {
        override fun run() {
            if (!gameManager.isGameRunning) return
            checkCollisions()
            if (gameManager.isDead()) { saveScore() } else { handler.postDelayed(this, 50) }
        }
    }

    private fun spawnObstacle() {
        val lane = random.nextInt(5)
        val obs = ImageView(this)
        val type = random.nextInt(100)
        if (gameManager.lives < 3 && type < 8) { obs.setImageResource(R.drawable.ic_heart_life); obs.tag = "HEART" }
        else if (type < 30) { obs.setImageResource(R.drawable.ic_coin); obs.tag = "COIN" }
        else {
            val rockImages = listOf(R.drawable.ic_rock1, R.drawable.ic_rock2, R.drawable.ic_rock3)
            obs.setImageResource(rockImages.random())
            obs.tag = "ROCK"
        }
        val params = FrameLayout.LayoutParams(120, 120)
        val screenWidth = resources.displayMetrics.widthPixels
        params.leftMargin = (screenWidth * laneBias[lane]).toInt() - 60
        params.topMargin = -150
        obstacleContainer.addView(obs, params)
        activeObstacles.add(obs)
        val screenH = resources.displayMetrics.heightPixels.toFloat()
        val animator = ObjectAnimator.ofFloat(obs, "translationY", 0f, screenH + 200)
        animator.duration = 2500
        animator.interpolator = LinearInterpolator()
        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) { activeObstacles.remove(obs); obstacleContainer.removeView(obs) }
        })
        animator.start()
    }

    private fun checkCollisions() {
        val carRect = Rect()
        ivCar.getGlobalVisibleRect(carRect); carRect.inset(30, 30)
        val toRemove = mutableListOf<ImageView>()
        for (obs in activeObstacles) {
            val obsRect = Rect()
            obs.getGlobalVisibleRect(obsRect); obsRect.inset(20, 20)
            if (Rect.intersects(carRect, obsRect)) { toRemove.add(obs); handleCollision(obs.tag.toString()) }
        }
        for (obs in toRemove) { obs.visibility = View.INVISIBLE; activeObstacles.remove(obs); obstacleContainer.removeView(obs) }
    }

    private fun handleCollision(tag: String) {
        when (tag) {
            "HEART" -> { gameManager.addLife(); SignalManager.getInstance().toast("Extra Life!") }
            "COIN" -> { gameManager.addScore(50); SignalManager.getInstance().toast("+50!") }
            else -> {
                gameManager.reduceLife()
                SignalManager.getInstance().toast("CRASH!")
                SignalManager.getInstance().vibrate()
                SignalManager.getInstance().playCrashSound()
                val shake = ObjectAnimator.ofFloat(ivCar, "translationX", 0f, 25f, -25f, 25f, -25f, 15f, -15f, 0f); shake.duration = 500; shake.start()
            }
        }
        updateUI()
    }

    private fun updateUI() {
        tvScore.text = "SCORE: ${gameManager.score}"
        tvOdometer.text = "${gameManager.distance} m"
        heart1.visibility = if (gameManager.lives >= 1) View.VISIBLE else View.INVISIBLE
        heart2.visibility = if (gameManager.lives >= 2) View.VISIBLE else View.INVISIBLE
        heart3.visibility = if (gameManager.lives >= 3) View.VISIBLE else View.INVISIBLE
    }

    private fun showStartMenu() {
        menuLayout.visibility = View.VISIBLE
        gameOverLayout.visibility = View.GONE
        highScoreLayout.visibility = View.GONE
        btnLeft.visibility = View.INVISIBLE; btnRight.visibility = View.INVISIBLE; ivCar.visibility = View.INVISIBLE; livesContainer.visibility = View.INVISIBLE; tvScore.visibility = View.INVISIBLE; tvOdometer.visibility = View.INVISIBLE
        if (isSensorMode) tiltDetector.stop()
    }

    private fun showGameOverScreen() {
        gameManager.stopGame()
        stopTimers()
        if (isSensorMode) tiltDetector.stop()
        menuLayout.visibility = View.GONE
        gameOverLayout.visibility = View.VISIBLE
        highScoreLayout.visibility = View.GONE
        btnLeft.visibility = View.INVISIBLE; btnRight.visibility = View.INVISIBLE; ivCar.visibility = View.INVISIBLE; livesContainer.visibility = View.INVISIBLE; tvScore.visibility = View.INVISIBLE; tvOdometer.visibility = View.INVISIBLE
    }

    private fun closeKeyboard() {
        val view = this.currentFocus
        if (view != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    // --- BAT YAM HACK LOGIC ---
    private fun saveScore() {
        gameManager.stopGame(); stopTimers()
        locationManager.getLastLocation { lat, lon ->
            var finalLat = lat
            var finalLon = lon

            // IF GPS FAILS (or Emulator), FAKE IT TO BAT YAM
            if (finalLat == 0.0 && finalLon == 0.0) {
                finalLat = 32.01 + (random.nextDouble() - 0.5) * 0.01
                finalLon = 34.74 + (random.nextDouble() - 0.5) * 0.01
            }

            val score = HighScore(currentPlayerName, gameManager.score, finalLat, finalLon)
            scoreManager.saveScore(score)
            showGameOverScreen()
        }
    }
}