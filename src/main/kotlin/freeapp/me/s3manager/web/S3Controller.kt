package freeapp.me.s3manager.web

import freeapp.me.s3manager.config.UserPrincipal
import freeapp.me.s3manager.service.S3Service
import freeapp.me.s3manager.web.dto.*
import io.github.wimdeblauwe.htmx.spring.boot.mvc.HxRequest
import jakarta.servlet.http.HttpSession
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile


@RequestMapping("/s3")
@Controller
class S3Controller(
    private val s3Service: S3Service,
) {

    @GetMapping("")
    fun index(): String {
        return "page/index"
    }

    @PostMapping("/connect")
    fun connect(
        @AuthenticationPrincipal principal: UserPrincipal,
        s3ConnectionRequestDto: S3ConnectionRequestDto,
        session: HttpSession,
        model: Model
    ): String {

        s3Service.testConnection(s3ConnectionRequestDto)
        s3Service.saveS3Key(principal.user, s3ConnectionRequestDto)

        // 첫 페이지 로드
        //return listObjects("", 1, 20, session, model)

        TODO()
    }

    @PostMapping("/disconnect")
    fun disconnect(session: HttpSession): String {
        session.removeAttribute("s3Config")
        return "components/s3/connectionForm"
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
