package freeapp.me.s3manager.web

import freeapp.me.s3manager.config.UserPrincipal
import freeapp.me.s3manager.service.UserService
import freeapp.me.s3manager.web.dto.UpdateProfileDto
import freeapp.me.s3manager.web.dto.UserDeleteRequestDto
import io.github.wimdeblauwe.htmx.spring.boot.mvc.HtmxRedirectView
import io.github.wimdeblauwe.htmx.spring.boot.mvc.HtmxRefreshView
import io.github.wimdeblauwe.htmx.spring.boot.mvc.HxRequest
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping


@RequestMapping("/user")
@Controller
class UserController(
    private val userService: UserService,
) {

    @HxRequest
    @DeleteMapping("")
    fun deleteUser(
        @AuthenticationPrincipal principal: UserPrincipal,
        deleteRequestDto: UserDeleteRequestDto,
    ): HtmxRedirectView {

        userService.deleteUser(
            principal.user.id,
            deleteRequestDto
        )
        return HtmxRedirectView("/")
    }


    @HxRequest
    @PutMapping("")
    fun updateUser(
        @AuthenticationPrincipal principal: UserPrincipal,
        profileDto: UpdateProfileDto,
        model: Model,
    ): HtmxRefreshView {

        userService.updateUser(principal.user.id, profileDto)

        return HtmxRefreshView()
    }


}
