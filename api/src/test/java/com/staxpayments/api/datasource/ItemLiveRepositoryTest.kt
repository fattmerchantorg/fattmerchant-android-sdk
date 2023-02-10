package com.staxpayments.api.datasource
import com.staxpayments.api.models.Item
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
class ItemLiveRepositoryTest {

    private val item = Item(
        id = "79256d84-3930-4e4b-bc71-9c6587cfc65c",
        userId = "b58d7eee-e68d-4d12-a1f8-62f5e71382ae",
        item = "meeseeks",
        createdAt = "2022-11-16 19:04:33",
        updatedAt = "2022-11-16 19:03:33",
        thumbnailId = null,
        details = null,
        files = null,
        deletedAt = null,
        isActive = true,
        deprecationWarning = "admin",
        isDiscount = true,
        isTaxable = true,
        code = "",
        inStock = 1,
        merchantId = "dd36b936-1eb7-4ece-bebc-b514c6a36ebd",
        thumbnail = null,
        user = null,
        price = 32.0,
        meta = null,
        isService = true
    )

    private lateinit var classUnderTest: ItemLiveRepository

    @Mock
    private lateinit var networkClients: NetworkClient

    @Before
    fun setup() {
        classUnderTest = ItemLiveRepository(
            networkClients
        )
    }

    @Test
    fun `given itemId  When getItemById Then return expected Item`() =
        runBlocking {

            val expectedResult = item
            val id = "123"

            // given
            given(
                networkClients.get("item/$id", responseType = Item.serializer())
            ).willReturn(expectedResult)

            // When
            val actualCommand = classUnderTest.getItemById(id = id)

            // Then
            assertEquals(expectedResult, actualCommand)
        }

    @Test
    fun `given itemRequest  When createItem Then return expected Item`() =
        runBlocking {

            val request = item
            val expectedResult = item

            // given
            given(
                networkClients.post("item", request = request, responseType = Item.serializer())
            ).willReturn(expectedResult)

            // When
            val actualCommand = classUnderTest.createItem(item)

            // Then
            assertEquals(expectedResult, actualCommand)
        }

    @Test
    fun `given itemRequest and itemId  When updateItem Then return expected Item`() =
        runBlocking {

            val request = item
            val expectedResult = item
            val id = "123"

            // given
            given(
                networkClients.put("item/$id", request = request, responseType = Item.serializer())
            ).willReturn(expectedResult)

            // When
            val actualCommand = classUnderTest.updateItem(item, id)

            // Then
            assertEquals(expectedResult, actualCommand)
        }
}
