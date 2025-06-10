package freeapp.me.s3manager.entity

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import java.time.LocalDateTime


@Entity
@Table(name = "s3_key")
class S3Key(
    user: User,
    region: String,
    bucket: String,
    accessKey: String,
    secretKey: String,
) : BaseEntity() {


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    var user = user

    @Column(nullable = false, name = "social_id")
    var region = region

    @Column(nullable = false, name = "bucket")
    var bucket = bucket

    @Column(nullable = false, name = "access_key", length = 256)
    val accessKey = accessKey

    @Column(nullable = false, length = 256, name = "secret_key")
    var secretKey = secretKey

    @Column(name = "deleted_at", nullable = true)
    var deletedAt: LocalDateTime? = null

    fun disconnect(time: LocalDateTime = LocalDateTime.now()) {
        this.deletedAt = time
    }

}
