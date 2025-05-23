package freeapp.me.s3manager.config.filter

import com.fasterxml.jackson.databind.ObjectMapper
import freeapp.me.s3manager.config.filter.CustomServletContextRequestLoggingFilter
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered
import org.springframework.web.filter.ServletContextRequestLoggingFilter


@Configuration
class FilterConfig(
    private val mapper:ObjectMapper
) {


    @Bean
    fun servletContextRequestLoggingFilter(): ServletContextRequestLoggingFilter {
        return CustomServletContextRequestLoggingFilter()
    }

    @Bean
    fun mdcLoggingFilterRegister(): FilterRegistrationBean<MDCLoggingFilter> {
        val bean: FilterRegistrationBean<MDCLoggingFilter> =
            FilterRegistrationBean(MDCLoggingFilter())
        bean.addUrlPatterns("/*")
        bean.order = Ordered.HIGHEST_PRECEDENCE //필터 순서.. 낮은 숫자가 먼저 실행됨
        return bean
    }



}
