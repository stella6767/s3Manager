package freeapp.me.s3manager.entity

import com.fasterxml.jackson.annotation.JsonCreator
import jakarta.persistence.*
import org.hibernate.annotations.Comment
import java.time.LocalDateTime


@Entity
@Table(name = "user")
class User(
    socialId: String? = null,
    status: Status,
    username: String,
    email: String,
    password: String,
    deleteReason: String = "",
    role: Role = Role.USER,
    signType: SignType,
    lastLoginDate: LocalDateTime? = null,
) : BaseEntity() {

    @Column(name = "social_id")
    var socialId: String? = socialId

    @Column(nullable = false)
    var username = username

    @Column(nullable = false, length = 100)
    val email = email

    @Column(nullable = false, length = 100)
    var password = password

    @Enumerated(EnumType.STRING)
    val role = role

    @Enumerated(EnumType.STRING)
    val signType = signType


    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    @Comment("PENDING(\"화원가입 진행 중\"), ACTIVATED(\"활성\"), DELETED(\"탈퇴\"), DEACTIVATED(\"비활성\")")
    var status: Status = status

    @Column(name = "last_login_date")
    var lastLoginDate = lastLoginDate

    @Column(name = "delete_reason", length = 500)
    var deleteReason = deleteReason


    fun updateLastLoginDate(time: LocalDateTime = LocalDateTime.now()) {
        this.lastLoginDate = time
    }


    fun delete(reason: String) {
        this.status = Status.DELETED
        this.deleteReason = reason
    }

    fun update(username: String, encPassword: String) {
        this.username = username
        this.password = encPassword
    }

    enum class Status(
        val displayName: String
    ) {
        PENDING("화원가입 진행 중"),
        ACTIVATED("활성"),
        DEACTIVATED("비활성"),
        DELETED("탈퇴"),
    }


    enum class Role(
        val value: String
    ) {

        USER("ROLE_USER"),
        ADMIN("ROLE_ADMIN"),
    }

    enum class SignType(
        val clientName: String,
        val authorizationUrl: String,
    ) {
        GOOGLE(
            "Google",
            "/oauth2/authorization/google"
        ),
        GITHUB(
            "GitHub",
            "/oauth2/authorization/github"
        ),
        EMAIL(
            "Email",
            "/oauth2/authorization/email"
        )
        ;

        companion object {
            @JsonCreator
            fun from(str: String): SignType? {
                return SignType.values().firstOrNull {
                    it.name == str.uppercase()
                } ?: throw RuntimeException("unsupported signtype")
            }
        }
    }

}
