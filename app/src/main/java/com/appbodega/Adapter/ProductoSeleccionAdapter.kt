package com.appbodega.Adapter

import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.appbodega.app.R
import com.appbodega.entity.Producto
import java.util.Locale

class ProductoSeleccionAdapter(
    private var lista: List<Producto>,
    private val onProductoClick: (Producto) -> Unit
) : RecyclerView.Adapter<ProductoSeleccionAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivProducto: ImageView = view.findViewById(R.id.ivProductoSeleccion)
        val tvNombre: TextView = view.findViewById(R.id.tvNombreProductoSeleccion)
        val tvCategoria: TextView = view.findViewById(R.id.tvCategoriaProductoSeleccion)
        val tvStock: TextView = view.findViewById(R.id.tvStockProductoSeleccion)
        val tvPrecio: TextView = view.findViewById(R.id.tvPrecioProductoSeleccion)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_producto_seleccion, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = lista[position]

        holder.tvNombre.text = item.nombre
        holder.tvCategoria.text = if (item.categoria.isNotEmpty()) item.categoria else "General"
        holder.tvStock.text = "Stock: ${item.cantidad} unid."
        holder.tvPrecio.text = "S/ ${String.format(Locale.US, "%.2f", item.precioVenta)}"

        if (item.cantidad <= 3) {
            holder.tvStock.setTextColor(ContextCompat.getColor(holder.itemView.context, android.R.color.holo_red_dark))
        } else {
            holder.tvStock.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.verde_principal))
        }

        if (item.imagenBase64.isNotEmpty()) {
            try {
                val bytes = Base64.decode(item.imagenBase64, Base64.DEFAULT)
                val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                holder.ivProducto.setImageBitmap(bmp)
            } catch (e: Exception) {
                holder.ivProducto.setImageResource(R.drawable.icono_camara)
            }
        } else {
            holder.ivProducto.setImageResource(R.drawable.icono_camara)
        }

        holder.itemView.setOnClickListener {
            onProductoClick(item)
        }
    }

    override fun getItemCount(): Int = lista.size

    fun actualizarLista(nuevaLista: List<Producto>) {
        lista = nuevaLista
        notifyDataSetChanged()
    }
}
