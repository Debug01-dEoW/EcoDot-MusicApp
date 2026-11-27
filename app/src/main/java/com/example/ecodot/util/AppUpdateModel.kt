package com.example.ecodot.util

import java.io.File

enum class UpdateSource {
    JSON,
    GITHUB
}

data class UpdateSection(
    val title: String,
    val items: List<String>
)

data class AppUpdateInfo(
    val versionCode: Long,
    val versionName: String,
    val downloadUrl: String,
    val changelog: String = "",
    val description: String? = null,
    val sections: List<UpdateSection> = emptyList(),
    val releaseDate: String? = null,
    val changelogUrl: String? = null,
    val guideUrl: String? = null,
    val isMandatory: Boolean = false,
    val source: UpdateSource = UpdateSource.JSON,
    val apkSize: Long = 0L,
    val publishedAt: String? = null
)

sealed interface UpdateStatus {
    object Idle : UpdateStatus
    object Checking : UpdateStatus
    data class Available(val info: AppUpdateInfo) : UpdateStatus
    object UpToDate : UpdateStatus
    data class Downloading(
        val progress: Float, // 0.0f - 1.0f
        val downloadedBytes: Long,
        val totalBytes: Long
    ) : UpdateStatus
    data class ReadyToInstall(val apkFile: File, val info: AppUpdateInfo) : UpdateStatus
    data class Error(val message: String) : UpdateStatus
}
