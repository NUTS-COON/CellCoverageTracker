package ru.firmachi.mobileapp.repositories

import ru.firmachi.mobileapp.models.CellData

interface CellDataLocalRepository {
    fun saveCellData(cellData: List<CellData>)
    fun getAllCellData(): List<CellData>
    fun getAllCellDataCount(): Int
    fun clearAll()
}