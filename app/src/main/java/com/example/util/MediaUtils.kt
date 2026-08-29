package com.example.util

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import android.provider.OpenableColumns
import com.example.model.RecordedVideo
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object MediaUtils {

  fun formatDuration(durationSeconds: Long): String {
    val mins = durationSeconds / 60
    val secs = durationSeconds % 60
    return String.format(Locale.US, "%02d:%02d", mins, secs)
  }

  fun formatDurationMs(durationMs: Long): String {
    val totalSeconds = TimeUnit.MILLISECONDS.toSeconds(durationMs)
    return formatDuration(totalSeconds)
  }

  fun formatFileSize(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB")
    val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt().coerceIn(0, units.size - 1)
    return String.format(
      Locale.US,
      "%.1f %s",
      bytes / Math.pow(1024.0, digitGroups.toDouble()),
      units[digitGroups]
    )
  }

  fun formatDate(timestampMs: Long): String {
    val sdf = SimpleDateFormat("MMM d, yyyy · HH:mm", Locale.getDefault())
    return sdf.format(Date(timestampMs))
  }

  fun getFileName(context: Context, uri: Uri): String {
    var result: String? = null
    if (uri.scheme == "content") {
      val cursor: Cursor? = context.contentResolver.query(uri, null, null, null, null)
      cursor?.use {
        if (it.moveToFirst()) {
          val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
          if (nameIndex != -1) {
            result = it.getString(nameIndex)
          }
        }
      }
    }
    if (result == null) {
      result = uri.path
      val cut = result?.lastIndexOf('/') ?: -1
      if (cut != -1 && result != null) {
        result = result!!.substring(cut + 1)
      }
    }
    return result ?: "Video_${System.currentTimeMillis()}"
  }

  fun queryAppVideos(context: Context): List<RecordedVideo> {
    val videos = mutableListOf<RecordedVideo>()
    val projection = arrayOf(
      MediaStore.Video.Media._ID,
      MediaStore.Video.Media.DISPLAY_NAME,
      MediaStore.Video.Media.DURATION,
      MediaStore.Video.Media.SIZE,
      MediaStore.Video.Media.DATE_ADDED
    )

    val selection = "${MediaStore.Video.Media.DISPLAY_NAME} LIKE ?"
    val selectionArgs = arrayOf("CamLoop_%")
    val sortOrder = "${MediaStore.Video.Media.DATE_ADDED} DESC"

    try {
      val cursor = context.contentResolver.query(
        MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
        projection,
        selection,
        selectionArgs,
        sortOrder
      )

      cursor?.use {
        val idColumn = it.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
        val nameColumn = it.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
        val durationColumn = it.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
        val sizeColumn = it.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
        val dateColumn = it.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED)

        while (it.moveToNext()) {
          val id = it.getLong(idColumn)
          val name = it.getString(nameColumn)
          val duration = it.getLong(durationColumn)
          val size = it.getLong(sizeColumn)
          val dateAdded = it.getLong(dateColumn) * 1000L // convert sec to ms

          val contentUri = ContentUris.withAppendedId(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            id
          )

          videos.add(
            RecordedVideo(
              id = id,
              uri = contentUri,
              title = name,
              durationMs = duration,
              sizeBytes = size,
              dateAdded = dateAdded
            )
          )
        }
      }
    } catch (e: Exception) {
      // Fallback query without filter if prefix filter isn't matching
      queryAllRecentVideos(context, videos)
    }

    if (videos.isEmpty()) {
      queryAllRecentVideos(context, videos)
    }

    return videos
  }

  private fun queryAllRecentVideos(context: Context, targetList: MutableList<RecordedVideo>) {
    try {
      val projection = arrayOf(
        MediaStore.Video.Media._ID,
        MediaStore.Video.Media.DISPLAY_NAME,
        MediaStore.Video.Media.DURATION,
        MediaStore.Video.Media.SIZE,
        MediaStore.Video.Media.DATE_ADDED
      )
      val sortOrder = "${MediaStore.Video.Media.DATE_ADDED} DESC"

      val cursor = context.contentResolver.query(
        MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
        projection,
        null,
        null,
        sortOrder
      )

      cursor?.use {
        val idColumn = it.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
        val nameColumn = it.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
        val durationColumn = it.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
        val sizeColumn = it.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
        val dateColumn = it.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED)

        var count = 0
        while (it.moveToNext() && count < 25) {
          val id = it.getLong(idColumn)
          val name = it.getString(nameColumn)
          val duration = it.getLong(durationColumn)
          val size = it.getLong(sizeColumn)
          val dateAdded = it.getLong(dateColumn) * 1000L

          val contentUri = ContentUris.withAppendedId(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            id
          )

          if (targetList.none { v -> v.id == id }) {
            targetList.add(
              RecordedVideo(
                id = id,
                uri = contentUri,
                title = name,
                durationMs = duration,
                sizeBytes = size,
                dateAdded = dateAdded
              )
            )
            count++
          }
        }
      }
    } catch (_: Exception) {
    }
  }
}
