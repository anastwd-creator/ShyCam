package com.example.ui.components

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CameraBlack
import com.example.ui.theme.CameraBorder
import com.example.ui.theme.CameraSurfaceDark
import com.example.ui.theme.CameraSurfaceElevated
import com.example.ui.theme.OverlayCyan
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

@Composable
fun PermissionRequestView(
  onRequestPermissions: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current

  Surface(
    color = CameraBlack,
    modifier = modifier.fillMaxSize()
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(28.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      // Big camera hero badge
      Box(
        modifier = Modifier
          .size(96.dp)
          .clip(CircleShape)
          .background(CameraSurfaceElevated)
          .border(2.dp, OverlayCyan, CircleShape),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.Videocam,
          contentDescription = null,
          tint = OverlayCyan,
          modifier = Modifier.size(48.dp)
        )
      }

      Spacer(modifier = Modifier.height(28.dp))

      Text(
        text = "Camera & Audio Access",
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        color = TextPrimary,
        textAlign = TextAlign.Center
      )

      Spacer(modifier = Modifier.height(10.dp))

      Text(
        text = "CamLoop records video with simultaneous reference overlay loops from your gallery.",
        fontSize = 14.sp,
        color = TextSecondary,
        textAlign = TextAlign.Center,
        lineHeight = 20.sp
      )

      Spacer(modifier = Modifier.height(32.dp))

      // Permission checklist items
      PermissionFeatureRow(
        icon = Icons.Default.CameraAlt,
        title = "Camera",
        description = "To preview and record high-definition video"
      )

      Spacer(modifier = Modifier.height(14.dp))

      PermissionFeatureRow(
        icon = Icons.Default.Mic,
        title = "Microphone",
        description = "To capture clear sound for your recordings"
      )

      Spacer(modifier = Modifier.height(14.dp))

      PermissionFeatureRow(
        icon = Icons.Default.VideoLibrary,
        title = "Gallery Access",
        description = "To import reference videos for silent loop overlays"
      )

      Spacer(modifier = Modifier.height(36.dp))

      Button(
        onClick = onRequestPermissions,
        modifier = Modifier
          .fillMaxWidth()
          .height(52.dp)
          .testTag("grant_permissions_button"),
        colors = ButtonDefaults.buttonColors(
          containerColor = OverlayCyan,
          contentColor = CameraBlack
        ),
        shape = RoundedCornerShape(14.dp)
      ) {
        Text(
          text = "Grant Permissions",
          fontSize = 16.sp,
          fontWeight = FontWeight.Bold
        )
      }

      Spacer(modifier = Modifier.height(12.dp))

      OutlinedButton(
        onClick = {
          val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
          }
          context.startActivity(intent)
        },
        modifier = Modifier
          .fillMaxWidth()
          .height(48.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
        border = ButtonDefaults.outlinedButtonBorder.copy(
          brush = androidx.compose.ui.graphics.SolidColor(CameraBorder)
        ),
        shape = RoundedCornerShape(14.dp)
      ) {
        Text(
          text = "Open App Settings",
          fontSize = 14.sp
        )
      }
    }
  }
}

@Composable
private fun PermissionFeatureRow(
  icon: ImageVector,
  title: String,
  description: String
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(14.dp))
      .background(CameraSurfaceDark)
      .border(1.dp, CameraBorder, RoundedCornerShape(14.dp))
      .padding(14.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Box(
      modifier = Modifier
        .size(40.dp)
        .clip(CircleShape)
        .background(CameraSurfaceElevated),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = OverlayCyan,
        modifier = Modifier.size(20.dp)
      )
    }

    Spacer(modifier = Modifier.width(14.dp))

    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = title,
        color = TextPrimary,
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold
      )
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = description,
        color = TextTertiary,
        fontSize = 12.sp
      )
    }
  }
}
