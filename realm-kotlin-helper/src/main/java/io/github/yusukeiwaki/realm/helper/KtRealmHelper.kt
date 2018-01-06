package io.github.yusukeiwaki.realm.helper

import android.os.Looper
import android.util.Log
import io.realm.OrderedRealmCollection
import io.realm.Realm
import io.realm.RealmModel
import kotlinx.coroutines.experimental.async
import kotlin.coroutines.experimental.suspendCoroutine

object KtRealmHelper {
    /**
     * 使うたびにcloseする必要があるので、
     * realm.use { ... } のように使うこと！
     */
    private val realm
        get() = Realm.getDefaultInstance()

    inline fun logError(e: Exception) {
        Log.w("RealmHelper", e.message, e)
    }

    fun <E : RealmModel> copyFromRealm(objects: Iterable<E>?): List<E> {
        if (objects == null) return emptyList()

        realm.use {
            try {
                return it.copyFromRealm(objects)
            } catch (e: Exception) {
                logError(e)
                return emptyList()
            }
        }
    }

    fun <E : RealmModel> copyFromRealm(entity: E?): E? {
        if (entity == null) return null

        realm.use {
            try {
                return it.copyFromRealm(entity)
            } catch (e: Exception) {
                logError(e)
                return null;
            }
        }
    }

    interface TransactionForRead<T> {
        fun execute(realm: Realm): T
    }

    fun <T : RealmModel> executeTransactionForRead(transaction: TransactionForRead<T>): T? {
        realm.use {
            try {
                return copyFromRealm(transaction.execute(it))
            } catch (e: Exception) {
                logError(e)
                return null;
            }
        }
    }

    fun <T : RealmModel> executeTransactionForReadList(transaction: TransactionForRead<OrderedRealmCollection<T>>): List<T> {
        realm.use {
            try {
                return copyFromRealm(transaction.execute(it))
            } catch(e: Exception) {
                logError(e)
                return emptyList()
            }
        }
    }

    suspend fun executeTransaction(transaction: Realm.Transaction) = async {
        if (shouldUseSyncTransaction) {
            executeTransactionSync(transaction)
        } else {
            executeTransactionAsync(transaction)
        }
    }

    private val shouldUseSyncTransaction: Boolean
        get() {
            // ref: realm-java:realm/realm-library/src/main/java/io/realm/AndroidNotifier.java
            // #isAutoRefreshAvailable()

            if (Looper.myLooper() == null) {
                return true
            }

            val threadName = Thread.currentThread().name
            return threadName != null && threadName.startsWith("IntentService[")
        }

    private fun executeTransactionSync(transaction: Realm.Transaction) {
        realm.use {
            realm.executeTransaction(transaction)
        }
    }

    private suspend fun executeTransactionAsync(transaction: Realm.Transaction) = suspendCoroutine<Unit> { continuation ->
        realm.let {
            //useだと、非同期書き込みが終わる前にcloseしてしまってエラーで落ちるのでletを使って手動でcloseする
            realm.executeTransactionAsync(transaction, Realm.Transaction.OnSuccess {
                continuation.resume(Unit)
                it.close()
            }, Realm.Transaction.OnError { error ->
                continuation.resumeWithException(error)
                it.close()
            })
        }
    }
}