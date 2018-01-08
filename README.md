# realm-kotlin-helpers

[realm-java-helpers](https://github.com/YusukeIwaki/realm-java-helpers)のKotlin版。
非同期書き込みのところが、従来はBolts-TaskやRxJavaを使っていたが、このライブラリではKotlinのasync/awaitを利用している。


## Setup

```
repositories {
  maven { url "https://dl.bintray.com/yusukeiwaki/maven" }
}

dependencies {
  implementation 'io.github.yusukeiwaki.realm-kotlin-helpers:realm-kotlin-helper:0.0.2'
}
```

## executeTransaction

```
launch {
    KtRealmHelper.executeTransaction { realm ->
        realm.createObject(User::class.java, 1)
    }.await()

    KtRealmHelper.executeTransaction { realm ->
        realm.createObject(User::class.java, 2)
    }.await()
}
```

## read/readList

```
// read a specific user
KtRealmHelper.read { realm ->
    realm.where(User::class.java).equalTo("id", userId).findFirst()
}?.let {
    Log.d("RealmHelperSample", "found: User[id=${it.id}]")
}

// read all users
KtRealmHelper.readList { realm ->
    realm.where(User::class.java).findAll()
}.forEach {
    Log.d("RealmHelperSample", "list: User[id=${it.id}]")
}
```