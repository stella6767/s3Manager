package freeapp.me.s3manager.service

import freeapp.me.s3manager.entity.S3Key
import freeapp.me.s3manager.entity.User
import freeapp.me.s3manager.repo.S3KeyRepository
import freeapp.me.s3manager.repo.S3ObjectRepository
import freeapp.me.s3manager.web.dto.*
import jakarta.persistence.EntityNotFoundException
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.*
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import java.time.LocalDateTime
import java.time.ZoneId


@Service
class S3Service(
    private val s3Client: S3Client,
    private val s3PreSigner: S3Presigner,
    private val s3KeyRepository: S3KeyRepository,
    private val s3ObjectRepository: S3ObjectRepository,
) {

    private val log = KotlinLogging.logger { }


    @Value("\${s3.bucket}")
    private lateinit var bucket: String

    @Value("\${s3.url}")
    private lateinit var staticUrl: String

    private val s3Utilities = s3Client.utilities()

    fun testConnection(
        connectReq: S3ConnectionRequestDto
    ) {
        val s3Client =
            createS3Client(
                connectReq.accessKey,
                connectReq.secretKey,
                connectReq.region
            )
        s3Client.headBucket(HeadBucketRequest.builder().bucket(connectReq.bucket).build())
        s3Client.close()
    }


    @Transactional
    fun saveS3Key(
        user: User,
        s3ConnectionRequestDto: S3ConnectionRequestDto
    ): S3Key {
        val s3Key =
            s3ConnectionRequestDto.toEntity(user)
        return s3KeyRepository.save(s3Key)
    }


    @Transactional(readOnly = true)
    fun getObjectsByS3Key(
        s3key: S3Key,
        prefix: String,
        size: Int,
        continuationToken: String,
    ): PaginatedS3Objects {

        val s3Client = createS3Client(
            s3key.accessKey,
            s3key.secretKey,
            s3key.region
        )

        return getObjectsBySize(s3Client, s3key.bucket, prefix, size, continuationToken)
    }


    @Transactional(readOnly = true)
    fun findS3KeyByUser(user: User): S3Key? {

        val s3Key =
            s3KeyRepository.findKeyByUser(user)

        return s3Key
    }


    @Transactional
    fun disconnectS3KeyByUser(user: User) {

        val s3Key =
            s3KeyRepository.findKeyByUser(user) ?: throw EntityNotFoundException("s3key not found")

        s3Key.disconnect()
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


    fun getObjectsBySize(
        s3Client: S3Client,
        bucket: String,
        prefix: String,
        size: Int,
        token: String
    ): PaginatedS3Objects {

        val objects =
            mutableListOf<S3ObjectInfo>()

        val request = ListObjectsV2Request.builder()
            .bucket(bucket)
            .prefix(prefix)
            .delimiter("/") // 폴더 구조 유지
            .maxKeys(size)
            .apply {
                if (!token.isNullOrBlank()) {
                    continuationToken(token)
                }
            }
            .build()

        val response = s3Client.listObjectsV2(request)

        // 폴더들 (CommonPrefixes) 추가
        response.commonPrefixes().forEach { commonPrefix ->
            val folderKey = commonPrefix.prefix()
            val folderName =
                folderKey.removeSuffix("/").substringAfterLast("/")
            if (folderName.isNotEmpty()) {
                objects.add(
                    S3ObjectInfo(
                        key = folderKey,
                        name = folderName,
                        isDirectory = true,
                        size = 0L,
                        lastModified = LocalDateTime.now(),
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
                        lastModified = s3Object.lastModified().atZone(ZoneId.systemDefault()).toLocalDateTime(),
                        extension = extension
                    )
                )
            }
        }


        return PaginatedS3Objects(
            objects,
            response.nextContinuationToken() ?: token,
            !response.isTruncated
        )
    }

}

