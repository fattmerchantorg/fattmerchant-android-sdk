package com.fattmerchant.omni.networking

import com.fattmerchant.omni.data.models.*
import com.google.gson.FieldNamingPolicy
import io.ktor.client.HttpClient
import io.ktor.client.call.NoTransformationFoundException
import io.ktor.client.call.receive
import io.ktor.client.features.json.GsonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.request.request
import io.ktor.client.response.HttpResponse
import io.ktor.client.response.readText
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.content.TextContent
import io.ktor.http.isSuccess

class OmniApi {

    enum class Environment {
        LIVE, DEV;

        fun baseUrl(): String = when (this) {
            LIVE -> "https://apiprod.fattlabs.com/"
            DEV -> "https://apidev.fattlabs.com/"
        }
    }

    val httpClient = HttpClient {
        install(JsonFeature) {
            serializer = GsonSerializer {
                serializeNulls()
                setFieldNamingStrategy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            }
        }
    }

    var token = ""
    var environment: Environment = Environment.LIVE

    /**
     * Uses the [token] field and returns the [Self] object associated with that token
     *
     * @param error
     * @return [Self], the owner of the [token]
     */
    suspend fun getSelf(error: (Error) -> Unit): Self? = get("self", error)

    /**
     * Gets the merchant which the [token] corresponds to
     *
     * @param error
     * @return
     */
    suspend fun getMerchant(error: (Error) -> Unit): Merchant? = getSelf(error)?.merchant

    /**
     * Creates a new invoice in Omni
     *
     * @param invoice
     * @return the created invoice
     */
    suspend fun createInvoice(invoice: Invoice, error: (Error) -> Unit): Invoice? =
        post("invoice", JsonParser.toJson(invoice), error)

    /**
     * Updates an new invoice in Omni
     *
     * @param invoice
     * @return the updated invoice
     */
    suspend fun updateInvoice(invoice: Invoice): Invoice? =
        put("invoice/${invoice.id}", JsonParser.toJson(invoice))

    /**
     * Creates a new customer in Omni
     *
     * @param customer
     * @return the created customer
     */
    suspend fun createCustomer(customer: Customer, error: (Error) -> Unit): Customer? =
        post("customer", JsonParser.toJson(customer), error)

    /**
     * Creates a transaction in Omni
     *
     * @param transaction
     * @return the created transaction
     */
    suspend fun createTransaction(transaction: Transaction, error: (Error) -> Unit): Transaction? =
        post("transaction", JsonParser.toJson(transaction), error)

    /**
     * Gets a list of transactions from Omni
     *
     * @return the list of transactions
     */
    suspend fun getTransactions(error: (Error) -> Unit): List<Transaction>? =
        get<PaginatedData<Transaction>>("transaction", error)?.data

    /**
     * Gets a list of transactions from Omni
     *
     * @return the list of transactions
     */
    suspend fun getInvoices(error: (Error) -> Unit): List<Invoice>? =
        get<PaginatedData<Invoice>>("invoice", error)?.data

    /**
     * Creates a payment method in Omni
     *
     * @param paymentMethod
     * @return the created payment method
     */
    suspend fun createPaymentMethod(paymentMethod: PaymentMethod, error: (Error) -> Unit): PaymentMethod? =
        post("payment-method", JsonParser.toJson(paymentMethod), error)

    private suspend inline fun <reified T> post(urlString: String, body: String, error: (Error) -> Unit): T? =
        this.request<T>(
            method = HttpMethod.Post,
            urlString = urlString,
            body = body,
            error = error
        )

    private suspend inline fun <reified T> put(urlString: String, body: String): T? = this.request<T>(
        method = HttpMethod.Put,
        urlString = urlString,
        body = body
    )

    suspend inline fun <reified T> get(urlString: String, error: (Error) -> Unit): T? = this.request<T>(
        method = HttpMethod.Get,
        urlString = urlString,
        error = error
    )

    suspend inline fun <reified T> request(method: HttpMethod, urlString: String, body: String? = null): T? =
        request(method, urlString, body) {}

    suspend inline fun <reified T> request(
        method: HttpMethod,
        urlString: String,
        body: String? = null,
        error: ((Error) -> Unit)
    ): T? {
        val url = environment.baseUrl() + urlString

        try {

            // Make the request and wait for the response
            val response = httpClient.request<HttpResponse>(url) {
                headers.append("Authorization", "Bearer $token")
                this.method = method
                body?.let {
                    this.body = TextContent(body, ContentType.Application.Json)
                }
            }

            // Attempt to get the object we're expecting
            if (response.status.isSuccess()) {
                return response.receive()
            }

            val responseText = response.readText()

            // Read the error text, if possible
            val  errorText: String? = try {
                (JsonParser.fromJson<Map<*, *>>(responseText)["error"] as? String)?.let {
                    it
                }
            } catch (e: Exception) {
                null
            }

            // Triage the error
            when (response.status.value) {
                in 300..399 -> {
                    // Redirect
                    error(Error(errorText ?: "Redirect"))
                    return null
                }

                in 400..499 -> {
                    // Client error
                    if (isTokenExpired(responseText)) {
                        error(Error("token_expired"))
                        return null
                    }

                    error(Error(errorText ?: "Client error"))
                    return null
                }

                in 500..599 -> {
                    // Server error
                    error(Error(errorText ?: "Server error"))
                    return null
                }
            }
            return null

        } catch (e: NoTransformationFoundException) {
            // We were expecting an object of type T, but couldn't transform the response body to T
            print(e)
            throw e
        } catch (e: Exception) {
            // This happens when there was an error executing the request
            // This is a good place to look at that exception and do something with it before throwing it back
            print(e)
            throw e
        }
    }

    fun isTokenExpired(response: String): Boolean = response.contains("token_expired")

}
