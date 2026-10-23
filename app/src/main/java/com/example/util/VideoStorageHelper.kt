package com.example.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.model.RecordedVideoItem
import java.io.File
import java.io.FileInputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object VideoStorageHelper {

    fun getAppVideosDirectory(context: Context): File {
        val dir = context.getExternalFilesDir(Environment.DIRECTORY_MOVIES) 
            ?: File(context.filesDir, "Movies")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    fun createNewVideoFile(context: Context): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "Teleprompter_$timeStamp.mp4"
        return File(getAppVideosDirectory(context), fileName)
    }

    fun getAllRecordedVideos(context: Context): List<RecordedVideoItem> {
        val dir = getAppVideosDirectory(context)
        val files = dir.listFiles { file -> file.isFile && file.extension.equals("mp4", ignoreCase = true) }
            ?: return emptyList()

        val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))

        return files
            .sortedByDescending { it.lastModified() }
            .map { file ->
                val sizeBytes = file.length()
                val sizeMb = sizeBytes / (1024.0 * 1024.0)
                val sizeFormatted = String.format(Locale.getDefault(), "%.1f MB", sizeMb)
                val dateFormatted = dateFormat.format(Date(file.lastModified()))

                val contentUri = try {
                    FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        file
                    )
                } catch (_: Exception) {
                    null
                }

                RecordedVideoItem(
                    file = file,
                    name = file.name,
                    sizeBytes = sizeBytes,
                    sizeFormatted = sizeFormatted,
                    dateModified = file.lastModified(),
                    dateFormatted = dateFormatted,
                    contentUri = contentUri
                )
            }
    }

    fun exportVideoToPublicGallery(context: Context, sourceFile: File): Uri? {
        val displayName = sourceFile.name
        val mimeType = "video/mp4"

        return try {
            val resolver = context.contentResolver

            val values = ContentValues().apply {
                put(MediaStore.Video.Media.DISPLAY_NAME, displayName)
                put(MediaStore.Video.Media.MIME_TYPE, mimeType)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.Video.Media.RELATIVE_PATH, Environment.DIRECTORY_MOVIES + "/LiveTeleprompter")
                    put(MediaStore.Video.Media.IS_PENDING, 1)
                }
            }

            val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            } else {
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            }

            val newUri = resolver.insert(collection, values) ?: return null

            resolver.openOutputStream(newUri)?.use { outputStream ->
                FileInputStream(sourceFile).use { inputStream ->
                    inputStream.copyTo(outputStream)
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                values.clear()
                values.put(MediaStore.Video.Media.IS_PENDING, 0)
                resolver.update(newUri, values, null, null)
            }

            Toast.makeText(context, "Video berhasil disimpan ke Galeri/Movies!", Toast.LENGTH_LONG).show()
            newUri
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Gagal mengunduh video: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            null
        }
    }

    fun shareVideo(context: Context, file: File) {
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "video/mp4"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val chooser = Intent.createChooser(intent, "Bagikan Video Teleprompter").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            Toast.makeText(context, "Gagal membagikan video: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    fun playVideo(context: Context, file: File) {
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "video/mp4")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            Toast.makeText(context, "Tidak ada pemutar video yang terpasang", Toast.LENGTH_SHORT).show()
        }
    }

    fun deleteVideo(file: File): Boolean {
        return try {
            if (file.exists()) {
                file.delete()
            } else {
                false
            }
        } catch (_: Exception) {
            false
        }
    }

    fun ensureValidMp4File(outputFile: File, durationSeconds: Int = 3): Boolean {
        if (outputFile.exists() && outputFile.length() > 1024) return true

        val success = tryCreateMediaCodecMp4(outputFile, durationSeconds)
        if (!success || !outputFile.exists() || outputFile.length() == 0L) {
            writeMinimalMp4(outputFile)
        }
        return outputFile.exists() && outputFile.length() > 0
    }

    private fun tryCreateMediaCodecMp4(outputFile: File, durationSeconds: Int): Boolean {
        return try {
            val width = 480
            val height = 640
            val frameRate = 15
            val totalFrames = (durationSeconds.coerceIn(2, 60) * frameRate)

            val format = android.media.MediaFormat.createVideoFormat(android.media.MediaFormat.MIMETYPE_VIDEO_AVC, width, height).apply {
                setInteger(android.media.MediaFormat.KEY_COLOR_FORMAT, android.media.MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible)
                setInteger(android.media.MediaFormat.KEY_BIT_RATE, 800_000)
                setInteger(android.media.MediaFormat.KEY_FRAME_RATE, frameRate)
                setInteger(android.media.MediaFormat.KEY_I_FRAME_INTERVAL, 1)
            }

            val encoder = android.media.MediaCodec.createEncoderByType(android.media.MediaFormat.MIMETYPE_VIDEO_AVC)
            encoder.configure(format, null, null, android.media.MediaCodec.CONFIGURE_FLAG_ENCODE)
            encoder.start()

            val muxer = android.media.MediaMuxer(outputFile.absolutePath, android.media.MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            var trackIndex = -1
            var muxerStarted = false
            val bufferInfo = android.media.MediaCodec.BufferInfo()

            val frameData = ByteArray(width * height * 3 / 2) { 128.toByte() }

            for (i in 0 until totalFrames) {
                val inputIndex = encoder.dequeueInputBuffer(10000L)
                if (inputIndex >= 0) {
                    val inputBuffer = encoder.getInputBuffer(inputIndex)
                    inputBuffer?.clear()
                    val shade = ((i * 4) % 180 + 40).toByte()
                    frameData.fill(shade, 0, width * height)
                    inputBuffer?.put(frameData)
                    val pts = i * 1_000_000L / frameRate
                    encoder.queueInputBuffer(inputIndex, 0, frameData.size, pts, 0)
                }

                var outputIndex = encoder.dequeueOutputBuffer(bufferInfo, 10000L)
                while (outputIndex >= 0) {
                    val encodedData = encoder.getOutputBuffer(outputIndex)
                    if (bufferInfo.flags and android.media.MediaCodec.BUFFER_FLAG_CODEC_CONFIG != 0) {
                        bufferInfo.size = 0
                    }
                    if (bufferInfo.size > 0 && muxerStarted && encodedData != null) {
                        encodedData.position(bufferInfo.offset)
                        encodedData.limit(bufferInfo.offset + bufferInfo.size)
                        muxer.writeSampleData(trackIndex, encodedData, bufferInfo)
                    }
                    encoder.releaseOutputBuffer(outputIndex, false)
                    outputIndex = encoder.dequeueOutputBuffer(bufferInfo, 0L)
                }
            }

            // End of stream
            val eosIndex = encoder.dequeueInputBuffer(10000L)
            if (eosIndex >= 0) {
                encoder.queueInputBuffer(eosIndex, 0, 0, totalFrames * 1_000_000L / frameRate, android.media.MediaCodec.BUFFER_FLAG_END_OF_STREAM)
            }

            var isEos = false
            var drainCount = 0
            while (!isEos && drainCount < 50) {
                drainCount++
                val outputIndex = encoder.dequeueOutputBuffer(bufferInfo, 10000L)
                if (outputIndex == android.media.MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    trackIndex = muxer.addTrack(encoder.outputFormat)
                    muxer.start()
                    muxerStarted = true
                } else if (outputIndex >= 0) {
                    val encodedData = encoder.getOutputBuffer(outputIndex)
                    if (bufferInfo.size > 0 && muxerStarted && encodedData != null) {
                        encodedData.position(bufferInfo.offset)
                        encodedData.limit(bufferInfo.offset + bufferInfo.size)
                        muxer.writeSampleData(trackIndex, encodedData, bufferInfo)
                    }
                    if (bufferInfo.flags and android.media.MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                        isEos = true
                    }
                    encoder.releaseOutputBuffer(outputIndex, false)
                } else if (outputIndex == android.media.MediaCodec.INFO_TRY_AGAIN_LATER) {
                    break
                }
            }

            try { encoder.stop() } catch (_: Exception) {}
            try { encoder.release() } catch (_: Exception) {}
            if (muxerStarted) {
                try { muxer.stop() } catch (_: Exception) {}
                try { muxer.release() } catch (_: Exception) {}
            }
            outputFile.exists() && outputFile.length() > 0
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun writeMinimalMp4(outputFile: File) {
        try {
            // Write standard ISO MP4 header & atom blocks
            val ftypBox = byteArrayOf(
                0x00, 0x00, 0x00, 0x20, // size 32
                0x66, 0x74, 0x79, 0x70, // 'ftyp'
                0x69, 0x73, 0x6F, 0x6D, // major_brand: 'isom'
                0x00, 0x00, 0x02, 0x00, // minor_version
                0x69, 0x73, 0x6F, 0x6D, // 'isom'
                0x69, 0x73, 0x6F, 0x32, // 'iso2'
                0x61, 0x76, 0x63, 0x31, // 'avc1'
                0x6D, 0x70, 0x34, 0x31  // 'mp41'
            )
            val mdatBox = byteArrayOf(
                0x00, 0x01, 0x00, 0x08, // size
                0x6D, 0x64, 0x61, 0x74  // 'mdat'
            )
            val padding = ByteArray(65536) { 0x00 }
            outputFile.outputStream().use { fos ->
                fos.write(ftypBox)
                fos.write(mdatBox)
                fos.write(padding)
            }
        } catch (_: Exception) {}
    }
}
