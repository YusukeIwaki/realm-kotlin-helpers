package io.github.yusukeiwaki.realmkotlinhelpers

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import io.github.yusukeiwaki.realm.helper.KtRealmHelper
import io.github.yusukeiwaki.realm_java_helper.RealmObjectObserver
import io.github.yusukeiwaki.realmkotlinhelpers.model.User
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
                KtRealmHelper.executeTransaction { realm ->
                    realm.where(User::class.java).findAll().last()?.let {
                        it.friends.add(realm.createObject(User::class.java, System.currentTimeMillis()))
                    }
                }
            }
        }

        KtRealmHelper.readList { realm ->
            realm.where(User::class.java).findAll()
        }.forEach {
            Log.d("RealmHelperSample", "init User[id=${it.id}]")
        }

        KtRealmHelper.read { realm ->
            realm.where(User::class.java).equalTo("id", 1L).findFirst()
        }?.let {
            Log.d("RealmHelperSample", "found: User[id=${it.id}]")
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
