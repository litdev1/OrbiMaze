package com.litdev.orbimaze

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import android.widget.ToggleButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.litdev.orbimaze.R
import io.github.sceneview.collision.Vector3

class SceneActivity : AppCompatActivity() {
    lateinit var sceneView: MainSceneView
    lateinit var textView: TextView
    lateinit var infoView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //enableEdgeToEdge()
        setContentView(R.layout.activity_scene)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setFullScreen(
            findViewById(R.id.main)
        )

        sceneView = findViewById<MainSceneView>(R.id.sceneView)
        textView = findViewById<TextView>(R.id.fpsView)
        infoView = findViewById<TextView>(R.id.infoView)

        val handler = Handler(Looper.getMainLooper())
        val updateRunnable = object : Runnable {
            override fun run() {
                updateUI()
                handler.postDelayed(this, 100)
            }
        }
        handler.postDelayed(updateRunnable, 0)

        val toggleButton = findViewById<ToggleButton>(R.id.toggleMode)
        toggleButton.setOnCheckedChangeListener { _, isChecked ->
            sceneView.viewMode = if (isChecked) 1 else 0
            if (!isChecked) {
                sceneView.cameraDir = sceneView.cameraDirStart
            }
            sceneView.modeCount++
        }

        val nextLevel = findViewById<Button>(R.id.nextLevel)
        nextLevel.setOnClickListener() { _ ->
            sceneView.gameState = -1
            sceneView.level++
            if (sceneView.level > ApplicationClass.instance.level) {
                sceneView.level = 1
            }
            sceneView.levelSet()
        }
    }

    @SuppressLint("DefaultLocale")
    private fun updateUI() {
        textView.text = String.format("%d fps", sceneView.fps)
        levelText()
    }

    fun levelText() {
        val level = sceneView.level
        var message = String.format("Level %d", level)
        when (level) {
            1 -> message += ApplicationClass.instance.getString(R.string.level1)
            2 -> message += ApplicationClass.instance.getString(R.string.level2)
        }
        infoView.text = message
    }

    private fun Activity.setFullScreen(
        rootView: View,
        fullScreen: Boolean = true,
        hideSystemBars: Boolean = true,
        fitsSystemWindows: Boolean = true
    ) {
        rootView.viewTreeObserver?.addOnWindowFocusChangeListener { hasFocus ->
            if (hasFocus) {
                WindowCompat.setDecorFitsSystemWindows(window, fitsSystemWindows)
                WindowInsetsControllerCompat(window, rootView).apply {
                    if (hideSystemBars) {
                        if (fullScreen) {
                            hide(
                                WindowInsetsCompat.Type.systemBars()
                            )
                        } else {
                            show(
                                WindowInsetsCompat.Type.systemBars()
                            )
                        }
                        systemBarsBehavior =
                            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                    }
                }
            }
        }
    }
}


