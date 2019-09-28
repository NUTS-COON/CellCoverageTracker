package ru.firmachi.mobileapp.repositories

import io.realm.Realm
import io.realm.Sort
import ru.firmachi.mobileapp.models.CellData

class CellDataLocalRepositoryImpl : CellDataLocalRepository {

    private var dataChangedCallback: ((List<CellData>) -> Unit)? = null

    override fun saveCellData(cellData: List<CellData>) {
        Realm.getDefaultInstance().executeTransaction {
            it.copyToRealm(cellData)
        }

        dataChangedCallback?.invoke(cellData)
    }


    override fun getAllCellData(): List<CellData> {
         return Realm.getDefaultInstance().where(CellData::class.java).findAll().toList()
    }


    override fun getLastDifferent(): List<CellData>{
        val cellData = Realm.getDefaultInstance()
            .where(CellData::class.java)
            .sort(CellData::timestamp.name, Sort.DESCENDING)
            .limit(2)
            .findAll()

        if(cellData.size == 2 && cellData[0]?.imei == cellData[0]?.imei){
            return cellData.take(1)
        }

        return cellData
    }


    override fun getAllCellDataCount(): Int {
        return Realm.getDefaultInstance().where(CellData::class.java).count().toInt()
    }


    override fun clearAll() {
        Realm.getDefaultInstance().executeTransaction {
            it.delete(CellData::class.java)
        }
    }


    override fun setNotifyDataChanged(callback: (List<CellData>) -> Unit){
        dataChangedCallback = callback
    }
}