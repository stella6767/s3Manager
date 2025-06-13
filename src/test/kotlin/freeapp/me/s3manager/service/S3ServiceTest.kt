package freeapp.me.s3manager.service

import freeapp.me.s3manager.repo.S3KeyRepository
import freeapp.me.s3manager.repo.S3ObjectRepository

import freeapp.me.s3manager.web.dto.S3ConnectionRequestDto
import freeapp.me.s3manager.web.dto.S3ObjectInfo
import org.junit.jupiter.api.Test

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestConstructor
import org.springframework.test.context.bean.override.mockito.MockitoBean
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.presigner.S3Presigner


@SpringBootTest(
    classes = [S3Service::class],
)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class S3ServiceTest(
    private val s3Service: S3Service,
) {

    @MockitoBean
    lateinit var s3Client: S3Client

    @MockitoBean
    lateinit var s3PreSigner: S3Presigner

    @MockitoBean
    lateinit var s3KeyRepository: S3KeyRepository

    @MockitoBean
    lateinit var s3ObjectRepository: S3ObjectRepository


    @Value("\${s3.accessKey}")
    private lateinit var accessKey: String

    @Value("\${s3.secretKey}")
    private lateinit var secretKey: String

    private val region = "ap-northeast-2"

    private val bucket = "qr-data-store"


    @Test
    fun test(){

        println(accessKey)
    }

    @Test
    fun testConnection() {

        val dto = S3ConnectionRequestDto(
            region, "qr-data-store", accessKey, secretKey,
        )

        s3Service.testConnection(dto)

    }

    @Test
    fun getObjectsBySize() {

        val client =
            s3Service.createS3Client(accessKey, secretKey, region)

        val s3Objects =
            s3Service.getObjectsBySize(client, bucket, "", 100, "")

        s3Objects.objects.forEach {
            println(it)
        }
    }

    @Test
    fun getUploadPresignedURL(){

        val client =
            s3Service.createS3Client(accessKey, secretKey, region)


    }


    @Test
    fun buildBreadcrumbs() {

        val objects =
            mutableListOf<S3ObjectInfo>()

        val breadcrumbs =
            s3Service.buildBreadcrumbs("b2b/test/", objects)

        println(breadcrumbs)

    }


}
