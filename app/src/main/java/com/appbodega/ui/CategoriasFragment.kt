package com.appbodega.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.appbodega.Adapter.CategoriaAdapter
import com.appbodega.Adapter.ProductoAdapter
import com.appbodega.app.InicioActivity
import com.appbodega.app.R
import com.appbodega.app.registro_ventas
import com.appbodega.entity.Producto
import com.appbodega.provider.CategoriaProvider
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class CategoriasFragment : Fragment() {

    private lateinit var btnMenu: ImageButton
    private lateinit var btnCerrar: ImageButton
    private lateinit var recyclerCategorias: RecyclerView
    private lateinit var adapterCategoria: CategoriaAdapter
    private lateinit var adapterProducto: ProductoAdapter
    private lateinit var etBuscar: TextInputEditText
    private lateinit var btnRegistrarProducto: MaterialButton
    private lateinit var btnRegistrarVenta: MaterialButton

    private val todosLosProductos = mutableListOf<Producto>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_categoria_bodega, container, false)

        btnMenu = view.findViewById(R.id.btnMenu)
        btnCerrar = view.findViewById(R.id.btnCerrar)
        etBuscar = view.findViewById(R.id.etBuscar)
        btnRegistrarProducto = view.findViewById(R.id.btnRegistrarProducto)
        btnRegistrarVenta = view.findViewById(R.id.btnRegistrarVenta)
        recyclerCategorias = view.findViewById(R.id.rvCategorias)

        btnRegistrarProducto.setOnClickListener {
            val fragment = RegistrarProductoFragment()
            parentFragmentManager.beginTransaction()
                .replace(R.id.flayContenedor, fragment)
                .addToBackStack(null)
                .commit()
        }

        btnRegistrarVenta.setOnClickListener {
            val intent = Intent(requireContext(), registro_ventas::class.java)
            startActivity(intent)
        }

        btnMenu.setOnClickListener {
            (activity as? InicioActivity)?.abrirMenu()
        }

        btnCerrar.setOnClickListener {
            requireActivity().finish()
        }

        recyclerCategorias.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        val todasLasCategorias = CategoriaProvider.listaCategorias
        adapterCategoria = CategoriaAdapter(todasLasCategorias) { categoria ->
            abrirFragmentCategoria(categoria.nombre)
        }
        recyclerCategorias.adapter = adapterCategoria

        cargarProductosParaBusquedaGlobal()

        etBuscar.addTextChangedListener { texto ->
            val query = texto.toString().trim()
            if (query.isEmpty()) {
                adapterCategoria = CategoriaAdapter(todasLasCategorias) { categoria ->
                    abrirFragmentCategoria(categoria.nombre)
                }
                recyclerCategorias.adapter = adapterCategoria
            } else {
                val categoriasCoincidentes = todasLasCategorias.filter {
                    it.nombre.contains(query, ignoreCase = true) ||
                    it.descripcion.contains(query, ignoreCase = true)
                }

                val productosCoincidentes = todosLosProductos.filter {
                    it.nombre.contains(query, ignoreCase = true) ||
                    it.descripcion.contains(query, ignoreCase = true) ||
                    it.codigoBarras.contains(query, ignoreCase = true) ||
                    it.categoria.contains(query, ignoreCase = true)
                }

                if (categoriasCoincidentes.isNotEmpty() && productosCoincidentes.isEmpty()) {
                    adapterCategoria = CategoriaAdapter(categoriasCoincidentes) { categoria ->
                        abrirFragmentCategoria(categoria.nombre)
                    }
                    recyclerCategorias.adapter = adapterCategoria
                } else if (productosCoincidentes.isNotEmpty()) {
                    adapterProducto = ProductoAdapter(
                        productosCoincidentes,
                        onActualizar = { producto ->
                            actualizarProducto(producto)
                        },
                        onEliminar = { producto ->
                            eliminarProducto(producto.id)
                        }
                    )
                    recyclerCategorias.adapter = adapterProducto
                } else {
                    adapterCategoria = CategoriaAdapter(emptyList()) {}
                    recyclerCategorias.adapter = adapterCategoria
                }
            }
        }

        return view
    }

    private fun cargarProductosParaBusquedaGlobal() {
        FirebaseDatabase.getInstance().getReference("productos")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    todosLosProductos.clear()
                    for (child in snapshot.children) {
                        val p = child.getValue(Producto::class.java)
                        if (p != null) {
                            todosLosProductos.add(p)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
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
    }

    private fun actualizarProducto(producto: Producto) {
        val fragment = EditarProductoFragment.newInstance(producto)
        parentFragmentManager.beginTransaction()
            .replace(R.id.flayContenedor, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun abrirFragmentCategoria(nombre: String) {
        val fragment: Fragment = when (nombre) {
            "Abarrotes" -> AbarrotesFragment()
            "Alcohol" -> AlcoholFragment()
            "Bebidas" -> BebidasFragment()
            "Limpieza" -> LimpiezaFragment()
            "Snacks" -> SnacksFragment()
            else -> CategoriasFragment()
        }

        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.flayContenedor, fragment)
            .addToBackStack(null)
            .commit()
    }
}
