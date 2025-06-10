package freeapp.me.s3manager.util

import com.linecorp.kotlinjdsl.querymodel.jpql.select.SelectQuery
import com.linecorp.kotlinjdsl.render.jpql.JpqlRendered
import jakarta.persistence.EntityManager
import jakarta.persistence.NoResultException
import org.springframework.data.domain.Pageable
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun <T : Any> EntityManager.getSingleResultOrNull(
    render: JpqlRendered,
    classType: Class<T>
): T? {

    val typedQuery = this.createQuery(render.query, classType).apply {
        render.params.forEach { name, value ->
            setParameter(name, value)
        }
    }

    return try {
        typedQuery.singleResult
    } catch (e: NoResultException) {
        null
    }

}


fun <T : Any> EntityManager.getRecentSingleResultOrNull(
    render: JpqlRendered,
    classType: Class<T>
): T? {

    val typedQuery = this.createQuery(render.query, classType).apply {
        render.params.forEach { name, value ->
            setParameter(name, value)
        }
        maxResults = 1
    }

    return typedQuery.resultList.firstOrNull()
}



inline fun <reified T : Any> EntityManager.getResult(
    render: JpqlRendered,
    classType: Class<T>
): MutableList<T> {

    val typedQuery = this.createQuery(render.query, classType).apply {
        render.params.forEach { name, value ->
            setParameter(name, value)
        }
    }
    return typedQuery.resultList
}




inline fun <reified T : Any> EntityManager.getResultWithPagination(
    render: JpqlRendered,
    type: Class<T>,
    pageable: Pageable
): MutableList<T> {

    val fetch = this.createQuery(render.query, type).apply {
        render.params.forEach { name, value ->
            setParameter(name, value)
        }
    }

    fetch.firstResult = pageable.offset.toInt()
    fetch.maxResults = pageable.pageSize

    return fetch.resultList
}

fun EntityManager.getCountByQuery(
    countQuery: String,
    render: JpqlRendered
): Long {

    val count = this.createQuery(countQuery, Long::class.java).apply {
        render.params.forEach { name, value ->
            setParameter(name, value)
        }
    }.singleResult

    return count
}




fun LocalDateTime.toStringByFormat(pattern: String = "yyyy-MM-dd HH:mm:ss"): String {
    val dateTimeFormatter =
        DateTimeFormatter.ofPattern(pattern)
    return dateTimeFormatter.format(this).toString()
}
