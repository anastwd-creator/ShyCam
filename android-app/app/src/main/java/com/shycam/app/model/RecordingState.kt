package com.shycam.app.model

import android.net.Uri

data class RecordedVideo(
  val id: Long,
  val uri: Uri,
  val title: String,
  val durationMs: Long,
  val sizeBytes: Long,
  val dateAdded: Long,
  val filePath: String? = null
)

enum class RecordingStatus {
  IDLE,
  STARTING,
  RECORDING,
  PAUSED,
  STOPPING
}

data class CameraRecordingUiState(
  val isCameraInitialized: Boolean = false,
  val hasPermissions: Boolean = false,
  val isBackCamera: Boolean = true,
  val isTorchOn: Boolean = false,
  val isAudioRecordingEnabled: Boolean = true,
  val zoomRatio: Float = 1.0f,
  val minZoomRatio: Float = 1.0f,
  val maxZoomRatio: Float = 5.0f,
  val recordingStatus: RecordingStatus = RecordingStatus.IDLE,
  val recordingDurationSeconds: Long = 0L,
  val lastRecordedVideo: RecordedVideo? = null,
  val recordedVideos: List<RecordedVideo> = emptyList(),
  val errorMessage: String? = null
)
