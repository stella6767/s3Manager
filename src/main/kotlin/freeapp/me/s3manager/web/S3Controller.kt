package freeapp.me.s3manager.web

import freeapp.me.s3manager.config.UserPrincipal
import freeapp.me.s3manager.service.S3Service
import freeapp.me.s3manager.web.dto.*
import io.github.wimdeblauwe.htmx.spring.boot.mvc.HtmxRedirectView
import io.github.wimdeblauwe.htmx.spring.boot.mvc.HtmxRefreshView
import io.github.wimdeblauwe.htmx.spring.boot.mvc.HtmxRequest
import io.github.wimdeblauwe.htmx.spring.boot.mvc.HxRequest
import jakarta.persistence.EntityNotFoundException
import jakarta.validation.Valid
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*


@RequestMapping("/s3")
@Controller
class S3Controller(
    private val s3Service: S3Service,
) {


    @HxRequest
    @PostMapping("/connect")
    fun connect(
        @AuthenticationPrincipal principal: UserPrincipal,
        @Valid s3ConnectionRequestDto: S3ConnectionRequestDto,
        model: Model
    ): HtmxRedirectView {

        s3Service.testConnection(s3ConnectionRequestDto)

        val s3Key =
            s3Service.saveS3Key(principal.user, s3ConnectionRequestDto)

        //s3Service.saveS3Objects(s3Key)

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
        @PageableDefault(size = 20) pageable: Pageable,
        @AuthenticationPrincipal principal: UserPrincipal,
    ): String {

        val s3Key =
            s3Service.findS3KeyByUser(user = principal.user)
                ?: throw EntityNotFoundException("s3Key not found")

        val objects =
            s3Service.getObjectsByS3Key(s3Key, dto.prefix, pageable, dto.continuationToken)

        val breadcrumbs =
            s3Service.buildBreadcrumbs(dto.prefix)

        model.addAttribute("bucket", s3Key.bucket)
        model.addAttribute("objects", objects.objects)
        model.addAttribute("size", pageable.pageSize)
        model.addAttribute("continuationToken", objects.continuationToken)
        model.addAttribute("currentPath", dto.prefix)
        model.addAttribute("breadcrumbs", breadcrumbs)

        if (htmxRequest.isHtmxRequest && !dto.isPush) {
            return "components/s3/objectList"
        }

        return "page/s3Browser"
    }


//    @HxRequest
//    @GetMapping("/list")
//    fun listObjects(
//        @RequestParam(defaultValue = "") prefix: String,
//        @PageableDefault(size = 10) pageable: Pageable,
//        session: HttpSession,
//        model: Model
//    ): String {
//
//        val config =
//            getS3Config(session) ?: return handleSessionExpired(model)
//
//        val result =
//            s3Service.listObjectsPaginated(config, prefix, pageable.pageNumber, pageable.pageSize)
//
//        if (!result.isSuccess) {
//            model.addAttribute("error", result.errorMessage)
//            return "components/s3/connectionForm"
//        }
//
//        val paginatedResult = result.data!!
//        val breadcrumbs = s3Service.buildBreadcrumbs(prefix)
//
//        model.addAttribute("bucket", config.bucket)
//        model.addAttribute("objects", paginatedResult.objects)
//        model.addAttribute("currentPath", prefix)
//        model.addAttribute("breadcrumbs", breadcrumbs)
//        model.addAttribute("totalCount", paginatedResult.totalCount)
//        //model.addAttribute("currentPage", page)
//        model.addAttribute("totalPages", paginatedResult.totalPages)
//
//
//        return   "components/s3/objectList"
//
//    }
//
//    @GetMapping("/search")
//    fun searchObjects(
//        @RequestParam query: String,
//        @RequestParam(defaultValue = "") prefix: String,
//        @RequestParam(defaultValue = "1") page: Int,
//        @RequestParam(defaultValue = "20") size: Int,
//        session: HttpSession,
//        model: Model
//    ): String {
//        val config = getS3Config(session)
//            ?: return handleSessionExpired(model)
//
//        val result = s3Service.searchObjects(config, prefix, query, page, size)
//        if (!result.isSuccess) {
//            model.addAttribute("error", result.errorMessage)
//            return "components/s3/objectList"
//        }
//
//        val paginatedResult = result.data!!
//
//        model.addAttribute("objects", paginatedResult.objects)
//        model.addAttribute("currentPath", prefix)
//        model.addAttribute("totalCount", paginatedResult.totalCount)
//        model.addAttribute("currentPage", page)
//        model.addAttribute("totalPages", paginatedResult.totalPages)
//        model.addAttribute("searchQuery", query)
//
//        return "components/s3/objectList"
//    }
//
//    @GetMapping("/object-info")
//    @ResponseBody
//    fun getObjectInfo(
//        @RequestParam key: String,
//        session: HttpSession
//    ): Map<String, Any> {
//        val config = getS3Config(session)
//            ?: return mapOf("success" to false, "error" to "세션이 만료되었습니다.")
//
//        val result = s3Service.getObjectInfo(config, key)
//
//        return if (result.isSuccess) {
//            val obj = result.data!!
//            mapOf(
//                "success" to true,
//                "key" to obj.key,
//                "name" to obj.name,
//                "size" to obj.getFormattedSize(),
//                "lastModified" to obj.getFormattedDate(),
//                "extension" to obj.extension,
//                "isDirectory" to obj.isDirectory,
//                "icon" to obj.getFileIcon()
//            )
//        } else {
//            mapOf("success" to false, "error" to result.errorMessage)
//        }
//    }
//
//
//    private fun getS3Config(session: HttpSession): S3Config? {
//        return session.getAttribute("s3Config") as? S3Config
//    }
//
//    private fun handleSessionExpired(model: Model): String {
//        model.addAttribute("error", "세션이 만료되었습니다. 다시 연결해주세요.")
//        return "components/s3/connectionForm"
//    }


}
