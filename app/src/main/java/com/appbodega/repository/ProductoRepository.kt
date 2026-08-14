//package com.appbodega.repository
//
//import android.content.ContentValues
//import android.content.Context
//import android.database.Cursor
//import com.appbodega.data.DbHelper
//import com.appbodega.entity.Producto
//
//class ProductoRepository(context: Context) {
//
//    private val dbHelper = DbHelper(context)
//    private fun cursorAProducto(cursor: Cursor): Producto {
//        return Producto(
//            id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
//            nombre = cursor.getString(cursor.getColumnIndexOrThrow("nombre")),
//            descripcion = cursor.getString(cursor.getColumnIndexOrThrow("descripcion")),
//            cantidad = cursor.getInt(cursor.getColumnIndexOrThrow("cantidad")),
//            categoria = cursor.getString(cursor.getColumnIndexOrThrow("categoria")),
//            precioCompra = cursor.getDouble(cursor.getColumnIndexOrThrow("precioCompra")),
//            precioVenta = cursor.getDouble(cursor.getColumnIndexOrThrow("precioVenta")),
//            imagenBytes = cursor.getBlob(cursor.getColumnIndexOrThrow("imagen"))
//        )
//    }
//
//    fun insertar(producto: Producto): Long {
//        val db = dbHelper.writableDatabase
//        val valores = ContentValues().apply {
//            put("nombre", producto.nombre)
//            put("descripcion", producto.descripcion)
//            put("cantidad", producto.cantidad)
//            put("categoria", producto.categoria)
//            put("precioCompra", producto.precioCompra)
//            put("precioVenta", producto.precioVenta)
//            put("imagen", producto.imagenBytes)
//        }
//        val id = db.insert("producto", null, valores)
//        db.close()
//        return id
//    }
//
//    fun listarTodos(): List<Producto> {
//        val db = dbHelper.readableDatabase
//        val lista = mutableListOf<Producto>()
//        val cursor = db.rawQuery("Select * from producto", null)
//        while (cursor.moveToNext()) {
//            lista.add(cursorAProducto(cursor))
//        }
//        cursor.close()
//        db.close()
//        return lista
//    }
//
//    fun listarPorCategoria(categoria: String): List<Producto> {
//        val db = dbHelper.readableDatabase
//        val lista = mutableListOf<Producto>()
//        val cursor = db.rawQuery(
//            "Select * from producto where categoria = ?",
//            arrayOf(categoria)
//        )
//        while (cursor.moveToNext()) {
//            lista.add(cursorAProducto(cursor))
//        }
//        cursor.close()
//        db.close()
//        return lista
//    }
//
//    fun buscarPorNombre(texto: String, categoria: String? = null): List<Producto> {
//        val db = dbHelper.readableDatabase
//        val lista = mutableListOf<Producto>()
//
//        val cursor = if (categoria != null) {
//            db.rawQuery(
//                "Select * from producto where nombre like ? and categoria = ?",
//                arrayOf("%$texto%", categoria)
//            )
//        } else {
//            db.rawQuery(
//                "Select * from producto where nombre like ?",
//                arrayOf("%$texto%")
//            )
//        }
//
//        while (cursor.moveToNext()) {
//            lista.add(cursorAProducto(cursor))
//        }
//        cursor.close()
//        db.close()
//        return lista
//    }
//
//    fun actualizar(producto: Producto): Int {
//        val db = dbHelper.writableDatabase
//        val valores = ContentValues().apply {
//            put("nombre", producto.nombre)
//            put("descripcion", producto.descripcion)
//            put("cantidad", producto.cantidad)
//            put("categoria", producto.categoria)
//            put("precioCompra", producto.precioCompra)
//            put("precioVenta", producto.precioVenta)
//            put("imagen", producto.imagenBytes)
//        }
//        val filas = db.update("producto", valores, "id = ?", arrayOf(producto.id.toString()))
//        db.close()
//        return filas
//    }
//
//    fun eliminar(id: Int): Int {
//        val db = dbHelper.writableDatabase
//        val filas = db.delete("producto", "id = ?", arrayOf(id.toString()))
//        db.close()
//        return filas
//    }
//}