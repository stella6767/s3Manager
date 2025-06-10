package freeapp.me.s3manager.web.dto

import freeapp.me.s3manager.entity.S3Key
import freeapp.me.s3manager.entity.S3Object
import freeapp.me.s3manager.entity.User
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import java.lang.Math.log
import java.lang.Math.pow
import java.time.LocalDateTime
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


data class S3ConnectionRequestDto(
    val region: String,
    val bucket: String,
    @field:NotBlank
    val accessKey: String,
    @field:NotBlank
    val secretKey: String,
) {
    fun toEntity(user: User): S3Key {

        return S3Key(
            user = user,
            region = this.region,
            bucket = this.bucket,
            accessKey = this.accessKey,
            secretKey = this.secretKey
        )
    }
}

data class S3keyInfo(
    val bucket: String,
    val region: String,
){
    companion object {
        fun fromEntity(s3Key: S3Key): S3keyInfo {
            return S3keyInfo(
                bucket = s3Key.bucket,
                region = s3Key.region
            )
        }
    }

}


data class S3ObjectInfo(
    val key: String,
    val name: String,
    val isDirectory: Boolean,
    val size: Long,
    val lastModified: LocalDateTime,
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

    fun toEntity(s3key: S3Key): S3Object {
        return S3Object(
            s3Key = s3key,
            objectKey = key,
            name = name,
            isDirectory = isDirectory,
            size = size,
            lastModified = lastModified,
            extension = extension,
        )
    }

    companion object {
        fun fromEntity(s3Object: S3Object): S3ObjectInfo {

            return S3ObjectInfo(
                key = s3Object.objectKey,
                name = s3Object.name,
                isDirectory = s3Object.isDirectory,
                size = s3Object.size,
                lastModified = s3Object.lastModified,
                extension = s3Object.extension,
            )
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
