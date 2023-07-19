package ru.oas.fap.extensions

import org.json.JSONObject
import ru.oas.fap.room.InventData
import ru.oas.fap.room.ScanData

fun addScan(scan: ScanData): JSONObject {
    val json = JSONObject()
    json.put("dateTime", scan.dateTime)
    json.put("numDoc", scan.numDoc)
    json.put("barcode", scan.barcode)
    json.put("nameNomen", scan.nameNomen)
    json.put("price", scan.price)
    json.put("part", scan.part)
    return json
}

fun addScanInvent(scan: InventData): JSONObject {
    val json = JSONObject()
    json.put("dateTime", scan.dateTime)
    json.put("numDoc", scan.numDoc)
    json.put("barcode", scan.barcode)
    json.put("nameNomen", scan.nameNomen)
    json.put("price", scan.price)
    json.put("part", scan.part)
    return json
}
