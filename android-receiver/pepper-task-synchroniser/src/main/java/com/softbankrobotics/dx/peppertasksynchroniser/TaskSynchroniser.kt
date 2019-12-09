package com.softbankrobotics.dx.peppertasksynchroniser

import android.content.Context
import android.util.Log
import io.chirp.chirpsdk.ChirpSDK
import io.chirp.chirpsdk.models.ChirpSDKState

class TaskSynchroniser(
    context: Context,
    appKey: String,
    appSecret: String,
    appConfig: String
) {

    private val TAG = "PepperSynchroniser"

    var chirpConnect: ChirpSDK = ChirpSDK(context, appKey, appSecret)

    init {
        Log.v(TAG, "Connect Version: " + chirpConnect.version)

        val setConfigError = chirpConnect.setConfig(appConfig)
        if (setConfigError.code > 0) {
            Log.e(TAG, setConfigError.message)
        }
    }

    fun start(): Boolean {
        val error = chirpConnect.start()
        if (error.code > 0) {
            Log.e(TAG, "ConnectError: " + error.message)
            return false
        }
        return  true
    }

    fun stop(): Boolean {
        val error = chirpConnect.stop()
        if (error.code > 0) {
            Log.e(TAG, "ConnectError: " + error.message)
            return false
        }
        return true
    }

    fun close() {
        try {
            chirpConnect.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun isStopped(): Boolean {
        return chirpConnect.getState() === ChirpSDKState.CHIRP_SDK_STATE_STOPPED
    }
}