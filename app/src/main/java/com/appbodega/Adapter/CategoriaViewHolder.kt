package com.appbodega.Adapter

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.appbodega.app.R

class CategoriaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val img = view.findViewById<ImageView>(R.id.imgCategoria)
    val nombre = view.findViewById<TextView>(R.id.tvNombreCategoria)
    val desc = view.findViewById<TextView>(R.id.tvDescripcion)
}

