package com.example.carggameapp

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ScoreManager(context: Context) {

    private val SP_NAME = "SuperLancerScores"
    private val KEY_SCORES = "scores_list"
    private val gson = Gson()
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE)

    // Load the list from storage
    fun getAllScores(): MutableList<HighScore> {
        val jsonString = sharedPreferences.getString(KEY_SCORES, null)
        return if (jsonString != null) {
            val type = object : TypeToken<MutableList<HighScore>>() {}.type
            gson.fromJson(jsonString, type)
        } else {
            mutableListOf()
        }
    }

    // Save a new score (Handles Top 10 Logic)
    fun saveScore(newScore: HighScore) {
        val currentScores = getAllScores()
        currentScores.add(newScore)

        // Sort by score (Highest first)
        currentScores.sortByDescending { it.score }

        // Keep only Top 10
        if (currentScores.size > 10) {
            currentScores.removeAt(currentScores.lastIndex) // Remove the 11th score
        }

        // Save back to storage
        val editor = sharedPreferences.edit()
        val jsonString = gson.toJson(currentScores)
        editor.putString(KEY_SCORES, jsonString)
        editor.apply()
    }
}