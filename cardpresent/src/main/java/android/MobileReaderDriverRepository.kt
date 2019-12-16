package android

import android.chipdna.ChipDnaDriver
import omni.data.MobileReader
import omni.data.MobileReaderDriver
import omni.data.models.Transaction
import omni.data.repository.MobileReaderDriverRepository

class MobileReaderDriverRepository : MobileReaderDriverRepository {

    private var chipDna = ChipDnaDriver()

    override suspend fun getDrivers(): List<MobileReaderDriver> {
        return listOf(chipDna)
    }

    override suspend fun getDriverFor(transaction: Transaction): MobileReaderDriver? {
        if (transaction.source?.contains("CPSDK") == true) {
            return chipDna
        }
        return null
    }

    override suspend fun getDriverFor(mobileReader: MobileReader): MobileReaderDriver? {
        return if (mobileReader.getName().contains("Miura")) {
            chipDna
        } else {
            null
        }
    }

}