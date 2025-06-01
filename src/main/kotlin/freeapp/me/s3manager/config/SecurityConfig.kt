package freeapp.me.s3manager.config

import freeapp.me.s3manager.entity.User
import freeapp.me.s3manager.repo.UserRepository
import freeapp.me.s3manager.service.sign.OAuth2SignService

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.ProviderManager
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.builders.WebSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.access.AccessDeniedHandler
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler
import org.springframework.security.web.util.matcher.AntPathRequestMatcher
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true, prePostEnabled = true)
class SecurityConfig(
    private val userRepository: UserRepository,
    private val configuration: AuthenticationConfiguration,
) {
    private val log = KotlinLogging.logger {}

    @Value("\${security.debug:false}")
    private val isDebug = false

    @Value("\${spring.profiles.active:Unknown}")
    private lateinit var activeProfile: String

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }


    @Bean
    fun webSecurityCustomizer(): WebSecurityCustomizer {
        return WebSecurityCustomizer { web: WebSecurity ->
            val arrays = arrayOf(
                AntPathRequestMatcher("/resources/*"),
                AntPathRequestMatcher("/static/*"), AntPathRequestMatcher("/img/*"),
                AntPathRequestMatcher("/js/*")
            )
            web.ignoring()
                .requestMatchers(*arrays)
        }
    }


    @Bean
    fun filterChain(
        http: HttpSecurity,
        encoder: PasswordEncoder,
    ): SecurityFilterChain {

        http.csrf { csrf -> csrf.disable() }
            .formLogin { it.disable() }
            .httpBasic { it.disable() }

        http
            .authorizeHttpRequests { authorizeHttpRequests ->
                authorizeHttpRequests
                    //.requestMatchers(*arrayOf("/v1")).permitAll()
                    //.requestMatchers("/v1/admin/**").hasAnyRole(User.Role.ADMIN.name)
                    //.anyRequest().authenticated()
                    .anyRequest().permitAll()
            }
            .oauth2Login { oauth2 ->
                oauth2
                    .userInfoEndpoint { endpoint ->
                        endpoint.userService(OAuth2SignService(userRepository, encoder))
                    }
                    .successHandler(CustomLoginSuccessHandler(userRepository))
                    .permitAll()
            }
            .logout {
                it.logoutRequestMatcher(AntPathRequestMatcher("/logout", "GET"))
                it.logoutSuccessHandler(CustomLogoutSuccessHandler())
                it.invalidateHttpSession(true)
                it.deleteCookies("JSESSIONID")
                    .permitAll()
            }    // 로그아웃은 기본설정으로 (/logout으로 인증해제)
            .sessionManagement {
                it.maximumSessions(1)
                    .expiredUrl("/auth/login?expired")
                    .maxSessionsPreventsLogin(false)
            }
            .exceptionHandling {
                it.accessDeniedHandler(WebAccessDeniedHandler()) // 권한이 없는 사용자 접근 시
                it.authenticationEntryPoint(WebAuthenticationEntryPoint()) //인증되지 않는 사용자 접근 시
            }
            .headers {
                it.frameOptions { config ->
                    config.sameOrigin()
                }
            }
            .cors {
                it.configurationSource(corsConfigurationSource())
            }


        return http.build()
    }


    class WebAccessDeniedHandler : AccessDeniedHandler {

        override fun handle(
            request: HttpServletRequest?,
            response: HttpServletResponse?,
            accessDeniedException: AccessDeniedException
        ) {
            throw accessDeniedException
        }
    }


    class WebAuthenticationEntryPoint() : AuthenticationEntryPoint {
        override fun commence(
            request: HttpServletRequest, response: HttpServletResponse,
            authException: AuthenticationException
        ) {
            // 인증되지 않은 경우 페이지 이동 시 사용
            response.sendRedirect("/")
            // 인증되지 않은 경우 에러코드 반환 시 사용
            //response.sendError(HttpServletResponse.SC_UNAUTHORIZED)
            //resolver.resolveException(request, response, null, authException)
        }
    }

    class CustomLogoutSuccessHandler : LogoutSuccessHandler {

        private val log = KotlinLogging.logger { }

        override fun onLogoutSuccess(
            request: HttpServletRequest,
            response: HttpServletResponse,
            authentication: Authentication?
        ) {

            log.info("logout success")
            val context =
                SecurityContextHolder.getContext()
            context.authentication = null
            SecurityContextHolder.clearContext()

            response.sendRedirect("/")
        }

    }

    class CustomLoginSuccessHandler(
        private val userRepository: UserRepository,
    ) : AuthenticationSuccessHandler {

        private val log = KotlinLogging.logger { }

        override fun onAuthenticationSuccess(
            request: HttpServletRequest,
            response: HttpServletResponse,
            authentication: Authentication
        ) {


            val principal = authentication.principal as UserPrincipal
//            val SESSION_TIMEOUT_IN_SECONDS = 60 * 120 //단위는 초, 2시간 간격으로 세션만료
//            request.session.maxInactiveInterval = SESSION_TIMEOUT_IN_SECONDS //세션만료시간.
            SecurityContextHolder.getContext().authentication = authentication

            principal.user.updateLastLoginDate()
            userRepository.save(principal.user)

            log.info("login success, ${request.requestURI} ")

            if (principal.user.role == User.Role.USER) {
                response.sendRedirect("/")
            }

        }
    }


    //@Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration()
        configuration.allowedOrigins = listOf("*")
        configuration.allowedMethods = listOf("HEAD", "GET", "PUT", "POST", "DELETE", "PATCH")
        configuration.allowedHeaders = listOf("*")
        configuration.exposedHeaders = listOf("*")
        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }


}
