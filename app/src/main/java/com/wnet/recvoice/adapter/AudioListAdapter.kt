package com.wnet.recvoice.adapter

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.wnet.recvoice.R
import com.wnet.recvoice.interfaces.AudioListListener
import es.dmoral.toasty.Toasty
import java.io.File
import java.io.IOException

class AudioListAdapter(var fileLocation: ArrayList<File>, val fileListener: AudioListListener) :
    RecyclerView.Adapter<AudioListAdapter.updateViewHolder>() {

    private lateinit var context: Context
    var playingPosition: Int? = null

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AudioListAdapter.updateViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.item_audio_list, parent, false
        )

        context = itemView.context

        return updateViewHolder(itemView)
    }



    override fun getItemCount(): Int {
        return fileLocation.size
    }

    fun updateDetails(list: ArrayList<File>){
        fileLocation = list
        notifyDataSetChanged()
    }

    fun updateAudioPos(position: Int?){
        playingPosition = position
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: updateViewHolder, position: Int) {
        holder.singleAudioList.text = fileLocation.get(position).name
        if (playingPosition!=null && playingPosition == position){
            holder.playSingleAudio.setImageResource(R.drawable.ic_baseline_pause)
        }else{
            holder.playSingleAudio.setImageResource(R.drawable.ic_baseline_play)
        }
        holder.playSingleAudio.setOnClickListener {
            fileListener.playAudioFile(position)
        }
        holder.shareSingleAudio.setOnClickListener {
            fileListener.shareAudioFile(position)
        }
        holder.deleteSingleAudio.setOnClickListener {
            fileListener.deleteAudioFile(position)
        }
    }

    class updateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val singleAudioList = itemView.findViewById<TextView>(R.id.singleAudio)
        val playSingleAudio = itemView.findViewById<ImageView>(R.id.playSingleAudio)
        val shareSingleAudio = itemView.findViewById<ImageView>(R.id.shareSingleAudio)
        val deleteSingleAudio = itemView.findViewById<ImageView>(R.id.deleteSingleAudio)
    }

}