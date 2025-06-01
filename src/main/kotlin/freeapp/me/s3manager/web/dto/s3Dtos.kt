package freeapp.me.s3manager.web.dto

import java.lang.Math.log
import java.lang.Math.pow
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter


data class InitialUploadReqDto(
    val filename: String,
) {


}

data class InitialUploadDto(
    val uploadId: String,
    val fileKey: String,
)


data class S3UploadSignedUrlDto(
    val fileKey: String,
    val uploadId: String,
    val partNumber: Int
)


data class S3UploadSignedUrlResDto(
    val partNumber: Int,
    val preSignedUrl: String,
)


data class S3UploadResultDto(
    val fileKey: String,
    val name: String,
    val size: Long,
    val fileUrl: String,
    val isRequest: Boolean,
    ) {


}


data class S3UploadCompleteDto(
    val taskId: Long,
    val uploadId: String,
    val fileKey: String,
    val isRequest: Boolean,
    val parts: List<S3UploadPartsDetailDto> = mutableListOf()
) {

}

data class S3UploadPartsDetailDto(
    val awsETag: String,
    val partNumber: Int
)


data class S3UploadAbortDto(
    val uploadId: String,
    val filename: String
)

data class PresignedURLDto(
    val url: String,
)


data class S3ConnectionRequestDto (
    val region: String,
    val bucket: String,
    val accessKey: String,
    val secretKey: String,
)

data class S3Config(
    val region: String,
    val bucket: String,
    val accessKey: String,
    val secretKey: String
)



data class S3ObjectInfo(
    val key: String,
    val name: String,
    val isDirectory: Boolean,
    val size: Long,
    val lastModified: Instant,
    val extension: String
) {
    fun getFormattedSize(): String {
        if (size == 0L) return "0 B"

        val k = 1024
        val sizes = arrayOf("B", "KB", "MB", "GB", "TB")
        val i = kotlin.math.floor(log(size.toDouble()) / log(k.toDouble())).toInt()

        return "%.1f %s".format(size / pow(k.toDouble(), i.toDouble()), sizes[i])
    }

    fun getFormattedDate(): String {
        val formatter = DateTimeFormatter.ofPattern("yyyy. M. d. a h:mm:ss")
            .withZone(ZoneId.of("Asia/Seoul"))
        return formatter.format(lastModified)
    }

    fun getFileIcon(): String {
        return when {
            isDirectory -> "📁"
            extension.lowercase() in listOf("jpg", "jpeg", "png", "gif", "svg", "webp") -> "🖼️"
            extension.lowercase() in listOf("pdf", "doc", "docx", "txt", "md") -> "📄"
            extension.lowercase() in listOf("mp4", "avi", "mov", "wmv", "mkv") -> "🎥"
            extension.lowercase() in listOf("mp3", "wav", "flac", "m4a") -> "🎵"
            extension.lowercase() in listOf("zip", "rar", "tar", "gz", "7z") -> "📦"
            extension.lowercase() in listOf("js", "html", "css", "json", "xml", "py", "java", "kt") -> "💻"
            else -> "📄"
        }
    }
}


data class FolderTreeNode(
    val name: String,
    val path: String,
    val children: MutableList<FolderTreeNode> = mutableListOf()
)

data class BreadcrumbItem(
    val name: String,
    val path: String
)
