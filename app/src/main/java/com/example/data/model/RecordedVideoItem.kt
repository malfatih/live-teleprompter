package com.example.data.model

import android.net.Uri
import java.io.File

data class RecordedVideoItem(
    val file: File,
    val name: String,
    val sizeBytes: Long,
    val sizeFormatted: String,
    val dateModified: Long,
    val dateFormatted: String,
    val contentUri: Uri? = null
)
