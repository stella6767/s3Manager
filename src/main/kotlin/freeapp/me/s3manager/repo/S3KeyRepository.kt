package freeapp.me.s3manager.repo

import freeapp.me.s3manager.entity.S3Key
import org.springframework.data.jpa.repository.JpaRepository

interface S3KeyRepository : JpaRepository<S3Key, Long> {
}
