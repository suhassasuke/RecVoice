package com.wnet.recvoice.interfaces

import java.io.File

interface AudioListListener {

    fun playAudioFile(pos: Int)
    fun shareAudioFile(pos: Int)
    fun deleteAudioFile(pos: Int)
}