package freeapp.me.s3manager.repo

import freeapp.me.s3manager.config.P6spyConfig
import freeapp.me.s3manager.config.RepositoriesTestConfig
import org.junit.jupiter.api.Test

import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.data.domain.PageRequest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestConstructor
import org.springframework.transaction.annotation.Transactional

@Transactional
@Import(*[RepositoriesTestConfig::class, P6spyConfig::class]) //, TestDataSource::class
@ActiveProfiles("dev")
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) //Any = h2
@DataJpaTest
class S3RepositoryTest(
    private val s3KeyRepository: S3KeyRepository,
    private val s3ObjectRepository: S3ObjectRepository
) {

    private val pageable = PageRequest.of(0, 10)


    @Test
    fun findObjectsByS3Key() {

        val s3Key =
            s3KeyRepository.getReferenceById(1)

        s3ObjectRepository.findObjectsByS3Key(s3Key, pageable)

    }
}
