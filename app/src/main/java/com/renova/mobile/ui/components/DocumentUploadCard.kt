package com.renova.mobile.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.rememberAsyncImagePainter
import com.renova.mobile.R
import com.renova.mobile.ui.theme.LocalRenovaColors
import com.renova.mobile.ui.theme.RenovaColors
import androidx.compose.ui.unit.sp
import com.renova.mobile.ui.viewmodels.ProfileViewModel
import com.renova.mobile.ui.viewmodels.VerificationStatus

enum class DocumentType {
    SELFIE,
    INE_FRONT,
    INE_BACK
}

data class DocumentCardData(
    val type: DocumentType,
    val imageUri: Uri? = null,
    val imageUrl: String? = null
)

@Composable
fun ImagePreviewDialog(
    imageSource: Any,
    title: String,
    onDismiss: () -> Unit,
    verificationStatus: VerificationStatus?
) {
    val colors = LocalRenovaColors.current

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = colors.cardBackground
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.textPrimary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.close),
                            tint = when (verificationStatus) {
                                VerificationStatus.REJECTED -> RenovaColors.Error
                                VerificationStatus.VERIFIED -> colors.primaryColor
                                VerificationStatus.PENDING -> RenovaColors.Warning
                                else -> colors.textSecondary
                            },
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Image(
                    painter = rememberAsyncImagePainter(imageSource),
                    contentDescription = title,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
            }
        }
    }
}

@Composable
fun DocumentUploadCard(
    document: DocumentCardData,
    verificationStatus: VerificationStatus,
    onImageSelected: (Uri) -> Unit,
    documentImages: Map<String, ByteArray>,
    isUploading: Boolean = false,
    modifier: Modifier = Modifier
) {
    val colors = LocalRenovaColors.current
    var showImagePreview by remember { mutableStateOf(false) }

    // Determinar si se puede subir/cambiar imagen
    val canUploadImage = verificationStatus == VerificationStatus.REJECTED ||
            verificationStatus == VerificationStatus.NO_DOCS

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            if (canUploadImage && !isUploading) {
                onImageSelected(it)
            }
        }
    }

    val label = when (document.type) {
        DocumentType.SELFIE -> stringResource(R.string.selfie)
        DocumentType.INE_FRONT -> stringResource(R.string.ine_front)
        DocumentType.INE_BACK -> stringResource(R.string.ine_back)
    }

    val icon = when (document.type) {
        DocumentType.SELFIE -> Icons.Default.CameraAlt
        else -> Icons.Default.CreditCard
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = when (verificationStatus){
            VerificationStatus.REJECTED -> RenovaColors.Error.copy(alpha = 0.125f)
            VerificationStatus.VERIFIED -> colors.primaryColor.copy(alpha = 0.125f)
            VerificationStatus.PENDING -> RenovaColors.Warning.copy(alpha = 0.125f)
            else -> colors.textSecondary.copy(alpha = 0.125f)
        },
        border = androidx.compose.foundation.BorderStroke(
            width =  1.5.dp,
            color = when (verificationStatus) {
                VerificationStatus.REJECTED -> RenovaColors.Error
                VerificationStatus.VERIFIED -> colors.primaryColor
                VerificationStatus.PENDING -> RenovaColors.Warning
                else -> colors.textSecondary
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(all = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        when (verificationStatus) {
                            VerificationStatus.REJECTED -> RenovaColors.Error.copy(alpha = 0.125f)
                            VerificationStatus.VERIFIED -> colors.primaryColor.copy(alpha = 0.125f)
                            VerificationStatus.PENDING -> RenovaColors.Warning.copy(alpha = 0.125f)
                            else -> colors.textSecondary.copy(alpha = 0.125f)
                        }
                    )
                    .then(
                        if ((verificationStatus == VerificationStatus.REJECTED ||
                                    verificationStatus == VerificationStatus.NO_DOCS) && !isUploading) {
                            Modifier
                                .border(
                                    width = 1.dp,
                                    color = if (verificationStatus == VerificationStatus.REJECTED)
                                        RenovaColors.Error
                                    else
                                        colors.textSecondary,
                                    shape = RoundedCornerShape(6.dp)
                                )
                                .clickable { launcher.launch("image/*") }
                        } else {
                            Modifier.clickable {
                                if (!isUploading && (document.imageUri != null || document.imageUrl != null)) {
                                    showImagePreview = true
                                }
                            }
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                when {
                    isUploading -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(8.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(32.dp),
                                color = when (verificationStatus) {
                                    VerificationStatus.REJECTED -> RenovaColors.Error
                                    VerificationStatus.VERIFIED -> colors.primaryColor
                                    VerificationStatus.PENDING -> RenovaColors.Warning
                                    else -> colors.textSecondary
                                },
                                strokeWidth = 3.dp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = stringResource(R.string.uploading),
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.textSecondary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    document.imageUri != null || document.imageUrl != null -> {
                        val imageSource = when {
                            document.imageUri != null -> document.imageUri
                            document.imageUrl?.startsWith("memory://") == true -> {
                                val key = document.imageUrl.removePrefix("memory://")
                                documentImages[key]
                            }
                            else -> document.imageUrl
                        }

                        Image(
                            painter = rememberAsyncImagePainter(imageSource),
                            contentDescription = label,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )

                        if (verificationStatus != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.3f)),
                                contentAlignment = Alignment.Center
                            ) {
                            }
                        }
                    }

                    else -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = if (verificationStatus == VerificationStatus.VERIFIED)
                                    icon
                                else
                                    Icons.Default.AddPhotoAlternate,
                                contentDescription = null,
                                tint = when (verificationStatus) {
                                    VerificationStatus.REJECTED -> RenovaColors.Error
                                    VerificationStatus.PENDING -> RenovaColors.Warning
                                    else -> colors.textSecondary
                                },
                                modifier = Modifier.size(32.dp)
                            )
                            if (verificationStatus != VerificationStatus.VERIFIED) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = stringResource(R.string.tap_to_upload),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = when (verificationStatus) {
                                        VerificationStatus.REJECTED -> RenovaColors.Error
                                        VerificationStatus.PENDING -> RenovaColors.Warning
                                        else -> colors.textSecondary
                                    },
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textSecondary
                )

                Spacer(modifier = Modifier.width(4.dp))

                when (verificationStatus) {
                    VerificationStatus.VERIFIED -> {
                        Icon(
                            imageVector = Icons.Default.VerifiedUser,
                            contentDescription = null,
                            tint = colors.primaryColor,
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    VerificationStatus.REJECTED -> {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = null,
                            tint = RenovaColors.Error,
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    VerificationStatus.PENDING -> {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = RenovaColors.Warning,
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    else -> {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = null,
                            tint = RenovaColors.Error,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }

    if (showImagePreview && !isUploading && (document.imageUri != null || document.imageUrl != null)) {
        val imageSource = when {
            document.imageUri != null -> document.imageUri
            document.imageUrl?.startsWith("memory://") == true -> {
                val key = document.imageUrl.removePrefix("memory://")
                documentImages[key]
            }
            else -> document.imageUrl
        }

        ImagePreviewDialog(
            imageSource = imageSource!!,
            title = label,
            onDismiss = { showImagePreview = false },
            verificationStatus = verificationStatus
        )
    }
}

@Composable
fun DocumentsUploadSection(
    documents: List<DocumentCardData>,
    verificationStatus: VerificationStatus,
    onImageSelected: (DocumentType, Uri) -> Unit,
    documentImages: Map<String, ByteArray> = emptyMap(),
    documentUploadState: ProfileViewModel.DocumentUploadState = ProfileViewModel.DocumentUploadState.Idle,
    modifier: Modifier = Modifier
) {
    val colors = LocalRenovaColors.current

    var isExpanded by remember { mutableStateOf(false) }

    // Determinar comportamiento según el estado
    val showCollapseButton = when (verificationStatus) {
        VerificationStatus.VERIFIED, VerificationStatus.PENDING -> true
        else -> false
    }

    val forceExpanded = when (verificationStatus) {
        VerificationStatus.REJECTED, VerificationStatus.NO_DOCS -> true
        else -> false
    }

    val actuallyExpanded = if (forceExpanded) true else isExpanded

    // Animación de la flecha
    val arrowRotation by animateFloatAsState(
        targetValue = if (actuallyExpanded) 180f else 0f,
        animationSpec = tween(300),
        label = "arrow"
    )

    Surface(
        modifier = modifier
            .padding(bottom = 80.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = colors.cardBackground,
        border = androidx.compose.foundation.BorderStroke(
            width = 2.dp,
            color = when (verificationStatus) {
                VerificationStatus.REJECTED -> RenovaColors.Error
                VerificationStatus.VERIFIED -> colors.primaryColor
                VerificationStatus.PENDING -> RenovaColors.Warning
                else -> colors.textSecondary
            }
        )
    ) {
        Column {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (showCollapseButton) {
                            Modifier.clickable { isExpanded = !isExpanded }
                        } else Modifier
                    ),
                color = when (verificationStatus) {
                    VerificationStatus.REJECTED -> RenovaColors.Error.copy(alpha = 0.125f)
                    VerificationStatus.VERIFIED -> colors.primaryColor.copy(alpha = 0.125f)
                    VerificationStatus.PENDING -> RenovaColors.Warning.copy(alpha = 0.125f)
                    else -> colors.textSecondary.copy(alpha = 0.125f)
                }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = when (verificationStatus) {
                                VerificationStatus.VERIFIED -> Icons.Default.VerifiedUser
                                VerificationStatus.REJECTED -> Icons.Default.Cancel
                                VerificationStatus.PENDING -> Icons.Default.Schedule
                                else -> Icons.Default.CameraAlt
                            },
                            contentDescription = null,
                            tint = when (verificationStatus) {
                                VerificationStatus.REJECTED -> RenovaColors.Error
                                VerificationStatus.VERIFIED -> colors.primaryColor
                                VerificationStatus.PENDING -> RenovaColors.Warning
                                else -> colors.textSecondary
                            },
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = stringResource(
                                when (verificationStatus) {
                                    VerificationStatus.VERIFIED -> R.string.documentation_verified
                                    VerificationStatus.REJECTED -> R.string.documentation_rejected
                                    VerificationStatus.PENDING -> R.string.documentation_pending
                                    else -> R.string.upload_documentation
                                }
                            ),
                            style = MaterialTheme.typography.titleSmall,
                            color = colors.textPrimary
                        )
                    }

                    if (showCollapseButton) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Toggle",
                            tint = when (verificationStatus) {
                                VerificationStatus.REJECTED -> RenovaColors.Error
                                VerificationStatus.VERIFIED -> colors.primaryColor
                                VerificationStatus.PENDING -> RenovaColors.Warning
                                else -> colors.textSecondary
                            },
                            modifier = Modifier
                                .size(32.dp)
                                .rotate(arrowRotation)
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = actuallyExpanded,
                enter = expandVertically(tween(300)),
                exit = shrinkVertically(tween(300))
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        documents.forEach { doc ->
                            val isCurrentlyUploading = documentUploadState is ProfileViewModel.DocumentUploadState.Loading &&
                                    documentUploadState.documentType == doc.type

                            DocumentUploadCard(
                                document = doc,
                                verificationStatus = verificationStatus,
                                onImageSelected = { uri ->
                                    if (verificationStatus == VerificationStatus.REJECTED ||
                                        verificationStatus == VerificationStatus.NO_DOCS) {
                                        onImageSelected(doc.type, uri)
                                    }
                                },
                                documentImages = documentImages,
                                isUploading = isCurrentlyUploading,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    if (verificationStatus == VerificationStatus.REJECTED ||
                        verificationStatus == VerificationStatus.NO_DOCS) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            shape = RoundedCornerShape(8.dp),
                            color = RenovaColors.Warning.copy(alpha = 0.125f),
                            border = androidx.compose.foundation.BorderStroke(
                                width = 1.5.dp,
                                color = RenovaColors.Warning
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = if (verificationStatus == VerificationStatus.REJECTED)
                                        Icons.Default.Warning
                                    else
                                        Icons.Default.Info,
                                    contentDescription = null,
                                    tint = RenovaColors.Warning,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = stringResource(R.string.documentation_auto_upload_hint_compressed),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.textPrimary,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}