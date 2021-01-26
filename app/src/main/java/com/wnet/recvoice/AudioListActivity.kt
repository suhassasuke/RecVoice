package com.wnet.recvoice

import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.wnet.recvoice.adapter.AudioListAdapter
import com.wnet.recvoice.interfaces.AudioListListener
import com.wnet.recvoice.utils.Constants
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_audio_list.*
import kotlinx.android.synthetic.main.header.*
import java.io.File
import java.io.IOException


class AudioListActivity : AppCompatActivity(), AudioListListener {

    lateinit var audioListAdapter: AudioListAdapter
    val files: ArrayList<File> = arrayListOf()

    private lateinit var player: MediaPlayer
    private var audioStatus = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audio_list)

        audioListAdapter = AudioListAdapter(arrayListOf(), this)

        backbutton.setOnClickListener {
            onBackPressed()
        }

        initRecyclerView()
    }

    fun initRecyclerView() {
        audio_list.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = audioListAdapter
        }
        updateRecyclerView()
    }

    fun updateRecyclerView() {
        val directory = File(applicationContext.filesDir.path)
        val temp: Array<File> = directory.listFiles()
        Log.d("Files", "Size: " + files.size)
        for (i in temp.indices) {
            files.add(temp[i])
        }
        files.sortDescending()
        audioListAdapter.updateDetails(files)
    }

    override fun playAudioFile(pos: Int) {

        player = MediaPlayer()
        try {
            Log.e("startPlaying", files.get(pos).name)
            player!!.setDataSource(files.get(pos).absolutePath)
            player!!.prepare()
        } catch (e: IOException) {
            e.printStackTrace()
            Log.e("LOG_TAG", "prepare() failed")
        }
        player!!.start()//start playing
        audioStatus = true
//        holder.playSingleAudio.setImageResource(R.drawable.ic_baseline_pause)
        audioListAdapter.updateAudioPos(pos)
        player.setOnCompletionListener {
            audioStatus = false
            audioListAdapter.updateAudioPos(null)
//            holder.playSingleAudio.setImageResource(R.drawable.ic_baseline_play)
        }
    }

    override fun shareAudioFile(pos: Int) {
        var uri: Uri? = null
        /**
         * Check if we're running on Android 5.0 or higher
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            uri = FileProvider.getUriForFile(
                this, Constants.PACKAGE_NAME.toString() + ".provider",
                files.get(pos)
            )
        } else {
            uri =
                Uri.fromFile(files.get(pos))
        }

        val share = Intent(Intent.ACTION_SEND)
        share.type = "audio/*"
        share.putExtra(Intent.EXTRA_STREAM, uri)
        startActivity(Intent.createChooser(share, "Share Audio File"))
    }


    override fun deleteAudioFile(pos: Int) {
        if (files.get(pos).exists()) {
            if (files.get(pos).delete()) {
                Toasty.success(baseContext, "Successfully Deleted", Toast.LENGTH_SHORT, true).show()
                files.removeAt(pos)
                audioListAdapter.updateDetails(files)
            } else {
                Toasty.error(baseContext, "Unable to Delete. Try Later!!", Toast.LENGTH_SHORT, true)
                    .show()
            }
        }
    }


}