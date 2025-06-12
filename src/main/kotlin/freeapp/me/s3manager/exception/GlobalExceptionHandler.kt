package freeapp.me.s3manager.exception

import gg.jte.TemplateOutput
import gg.jte.output.StringOutput
import io.github.wimdeblauwe.htmx.spring.boot.mvc.HtmxRequest
import io.github.wimdeblauwe.htmx.spring.boot.mvc.HtmxResponse
import io.github.wimdeblauwe.htmx.spring.boot.mvc.HtmxReswap
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.core.annotation.AnnotationUtils
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.servlet.ModelAndView
import java.util.Map


@ControllerAdvice
class GlobalExceptionHandler(

) {

    private val log = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    /**
     * Handle exception
     *
     *  Error는 Exception의 하위 클래스가 아니므로 잡지 못한다.
     *  필터, 인터셉터, 서블릿 컨테이너 레벨, 혹은 비동기 작업 내에서 발생한 예외는 Spring MVC의 예외 처리 체인에 도달하지 않을 수 있음
     *
     * @param ex
     * @return
     */
    @ExceptionHandler(Exception::class)
    fun handleException(
        ex: Exception,
        htmxResponse: HtmxResponse,
        htmxRequest: HtmxRequest,
        response: HttpServletResponse,
        model: Model,
    ): String {

        if (!ex.localizedMessage.contains(".well-known/appspecific/com.chrome.devtools")) {
            log.error(ex.stackTraceToString())
        }

        val status =
            if (ex is ResponseStatusException) {
                ex.statusCode
            } else {
                AnnotationUtils.findAnnotation(ex.javaClass, ResponseStatus::class.java)?.value
                    ?: HttpStatus.BAD_REQUEST
            }

        response.status = status.value()
        htmxResponse.reswap = HtmxReswap.innerHtml()
        htmxResponse.retarget = "#error-alert-container"

        if (htmxRequest.isHtmxRequest) {
            htmxResponse.pushUrl = htmxRequest.currentUrl
        }

        if (!htmxRequest.isHtmxRequest){
            model.addAttribute("isHtmxRequest", false )
        }

        model.addAttribute("msg", ex.message)

        return "components/util/errorAlert"
    }



}
