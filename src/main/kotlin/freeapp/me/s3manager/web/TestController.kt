package freeapp.me.s3manager.web

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller
class TestController {

    @GetMapping("/")
    fun index(): String {

        return "page/index"
    }

}
