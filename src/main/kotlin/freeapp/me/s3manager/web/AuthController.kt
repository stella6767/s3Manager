package freeapp.me.s3manager.web

import freeapp.me.s3manager.config.UserPrincipal
import freeapp.me.s3manager.service.UserService
import freeapp.me.s3manager.service.sign.SignService
import freeapp.me.s3manager.web.dto.*
import io.github.wimdeblauwe.htmx.spring.boot.mvc.HtmxRedirectView
import io.github.wimdeblauwe.htmx.spring.boot.mvc.HtmxResponse
import io.github.wimdeblauwe.htmx.spring.boot.mvc.HxRequest
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.security.core.annotation.AuthenticationPrincipal

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.ResponseBody

@Controller
class AuthController(
    private val signService: SignService,
    private val userService: UserService,
) {


    @GetMapping("/sign-up")
    fun signUpPage(
        model: Model,
    ): String {
        model.addAttribute("isSignUp", true)
        return "page/sign"
    }

    @GetMapping("/login")
    fun loginPage(
        model: Model,
    ): String {
        model.addAttribute("isSignUp", false)
        return "page/sign"
    }

    @GetMapping("/profile")
    fun profilePage(
        model: Model,
        @AuthenticationPrincipal principal: UserPrincipal,
    ): String {

        model.addAttribute("currentUser",
            userService.findUserById(principal.user.id)
        )

        return "page/profile"
    }


    @HxRequest
    @PostMapping("/login")
    fun login(
        model: Model,
        loginDto: LoginDto,
        httpRequest: HttpServletRequest,
    ): HtmxRedirectView {

        signService.login(loginDto, httpRequest)

        return HtmxRedirectView("/")
    }


    @HxRequest
    @PostMapping("/sign-up")
    fun signUp(
        model: Model,
        @Valid signUpDto: SignUpDto,
        htmxResponse: HtmxResponse
    ): String {

        val dto = signService.signUp(signUpDto)

        model.addAttribute("email", dto.email)
        model.addAttribute("token", dto.token)
        model.addAttribute("expireMinute", dto.expireMinute)

        return "component/auth/verifyCode"
    }

    @HxRequest
    @PostMapping("/verify-code")
    fun verify(
        model: Model,
        verifyDto: VerifyDto,
        httpRequest: HttpServletRequest
    ): HtmxRedirectView {

        signService.signUpWithEmailVerify(verifyDto, httpRequest)

        return HtmxRedirectView("/")
    }


    @ResponseBody
    @PostMapping("/resend-code")
    fun resendCode(
        model: Model,
        resendCodeDto: ResendCodeDto,
        httpRequest: HttpServletRequest
    ): String {

        return signService.resendCode(resendCodeDto)
    }



}
