package freeapp.me.s3manager.web

import com.fasterxml.jackson.databind.ObjectMapper
import freeapp.me.s3manager.config.UserPrincipal
import freeapp.me.s3manager.service.S3Service
import freeapp.me.s3manager.web.dto.*
import io.github.wimdeblauwe.htmx.spring.boot.mvc.HtmxRedirectView
import io.github.wimdeblauwe.htmx.spring.boot.mvc.HtmxRefreshView
import io.github.wimdeblauwe.htmx.spring.boot.mvc.HtmxRequest
import io.github.wimdeblauwe.htmx.spring.boot.mvc.HtmxResponse
import io.github.wimdeblauwe.htmx.spring.boot.mvc.HxRequest
import jakarta.persistence.EntityNotFoundException
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*


@RequestMapping("/s3")
@Controller
class S3Controller(
    private val s3Service: S3Service,
    private val mapper: ObjectMapper,
) {


    @HxRequest
    @PostMapping("/connect")
    fun connect(
        @AuthenticationPrincipal principal: UserPrincipal,
        @Valid s3ConnectionRequestDto: S3ConnectionRequestDto,
        model: Model
    ): HtmxRedirectView {

        s3Service.testConnection(s3ConnectionRequestDto)
        s3Service.saveS3Key(principal.user, s3ConnectionRequestDto)

        return HtmxRedirectView("/s3/browser")
    }


    @HxRequest
    @PutMapping("/disconnect")
    fun disconnect(
        @AuthenticationPrincipal principal: UserPrincipal,
    ): HtmxRefreshView {
        s3Service.disconnectS3KeyByUser(principal.user)
        return HtmxRefreshView()
    }


    @GetMapping("/browser")
    fun s3Browser(
        model: Model,
        htmxRequest: HtmxRequest,
        dto: S3BrowserRequestDto,
        @AuthenticationPrincipal principal: UserPrincipal,
    ): String {

        val s3Key =
            s3Service.findS3KeyByUser(user = principal.user)
                ?: throw EntityNotFoundException("s3Key not found")

        val objects =
            s3Service.getObjectsByS3Key(s3Key, dto.prefix, dto.size, dto.continuationToken)

        val finalObjects =
            s3Service.buildBreadcrumbs(dto.prefix, objects.objects)


        model.addAttribute("bucket", s3Key.bucket)
        model.addAttribute("objects", finalObjects)
        model.addAttribute("size", dto.size)
        model.addAttribute("continuationToken", objects.continuationToken)
        model.addAttribute("currentPath", dto.prefix)
        model.addAttribute("isLast", objects.isLast)

        if (htmxRequest.isHtmxRequest) {
            return "components/s3/objectList"
        }

        return "page/s3Browser"
    }

    @HxRequest
    @GetMapping("/browser/rows")
    fun s3Rows(
        model: Model,
        htmxRequest: HtmxRequest,
        dto: S3BrowserRequestDto,
        @AuthenticationPrincipal principal: UserPrincipal,
    ): String {

        val s3Key =
            s3Service.findS3KeyByUser(user = principal.user)
                ?: throw EntityNotFoundException("s3Key not found")

        val objects =
            s3Service.getObjectsByS3Key(
                s3Key, dto.prefix, dto.size, dto.continuationToken
            )

        model.addAttribute("objects", objects.objects)
        model.addAttribute("size", dto.size)
        model.addAttribute("continuationToken", objects.continuationToken)
        model.addAttribute("isLast", objects.isLast)
        model.addAttribute("currentPath", dto.prefix)


        return "components/s3/objectBody"
    }

    @GetMapping("/download")
    @ResponseBody
    fun download(
        @RequestParam fileKey: String,
        @AuthenticationPrincipal principal: UserPrincipal,
        htmxResponse: HtmxResponse,
    ): ResponseEntity<Void> {

        val s3Key =
            s3Service.findS3KeyByUser(user = principal.user)
                ?: throw EntityNotFoundException("s3Key not found")

        val downloadDto =
            s3Service.getPresignedUrl(fileKey, s3Key.bucket)

        // JSON 생성: {"eventName":{"key":"value"}}
        val triggerJson =
            mapper.writeValueAsString(DownloadEventDto(downloadDto))

        htmxResponse.addTrigger(triggerJson)

        return ResponseEntity
            .status(HttpStatus.OK)
            .build()
    }




}
