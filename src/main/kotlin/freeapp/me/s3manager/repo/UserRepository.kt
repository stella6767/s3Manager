package freeapp.me.s3manager.repo

import com.linecorp.kotlinjdsl.dsl.jpql.jpql
import com.linecorp.kotlinjdsl.render.jpql.JpqlRenderContext
import com.linecorp.kotlinjdsl.render.jpql.JpqlRenderer
import freeapp.me.s3manager.entity.User
import freeapp.me.s3manager.util.getResult
import freeapp.me.s3manager.util.getSingleResultOrNull
import jakarta.persistence.EntityManager
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<User, Long>, UserCustomRepository {

}


interface UserCustomRepository {

    fun findUsersByEmailAndSignTypeAndStatus(
        email: String, signType: User.SignType, status: User.Status
    ): MutableList<User>

    fun findUserBySocialIdAndSignTypeAndStatus(socialId: String, signType: User.SignType, status: User.Status): User?
    fun findActiveUserByEmailAndSignType(email: String, signType: User.SignType): User?
}


class UserCustomRepositoryImpl(
    private val renderer: JpqlRenderer,
    private val ctx: JpqlRenderContext,
    private val em: EntityManager,
) : UserCustomRepository {


    override fun findUserBySocialIdAndSignTypeAndStatus(
        socialId: String,
        signType: User.SignType,
        status: User.Status
    ): User? {

        val query = jpql {
            select(
                entity(User::class),
            ).from(
                entity(User::class),
            ).where(
                and(
                    path(User::socialId).equal(socialId),
                    path(User::signType).equal(signType),
                    path(User::status).equal(status),
                )
            )
        }


        val render = renderer.render(query = query, ctx)
        return em.getSingleResultOrNull(render, User::class.java)
    }

    override fun findActiveUserByEmailAndSignType(
        email: String,
        signType: User.SignType,
    ): User? {

        val query = jpql {
            select(
                entity(User::class),
            ).from(
                entity(User::class),
            ).where(
                and(
                    path(User::email).equal(email.trim()),
                    path(User::status).equal(User.Status.ACTIVATED),
                    path(User::signType).equal(signType),
                )
            )
        }

        val render = renderer.render(query = query, ctx)
        return em.getSingleResultOrNull(render, User::class.java)
    }


    override fun findUsersByEmailAndSignTypeAndStatus(
        email: String,
        signType: User.SignType,
        status: User.Status
    ): MutableList<User> {

        val query = jpql {
            select(
                entity(User::class),
            ).from(
                entity(User::class),
            ).where(
                and(
                    path(User::email).equal(email.trim()),
                    path(User::status).equal(status),
                    path(User::signType).equal(signType),
                )
            )
        }

        val render = renderer.render(query = query, ctx)
        return em.getResult(render, User::class.java)
    }


}
