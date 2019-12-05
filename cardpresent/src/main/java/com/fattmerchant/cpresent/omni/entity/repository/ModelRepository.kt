package com.fattmerchant.cpresent.omni.entity.repository

import com.fattmerchant.cpresent.omni.entity.models.Model
import com.fattmerchant.cpresent.omni.networking.OmniApi

/**
 * Provides communication with Omni to manage models of the given type
 *
 * Implementors of this interface will be responsible for CRUD operations
 *
 * @param T - the kind of model this repository will serve
 */
interface ModelRepository<T: Model> {

    /** Responsible for communications with Omni */
    var omniApi: OmniApi

    /**
     * Saves a model of type [T] in Omni
     *
     * @param model the model to save in Omni
     * @param error
     * @return
     */
    suspend fun create(model: T, error: (Error) -> Unit): T? = null

    /**
     * Updates a model of type [T] in Omni
     *
     * @param model the model to update in Omni
     * @param error
     * @return
     */
    suspend fun update(model: T, error: (Error) -> Unit): T? = null

    /**
     * Deletes model of type [T] in Omni
     *
     * @param model
     * @param error
     * @return
     */
    suspend fun delete(model: T, error: (Error) -> Unit): Boolean? = null

    /**
     * Gets a model with the given [id]
     *
     * @param id - the id of the model
     * @param error
     * @return the model itself, if found
     */
    suspend fun getById(id: String, error: (Error) -> Unit): T? = null

    /**
     * Gets a list of models of type [T] from Omni
     *
     * @param error
     * @return the list of models, if found
     */
    suspend fun get(error: (Error) -> Unit): List<T>? = null
}