package com.staxpayments.sdk.data.repository

import com.staxpayments.sdk.data.models.Model
import com.staxpayments.sdk.data.models.OmniException
import com.staxpayments.sdk.networking.OmniApi

/**
 * Provides communication with Omni to manage models of the given type
 *
 * Implementors of this interface will be responsible for CRUD operations
 *
 * @param T - the kind of model this repository will serve
 */
internal interface ModelRepository<T : Model> {

    /** Responsible for communications with Omni */
    var omniApi: OmniApi

    /**
     * Saves a model of type [T] in Omni
     *
     * @param model the model to save in Omni
     * @param error
     * @return
     */
    suspend fun create(model: T, error: (OmniException) -> Unit): T? = null

    /**
     * Updates a model of type [T] in Omni
     *
     * @param model the model to update in Omni
     * @param error
     * @return
     */
    suspend fun update(model: T, error: (OmniException) -> Unit): T? = null

    /**
     * Deletes model of type [T] in Omni
     *
     * @param model
     * @param error
     * @return
     */
    suspend fun delete(model: T, error: (OmniException) -> Unit): Boolean? = null

    /**
     * Gets a model with the given [id]
     *
     * @param id - the id of the model
     * @param error
     * @return the model itself, if found
     */
    suspend fun getById(id: String, error: (OmniException) -> Unit): T? = null

    /**
     * Gets a list of models of type [T] from Omni
     *
     * @param error
     * @return the list of models, if found
     */
    suspend fun get(error: (OmniException) -> Unit): List<T>? = null
}
