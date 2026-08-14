package com.appbodega.Adapter

import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.appbodega.app.R

class ProductoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val imgProducto: ImageView = view.findViewById(R.id.ivProducto)
    val tvNombre: TextView = view.findViewById(R.id.tvNombreProducto)
    val tvTipoProducto: TextView? = view.findViewById(R.id.tvTipoProducto)
    val tvDescripcion: TextView = view.findViewById(R.id.tvDescripcionProducto)
    val tvCantidad: TextView = view.findViewById(R.id.tvStock)
    val tvPrecio: TextView = view.findViewById(R.id.tvPrecio)
    val btnEliminar: ImageButton = itemView.findViewById(R.id.btnEliminar)
    val btnActualizar: ImageButton = itemView.findViewById(R.id.btnActualizar)
}