package com.appbodega.ui

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.appbodega.Adapter.ProductoAdapter
import com.appbodega.app.R
import com.appbodega.entity.Producto
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AbarrotesFragment : Fragment(R.layout.fragment_abarrotes) {

    private lateinit var recyclerProductos: RecyclerView
    private lateinit var adapter: ProductoAdapter
    private lateinit var btnBack: ImageButton
    private lateinit var etBuscar: TextInputEditText
    private val listaProductos = mutableListOf<Producto>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnBack = view.findViewById(R.id.btnBack)
        recyclerProductos = view.findViewById(R.id.rvProductos)
        etBuscar = view.findViewById(R.id.etBuscar)
        adapter = ProductoAdapter(
            listaProductos,
            onActualizar = { producto -> actualizarProducto(producto) },
            onEliminar = { producto -> eliminarProducto(producto.id) }
        )
        recyclerProductos.layoutManager = LinearLayoutManager(requireContext())
        recyclerProductos.adapter = adapter
        cargarProductos()

        etBuscar.addTextChangedListener { texto ->
            val query = texto.toString().trim()
            val filtrados = if (query.isEmpty()) {
                listaProductos
            } else {
                listaProductos.filter {
                    it.nombre.contains(query, ignoreCase = true) ||
                    it.codigoBarras.contains(query, ignoreCase = true) ||
                    it.id.contains(query, ignoreCase = true)
                }
            }
            adapter.actualizar(filtrados)
        }

        btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun cargarProductos() {
        FirebaseDatabase.getInstance()
            .getReference("productos")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    listaProductos.clear()
                    for (item in snapshot.children) {
                        val producto = item.getValue(Producto::class.java)
                        if (producto != null && producto.categoria.equals("Abarrotes", ignoreCase = true)) {
                            listaProductos.add(producto)
                        }
                    }
                    val query = etBuscar.text?.toString()?.trim().orEmpty()
                    if (query.isNotEmpty()) {
                        val filtrados = listaProductos.filter {
                            it.nombre.contains(query, ignoreCase = true) ||
                            it.codigoBarras.contains(query, ignoreCase = true) ||
                            it.id.contains(query, ignoreCase = true)
                        }
                        adapter.actualizar(filtrados)
                    } else {
                        adapter.actualizar(listaProductos)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(requireContext(), "Error al cargar: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun eliminarProducto(idProducto: String) {
        FirebaseDatabase.getInstance()
            .getReference("productos")
            .child(idProducto)
            .removeValue()
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Producto eliminado", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), e.message, Toast.LENGTH_LONG).show()
            }
    }

    private fun actualizarProducto(producto: Producto) {
        FirebaseDatabase.getInstance()
            .getReference("productos")
            .child(producto.id)
            .setValue(producto)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Producto actualizado", Toast.LENGTH_SHORT).show()
            }
    }
}
