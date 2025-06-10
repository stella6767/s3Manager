package freeapp.me.s3manager.web

import freeapp.me.s3manager.config.UserPrincipal
import freeapp.me.s3manager.service.S3Service
import freeapp.me.s3manager.web.dto.S3keyInfo
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping

@Controller
class IndexController(
    private val s3Service: S3Service,
) {

    @GetMapping("/")
    fun index(
        model: Model,
        @AuthenticationPrincipal principal: UserPrincipal?,
    ): String {
        if (principal != null) {
            s3Service.findS3KeyByUser(principal.user)?.let {
                model.addAttribute("s3Key", S3keyInfo.fromEntity(it))
            }
        }
        return "page/s3Connection"
    }

}
