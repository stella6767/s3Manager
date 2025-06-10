package freeapp.me.s3manager.config

import com.linecorp.kotlinjdsl.render.jpql.JpqlRenderContext
import com.linecorp.kotlinjdsl.render.jpql.JpqlRenderer
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean

@TestConfiguration
class RepositoriesTestConfig(

) {

    @PersistenceContext
    private lateinit var entityManager: EntityManager

    @Bean
    fun jpqlRenderContext(): JpqlRenderContext {
        return JpqlRenderContext()
    }
    @Bean
    fun jpqlRenderer(): JpqlRenderer {
        return JpqlRenderer()
    }


}
