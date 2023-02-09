package com.staxpayments.api.datasource

import com.staxpayments.api.models.Customer
import com.staxpayments.api.network.NetworkClient
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.BDDMockito.given
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class CustomerLiveRepositoryTest {

    private val customer = Customer(
        id = "680afd1b-713f-46c1-af46-1a56b8b5f8a6",
        firstName = "Ade",
        lastName = "GIFTFORYOU",
        company = "",
        email = "ted88@gmail.com",
        CCemails = emptyList(),
        CCsms = null,
        phone = "",
        address1 = "",
        address2 = "",
        addressCity = "",
        addressState = "",
        addressZip = "",
        addressCountry = "",
        notes = "",
        reference = "",
        options = null,
        createdAt = "2020-10-07 15:50:39",
        updatedAt = "2022-01-31 21:59:09",
        deletedAt = null,
        doesAllowInvoiceCreditCardPayments = true,
        gravatar = "//www.gravatar.com/avatar/f3f5de60b600a14d62657be794b7b23d",
        hasAddress = true,
        missingAddressComponents = emptyList()
    )



    private lateinit var classUnderTest: CustomerLiveRepository

    @Mock
    private lateinit var networkClients: NetworkClient

    @Before
    fun setup() {
        classUnderTest = CustomerLiveRepository(
            networkClients
        )
    }

    @Test
    fun `given userId  When getCustomerById Then return expected customer`() =
        runBlocking {

            val expectedResult = customer
            val id = "123"

            // given
            given(
                networkClients.get("customer/$id", responseType = Customer.serializer())
            ).willReturn(expectedResult)

            // When
            val actualCommand = classUnderTest.getCustomerById(id = id)

            // Then
            assertEquals(expectedResult, actualCommand)
        }

    @Test
    fun `given customerRequest  When createCustomer Then return expected customer`() =
        runBlocking {

            val request = customer
            val expectedResult = customer

            // given
            given(
                networkClients.post("customer", request = request, responseType = Customer.serializer())
            ).willReturn(expectedResult)

            // When
            val actualCommand = classUnderTest.createCustomer(customer)

            // Then
            assertEquals(expectedResult, actualCommand)
        }

    @Test
    fun `given customerRequest and userid  When updateCustomer Then return expected customer`() =
        runBlocking {

            val request = customer
            val expectedResult = customer
            val id = "123"

            // given
            given(
                networkClients.put("customer/$id", request = request, responseType = Customer.serializer())
            ).willReturn(expectedResult)

            // When
            val actualCommand = classUnderTest.updateCustomer(customer, id)

            // Then
            assertEquals(expectedResult, actualCommand)
        }
}
