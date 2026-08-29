package com.shycam.app.ui.components

import android.view.ViewGroup
import androidx.camera.core.Camera
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceOrientedMeteringPointFactory
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FallbackStrategy
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.VideoCapture
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.shycam.app.ui.theme.OverlayCyan
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.roundToInt

@Composable
fun CameraViewFinder(
  isBackCamera: Boolean,
  isTorchOn: Boolean,
  showGrid: Boolean = true,
  onVideoCaptureReady: (VideoCapture<Recorder>, CameraControl) -> Unit,
  onZoomRangeChanged: (Float, Float) -> Unit,
  onZoomRatioChanged: (Float) -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val lifecycleOwner = LocalLifecycleOwner.current
  val coroutineScope = rememberCoroutineScope()

  var previewView by remember { mutableStateOf<PreviewView?>(null) }
  var cameraControl by remember { mutableStateOf<CameraControl?>(null) }
  var camera by remember { mutableStateOf<Camera?>(null) }

  // Tap-to-focus animation state
  var focusTargetOffset by remember { mutableStateOf<Offset?>(null) }
  val focusRingScale = remember { Animatable(1.5f) }
  val focusRingAlpha = remember { Animatable(0f) }

  val cameraExecutor: ExecutorService = remember { Executors.newSingleThreadExecutor() }

  DisposableEffect(Unit) {
    onDispose {
      cameraExecutor.shutdown()
    }
  }

  // Handle Torch Toggle
  LaunchedEffect(isTorchOn, camera) {
    try {
      camera?.cameraControl?.enableTorch(isTorchOn)
    } catch (_: Exception) {}
  }

  // Setup CameraX when PreviewView, Lifecycle, or Camera Direction changes
  LaunchedEffect(isBackCamera, previewView, lifecycleOwner) {
    val pv = previewView ?: return@LaunchedEffect
    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

    cameraProviderFuture.addListener({
      try {
        val cameraProvider = cameraProviderFuture.get()

        val preview = Preview.Builder().build().also {
          it.setSurfaceProvider(pv.surfaceProvider)
        }

        val qualitySelector = QualitySelector.from(
          Quality.FHD,
          FallbackStrategy.lowerQualityOrHigherThan(Quality.SD)
        )

        val recorder = Recorder.Builder()
          .setQualitySelector(qualitySelector)
          .setExecutor(cameraExecutor)
          .build()

        val videoCapture = VideoCapture.withOutput(recorder)

        val cameraSelector = if (isBackCamera) {
          CameraSelector.DEFAULT_BACK_CAMERA
        } else {
          CameraSelector.DEFAULT_FRONT_CAMERA
        }

        cameraProvider.unbindAll()
        val boundCamera = cameraProvider.bindToLifecycle(
          lifecycleOwner,
          cameraSelector,
          preview,
          videoCapture
        )

        camera = boundCamera
        val control = boundCamera.cameraControl
        cameraControl = control

        // Zoom range
        boundCamera.cameraInfo.zoomState.observe(lifecycleOwner) { zoomState ->
          zoomState?.let {
            onZoomRangeChanged(it.minZoomRatio, it.maxZoomRatio)
            onZoomRatioChanged(it.zoomRatio)
          }
        }

        onVideoCaptureReady(videoCapture, control)
      } catch (e: Exception) {
        e.printStackTrace()
      }
    }, ContextCompat.getMainExecutor(context))
  }

  Box(modifier = modifier.fillMaxSize()) {
    // Camera Preview Surface
    AndroidView(
      factory = { ctx ->
        PreviewView(ctx).apply {
          layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
          )
          scaleType = PreviewView.ScaleType.FILL_CENTER
          implementationMode = PreviewView.ImplementationMode.COMPATIBLE
        }.also {
          previewView = it
        }
      },
      modifier = Modifier
        .fillMaxSize()
        .testTag("camera_preview_view")
        .pointerInput(previewView) {
          detectTransformGestures { _, _, zoom, _ ->
            camera?.let { cam ->
              val currentZoom = cam.cameraInfo.zoomState.value?.zoomRatio ?: 1f
              val newZoom = currentZoom * zoom
              cam.cameraControl.setZoomRatio(newZoom)
            }
          }
        }
        .pointerInput(previewView) {
          detectTapGestures { tapOffset ->
            val pv = previewView ?: return@detectTapGestures
            val factory = SurfaceOrientedMeteringPointFactory(
              pv.width.toFloat(),
              pv.height.toFloat()
            )
            val point = factory.createPoint(tapOffset.x, tapOffset.y)
            val action = FocusMeteringAction.Builder(point).build()

            cameraControl?.startFocusAndMetering(action)

            // Trigger visual tap-to-focus ring
            focusTargetOffset = tapOffset
            coroutineScope.launch {
              focusRingScale.snapTo(1.4f)
              focusRingAlpha.snapTo(1f)
              launch { focusRingScale.animateTo(1.0f, tween(250)) }
              delay(900L)
              focusRingAlpha.animateTo(0f, tween(300))
              focusTargetOffset = null
            }
          }
        }
    )

    // Optional 3x3 Framing Grid lines for cinematic composition
    if (showGrid) {
      Canvas(modifier = Modifier.fillMaxSize()) {
        val strokeWidth = 1.dp.toPx()
        val gridColor = Color.White.copy(alpha = 0.18f)

        val colWidth = size.width / 3f
        val rowHeight = size.height / 3f

        // Vertical lines
        drawLine(
          color = gridColor,
          start = Offset(colWidth, 0f),
          end = Offset(colWidth, size.height),
          strokeWidth = strokeWidth
        )
        drawLine(
          color = gridColor,
          start = Offset(colWidth * 2f, 0f),
          end = Offset(colWidth * 2f, size.height),
          strokeWidth = strokeWidth
        )

        // Horizontal lines
        drawLine(
          color = gridColor,
          start = Offset(0f, rowHeight),
          end = Offset(size.width, rowHeight),
          strokeWidth = strokeWidth
        )
        drawLine(
          color = gridColor,
          start = Offset(0f, rowHeight * 2f),
          end = Offset(size.width, rowHeight * 2f),
          strokeWidth = strokeWidth
        )
      }
    }

    // Tap-to-Focus visual ring
    focusTargetOffset?.let { offset ->
      Box(
        modifier = Modifier
          .offset {
            IntOffset(
              x = (offset.x - 30.dp.toPx()).roundToInt(),
              y = (offset.y - 30.dp.toPx()).roundToInt()
            )
          }
          .size(60.dp)
          .border(
            width = 1.5.dp,
            color = OverlayCyan.copy(alpha = focusRingAlpha.value),
            shape = CircleShape
          )
      )
    }
  }
}
