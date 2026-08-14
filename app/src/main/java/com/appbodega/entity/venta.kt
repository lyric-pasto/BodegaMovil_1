package com.appbodega.entity

import java.io.Serializable

data class venta(
    var id: String = "",
    var codigo: String = "",
    var fecha: String = "",
    var cantidad: Int = 0,
    var metodo: String = "Efectivo",
    var categoria: String = "",
    var total: Double = 0.0,
    var productoId: String = "",
    var nombreProducto: String = "",
    var tipoCliente: String = "Normal", // "Normal" o "RUC"
    var documentoCliente: String = "",
    var nombreCliente: String = "",
    var precioUnitario: Double = 0.0
) : Serializable