package freeapp.me.s3manager.web.dto

import freeapp.me.s3manager.entity.S3Key
import freeapp.me.s3manager.entity.User
import jakarta.validation.constraints.NotBlank
import java.lang.Math.log
import java.lang.Math.pow
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

enum class UploadType() {
    SINGLE,
    MULTIPART
}


data class UploadInitiateResponseDto(
    val uploadType: UploadType,
    val fileKey: String,
    val presignedUrl: String? = null, // 작은 파일용
    val uploadId: String? = null      // 큰 파일용
)


data class InitialUploadReqDto(
    val filename: String,
    val fileSize: Long,
    val targetObjectDir: String,
    val contentType: String
)



data class PresignedPartRequestDto(
    val fileKey: String,
    val uploadId: String,
    val partNumber: Int
)


data class PresignedPartResponseDto(
    val partNumber: Int,
    val preSignedUrl: String,
)


data class S3UploadResultDto(
    val fileKey: String,
    //val name: String,
    //val size: Long,
    //val fileUrl: String,
) {


}


data class S3UploadCompleteDto(
    val uploadId: String,
    val fileKey: String,
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
) {
    companion object {
        fun fromEntity(s3Key: S3Key): S3keyInfo {
            return S3keyInfo(
                bucket = s3Key.bucket,
                region = s3Key.region
            )
        }
    }

}

// 페이지네이션 결과 DTO
data class PaginatedS3Objects(
    val objects: MutableList<S3ObjectInfo>,
    val continuationToken: String,
    val isLast: Boolean
)


data class S3BrowserRequestDto(
    val prefix: String = "",
    val continuationToken: String = "",
    val size: Int = 20,
)


data class S3ObjectInfo(
    val key: String,
    val name: String,
    val isDirectory: Boolean,
    val size: Long,
    val lastModified: LocalDateTime?,
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
        val formatter =
            DateTimeFormatter.ofPattern("yyyy. M. d. a h:mm:ss")
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

    companion object {

        fun toDirectoryDto(key: String, name: String): S3ObjectInfo {

            return S3ObjectInfo(
                key = key,
                name = name,
                isDirectory = true,
                size = 0L,
                lastModified = null,
                extension = ""
            )

        }
    }

}


data class DownloadDto(
    val url: String,
    val filename: String,
)

data class DownloadEventDto(
    val downloadFile: DownloadDto
)
