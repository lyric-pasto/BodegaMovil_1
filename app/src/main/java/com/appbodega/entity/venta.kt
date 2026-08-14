package com.appbodega.entity

import java.io.Serializable

data class venta(
    val id: String = "",           // String porque Firebase genera IDs tipo "-NxAbc123"
    val codigo: String = "",       // "V-001", generado automáticamente
    val fecha: String = "",        // "dd/MM/yyyy"
    val cantidad: Int = 0,
    val metodo: String = "",       // "Efectivo" o "Yape"
    val categoria: String = "",    // categoría del producto vendido
    val total: Double = 0.0,
    val productoId: String = ""    // String, igual que Producto.id
) : Serializable
