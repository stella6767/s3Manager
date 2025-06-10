package freeapp.me.s3manager.repo

import com.linecorp.kotlinjdsl.dsl.jpql.jpql
import com.linecorp.kotlinjdsl.render.jpql.JpqlRenderContext
import com.linecorp.kotlinjdsl.render.jpql.JpqlRenderer
import freeapp.me.s3manager.entity.S3Key
import freeapp.me.s3manager.entity.User
import freeapp.me.s3manager.entity.UserVerify
import freeapp.me.s3manager.util.getRecentSingleResultOrNull
import freeapp.me.s3manager.util.getSingleResultOrNull
import jakarta.persistence.EntityManager
import org.springframework.data.jpa.repository.JpaRepository

interface S3KeyRepository : JpaRepository<S3Key, Long>, S3KeyCustomRepository {
}

interface S3KeyCustomRepository {
    fun findKeyByUser(user: User): S3Key?
}


class S3KeyCustomRepositoryImpl(
    private val renderer: JpqlRenderer,
    private val ctx: JpqlRenderContext,
    private val em: EntityManager,
) : S3KeyCustomRepository {


    override fun findKeyByUser(user: User): S3Key? {

        val query = jpql {
            select(
                entity(S3Key::class),
            ).from(
                entity(S3Key::class),
                leftFetchJoin(S3Key::user)
            ).where(
                and(
                    path(S3Key::user).equal(user)
                )
            ).orderBy(
                path(S3Key::id).desc()
            )
        }

        val render =
            renderer.render(query = query, ctx)

        return em.getRecentSingleResultOrNull(render, S3Key::class.java)
    }

}
