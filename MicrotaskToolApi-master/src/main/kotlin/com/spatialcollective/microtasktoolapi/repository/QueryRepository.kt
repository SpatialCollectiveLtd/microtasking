package com.spatialcollective.microtasktoolapi.repository

import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext

private const val BATCH_SIZE = 500

@Repository
class QueryRepository {
    @PersistenceContext
    private lateinit var entityManager: EntityManager

    @Transactional
    fun insertAllWithQuery(tableName: String, columns: String, values: String) {
        entityManager.createNativeQuery("INSERT INTO $tableName $columns VALUES $values").executeUpdate()
        entityManager.flush()
        entityManager.clear()
    }

    @Transactional
    fun insertAllWithList(tableName: String, columns: String, valuesList: List<String>) {
        val chunkedLinkList = valuesList.chunked(BATCH_SIZE)
        chunkedLinkList.forEach {
            val values = it.toString().replace("[", "").replace("]", "")
            insertAllWithQuery(tableName, columns, values)
        }

    }
}