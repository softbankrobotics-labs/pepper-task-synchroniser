package com.softbankrobotics.dx.peppersynchroniser

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.aldebaran.qi.Future
import com.aldebaran.qi.sdk.QiContext
import com.aldebaran.qi.sdk.QiSDK
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks
import com.aldebaran.qi.sdk.`object`.actuation.Animate
import com.aldebaran.qi.sdk.`object`.holder.AutonomousAbilitiesType
import com.aldebaran.qi.sdk.builder.AnimateBuilder
import com.aldebaran.qi.sdk.builder.AnimationBuilder
import com.aldebaran.qi.sdk.builder.HolderBuilder
import com.softbankrobotics.dx.peppersynchronisersample.BuildConfig
import com.softbankrobotics.dx.peppersynchronisersample.R
import com.softbankrobotics.dx.peppertasksynchroniser.TaskSynchroniser
import io.chirp.chirpsdk.models.ChirpSDKState
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), RobotLifecycleCallbacks {

    private val TAG = "PepperSynchroniser"
    private val REQUEST_RECORD_AUDIO = 1

    private lateinit var qiContext: QiContext

    private var startStopSdkBtnPressed: Boolean = false

    private val taskSynchroniser: TaskSynchroniser by lazy {
        TaskSynchroniser(this, BuildConfig.APPKey, BuildConfig.APPSecret, BuildConfig.APPConfig)
    }

    private val PEPPER_BLUE = "pepper_blue"
    private val PEPPER_ORANGE = "pepper_orange"
    private val PEPPER_GREEN = "pepper_green"

    private var mediaPlayer: MediaPlayer? = null

    private lateinit var animateAction: Animate
    private lateinit var animateFuture: Future<Void>
    private lateinit var trajectoryFuture: Future<Void>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        QiSDK.register(this, this)

        iv_logo.setOnClickListener { startStopSDK() }
    }

    override fun onRobotFocusGained(qiContext: QiContext) {
        this.qiContext = qiContext

        holdBasicAwareness(qiContext)

        initialiseSDK()

        buildAnimation(qiContext)
    }

    override fun onRobotFocusLost() {
        Log.e(TAG, "Robot Focus is Lost")
    }

    override fun onRobotFocusRefused(reason: String?) {
        Log.e(TAG, "Robot Focus is refused $reason")
    }

    private fun holdBasicAwareness(qiContext: QiContext) {
        Log.d(TAG, "Holding Basic Awaresness")

        val holder = HolderBuilder.with(qiContext)
            .withAutonomousAbilities(
                AutonomousAbilitiesType.BASIC_AWARENESS
            )
            .build()

        holder.async().hold().thenConsume {
            when {
                it.isSuccess -> Log.d(TAG, "BasicAwareness held with: SUCCESS")
                it.isCancelled -> Log.d(TAG, "BasicAwareness held with: CANCELLED")
                else -> Log.d(TAG, "BasicAwareness held with: ERROR: ${it.error}")
            }
        }


    }

    private fun initialiseSDK() {

        taskSynchroniser.chirpConnect.onStateChanged { oldState: ChirpSDKState, newState: ChirpSDKState ->
            /**
             * onStateChanged is called when the SDK changes state.
             */
            Log.v(TAG, "ConnectCallback: onStateChanged $oldState -> $newState")
        }

        taskSynchroniser.chirpConnect.onReceiving { channel: Int ->
            /**
             * onReceiving is called when a receive event begins.
             * No data has yet been received.
             */
            Log.v(TAG, "ConnectCallback: onReceiving on channel: $channel")

            runOnUiThread {
                Toast.makeText(this, getString(R.string.sdk_receiving), Toast.LENGTH_SHORT).show()
                showImage(PEPPER_GREEN)
            }
        }

        taskSynchroniser.chirpConnect.onReceived { payload: ByteArray?, channel: Int ->
            /**
             * onReceived is called when a receive event has completed.
             * If the payload was decoded successfully, it is passed in payload.
             * Otherwise, payload is null.
             */
            val hexData = payload?.let { String(it) }
            Log.v(TAG, "ConnectCallback: onReceived: $hexData on channel: $channel")

            runOnUiThread {
                Toast.makeText(
                    this,
                    "${getString(R.string.sdk_received)}: $hexData",
                    Toast.LENGTH_SHORT
                ).show()
            }

            if (hexData.equals("Go", ignoreCase = true)) {
                startDance()
            } else if (hexData.equals("Stop", ignoreCase = true)) {
                stopDance()
            } else if (hexData.equals("Close", ignoreCase = true)) {
                startStopSDK()
            } else {
                showImage(PEPPER_ORANGE)
            }
        }
    }

    private fun buildAnimation(qiContext: QiContext?) {
        val animation = AnimationBuilder.with(qiContext) // Create the builder with the context.
            .withResources(R.raw.dance) // Set the animation resource.
            .build() // Build the animation.

        animateAction = AnimateBuilder.with(qiContext) // Create the builder with the context.
            .withAnimation(animation) // Set the animation.
            .build() // Build the animate action.

        animateAction.addOnLabelReachedListener { label, time ->
            Log.d(TAG, "label reached : $label")

            val splitLabels = label.split(":").toTypedArray()
            when (splitLabels[0]) {
                "audio" -> startSound(splitLabels[1])
                "trajectory" -> startPmt(splitLabels[1])
                else -> {
                    Log.d(TAG, "Unknown type of element : ${splitLabels[0]}")
                }
            }
        }
    }

    private fun startDance() {
        Log.d(TAG, "Start dance")
        showImage(PEPPER_BLUE)
        animateFuture = animateAction.async().run().andThenConsume {
            Log.d(TAG, "Dance finished with: SUCCESS")

            showImage(PEPPER_ORANGE)

            if (::qiContext.isInitialized) {
                holdBasicAwareness(qiContext)
            }
        }
    }

    private fun stopDance() {
        Log.d(TAG, "Stop dance")
        showImage(PEPPER_ORANGE)

        if (::qiContext.isInitialized) {
            holdBasicAwareness(qiContext)
        }

        if (::animateFuture.isInitialized) {
            animateFuture.requestCancellation()
        }

        if (::trajectoryFuture.isInitialized) {
            trajectoryFuture.requestCancellation()
        }

        if (mediaPlayer != null) {
            mediaPlayer?.stop()
            mediaPlayer?.reset()
            mediaPlayer?.release()
            mediaPlayer = null
        }
    }

    private fun startSound(name: String) {
        Log.d(TAG, "SOUND NAME : $name")
        val resID = getResourceID(name, "raw")
        mediaPlayer = MediaPlayer.create(this, resID)
        mediaPlayer?.start()
    }

    private fun startPmt(name: String) {
        Log.d(TAG, "PMT NAME : $name")
        val resID = getResourceID(name, "raw")
        val animation = AnimationBuilder.with(qiContext)
            .withResources(resID)
            .build()

        val pmtAnimate = AnimateBuilder.with(qiContext)
            .withAnimation(animation)
            .build()

        trajectoryFuture = pmtAnimate.async().run()
    }

    private fun showImage(name: String) {
        val resID = getResourceID(name, "drawable")

        runOnUiThread {
            iv_logo.setImageResource(resID)
        }
    }

    private fun getResourceID(name: String, folder: String): Int {
        val resID = resources.getIdentifier(name, folder, packageName)
        Log.d(TAG, "RESOURCE ID $resID")
        return resID
    }


    private fun startStopSDK() {
        Log.d(TAG, "Start/Stop SDK")

        startStopSdkBtnPressed = true

        if (taskSynchroniser.isStopped()) {
            startSDK()
        } else {
            stopSDK()
        }
    }

    private fun startSDK() {
        if (taskSynchroniser.start()) {
            Log.d(TAG, "Listening")
            Toast.makeText(this, getString(R.string.sdk_listening), Toast.LENGTH_SHORT).show()
            showImage(PEPPER_ORANGE)
        } else {
            Log.d(TAG, getString(R.string.sdk_error_start))
            Toast.makeText(this, getString(R.string.sdk_error_start), Toast.LENGTH_SHORT).show()
            showImage(PEPPER_BLUE)
        }
    }

    private fun stopSDK() {
        if (taskSynchroniser.stop()) {
            Log.d(TAG, "Stopped listening")
            Toast.makeText(this, getString(R.string.sdk_stopped), Toast.LENGTH_SHORT).show()
            stopDance()
        } else {
            Log.d(TAG, getString(R.string.sdk_error_stop))
            Toast.makeText(this, getString(R.string.sdk_error_stop), Toast.LENGTH_SHORT).show()
        }

        showImage(PEPPER_BLUE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_RECORD_AUDIO -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (startStopSdkBtnPressed) stopSDK()
                }
                return
            }
        }
    }

    private fun hideSystemUI() {
        // Enables regular immersive mode.
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                // Set the content to appear under the system bars so that the
                // content doesn't resize when the system bars hide and show.
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                // Hide the nav bar and status bar
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN)
    }

    override fun onResume() {
        super.onResume()

        hideSystemUI()

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                REQUEST_RECORD_AUDIO
            )
        }
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause")
        taskSynchroniser.stop()
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop")
        stopSDK()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")
        try {
            Log.d(TAG, "Synchroniser closed")
            taskSynchroniser.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
