package com.example.model

import android.net.Uri

enum class OverlayPreset(val title: String) {
  PIP_CORNER("Floating PiP"),
  GHOST_GUIDE("Ghost Overlay"),
  SPLIT_TOP("Top Half"),
  SPLIT_BOTTOM("Bottom Half"),
  FULL_SCREEN("Full Reference")
}

data class OverlayVideoState(
  val uri: Uri? = null,
  val title: String = "",
  val durationMs: Long = 0L,
  val isLooping: Boolean = true,
  val isMuted: Boolean = true, // Must be muted without audio as requested
  val opacity: Float = 1.0f,
  val scale: Float = 1.0f,
  val offsetX: Float = 0f,
  val offsetY: Float = 0f,
  val preset: OverlayPreset = OverlayPreset.PIP_CORNER,
  val playbackSpeed: Float = 1.0f,
  val isVisible: Boolean = true,
  val isControlsExpanded: Boolean = false,
  val isLocked: Boolean = false
)
