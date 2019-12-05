//package com.fattmerchant.cpresent.android.transaction
//
//import com.fattmerchant.cpresent.android.api.OmniApi
//import com.fattmerchant.cpresent.omni.entity.models.Transaction as OmniTransaction
//import com.fattmerchant.cpresent.omni.entity.repository.TransactionRepository
//import timber.log.Timber
//
//class TransactionRepository(
//    val omniApi: OmniApi
//) : TransactionRepository {
//
//    override suspend fun get(): List<OmniTransaction> {
//        try {
//            val transactions = omniApi.getTransactions().data
//            Timber.i("Fetched transactions")
//            return transactions
//        } catch (e: Throwable) {
//            Timber.e(e)
//        }
//        return listOf()
//    }
//
//    override suspend fun create(model: OmniTransaction): OmniTransaction {
//        try {
//            val created = omniApi.createTransaction(Transaction.fromOmniTransaction(model))
//            Timber.i("created one")
//            return created
//        } catch (e: Throwable) {
//            Timber.e(e)
//        }
//
//        return OmniTransaction()
//    }
//
//    override suspend fun update(model: OmniTransaction): OmniTransaction {
//        return OmniTransaction()
//    }
//
//    override suspend fun delete(model: OmniTransaction): Boolean {
//        return true
//    }
//
//    override suspend fun getById(id: String): OmniTransaction {
//        return OmniTransaction()
//    }
//
//}
