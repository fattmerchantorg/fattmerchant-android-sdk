package com.fattmerchant.omni.networking

import com.fattmerchant.omni.data.Amount
import com.fattmerchant.omni.data.models.ChargeRequest
import com.fattmerchant.omni.data.models.Customer
import com.fattmerchant.omni.data.models.Invoice
import com.fattmerchant.omni.data.models.Merchant
import com.fattmerchant.omni.data.models.MobileReaderDetails
import com.fattmerchant.omni.data.models.PaymentMethod
import com.fattmerchant.omni.data.models.Self
import com.fattmerchant.omni.data.models.Transaction
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
import org.json.JSONObject

class OmniApi {

    enum class Environment {
        LIVE, DEV;

        fun baseUrl(): String = when (this) {
            LIVE -> "https://apiprod.fattlabs.com/"
            DEV -> "https://apidev.fattlabs.com/"
        }
    }

    private val httpClient = HttpClient {
        install(JsonFeature) {
            serializer = GsonSerializer {
                serializeNulls()
                setFieldNamingStrategy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            }
        }
    }

    internal var token = ""
    internal var environment: Environment = Environment.LIVE

    /**
     * Uses the [token] field and returns the [Self] object associated with that token
     *
     * @param error
     * @return [Self], the owner of the [token]
     */
    internal suspend fun getSelf(error: (Error) -> Unit): Self? = get("self", error)

    /**
     * Gets the mobile reader settings which the [token] corresponds to
     *
     * @param error
     * @return
     */
    internal suspend fun getMobileReaderSettings(error: (Error) -> Unit): MobileReaderDetails? = get("team/gateway/hardware/mobile", error)

    /**
     * Gets the merchant which the [token] corresponds to
     *
     * @param error
     * @return
     */
    internal suspend fun getMerchant(error: (Error) -> Unit): Merchant? = getSelf(error)?.merchant

    /**
     * Gets the invoice which the [id] corresponds to
     *
     * @param error
     * @return the found invoice
     */
    internal suspend fun getInvoice(id: String, error: (Error) -> Unit): Invoice? = get("invoice/$id", error)

    /**
     * Creates a new invoice in Omni
     *
     * @param invoice
     * @return the created invoice
     */
    internal suspend fun createInvoice(invoice: Invoice, error: (Error) -> Unit): Invoice? =
        post("invoice", JsonParser.toJson(invoice), error)

    /**
     * Updates an new invoice in Omni
     *
     * @param invoice
     * @return the updated invoice
     */
    internal suspend fun updateInvoice(invoice: Invoice): Invoice? =
        put("invoice/${invoice.id}", JsonParser.toJson(invoice))

    /**
     * Gets the customer which the [id] corresponds to
     *
     * @param error
     * @return the found customer
     */
    internal suspend fun getCustomer(id: String, error: (Error) -> Unit): Customer? = get("customer/$id", error)

    /**
     * Creates a new customer in Omni
     *
     * @param customer
     * @return the created customer
     */
    internal suspend fun createCustomer(customer: Customer, error: (Error) -> Unit): Customer? =
        post("customer", JsonParser.toJson(customer), error)

    /**
     * Creates a transaction in Omni
     *
     * @param transaction
     * @return the created transaction
     */
    internal suspend fun createTransaction(transaction: Transaction, error: (Error) -> Unit): Transaction? =
        post("transaction", JsonParser.toJson(transaction), error)

    /**
     * Posts a void-or-refund to Omni
     *
     * @param transactionId the id of the transaction to void or refund
     * @param total the amount in dollars to void or refund
     * @return the voided or refunded transaction
     */
    internal suspend fun postVoidOrRefund(transactionId: String, total: String? = null, error: (Error) -> Unit): Transaction? {
        val body = mutableMapOf<Any?, Any?>()
        total?.let {
            body["total"] = it
        }
        return post("transaction/$transactionId/void-or-refund", JSONObject(body).toString(), error)
    }

    internal suspend fun captureTransaction(transactionId: String, total: Amount?, error: (Error) -> Unit): Transaction? {
        return captureAuthedTransaction(transactionId, total, error)
    }

    internal suspend fun voidTransaction(transactionId: String, error: (Error) -> Unit): Transaction? {
        return post("/transaction/$transactionId/void", "", error)
    }

    /**
     * Updates an transaction in Omni
     *
     * @param transaction
     * @return the updated transaction
     */
    internal suspend fun updateTransaction(transaction: Transaction): Transaction? =
        put("transaction/${transaction.id}", JsonParser.toJson(transaction))

    /**
     * Gets a list of transactions from Omni
     *
     * @return the list of transactions
     */
    internal suspend fun getTransactions(error: (Error) -> Unit): List<Transaction>? =
        get<PaginatedData<Transaction>>("transaction", error)?.data

    /**
     * Gets a list of transactions from Omni
     *
     * @return the list of transactions
     */
    internal suspend fun getInvoices(error: (Error) -> Unit): List<Invoice>? =
        get<PaginatedData<Invoice>>("invoice", error)?.data

    /**
     * Creates a payment method in Omni
     *
     * @param paymentMethod
     * @return the created payment method
     */
    internal suspend fun createPaymentMethod(paymentMethod: PaymentMethod, error: (Error) -> Unit): PaymentMethod? {
        // If the payment method is already tokenized, use the payment-method/token route
        // that allows us to pass the token to Omni
        val url = if (paymentMethod.paymentToken != null) {
            "payment-method/token"
        } else {
            "payment-method"
        }

        return post(url, JsonParser.toJson(paymentMethod), error)
    }

    internal suspend fun charge(chargeRequest: ChargeRequest, error: (Error) -> Unit): Transaction? {
        return post("charge", JsonParser.toJson(chargeRequest), error)
    }

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

    private suspend inline fun <reified T> get(urlString: String, error: (Error) -> Unit): T? = this.request<T>(
        method = HttpMethod.Get,
        urlString = urlString,
        error = error
    )

    private suspend inline fun <reified T> request(method: HttpMethod, urlString: String, body: String? = null): T? =
        request(method, urlString, body) {}

    private suspend inline fun captureAuthedTransaction(
        transactionId: String,
        captureAmount: Amount?,
        error: ((Error) -> Unit)
    ): Transaction? {
        val url = environment.baseUrl() + "/transaction/$transactionId/capture"

        var body: String? = null

        captureAmount?.let {
            val b = mutableMapOf<Any?, Any?>()
            b["total"] = it.dollars()

            body = JSONObject(b).toString()
        }

        try {

            // Make the request and wait for the response
            val response = httpClient.request<HttpResponse>(url) {
                headers.append("Authorization", "Bearer $token")
                this.method = HttpMethod.Post
                body?.let {
                    this.body = TextContent(it, ContentType.Application.Json)
                }
            }

            // Attempt to get the object we're expecting
            if (response.status.isSuccess()) {
                return response.receive()
            }

            val responseText = response.readText()

            // Parse the transaction
            val transaction = JsonParser.gson.fromJson(responseText, Transaction::class.java)

            // Get the first child capture
            val firstChildCapture = transaction.childCaptures?.firstOrNull()

            // Return the message from the first child capture
            val failedCaptureMessage = firstChildCapture?.message

            // Read the error text, if possible
            val errorText: String? = failedCaptureMessage ?: try {
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

    private suspend inline fun <reified T> request(
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
            val errorText: String? = try {
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

    private fun isTokenExpired(response: String): Boolean = response.contains("token_expired")
}
