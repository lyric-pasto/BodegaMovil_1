package com.appbodega.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.appbodega.app.R
import com.appbodega.entity.Categoria

class CategoriaAdapter (

    private val lista: List<Categoria>,
    private val onClick: (Categoria) -> Unit )
    : RecyclerView.Adapter<CategoriaViewHolder>()
    {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoriaViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_categoria, parent, false)
            return CategoriaViewHolder(view)
        }

        override fun onBindViewHolder(holder: CategoriaViewHolder, position: Int) {
            val item = lista[position]

            holder.nombre.text = item.nombre
            holder.desc.text = item.descripcion
            holder.img.setImageResource(item.imagen)

            holder.itemView.setOnClickListener {
                onClick(item)
            }
        }

        override fun getItemCount() = lista.size
    }
