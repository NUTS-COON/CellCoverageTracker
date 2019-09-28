package ru.firmachi.mobileapp.models

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.util.*

open class CellData : RealmObject() {

    @PrimaryKey
    var id: String = UUID.randomUUID().toString()
    var latitude: Double = 0.0
    var longitude: Double = 0.0
    var cellType: String = ""
    var operatorName: String = ""
    var level: Int = 0
    var dbm: Int = 0
    var timestamp: String = ""
    var imei: String = ""
}