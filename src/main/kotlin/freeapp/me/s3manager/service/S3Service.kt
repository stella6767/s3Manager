package freeapp.me.s3manager.service

import freeapp.me.s3manager.entity.User
import freeapp.me.s3manager.repo.S3KeyRepository
import freeapp.me.s3manager.web.dto.*
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.*
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import java.time.Instant
import kotlin.math.ceil


@Service
class S3Service(
    private val s3Client: S3Client,
    private val s3PreSigner: S3Presigner,
    private val s3KeyRepository: S3KeyRepository,
) {

    private val log = KotlinLogging.logger { }


    @Value("\${s3.bucket}")
    private lateinit var bucket: String

    @Value("\${s3.url}")
    private lateinit var staticUrl: String

    private val s3Utilities = s3Client.utilities()


    fun saveS3Key(
        user: User,
        s3ConnectionRequestDto: S3ConnectionRequestDto
    ) {
        val s3Key =
            s3ConnectionRequestDto.toEntity(user)
        s3KeyRepository.save(s3Key)
    }

    fun testConnection(
        connectReq: S3ConnectionRequestDto
    ) {
        val s3Client =
            createS3Client(connectReq.accessKey,
                connectReq.secretKey,
                connectReq.region
            )
        s3Client.headBucket(HeadBucketRequest.builder().bucket(connectReq.bucket).build())
        s3Client.close()
    }

    fun listObjectsPaginated(
        config: S3Config,
        prefix: String,
        page: Int,
        pageSize: Int
    ): ServiceResult<PaginatedS3Objects> {

        TODO()


//        return try {
//            val s3Client = createS3Client(config)
//
//            // 모든 객체를 가져와서 메모리에서 페이지네이션 (심플한 방식)
//            val allObjects = getAllObjects(s3Client, config.bucket, prefix)
//
//            val totalCount = allObjects.size.toLong()
//            val totalPages = ceil(totalCount.toDouble() / pageSize).toInt()
//            val startIndex = (page - 1) * pageSize
//            val endIndex = minOf(startIndex + pageSize, allObjects.size)
//
//            val paginatedObjects = if (startIndex < allObjects.size) {
//                allObjects.subList(startIndex, endIndex)
//            } else {
//                emptyList()
//            }
//
//            s3Client.close()
//
//            ServiceResult.success(
//                PaginatedS3Objects(
//                    objects = paginatedObjects,
//                    totalCount = totalCount,
//                    totalPages = totalPages,
//                    currentPage = page,
//                    pageSize = pageSize
//                )
//            )
//        } catch (e: S3Exception) {
//            ServiceResult.error("객체 목록 조회 실패: ${e.awsErrorDetails()?.errorMessage() ?: e.message}")
//        } catch (e: Exception) {
//            ServiceResult.error("객체 목록 조회 실패: ${e.message}")
//        }
    }

//    fun searchObjects(
//        config: S3Config,
//        prefix: String,
//        query: String,
//        page: Int,
//        pageSize: Int
//    ): ServiceResult<PaginatedS3Objects> {
//        return try {
//            val s3Client = createS3Client(config)
//
//            val allObjects = getAllObjects(s3Client, config.bucket, prefix)
//            val filteredObjects = allObjects.filter {
//                it.name.contains(query, ignoreCase = true)
//            }
//
//            val totalCount = filteredObjects.size.toLong()
//            val totalPages = ceil(totalCount.toDouble() / pageSize).toInt()
//            val startIndex = (page - 1) * pageSize
//            val endIndex = minOf(startIndex + pageSize, filteredObjects.size)
//
//            val paginatedObjects = if (startIndex < filteredObjects.size) {
//                filteredObjects.subList(startIndex, endIndex)
//            } else {
//                emptyList()
//            }
//
//            s3Client.close()
//
//            ServiceResult.success(
//                PaginatedS3Objects(
//                    objects = paginatedObjects,
//                    totalCount = totalCount,
//                    totalPages = totalPages,
//                    currentPage = page,
//                    pageSize = pageSize
//                )
//            )
//        } catch (e: S3Exception) {
//            ServiceResult.error("검색 실패: ${e.awsErrorDetails()?.errorMessage() ?: e.message}")
//        } catch (e: Exception) {
//            ServiceResult.error("검색 실패: ${e.message}")
//        }
//    }

//    fun getObjectInfo(config: S3Config, key: String): ServiceResult<S3ObjectInfo> {
//        return try {
//            val s3Client = createS3Client(config)
//
//            val response = s3Client.headObject(
//                HeadObjectRequest.builder()
//                    .bucket(config.bucket)
//                    .key(key)
//                    .build()
//            )
//
//            val name = key.substringAfterLast("/")
//            val extension = if (name.contains(".")) name.substringAfterLast(".") else ""
//
//            val objectInfo = S3ObjectInfo(
//                key = key,
//                name = name,
//                isDirectory = key.endsWith("/"),
//                size = response.contentLength(),
//                lastModified = response.lastModified(),
//                extension = extension
//            )
//
//            s3Client.close()
//            ServiceResult.success(objectInfo)
//        } catch (e: S3Exception) {
//            ServiceResult.error("객체 정보 조회 실패: ${e.awsErrorDetails()?.errorMessage() ?: e.message}")
//        } catch (e: Exception) {
//            ServiceResult.error("객체 정보 조회 실패: ${e.message}")
//        }
//    }

    fun buildBreadcrumbs(prefix: String): List<BreadcrumbItem> {
        if (prefix.isEmpty()) return emptyList()

        val parts = prefix.trim('/').split("/")
        val breadcrumbs = mutableListOf<BreadcrumbItem>()
        var currentPath = ""

        for (part in parts) {
            currentPath += "$part/"
            breadcrumbs.add(BreadcrumbItem(name = part, path = currentPath))
        }

        return breadcrumbs
    }

    // Private helper methods
    fun createS3Client(
        accessKey: String,
        secretKey: String,
        region: String,
    ): S3Client {

        val credentials =
            AwsBasicCredentials.create(accessKey, secretKey)

        return S3Client.builder()
            .region(Region.of(region))
            .credentialsProvider(StaticCredentialsProvider.create(credentials))
            .build()
    }

    private fun getAllObjects(
        s3Client: S3Client,
        bucket: String,
        prefix: String
    ): List<S3ObjectInfo> {

        val objects = mutableListOf<S3ObjectInfo>()
        var continuationToken: String? = null

        do {
            val request = ListObjectsV2Request.builder()
                .bucket(bucket)
                .prefix(prefix)
                .delimiter("/") // 폴더 구조 유지
                .maxKeys(1000)
                .apply {
                    if (continuationToken != null) {
                        continuationToken(continuationToken)
                    }
                }
                .build()

            val response = s3Client.listObjectsV2(request)

            // 폴더들 (CommonPrefixes) 추가
            response.commonPrefixes().forEach { commonPrefix ->
                val folderKey = commonPrefix.prefix()
                val folderName = folderKey.removeSuffix("/").substringAfterLast("/")

                if (folderName.isNotEmpty()) {
                    objects.add(
                        S3ObjectInfo(
                            key = folderKey,
                            name = folderName,
                            isDirectory = true,
                            size = 0L,
                            lastModified = Instant.now(),
                            extension = ""
                        )
                    )
                }
            }

            // 파일들 추가
            response.contents().forEach { s3Object ->
                val key = s3Object.key()

                // 현재 레벨의 객체만 포함 (중첩된 폴더 내부 파일 제외)
                if (key != prefix && !key.removePrefix(prefix).contains("/")) {
                    val name = key.substringAfterLast("/")
                    val extension = if (name.contains(".")) name.substringAfterLast(".") else ""

                    objects.add(
                        S3ObjectInfo(
                            key = key,
                            name = name,
                            isDirectory = false,
                            size = s3Object.size(),
                            lastModified = s3Object.lastModified(),
                            extension = extension
                        )
                    )
                }
            }

            continuationToken = response.nextContinuationToken()
        } while (response.isTruncated)

        return objects.sortedWith(compareBy<S3ObjectInfo> { !it.isDirectory }.thenBy { it.name.lowercase() })
    }

}

data class ServiceResult<T>(
    val isSuccess: Boolean,
    val data: T? = null,
    val errorMessage: String = "",
    val message: String = ""
) {
    companion object {
        fun <T> success(data: T, message: String = ""): ServiceResult<T> {
            return ServiceResult(isSuccess = true, data = data, message = message)
        }

        fun <T> error(errorMessage: String): ServiceResult<T> {
            return ServiceResult(isSuccess = false, errorMessage = errorMessage)
        }
    }
}

// 페이지네이션 결과 DTO (Controller에서 사용)
data class PaginatedS3Objects(
    val objects: List<S3ObjectInfo>,
    val totalCount: Long,
    val totalPages: Int,
    val currentPage: Int,
    val pageSize: Int
)
