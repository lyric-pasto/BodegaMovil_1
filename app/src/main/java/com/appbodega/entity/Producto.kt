package com.appbodega.entity

import java.io.Serializable
data class Producto(
    var nombre: String = "",
    var descripcion: String = "",
    var cantidad: Int = 0,
    var categoria: String = "",
    var precioCompra: Double = 0.0,
    var precioVenta: Double = 0.0,
    var imagenBase64: String = "",
    var id: String = "",
    var codigoBarras: String = ""
) : Serializable //me permite convertir un objeto en datos para poder enviarlo entre pantallas o almacenarlo.