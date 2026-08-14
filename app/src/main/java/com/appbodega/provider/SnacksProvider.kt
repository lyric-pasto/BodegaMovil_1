package com.appbodega.provider

import com.appbodega.entity.Producto

object SnacksProvider {

    val lista = mutableListOf<Producto>(
        Producto(
            nombre = "Doritos",
            descripcion = "Snack de maíz sabor queso",
            cantidad = 20,
            categoria = "Snacks",
            precioVenta = 3.50,
            precioCompra = 2.50,
            imagenBase64 = ""
        ),

        Producto(
            nombre = "Cheetos",
            descripcion = "Snack crujiente",
            cantidad = 15,
            categoria = "Snacks",
            precioVenta = 3.20,
            precioCompra = 2.20,
            imagenBase64 = ""
        )
    )
}