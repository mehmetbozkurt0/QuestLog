package com.mehmetbozkurt.questlog.feature.proof

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.material3.OutlinedTextFieldDefaults
import com.mehmetbozkurt.questlog.core.designsystem.component.Eyebrow
import com.mehmetbozkurt.questlog.core.designsystem.component.rimColor
import com.mehmetbozkurt.questlog.core.designsystem.uppercaseLocalized
import com.mehmetbozkurt.questlog.R
import com.mehmetbozkurt.questlog.core.designsystem.component.GlassPanel
import com.mehmetbozkurt.questlog.core.designsystem.component.wellColor
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.mehmetbozkurt.questlog.core.designsystem.theme.Spacing
import com.mehmetbozkurt.questlog.core.media.ProofPhotoStore
import kotlinx.coroutines.launch
import java.io.File

data class ProofDraft(
    val note: String?,
    val photoLocalPath: String?,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProofSheet(
    logId: String,
    questTitle: String,
    onDismiss: () -> Unit,
    onConfirm: (ProofDraft) -> Unit,
) {
    val context = LocalContext.current
    val store = remember { ProofPhotoStore(context.applicationContext) }
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var note by remember { mutableStateOf("") }
    var photoPath by remember { mutableStateOf<String?>(null) }
    var processing by remember { mutableStateOf(false) }
    var captureFile by remember { mutableStateOf<File?>(null) }
    var captureUri by remember { mutableStateOf<Uri?>(null) }

    fun importFrom(block: suspend () -> String?) {
        processing = true
        scope.launch {
            photoPath = block() ?: photoPath
            processing = false
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri -> if (uri != null) importFrom { store.importFromUri(logId, uri) } }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { saved ->
        val file = captureFile
        captureUri = null
        captureFile = null
        if (saved && file != null) importFrom { store.importFromFile(logId, file) }
        else file?.delete()
    }

    LaunchedEffect(captureUri) {
        captureUri?.let { cameraLauncher.launch(it) }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.lg)
                .padding(bottom = Spacing.xxl),
        ) {
            Text(
                stringResource(R.string.proof_title).uppercaseLocalized(),
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(Spacing.xs))
            Text(
                questTitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(Spacing.lg))

            val dashColor = MaterialTheme.colorScheme.outline
            val dashShape = MaterialTheme.shapes.large

            Column(
                Modifier
                    .fillMaxWidth()
                    .drawBehind {
                        drawRoundRect(
                            color = dashColor,
                            cornerRadius = CornerRadius(8.dp.toPx()),
                            style = Stroke(
                                width = 1.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(
                                    floatArrayOf(6.dp.toPx(), 5.dp.toPx())
                                ),
                            ),
                        )
                    }
                    .padding(Spacing.lg),
            ) {
                Eyebrow(stringResource(R.string.proof_section_evidence))

                Spacer(Modifier.height(Spacing.sm))

                Text(
                    stringResource(R.string.proof_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(Modifier.height(Spacing.lg))

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    placeholder = { Text(stringResource(R.string.proof_note_placeholder)) },
                    minLines = 2,
                    shape = dashShape,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = wellColor(),
                        focusedContainerColor = wellColor(),
                        unfocusedBorderColor = rimColor(),
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(Modifier.height(Spacing.md))

            val path = photoPath
            if (path != null) {
                Box(Modifier.fillMaxWidth()) {
                    AsyncImage(
                        model = path,
                        contentDescription = stringResource(R.string.proof_photo),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(MaterialTheme.shapes.medium),
                    )
                    FilledIconButton(
                        onClick = {
                            store.delete(path)
                            photoPath = null
                        },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(Spacing.sm),
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = stringResource(R.string.proof_remove_photo),
                        )
                    }
                }
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    OutlinedButton(
                        shape = MaterialTheme.shapes.large,
                        onClick = {
                            galleryLauncher.launch(
                                PickVisualMediaRequest(
                                    ActivityResultContracts.PickVisualMedia.ImageOnly
                                )
                            )
                        },
                        enabled = !processing,
                        modifier = Modifier.weight(1f),
                    ) {
                        Icon(
                            Icons.Default.Image,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(Modifier.width(Spacing.xs))
                        Text(
                            stringResource(R.string.proof_gallery),
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                    OutlinedButton(
                        shape = MaterialTheme.shapes.large,
                        onClick = {
                            val (file, uri) = store.newCaptureTarget(logId)
                            captureFile = file
                            captureUri = uri
                        },
                        enabled = !processing,
                        modifier = Modifier.weight(1f),
                    ) {
                        Icon(
                            Icons.Default.PhotoCamera,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(Modifier.width(Spacing.xs))
                        Text(
                            stringResource(R.string.proof_camera),
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                }
            }

                if (processing) {
                    Spacer(Modifier.height(Spacing.sm))
                    LinearProgressIndicator(Modifier.fillMaxWidth())
                }
            }

            Spacer(Modifier.height(Spacing.lg))

            val bonus = when {
                photoPath != null -> stringResource(R.string.proof_bonus_photo)
                note.isNotBlank() -> stringResource(R.string.proof_bonus_note)
                else -> null
            }

            Button(
                shape = MaterialTheme.shapes.large,
                onClick = {
                    onConfirm(
                        ProofDraft(
                            note = note.trim().takeIf { it.isNotEmpty() },
                            photoLocalPath = photoPath,
                        )
                    )
                },
                enabled = !processing,
                modifier = Modifier.fillMaxWidth().height(52.dp),
            ) {
                Text(
                    if (bonus != null) stringResource(R.string.proof_complete_with_bonus, bonus)
                    else stringResource(R.string.proof_complete),
                    style = MaterialTheme.typography.labelLarge,
                )
            }

            TextButton(
                onClick = {
                    store.delete(photoPath)
                    onConfirm(ProofDraft(null, null))
                },
                enabled = !processing,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    stringResource(R.string.proof_complete_without),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}
