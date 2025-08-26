package io.felipepoliveira.fpmtoolkit.dao.jpa

import io.felipepoliveira.fpmtoolkit.dao.DAO
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import jakarta.persistence.Query

abstract class BaseJpa<IDType, ModelType> : DAO<IDType, ModelType>  {

    @PersistenceContext
    lateinit var entityManager: EntityManager

    abstract fun getModelType(): Class<ModelType>

    /**
     * Create a SmartQuery instance. A Smart Query is a class constructed to produce a better syntax way to write
     * HQLs
     */
    fun paginatedQuery(typeAlias: String) = HqlSmartQuery(entityManager, getModelType(), typeAlias).asPagination()

    /**
     * Create a SmartQuery instance. A Smart Query is a class constructed to produce a better syntax way to write
     * HQLs
     */
    fun query(typeAlias: String) = HqlSmartQuery(entityManager, getModelType(), typeAlias)

    override fun delete(m: ModelType) {
        entityManager.remove(m)
    }

    override fun findById(id: IDType): ModelType? {
        return entityManager.find(getModelType(), id)
    }

    override fun persist(m: ModelType): ModelType {
        entityManager.persist(m)
        return m
    }

    override fun update(m: ModelType): ModelType {
        entityManager.merge(m)
        return m
    }
}

class HqlSmartQuery<ModelType>(private val entityManager: EntityManager, modelType: Class<ModelType>, private val typeAlias: String) {

    private var hqlFromPart: String = "FROM ${modelType.simpleName} $typeAlias"

    private var hqlJoinPart: String = ""

    private var hqlSelectPart = "SELECT $typeAlias"

    private var hqlWherePart: String = ""

    fun asPagination(): HqlSmartQuery<ModelType> {
        hqlSelectPart = "SELECT COUNT($typeAlias)"
        return this
    }

    /**
     * Change the default `FROM $typeAlias` part of the HQL to `FROM $fromHql` given by this method
     */
    fun from(fromHql: String): HqlSmartQuery<ModelType> {
        hqlFromPart = "FROM $fromHql"
        return this
    }

    fun join(field: String, joinFieldAlias: String): HqlSmartQuery<ModelType> {
        hqlJoinPart += " JOIN $field $joinFieldAlias"
        return this
    }

    /**
     * Change the default `SELECT $typeAlias` part of the HQL to `SELECT fieldsHql` given by this method
     */
    fun select(fieldsHql: String): HqlSmartQuery<ModelType> {
        hqlFromPart = "SELECT $fieldsHql"
        return this
    }

    /**
     * Set the WHERE clause to `WHERE $whereHql` clause into the final HQL
     */
    fun where(whereHql: String): HqlSmartQueryWhereClause<ModelType> {
        hqlWherePart = "WHERE $whereHql"
        return HqlSmartQueryWhereClause(this)
    }

    /**
     * Create
     */
    fun prepare(): Query {
        val hql = "$hqlSelectPart $hqlFromPart $hqlJoinPart $hqlWherePart"
        return entityManager.createQuery(hql)
    }

    /**
     * Add Where Clause semantics to HqlSmartQuery
     */
    class HqlSmartQueryWhereClause<ModelType> internal constructor(
        private val source: HqlSmartQuery<ModelType>
    ) {

        private val parameters = mutableMapOf<String, Any>()

        /**
         * Add a AND clause to the existing WHERE HQL query string
         */
        fun and(whereHql: String): HqlSmartQueryWhereClause<ModelType> {
            source.hqlWherePart += " AND $whereHql"
            return this
        }

        fun andStartQueryGroup(whereHql: String): HqlSmartQueryWhereClause<ModelType> {
            source.hqlWherePart += " AND ($whereHql"
            return this
        }

        fun closeQueryGroup(): HqlSmartQueryWhereClause<ModelType> {
            source.hqlWherePart += ")"
            return this
        }

        /**
         * Finish building the syntax for the Hql Smart Query and return the main reference
         */
        fun build(): HqlSmartQuery<ModelType> = source

        fun or(whereHql: String): HqlSmartQueryWhereClause<ModelType> {
            source.hqlWherePart += " OR $whereHql"
            return this
        }

        fun prepare(): Query {
            val jpaQuery = source.prepare()
            // add all parameters into the query
            for (parameter in parameters) {
                jpaQuery.setParameter(parameter.key, parameter.value)
            }

            return jpaQuery
        }

        fun <T> setParameter(key: String, value: T): HqlSmartQueryWhereClause<ModelType> {
            parameters[key] = value as Any
            return this
        }
    }

}