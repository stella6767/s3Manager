package freeapp.me.s3manager.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class GlobalProperties {


    companion object {
        @JvmField
        var staticUrl: String = ""
    }


    @Value("\${global.staticUrl}")
    fun setStaticUrl(url: String) {
        staticUrl = url
    }

}
