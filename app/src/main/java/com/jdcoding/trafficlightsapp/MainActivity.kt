package com.jdcoding.trafficlightsapp

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    private lateinit var redLight: ImageView
    private lateinit var yellowLight: ImageView
    private lateinit var greenLight: ImageView
    private lateinit var vehicule: ImageView
    private lateinit var activeNight: Switch
    private lateinit var mainLayout: RelativeLayout
    private val handler = Handler(Looper.getMainLooper())
    private var isMidnightBlinking = false

    private lateinit var moveAnimation: ObjectAnimator

    private lateinit var blinkYellowRunnable: Runnable

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        redLight = findViewById(R.id.redLight)
        yellowLight = findViewById(R.id.yellowLight)
        greenLight = findViewById(R.id.greenLight)
        vehicule = findViewById(R.id.vehicule)
        activeNight = findViewById(R.id.activeNight)
        mainLayout = findViewById(R.id.main)

        moveAnimation = ObjectAnimator.ofFloat(vehicule, "translationX", -150f, 1150f) // Déplacer de 0 à 1150 pixels
        moveAnimation.duration = 10000
        moveAnimation.repeatCount = ObjectAnimator.INFINITE
        moveAnimation.start()

        activeNight.setOnCheckedChangeListener { _, isChecked ->
            mainLayout.setBackgroundResource(if (isChecked) R.drawable.night else R.drawable.day)
            if (isChecked) {
                vehicule.setImageResource(R.drawable.camionnight)
            } else {
                vehicule.setImageResource(R.drawable.camionday)
            }

            if (!isChecked) {
                isMidnightBlinking = false
                stopBlinkingYellow()
                startTrafficLightCycle()
            }
        }

        startTrafficLightCycle()
    }

    private fun startTrafficLightCycle() {
        handler.post { runTrafficLightCycle() }
    }

    private fun runTrafficLightCycle() {
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

        if (currentHour in 0..5 || activeNight.isChecked) {
            isMidnightBlinking = true
            startBlinkingYellow()
            moveAnimation.resume()
        } else {
            isMidnightBlinking = false
            switchToYellow()
        }
    }

    private fun switchToYellow() {
        resetLights()
        yellowLight.setBackgroundResource(R.drawable.circle_yellow)

        if (!isMidnightBlinking) {
            moveAnimation.resume()
        }

        handler.postDelayed({ switchToRed() }, 3000)
    }

    private fun switchToRed() {
        resetLights()
        redLight.setBackgroundResource(R.drawable.circle)

        moveAnimation.pause()

        handler.postDelayed({ switchToGreen() }, 5000)
    }

    private fun switchToGreen() {
        resetLights()
        greenLight.setBackgroundResource(R.drawable.green_circle)

        moveAnimation.resume()

        handler.postDelayed({ runTrafficLightCycle() }, 5000)
    }

    private fun startBlinkingYellow() {
        resetLights()

        blinkYellowRunnable = object : Runnable {
            override fun run() {
                if (isMidnightBlinking) {
                    val currentBackground = yellowLight.background
                    val onDrawable = ResourcesCompat.getDrawable(resources, R.drawable.circle_yellow, null)
                    val offDrawable = ResourcesCompat.getDrawable(resources, R.drawable.circle_off, null)

                    if (currentBackground.constantState == onDrawable?.constantState) {
                        yellowLight.setBackgroundResource(R.drawable.circle_off)
                    } else {
                        yellowLight.setBackgroundResource(R.drawable.circle_yellow)
                    }

                    handler.postDelayed(this, 500)
                }
            }
        }
        handler.post(blinkYellowRunnable)
    }

    private fun stopBlinkingYellow() {
        handler.removeCallbacks(blinkYellowRunnable)
        yellowLight.setBackgroundResource(R.drawable.circle_off)
    }

    private fun resetLights() {
        redLight.setBackgroundResource(R.drawable.circle_off)
        yellowLight.setBackgroundResource(R.drawable.circle_off)
        greenLight.setBackgroundResource(R.drawable.circle_off)
    }
}
