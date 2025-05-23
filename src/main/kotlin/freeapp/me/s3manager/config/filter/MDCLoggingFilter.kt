package freeapp.me.s3manager.config.filter


import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse

import org.slf4j.MDC
import java.util.*


class MDCLoggingFilter(

) : Filter {


    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        val uuid = UUID.randomUUID()
        MDC.put("request_id", uuid.toString())
        response.characterEncoding = Charsets.UTF_8.name()
        chain.doFilter(request, response)
        MDC.clear()
    }

}
