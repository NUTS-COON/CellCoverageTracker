package ru.firmachi.mobileapp.repositories

import io.realm.Realm
import ru.firmachi.mobileapp.models.CellData

class CellDataLocalRepositoryImpl : CellDataLocalRepository {


    override fun saveCellData(cellData: List<CellData>) {
        Realm.getDefaultInstance().executeTransaction {
            it.copyToRealm(cellData)
        }
    }


    override fun getAllCellData(): List<CellData> {
         return Realm.getDefaultInstance().where(CellData::class.java).findAll().toList()
    }


    override fun getAllCellDataCount(): Int {
        return Realm.getDefaultInstance().where(CellData::class.java).count().toInt()
    }


    override fun clearAll() {
        Realm.getDefaultInstance().executeTransaction {
            it.delete(CellData::class.java)
        }
    }
}