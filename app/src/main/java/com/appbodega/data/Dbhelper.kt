//package com.appbodega.data
//
//
//import android.content.Context
//import android.database.sqlite.SQLiteDatabase
//import android.database.sqlite.SQLiteOpenHelper
//
//
//class DbHelper(context: Context) :
//    SQLiteOpenHelper(context, "bodega.db", null, 1) {
//
//    override fun onCreate(db: SQLiteDatabase) {
//        db.execSQL(
//            """
//            create table producto(
//                id INTEGER Primary Key Autoincrement not null,
//                nombre TEXT,
//                descripcion TEXT,
//                cantidad INTEGER,
//                categoria TEXT,
//                precioCompra REAL,
//                precioVenta REAL,
//                imagen BLOB
//            );
//            """.trimIndent()
//        )
//
//        db.execSQL(
//            """
//            create table venta(
//                id INTEGER Primary Key Autoincrement not null,
//                codigo TEXT,
//                fecha TEXT,
//                cantidad INTEGER,
//                metodo TEXT,
//                categoria TEXT,
//                total REAL,
//                productoId INTEGER
//            );
//            """.trimIndent()
//        )
//    }
//
//    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
//        db.execSQL("Drop table if exists producto")
//        db.execSQL("Drop table if exists venta")
//        onCreate(db)
//    }
//}