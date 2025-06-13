package freeapp.me.s3manager.service

import freeapp.me.s3manager.entity.S3Key
import freeapp.me.s3manager.entity.User
import freeapp.me.s3manager.repo.S3KeyRepository
import freeapp.me.s3manager.repo.S3ObjectRepository
import freeapp.me.s3manager.web.dto.*
import jakarta.persistence.EntityNotFoundException
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.*
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest
import software.amazon.awssdk.services.s3.presigner.model.UploadPartPresignRequest
import java.io.File
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.time.Duration
import java.time.ZoneId


@Service
class S3Service(
    private val s3Client: S3Client,
    private val s3PreSigner: S3Presigner,
    private val s3KeyRepository: S3KeyRepository,
    private val s3ObjectRepository: S3ObjectRepository,
) {

    private val log = KotlinLogging.logger { }
    private val s3Utilities = s3Client.utilities()
    private val UPLOAD_THRESHOLD = 100 * 1024 * 1024 // 100MB


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

        val s3Objects =
            getObjectsBySize(s3Client, s3key.bucket, prefix, size, continuationToken)

        s3Client.close()

        return s3Objects
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

    fun buildBreadcrumbs(
        prefix: String,
        objects: MutableList<S3ObjectInfo>
    ): List<S3ObjectInfo> {

        if (prefix.isEmpty()) return objects

        val parts =
            prefix.split("/").dropLast(1)

        val directoryDto =
            S3ObjectInfo.toDirectoryDto("", "")

        objects.add(0, directoryDto)

        var currentPath = ""
        for ((index, part) in parts.withIndex()) {
            currentPath += "$part/"
            objects.add(index + 1, S3ObjectInfo.toDirectoryDto(currentPath, part))
        }

        return objects
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


    fun test() {

    }


    fun getDownloadPresignedUrl(
        fileKey: String,
        bucket: String
    ): DownloadDto {

        val filename = fileKey.substringAfterLast('/')
        val encodedFileName =
            URLEncoder.encode(filename, StandardCharsets.UTF_8.toString())

        val getObjectRequest = GetObjectRequest.builder()
            .bucket(bucket)
            .key(fileKey)
            .responseContentDisposition("attachment; filename=\"$encodedFileName\"")
            .build()

        val presignRequest = GetObjectPresignRequest.builder()
            .signatureDuration(Duration.ofMinutes(30))
            .getObjectRequest(getObjectRequest)
            .build()

        val presignedUrl =
            s3PreSigner.presignGetObject(presignRequest)

        val urlString = presignedUrl.url().toString()

        val downloadDto = DownloadDto(
            url = urlString,
            filename = encodedFileName
        )

        return downloadDto
    }

    fun initiateUpload(
        bucket: String,
        targetObjectDir: String,
        filename: String,
        contentType: String,
        fileSize: Long,
    ): UploadInitiateResponseDto {

        val fileKey =
            targetObjectDir + File.separator + filename

        if (fileSize <= UPLOAD_THRESHOLD) {
            return UploadInitiateResponseDto(
                uploadType = UploadType.SINGLE,
                fileKey = fileKey,
                presignedUrl = getSingleUploadUrl(bucket, fileKey, contentType)
            )
        }
        return UploadInitiateResponseDto(
            uploadType = UploadType.MULTIPART,
            fileKey = fileKey,
            uploadId = initiateMultipartUpload(bucket, fileKey, contentType)
        )
    }


    fun initiateMultipartUpload(
        bucket: String,
        fileKey: String,
        contentType: String,
    ): String {

        val createMultipartUploadRequest =
            CreateMultipartUploadRequest.builder()
                .bucket(bucket) // 버킷 설정
                .key(fileKey) // 업로드될 경로 설정
                .contentType(contentType)
                .build()

        // Amazon S3는 멀티파트 업로드에 대한 고유 식별자인 업로드 ID가 포함된 응답을 반환합니다.
        val response =
            s3Client.createMultipartUpload(createMultipartUploadRequest)

        return response.uploadId()
    }


    fun getPresignedPartUrl(
        bucket: String,
        dto: PresignedPartRequestDto,
    ): String {

        val uploadPartRequest =
            UploadPartRequest.builder()
                .bucket(bucket)
                .key(dto.fileKey)
                .uploadId(dto.uploadId)
                .partNumber(dto.partNumber)
                .build()

        // 미리 서명된 URL 요청
        // connection pool 문제를 어떻게 해결해야할까..
        val uploadPartPresignedRequest =
            UploadPartPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(30))
                .uploadPartRequest(uploadPartRequest)
                .build()

        // 클라이언트에서 S3로 직접 업로드하기 위해 사용할 인증된 URL을 받는다.
        val presignedUploadPartRequest =
            s3PreSigner.presignUploadPart(uploadPartPresignedRequest)

        return presignedUploadPartRequest.url().toString()
    }



    private fun getSingleUploadUrl(
        bucket: String,
        fileKey: String,
        contentType: String
    ): String {
        val putObjectRequest = PutObjectRequest.builder()
            .bucket(bucket)
            .key(fileKey)
            .contentType(contentType)
            .build()

        val preSignRequest = PutObjectPresignRequest.builder()
            .signatureDuration(Duration.ofMinutes(30)) // The URL will expire in  minutes.
            .putObjectRequest(putObjectRequest)
            .build()

        val uploadSignedUrl =
            s3PreSigner.presignPutObject(preSignRequest).url().toString()

        return uploadSignedUrl
    }



    fun completeUpload(
        bucket: String,
        s3UploadCompleteDto: S3UploadCompleteDto,
    ): S3UploadResultDto {

        val completedParts: MutableList<CompletedPart> = ArrayList()

        // 모든 한 영상에 대한 모든 부분들에 부분 번호와 Etag를 설정함
        for (partForm in s3UploadCompleteDto.parts) {
            val part = CompletedPart.builder()
                .partNumber(partForm.partNumber)
                .eTag(partForm.awsETag)
                .build()
            completedParts.add(part)
        }

        // 멀티파트 업로드 완료 요청을 AWS 서버에 보냄
        val completedMultipartUpload =
            CompletedMultipartUpload.builder().parts(completedParts).build()

        val fileKey = s3UploadCompleteDto.fileKey

        val completeMultipartUploadRequest =
            CompleteMultipartUploadRequest.builder()
                .bucket(bucket) // 버킷 설정
                .key(fileKey)
                .uploadId(s3UploadCompleteDto.uploadId) // 업로드 아이디
                .multipartUpload(completedMultipartUpload) // 영상의 모든 부분 번호, Etag
                .build()

        val completeMultipartUploadResponse =
            s3Client.completeMultipartUpload(completeMultipartUploadRequest)
        val objectKey = completeMultipartUploadResponse.key()
        val bucket = completeMultipartUploadResponse.bucket()

        println(objectKey)

        return S3UploadResultDto(
            fileKey = fileKey,
        )
    }


    fun abortMultipartUpload(
        bucket: String,
        s3UploadAbortDto: S3UploadAbortDto,
    ) {

        val abortMultipartUploadRequest =
            AbortMultipartUploadRequest.builder()
                .bucket(bucket)
                .key(s3UploadAbortDto.filename)
                .uploadId(s3UploadAbortDto.uploadId)
                .build()

        log.info { "abort uploadID===>" + s3UploadAbortDto.uploadId }

        try {
            s3Client.abortMultipartUpload(abortMultipartUploadRequest)
        } catch (e: NoSuchUploadException) {
            log.error { e.localizedMessage }
        }
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
                    S3ObjectInfo.toDirectoryDto(folderKey, folderName)
                )
            }
        }

        // 파일들 추가
        response.contents().forEach { s3Object ->
            val key = s3Object.key()

            println(key)

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

