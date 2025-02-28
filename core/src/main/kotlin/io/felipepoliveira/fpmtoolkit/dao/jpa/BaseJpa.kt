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

    private var hqlSelectPart = "SELECT $typeAlias"

    private var hqlWherePart: String = ""

    fun asPagination(): HqlSmartQuery<ModelType> {
        hqlFromPart = "SELECT COUNT($typeAlias)"
        return this
    }

    /**
     * Change the default `FROM $typeAlias` part of the HQL to `FROM $fromHql` given by this method
     */
    fun from(fromHql: String): HqlSmartQuery<ModelType> {
        hqlFromPart = "FROM $fromHql"
        return this
    }

    /**
     * Change the default `SELECT $typeAlias` part of the HQL to `SELECT $fromHql` given by this method
     */
    fun select(fromHql: String): HqlSmartQuery<ModelType> {
        hqlFromPart = "SELECT $fromHql"
        return this
    }

    /**
     * Set the WHERE clause to `WHERE $whereHql` clause into the final HQL
     */
    fun where(whereHql: String): HqlSmartQuery<ModelType> {
        hqlWherePart = "WHERE $whereHql"
        return this
    }

    /**
     * Create
     */
    fun prepare(): Query = entityManager.createQuery("$hqlSelectPart $hqlFromPart $hqlWherePart")
}