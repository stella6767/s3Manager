package freeapp.me.s3manager.web.dto

import freeapp.me.s3manager.entity.User
import org.springframework.web.multipart.MultipartFile

data class UserResponseDto(
    val username: String,
    val email: String,
    val role: User.Role,
    val status: User.Status,
    val signType: User.SignType,
) {
    companion object {

        fun fromEntity(user: User): UserResponseDto {
            return UserResponseDto(
                user.username,
                user.email,
                user.role,
                user.status,
                user.signType
            )
        }
    }

}


data class UpdateProfileDto(
    val password: String,
    val username: String,

)
