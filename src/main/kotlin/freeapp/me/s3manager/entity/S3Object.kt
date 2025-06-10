package freeapp.me.s3manager.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.LocalDateTime


@Entity
@Table(name = "s3_object")
class S3Object(
    s3Key: S3Key,
    objectKey: String,
    name: String,
    isDirectory: Boolean,
    size: Long,
    lastModified: LocalDateTime,
    extension: String
) : BaseEntity() {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "s3_key_id", nullable = false)
    val s3key = s3Key

    @Column(nullable = false, name = "object_key")
    val objectKey = objectKey

    @Column(nullable = false, name = "name")
    val name = name

    @Column(nullable = false, name = "is_directory")
    val isDirectory = isDirectory

    @Column(name = "size")
    val size = size

    @Column(name = "last_modified")
    val lastModified = lastModified

    @Column(name = "extension")
    val extension = extension

}
