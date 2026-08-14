package com.appbodega.provider

import com.appbodega.entity.Producto

object BebidasProvider {

    val lista = mutableListOf<Producto>(
        Producto(
            nombre = "Coca Cola",
            descripcion = "Gaseosa 500ml",
            cantidad = 25,
            categoria = "Bebidas",
            precioVenta = 3.50,
            precioCompra = 2.80,
            imagenBase64 = ""
        ),

        Producto(
            nombre = "Pepsi",
            descripcion = "Gaseosa 500ml",
            cantidad = 18,
            categoria = "Bebidas",
            precioVenta = 3.30,
            precioCompra = 2.70,
            imagenBase64 = ""
        )
    )
}