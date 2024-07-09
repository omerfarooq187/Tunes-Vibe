package com.example.android.tunesvibe.data.repository

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore.Audio.Media
import android.util.Log
import com.example.android.tunesvibe.data.model.Audio
import kotlinx.coroutines.flow.MutableStateFlow

class AudioRepositoryImpl(private val context: Context) : AudioRepository {

    private val audiosList = MutableStateFlow<List<Audio>>(emptyList())

    private fun addAudio(audio: Audio) {
        audiosList.value += audio
    }

    override fun retrieveSongs():MutableStateFlow<List<Audio>>  {
        val projection = arrayOf(
            Media._ID,
            Media.TITLE,
            Media.ARTIST,
            Media.ALBUM_ID,
            Media.DATA
        )
        val cursor: Cursor? = context.contentResolver.query(
            Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            null
        )
        cursor?.use {
            val columnIndexId = it.getColumnIndexOrThrow(Media._ID)
            val columnIndexTitle = it.getColumnIndexOrThrow(Media.TITLE)
            val columnIndexArtist = it.getColumnIndexOrThrow(Media.ARTIST)
            val columnIndexAlbumId = it.getColumnIndexOrThrow(Media.ALBUM_ID)
            val columnIndexData = it.getColumnIndexOrThrow(Media.DATA)

            while (it.moveToNext()) {
                val id = it.getInt(columnIndexId)
                val title = it.getString(columnIndexTitle)
                val artist = it.getString(columnIndexArtist)
                val albumId = it.getLong(columnIndexAlbumId)
                val data = it.getString(columnIndexData)

                addAudio(
                    Audio(
                        id,
                        title,
                        artist,
                        albumArtUri(context,albumId),
                        data
                    )
                )
            }
        }
        return audiosList
    }

    private fun albumArtUri(context: Context,albumId:Long) :Uri?{
        val albumArtUri = Uri.parse("content://media/external/audio/albumart/$albumId")
        return if (isValidUri(context,albumArtUri)) {
            albumArtUri
        } else {
            null
        }
    }

    private fun isValidUri(context: Context, uri: Uri) :Boolean{
        val contentResolver = context.contentResolver
        return try {
            contentResolver.openInputStream(uri)?.use {
                true
            }?:false
        } catch (e:Exception) {
            e.printStackTrace()
            false
        }
    }
}