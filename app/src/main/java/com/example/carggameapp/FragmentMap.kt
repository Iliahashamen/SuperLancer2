package com.example.carggameapp

import android.os.Bundle
import android.view.View
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.SupportMapFragment // We inherit from this directly!
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

// FIX: We extend SupportMapFragment directly to avoid "Duplicate ID" crashes
class FragmentMap : SupportMapFragment() {

    fun zoom(lat: Double, lon: Double) {
        // "getMapAsync" is now built-in because we ARE the map fragment
        getMapAsync { googleMap ->
            val pos = LatLng(lat, lon)
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(pos, 15f))
        }
    }

    fun addMarkers(scores: List<HighScore>) {
        getMapAsync { googleMap ->
            googleMap.clear()

            for (score in scores) {
                // Only add valid locations
                if (score.lat != 0.0 && score.lon != 0.0) {
                    val pos = LatLng(score.lat, score.lon)
                    googleMap.addMarker(MarkerOptions().position(pos).title("${score.name}: ${score.score}"))
                }
            }

            // Zoom logic
            if (scores.isNotEmpty() && scores[0].lat != 0.0) {
                val first = LatLng(scores[0].lat, scores[0].lon)
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(first, 13f))
            } else {
                // Default to Bat Yam if list is empty
                val batYam = LatLng(32.01, 34.74)
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(batYam, 13f))
            }
        }
    }
}