package io.github.yusukeiwaki.realmkotlinhelpers

import android.app.IntentService
import android.content.Intent
import io.github.yusukeiwaki.realm.helper.KtRealmHelper
import io.github.yusukeiwaki.realmkotlinhelpers.model.User
import kotlinx.coroutines.experimental.launch

class MainIntentService : IntentService("MainIntentService") {

    override fun onHandleIntent(intent: Intent?) {
        launch {
            KtRealmHelper.executeTransaction { realm ->
                realm.createObject(User::class.java, System.currentTimeMillis())
            }.await()

            KtRealmHelper.executeTransaction { realm ->
                realm.createObject(User::class.java, System.currentTimeMillis()).let {
                    it.friends.add(realm.where(User::class.java).findFirst())
                }
            }.await()
        }
    }
}
