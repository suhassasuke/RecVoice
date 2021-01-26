package com.wnet.recvoice

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.header.*
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


private const val LOG_TAG = "AudioRecordTest"
private const val REQUEST_RECORD_AUDIO_PERMISSION = 200

enum class RecordState {
    START, PAUSE, RESUME, STOP
}

class MainActivity : AppCompatActivity() {


    private var fileName: String = ""

    private var recordButton: LinearLayout? = null
    private var recordIcon: FloatingActionButton? = null
    private var recorder: MediaRecorder? = null

    private var stopOrListButton: FloatingActionButton? = null

    private var badgeNumber = 0

    private var seconds = 0
    private var running = false

    var mRecordingState = RecordState.START

    val ShowHideHandler = Handler()
    val runTimerHandler = Handler()

    // Requesting permission to RECORD_AUDIO
    private var permissionToRecordAccepted = false
    private var permissions: Array<String> = arrayOf(
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE
    )

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionToRecordAccepted = if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            grantResults[0] == PackageManager.PERMISSION_GRANTED || grantResults[1] == PackageManager.PERMISSION_GRANTED || grantResults[2] == PackageManager.PERMISSION_GRANTED
        } else {
            false
        }
        if (!permissionToRecordAccepted) finish()
    }

    private fun initRecord() {

        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
            || ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
            || ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION)
        } else {
            // Record to the external cache directory for visibility
            val file_path = applicationContext.filesDir.path
//            val file_path = Environment.getExternalStorageDirectory().path

            var file: File = File(file_path)
            if (!file.exists()) {
                file.mkdirs()
            }

            val sdf = SimpleDateFormat("'RecVoice-'ddMyyyyhhmmss")
            val currentDate = sdf.format(Date())
//            val current_time = Date(java.lang.Long.valueOf(date))

            fileName = file.absolutePath + "/" + currentDate + ".mp3";
//            fileName = file.absolutePath + "/audiorecordtest.3gp" //"/" + current_time + ".3gp";
//            fileName = "${externalCacheDir!!.absolutePath}/audiorecordtest.3gp"
//            startRecording()
            Log.d("File Path: ", fileName)
        }
    }

    override fun onResume() {
        super.onResume()
        //initiating record
        initRecord()
    }


    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION)

        recordButton = findViewById(R.id.recordButton)
        recordIcon = findViewById(R.id.recordIcon)
        stopOrListButton = findViewById(R.id.StopOrListButton)
//        playButton = findViewById(R.id.playButton)
        backbutton.visibility = View.GONE


        recordButton!!.setOnClickListener {
            when (mRecordingState) {
                RecordState.START -> {
                    recordIcon!!.setImageDrawable(resources.getDrawable(R.drawable.ic_pause_white))
                    stopOrListButton!!.setImageDrawable(resources.getDrawable(R.drawable.ic_baseline_stop))
                    startRecording()
                    mRecordingState = RecordState.PAUSE
                }
                RecordState.PAUSE -> {
                    recordIcon!!.setImageDrawable(resources.getDrawable(R.drawable.ic_white_circle))
                    stopOrListButton!!.setImageDrawable(resources.getDrawable(R.drawable.ic_baseline_stop))
                    pauseRecording()
                    mRecordingState = RecordState.RESUME
                }
                RecordState.RESUME -> {
                    recordIcon!!.visibility = View.VISIBLE
                    recordIcon!!.setImageDrawable(resources.getDrawable(R.drawable.ic_pause_white))
                    stopOrListButton!!.setImageDrawable(resources.getDrawable(R.drawable.ic_baseline_stop))
                    resumeRecording()
                    mRecordingState = RecordState.PAUSE
                }
            }
        }

        stopOrListButton!!.setOnClickListener {
            if (mRecordingState.equals(RecordState.PAUSE) || mRecordingState.equals(RecordState.RESUME)) {
                recordIcon!!.visibility = View.VISIBLE
                recordIcon!!.setImageDrawable(resources.getDrawable(R.drawable.ic_white_circle))
                stopOrListButton!!.setImageDrawable(resources.getDrawable(R.drawable.ic_baseline_folder))
                mRecordingState = RecordState.START
                textviewtime.text = "00:00:00"
                stopRecording()
                badge.visibility = View.VISIBLE
                badgeNumber = badgeNumber + 1
                badge.setText(badgeNumber.toString())
                //re-initiating new record file
                initRecord()
            } else {
                badge.visibility = View.GONE
                badgeNumber = 0
                //go to audiolist activity
                val intent = Intent(this, AudioListActivity::class.java)
                // To pass any data to next activity
//                intent.putExtra("keyIdentifier", value)
                // start your next activity
                startActivity(intent)
            }
        }

        settingButton.setOnClickListener {
            startActivity(Intent(this, SettingActivity::class.java))
        }

    }

    private fun startRecording() {
        recorder = MediaRecorder()
        recorder!!.setAudioSource(MediaRecorder.AudioSource.MIC)
        recorder!!.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        recorder!!.setOutputFile(fileName)
        recorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        Log.e("startRecording", fileName)
        try {
            recorder!!.prepare()
        } catch (e: IOException) {
            e.printStackTrace()
            Log.e(LOG_TAG, "prepare() failed")
        }
        runTimer()
        runShowHideButton()
        recorder!!.start()
        startTime()

    }

    private fun pauseRecording() {
        pauseTime()
        Handler(Looper.getMainLooper()).postDelayed({
            //Do something after 100ms
            recorder!!.pause()
        }, 100)

    }

    private fun resumeRecording() {
        startTime()
        recorder!!.resume()
    }

    private fun stopRecording() {
        stopTime()
        recorder?.apply {
            stop()
            reset()
            release()
        }
        recorder = null
        ShowHideHandler.removeCallbacksAndMessages(null)
        runTimerHandler.removeCallbacksAndMessages(null)
    }

    override fun onDestroy() {
        super.onDestroy()
        recorder?.release()
        recorder = null
    }

    fun startTime() {
        running = true
    }

    fun pauseTime() {
        running = false
    }

    fun stopTime() {
        running = false
        seconds = 0
    }

    private fun runShowHideButton() {

        ShowHideHandler.post(object : Runnable {
            @SuppressLint("RestrictedApi")
            override fun run() {
                if (!running && !textviewtime.text.equals("00:00:00")) {
                    if (recordIcon!!.isVisible)
                        recordIcon!!.visibility = View.INVISIBLE
                    else
                        recordIcon!!.visibility = View.VISIBLE
                }
                ShowHideHandler.postDelayed(this, 800)
            }

        })
    }


    private fun runTimer() {
        runTimerHandler.post(object : Runnable {
            override fun run() {
                if (running) {
                    val hours: Int = seconds / 3600
                    val minutes: Int = seconds % 3600 / 60
                    val secs: Int = seconds % 60

                    // Format the seconds into hours, minutes,
                    // and seconds.
                    val time = java.lang.String
                        .format(
                            Locale.getDefault(),
                            "%02d:%02d:%02d", hours,
                            minutes, secs
                        )

                    // Set the text view text.
                    textviewtime.text = time

                    // If running is true, increment the
                    // seconds variable.

                    seconds++
                }

                // Post the code again
                // with a delay of 1 second.
                runTimerHandler.postDelayed(this, 1000)
            }
        })
    }

}