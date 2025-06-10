package freeapp.me.s3manager.repo

import com.linecorp.kotlinjdsl.dsl.jpql.jpql
import com.linecorp.kotlinjdsl.render.jpql.JpqlRenderContext
import com.linecorp.kotlinjdsl.render.jpql.JpqlRenderer
import freeapp.me.s3manager.entity.S3Key
import freeapp.me.s3manager.entity.S3Object
import freeapp.me.s3manager.util.getCountByQuery
import freeapp.me.s3manager.util.getResultWithPagination
import jakarta.persistence.EntityManager
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.support.PageableExecutionUtils
import org.springframework.jdbc.core.JdbcTemplate
import java.sql.Timestamp
import javax.sql.DataSource

interface S3ObjectRepository : JpaRepository<S3Object, Long>, S3ObjectCustomRepository {
}

interface S3ObjectCustomRepository {
    fun bulkInsert(s3Objects: List<S3Object>): Array<out IntArray>
    fun findObjectsByS3Key(s3Key: S3Key, pageable: Pageable): Page<S3Object>
}

class S3ObjectCustomRepositoryImpl(
    dataSource: DataSource,
    private val renderer: JpqlRenderer,
    private val ctx: JpqlRenderContext,
    private val em: EntityManager,
) : S3ObjectCustomRepository {

    private val jdbcTemplate = JdbcTemplate(dataSource)


    override fun findObjectsByS3Key(
        s3key: S3Key,
        pageable: Pageable
    ): Page<S3Object> {

        val query = jpql {
            select(
                entity(S3Object::class),
            ).from(
                entity(S3Object::class),
                fetchJoin(S3Object::s3key),
            ).where(
                path(S3Object::s3key).equal(s3key)
            ).orderBy(
                path(S3Object::id).desc(),
            )
        }

        val countQuery = jpql {
            select(
                count(path(S3Object::id)),
            ).from(
                entity(S3Object::class),
                fetchJoin(S3Object::s3key),
            ).where(
                path(S3Object::s3key).equal(s3key)
            ).orderBy(
                path(S3Object::id).desc(),
            )
        }

        val render =
            renderer.render(query = query, ctx)

        val fetch =
            em.getResultWithPagination(render, S3Object::class.java, pageable)

        val count =
            em.getCountByQuery(renderer.render(query = countQuery, ctx).query, render)

        return PageableExecutionUtils.getPage(
            fetch, pageable
        ) { count }
    }


    override fun bulkInsert(s3Objects: List<S3Object>): Array<out IntArray> {

        val sql = """
                INSERT INTO
                s3manager.s3_object
                (s3_key_id, object_key, name,  is_directory, size, last_modified, extension, created_at, updated_at)
                VALUES
                ( ?, ?, ?, ?, ?, ?, ?, ?, ? )
            """.trimIndent()

        try {

            val batchUpdate = jdbcTemplate.batchUpdate(
                sql,
                s3Objects,
                s3Objects.size
            ) { ps, argument ->

                val isDirectoryDB = if (argument.isDirectory) "Y" else "N"

                ps.setLong(1, argument.s3key.id) // 쿼리의 ?의 순서대로 1번으로 할당되며 해당 쿼리 ? 대신 치환
                ps.setString(2, argument.objectKey)
                ps.setString(3, argument.name)
                ps.setString(4, isDirectoryDB)
                ps.setLong(5, argument.size)
                ps.setTimestamp(6, Timestamp.valueOf(argument.lastModified))
                ps.setString(7, argument.extension)
                ps.setTimestamp(8, Timestamp.valueOf(argument.createdAt))
                ps.setTimestamp(9, Timestamp.valueOf(argument.updatedAt))
            }

            return batchUpdate

        } catch (e: Exception) {

            throw e
        }

    }


}
