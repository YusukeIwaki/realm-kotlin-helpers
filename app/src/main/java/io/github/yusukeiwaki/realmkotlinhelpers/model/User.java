package io.github.yusukeiwaki.realmkotlinhelpers.model;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class User extends RealmObject {
    @PrimaryKey public long id;
    public RealmList<User> friends;
}
