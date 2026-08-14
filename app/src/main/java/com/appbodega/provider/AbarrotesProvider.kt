package com.appbodega.provider

import com.appbodega.entity.Producto

object AbarrotesProvider {

        val lista = mutableListOf<Producto>(

        Producto(
            nombre = "Arroz Costeño",
            descripcion = "Arroz extra 1kg",
            cantidad = 30,
            categoria = "Abarrotes",
            precioVenta = 4.50,
            precioCompra = 3.80,
            imagenBase64 = ""
        ),

        Producto(
            nombre = "Azúcar Rubia",
            descripcion = "Azúcar 1kg",
            cantidad = 25,
            categoria = "Abarrotes",
            precioVenta = 3.80,
            precioCompra = 3.10,
            imagenBase64 = ""
        ),

        Producto(
            nombre = "Fideos Nicolini",
            descripcion = "Pasta 500g",
            cantidad = 40,
            categoria = "Abarrotes",
            precioVenta = 2.20,
            precioCompra = 1.70,
            imagenBase64 = ""
        ),

        Producto(
            nombre = "Aceite Primor",
            descripcion = "Aceite vegetal 1L",
            cantidad = 18,
            categoria = "Abarrotes",
            precioVenta = 9.50,
            precioCompra = 7.80,
            imagenBase64 = ""
        ),

        Producto(
            nombre = "Sal Yodada",
            descripcion = "Sal de mesa 1kg",
            cantidad = 35,
            categoria = "Abarrotes",
            precioVenta = 1.50,
            precioCompra = 1.00,
            imagenBase64 = ""
        )
    )
}