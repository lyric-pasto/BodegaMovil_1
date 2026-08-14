package com.appbodega.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.appbodega.app.R
import com.appbodega.entity.venta
import java.util.Locale

class VentaAdapter(
    private var lista: List<venta>,
    private val onItemClick: (venta) -> Unit
) : RecyclerView.Adapter<VentaAdapter.VentaViewHolder>() {

    fun updateList(nuevaLista: List<venta>) {
        lista = nuevaLista
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VentaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_venta, parent, false)
        return VentaViewHolder(view)
    }

    override fun onBindViewHolder(holder: VentaViewHolder, position: Int) {
        holder.bind(lista[position])
    }

    override fun getItemCount(): Int = lista.size

    inner class VentaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val txtCodigo: TextView = itemView.findViewById(R.id.txtCodigo)
        private val txtFecha: TextView = itemView.findViewById(R.id.txtFecha)
        private val txtCantidad: TextView = itemView.findViewById(R.id.txtCantidad)
        private val txtMetodo: TextView = itemView.findViewById(R.id.txtMetodo)
        private val txtTotal: TextView = itemView.findViewById(R.id.txtTotal)
        private val btnVer: ImageButton = itemView.findViewById(R.id.btnVer)

        fun bind(item: venta) {
            txtCodigo.text = if (item.codigo.length > 8) item.codigo.takeLast(7) else item.codigo
            txtFecha.text = item.fecha
            txtCantidad.text = item.cantidad.toString()
            txtMetodo.text = item.metodo
            txtTotal.text = "S/ ${String.format(Locale.US, "%.2f", item.total)}"

            val clickListener = View.OnClickListener { onItemClick(item) }
            itemView.setOnClickListener(clickListener)
            btnVer.setOnClickListener(clickListener)
        }
    }
}
