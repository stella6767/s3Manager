package freeapp.me.s3manager.web.dto

import org.springframework.web.multipart.MultipartFile


data class TextReqDto(
    val text:String,
)

data class VCardReqDto(
    val firstName: String,
    val lastName: String,
    val phoneNumber:String,
)


data class WifiReqDto(
    val ssid: String,
    val encryption: String,
    val password:String,
)


data class CallReqDto(
    val countryCode: String,
    val phoneNumber:String,
)

data class PDFReqDto(
    val file: MultipartFile,
)
