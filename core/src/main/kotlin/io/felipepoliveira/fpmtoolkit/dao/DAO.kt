package io.felipepoliveira.fpmtoolkit.dao

interface DAO<IDType, ModelType> {

    /**
     * Delete a model form the database. The deleted mode should be a valid
     * model retrieved from the database
     */
    fun delete(m: ModelType)

    /**
     * Find a model by its ID. If the model is not found return a null instead
     */
    fun findById(id: IDType): ModelType?

    /**
     * Persist a model into the database and return its changed state
     */
    fun persist(m: ModelType): ModelType

    /**
     * Update a model from the database and return its changed state. The updated model should be
     * a valid model retrieved from the database so it can be updated correctly
     */
    fun update(m: ModelType): ModelType
}