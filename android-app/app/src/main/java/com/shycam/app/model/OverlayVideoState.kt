package com.shycam.app.model

import android.net.Uri
import androidx.annotation.StringRes
import com.shycam.app.R

enum class OverlayPreset(@StringRes val titleRes: Int) {
  PIP_CORNER(R.string.preset_pip_corner),
  GHOST_GUIDE(R.string.preset_ghost_guide),
  SPLIT_TOP(R.string.preset_split_top),
  SPLIT_BOTTOM(R.string.preset_split_bottom),
  FULL_SCREEN(R.string.preset_full_screen)
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
  val previousPreset: OverlayPreset = OverlayPreset.PIP_CORNER,
  val playbackSpeed: Float = 1.0f,
  val isVisible: Boolean = true,
  val isControlsExpanded: Boolean = false,
  val isLocked: Boolean = false
)
