//package com.appbodega.repository
//
//import android.content.ContentValues
//import android.content.Context
//import android.database.Cursor
//import com.appbodega.data.DbHelper
//import com.appbodega.entity.venta
//
//class VentaRepository(context: Context) {
//
//    private val dbHelper = DbHelper(context)
//
//    private fun cursorAVenta(cursor: Cursor): venta {
//        return venta(
//            id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
//            codigo = cursor.getString(cursor.getColumnIndexOrThrow("codigo")),
//            fecha = cursor.getString(cursor.getColumnIndexOrThrow("fecha")),
//            cantidad = cursor.getInt(cursor.getColumnIndexOrThrow("cantidad")),
//            metodo = cursor.getString(cursor.getColumnIndexOrThrow("metodo")),
//            categoria = cursor.getString(cursor.getColumnIndexOrThrow("categoria")),
//            total = cursor.getDouble(cursor.getColumnIndexOrThrow("total")),
//            productoId = cursor.getInt(cursor.getColumnIndexOrThrow("productoId"))
//        )
//    }
//
//    fun insertar(venta: venta): Long {
//        val db = dbHelper.writableDatabase
//        val valores = ContentValues().apply {
//            put("codigo", venta.codigo)
//            put("fecha", venta.fecha)
//            put("cantidad", venta.cantidad)
//            put("metodo", venta.metodo)
//            put("categoria", venta.categoria)
//            put("total", venta.total)
//            put("productoId", venta.productoId)
//        }
//        val id = db.insert("venta", null, valores)
//        db.close()
//        return id
//    }
//
//    fun listarTodas(): List<venta> {
//        val db = dbHelper.readableDatabase
//        val lista = mutableListOf<venta>()
//        val cursor = db.rawQuery("Select * from venta order by id desc", null)
//        while (cursor.moveToNext()) {
//            lista.add(cursorAVenta(cursor))
//        }
//        cursor.close()
//        db.close()
//        return lista
//    }
//
//    fun listarPorMetodo(metodo: String): List<venta> {
//        val db = dbHelper.readableDatabase
//        val lista = mutableListOf<venta>()
//        val cursor = db.rawQuery(
//            "Select * from venta where metodo = ? order by id desc",
//            arrayOf(metodo)
//        )
//        while (cursor.moveToNext()) {
//            lista.add(cursorAVenta(cursor))
//        }
//        cursor.close()
//        db.close()
//        return lista
//    }
//
//    fun listarPorRangoFechas(desde: String, hasta: String): List<venta> {
//        val db = dbHelper.readableDatabase
//        val lista = mutableListOf<venta>()
//        val cursor = db.rawQuery(
//            "Select * from venta where fecha between ? and ? order by fecha asc",
//            arrayOf(desde, hasta)
//        )
//        while (cursor.moveToNext()) {
//            lista.add(cursorAVenta(cursor))
//        }
//        cursor.close()
//        db.close()
//        return lista
//    }
//
//    fun totalVentas(): Double {
//        val db = dbHelper.readableDatabase
//        val cursor = db.rawQuery("Select sum(total) as total from venta", null)
//        var total = 0.0
//        if (cursor.moveToFirst()) {
//            total = cursor.getDouble(cursor.getColumnIndexOrThrow("total"))
//        }
//        cursor.close()
//        db.close()
//        return total
//    }
//}