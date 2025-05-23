package freeapp.me.s3manager.web.dto

data class EmailDto(
    val emailTemplate: EmailTemplate,
    val body: EmailBodyDto,
)

data class EmailBodyDto(
    val verificationCode: String = "",
    val validityMinutes: String = "",
)


enum class EmailTemplate(
    val templateTarget: String,
    val subject: String,
    val desc: String,
) {

    VERIFICATION(
        "mail/verification.kte",
        "QR Generator verification Code",
        "인증 코드",
    ),

}
