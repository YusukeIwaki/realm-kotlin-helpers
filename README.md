# realm-kotlin-helpers

[realm-java-helpers](https://github.com/YusukeIwaki/realm-java-helpers)のKotlin版。
非同期書き込みのところが、従来はBolts-TaskやRxJavaを使っていたが、このライブラリではKotlinのasync/awaitを利用している。

## executeTransaction

```
launch {
    KtRealmHelper.executeTransaction(Realm.Transaction { realm ->
        realm.createObject(User::class.java, 1)
    }).await()

    KtRealmHelper.executeTransaction(Realm.Transaction { realm ->
        realm.createObject(User::class.java, 2)
    }).await()
}
```

