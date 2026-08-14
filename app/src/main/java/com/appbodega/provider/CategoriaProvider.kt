package com.appbodega.provider

import com.appbodega.app.R
import com.appbodega.entity.Categoria
import com.appbodega.entity.Producto

object CategoriaProvider {

    val listaCategorias =  mutableListOf<Categoria>(

        Categoria(
            "Abarrotes",
            "Arroz, azúcar, fideos, aceite...",
            R.drawable.grocery_cataloge
        ),

        Categoria(
            "Bebidas",
            "Gaseosas, jugos y agua",
            R.drawable.drink_cataloge
        ),

        Categoria(
            "Snacks",
            "Papas, galletas y chocolates",
            R.drawable.snack_cataloge
        ),

        Categoria(
            "Limpieza",
            "Detergentes y desinfectantes",
            R.drawable.clean_cataloge
        ),

        Categoria(
            "Alcohol",
            "Cervezas, vinos y bebidas alcohólicas",
            R.drawable.beer_cataloge
        )

    )
}