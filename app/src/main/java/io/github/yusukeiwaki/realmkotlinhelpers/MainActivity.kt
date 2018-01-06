package io.github.yusukeiwaki.realmkotlinhelpers

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import io.github.yusukeiwaki.realm.helper.KtRealmHelper
import io.github.yusukeiwaki.realm_java_helper.RealmObjectObserver
import io.github.yusukeiwaki.realmkotlinhelpers.model.User
import io.realm.Realm
import kotlinx.coroutines.experimental.launch

class MainActivity : Activity() {

    private lateinit var lastUserObserver: RealmObjectObserver<User>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        lastUserObserver = RealmObjectObserver { realm ->
            realm.where(User::class.java).isNotEmpty("friends")
        }
        lastUserObserver.setOnUpdateListener { user ->
            user?.let { updateText(it) }
        }

        startService(Intent(this, MainIntentService::class.java))

        findViewById<Button>(R.id.button).setOnClickListener { _ ->
            launch {
                KtRealmHelper.executeTransaction(Realm.Transaction { realm ->
                    realm.where(User::class.java).findAll().last()?.let {
                        it.friends.add(realm.createObject(User::class.java, System.currentTimeMillis()))
                    }
                })
            }
        }
    }

    override fun onResume() {
        super.onResume()
        lastUserObserver.subscribe()
    }

    override fun onPause() {
        lastUserObserver.unsubscribe()
        super.onPause()
    }

    private fun updateText(user: User) {
        findViewById<TextView>(R.id.text).setText("user ${user.id} has ${user.friends.size} friends.")
    }
}
