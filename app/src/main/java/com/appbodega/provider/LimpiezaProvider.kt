package com.appbodega.provider


import com.appbodega.entity.Producto

object LimpiezaProvider {

    val lista = mutableListOf<Producto>(

        Producto(
            nombre = "Detergente Opal",
            descripcion = "Detergente para ropa 1kg",
            cantidad = 20,
            categoria = "Limpieza",
            precioVenta = 8.50,
            precioCompra = 6.80,
            imagenBase64 = ""
        ),

        Producto(
            nombre = "Cloro Ayudin",
            descripcion = "Desinfectante multiuso",
            cantidad = 15,
            categoria = "Limpieza",
            precioVenta = 5.20,
            precioCompra = 3.90,
            imagenBase64 = ""
        ),

        Producto(
            nombre = "Escoba",
            descripcion = "Escoba de plástico resistente",
            cantidad = 12,
            categoria = "Limpieza",
            precioVenta = 7.00,
            precioCompra = 5.00,
            imagenBase64 = ""
        ),

        Producto(
            nombre = "Trapeador",
            descripcion = "Mopa absorbente",
            cantidad = 10,
            categoria = "Limpieza",
            precioVenta = 12.00,
            precioCompra = 9.00,
            imagenBase64 = ""
        ),

        Producto(
            nombre = "Lavavajillas",
            descripcion = "Jabón líquido para platos",
            cantidad = 18,
            categoria = "Limpieza",
            precioVenta = 6.50,
            precioCompra = 4.80,
            imagenBase64 = ""
        )
    )
}