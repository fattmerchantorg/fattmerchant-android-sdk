package com.staxpayments.api.repository

import com.staxpayments.exceptions.StaxException
import com.staxpayments.api.StaxApi
import com.staxpayments.api.models.Model

/**
 * Provides communication with Stax to manage models of the given type
 *
 * Implementors of this interface will be responsible for CRUD operations
 *
 * @param T - the kind of model this repository will serve
 */
internal interface ModelRepository<T : Model> {

    /** Responsible for communications with Stax */
    var staxApi: StaxApi

    /**
     * Saves a model of type [T] in Stax
     *
     * @param model the model to save in Stax
     * @param error
     * @return
     */
    suspend fun create(model: T, error: (StaxException) -> Unit): T? = null

    /**
     * Updates a model of type [T] in Stax
     *
     * @param model the model to update in Stax
     * @param error
     * @return
     */
    suspend fun update(model: T, error: (StaxException) -> Unit): T? = null

    /**
     * Deletes model of type [T] in Stax
     *
     * @param model
     * @param error
     * @return
     */
    suspend fun delete(model: T, error: (StaxException) -> Unit): Boolean? = null

    /**
     * Gets a model with the given [id]
     *
     * @param id - the id of the model
     * @param error
     * @return the model itself, if found
     */
    suspend fun getById(id: String, error: (StaxException) -> Unit): T? = null

    /**
     * Gets a list of models of type [T] from Stax
     *
     * @param error
     * @return the list of models, if found
     */
    suspend fun get(error: (StaxException) -> Unit): List<T>? = null
}
