package freeapp.me.s3manager.config

import freeapp.me.s3manager.web.dto.UserResponseDto
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.servlet.HandlerInterceptor
import org.springframework.web.servlet.ModelAndView

class AuthenticationInterceptor : HandlerInterceptor {

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        return true
    }

    override fun postHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
        modelAndView: ModelAndView?
    ) {

        val authentication = SecurityContextHolder.getContext().authentication
        if (authentication != null && authentication.isAuthenticated &&
            authentication.principal is UserPrincipal
        ) {
            val principal = authentication.principal as UserPrincipal
            modelAndView?.addObject("isLoggedIn", true)
            modelAndView?.addObject("currentUser", UserResponseDto.fromEntity(principal.user))
        } else {
            modelAndView?.addObject("isLoggedIn", false)
        }

    }

}
