package com.fattmerchant.cpresent

import androidx.test.core.app.ApplicationProvider
import androidx.test.runner.AndroidJUnit4
import com.fattmerchant.android.MobileReaderDriverRepository
import com.fattmerchant.omni.data.MobileReaderDriver
import com.fattmerchant.omni.data.models.Merchant
import com.fattmerchant.omni.data.models.OmniException
import com.fattmerchant.omni.usecase.InitializeDrivers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class InitializeDriversTest {

    @Test
    fun initializationFailsIfNoCredsProvided() {
        // Setup
        var error: OmniException? = null
        val expectedError = MobileReaderDriver.InitializeMobileReaderDriverException("emv_password not found")
        val merchant = Merchant().apply {
            // Note that the empty map will cause the ChipDnaDriver to never get the creds it needs to initialize itself
            options = mapOf()
        }
        val args = mapOf(
                "appContext" to ApplicationProvider.getApplicationContext(),
                "appId" to "123",
                "merchant" to merchant
        )
        val job = InitializeDrivers(MobileReaderDriverRepository(), args, Dispatchers.Default)

        // Start the job and capture the error
        runBlocking {
            job.start {
                error = it
            }
        }

        // Assert that the error returned is the expected one
        assertEquals(error?.message, expectedError.message)
        assertEquals(error?.detail, expectedError.detail)
    }
}