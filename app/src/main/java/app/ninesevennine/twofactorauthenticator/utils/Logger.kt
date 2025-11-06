package app.ninesevennine.twofactorauthenticator.utils

import android.content.Context
import android.util.Log
import app.ninesevennine.twofactorauthenticator.BuildConfig
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import java.io.File
import java.io.RandomAccessFile
import java.util.concurrent.atomic.AtomicReference
import kotlin.math.max
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

object Logger {
    private const val MAX_LOG_SIZE = 3 * 1024 * 1024 // 3MB
    private const val LOG_FILENAME = "debug.log"
    private const val MAX_TAG_LENGTH = 20
    private const val MAX_MESSAGE_LENGTH = 2000

    private val logFileRef = AtomicReference<File>()

    fun initialize(context: Context) {
        logFileRef.compareAndSet(
            null,
            File(context.noBackupFilesDir.path, LOG_FILENAME).also {
                it.parentFile?.mkdirs()
            }
        )
    }

    fun d(tag: String, message: String) {
        if (BuildConfig.DEBUG) {
            Log.d(tag, message)
        }

        log("D", tag, message)
    }

    fun i(tag: String, message: String) {
        if (BuildConfig.DEBUG) {
            Log.i(tag, message)
        }

        log("I", tag, message)
    }

    fun w(tag: String, message: String) {
        if (BuildConfig.DEBUG) {
            Log.w(tag, message)
        }

        log("W", tag, message)
    }

    fun e(tag: String, message: String) {
        if (BuildConfig.DEBUG) {
            Log.e(tag, message)
        }

        log("E", tag, message)
    }

    @OptIn(ExperimentalTime::class)
    private fun log(level: String, tag: String, message: String) {
        val logFile = logFileRef.get() ?: return

        try {
            val entry = buildString {
                append(formatIsoTimestamp(Clock.System.now()))
                append(" [$level]")
                append(" [${tag.take(MAX_TAG_LENGTH)}]")
                append(": ${message.take(MAX_MESSAGE_LENGTH)}\n")
            }

            writeEntry(logFile, entry)
        } catch (e: Exception) {
            Log.e("Logger", "Log failed", e)
        }
    }

    // ISO 8601 formatting with UTC timezone
    @OptIn(ExperimentalTime::class)
    private fun formatIsoTimestamp(instant: Instant): String {
        val dateTime = instant.toLocalDateTime(TimeZone.UTC)
        return buildString {
            append(dateTime.year)
            append("-")
            append(dateTime.month.number.toString().padStart(2, '0'))
            append("-")
            append(dateTime.day.toString().padStart(2, '0'))
            append("T")

            append(dateTime.hour.toString().padStart(2, '0'))
            append(":")
            append(dateTime.minute.toString().padStart(2, '0'))
            append(":")
            append(dateTime.second.toString().padStart(2, '0'))
            append(".")

            val micros = (dateTime.nanosecond / 1000).toString().padStart(6, '0')
            append(micros)
            append("Z")
        }
    }

    @OptIn(ExperimentalTime::class)
    private fun writeEntry(logFile: File, entry: String) {
        RandomAccessFile(logFile, "rw").use { raf ->
            val fileSize = raf.length()

            // Only truncate if significantly over limit (20% buffer)
            if (fileSize > MAX_LOG_SIZE * 1.2) {
                val keepPosition = max(0, fileSize - MAX_LOG_SIZE / 2)
                raf.seek(keepPosition)

                // Find next newline with optimized scanning
                var newlinePos = keepPosition
                val buffer = ByteArray(1024)
                var bytesRead: Int
                var found = false

                while (raf.filePointer < fileSize && !found) {
                    bytesRead = raf.read(buffer)
                    for (i in 0 until bytesRead) {
                        if (buffer[i] == '\n'.code.toByte()) {
                            newlinePos = raf.filePointer - bytesRead + i + 1
                            found = true
                            break
                        }
                    }
                }

                raf.seek(newlinePos)
                val remainingSize = (fileSize - newlinePos).toInt()
                val remaining = ByteArray(remainingSize)
                raf.readFully(remaining)

                // Overwrite and truncate
                raf.seek(0)
                raf.write(remaining)
                raf.setLength(remainingSize.toLong())

                // Add truncation notice
                val trunkMsg = "${Clock.System.now()} [I] [LOGGER]: Truncated ${fileSize/1024}KB to ${remainingSize/1024}KB\n"
                raf.seek(raf.length())
                raf.writeBytes(trunkMsg)
            }

            // Always append new entry
            raf.seek(raf.length())
            raf.writeBytes(entry)
        }
    }

    fun getFullLog(): String {
        val logFile = logFileRef.get() ?: return ""
        return try {
            logFile.bufferedReader().use { it.readText() }
        } catch (e: Exception) {
            Log.e("Logger", "Failed to read log file", e)
            ""
        }
    }
}