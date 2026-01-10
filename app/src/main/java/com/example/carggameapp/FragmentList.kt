package com.example.carggameapp

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment

class FragmentList : Fragment() {

    private lateinit var tvScores: TextView
    // Callback to MainActivity
    var listener: ((Double, Double) -> Unit)? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // Create scrollable text view programmatically
        val scrollView = android.widget.ScrollView(context)
        tvScores = TextView(context)
        tvScores.textSize = 18f
        tvScores.setTextColor(android.graphics.Color.WHITE)
        tvScores.setPadding(40, 40, 40, 40)
        tvScores.gravity = Gravity.CENTER_HORIZONTAL
        scrollView.addView(tvScores)
        return scrollView
    }

    fun updateList(scores: List<HighScore>) {
        val sb = StringBuilder()
        if (scores.isEmpty()) {
            sb.append("No records yet.")
        } else {
            for ((i, record) in scores.withIndex()) {
                sb.append("${i + 1}. ${record.name}\n")
                sb.append("   Score: ${record.score}\n")
                sb.append("   (Click to see on Map)\n\n")
            }
        }
        tvScores.text = sb.toString()

        tvScores.setOnClickListener {
            // Zoom to top player if clicked (Demo)
            if (scores.isNotEmpty()) {
                listener?.invoke(scores[0].lat, scores[0].lon)
            }
        }
    }
}