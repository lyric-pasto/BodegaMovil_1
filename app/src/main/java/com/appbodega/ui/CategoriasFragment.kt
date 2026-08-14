package com.appbodega.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.appbodega.Adapter.CategoriaAdapter
import com.appbodega.app.InicioActivity
import com.appbodega.app.R
import com.appbodega.app.registro_ventas
import com.appbodega.provider.CategoriaProvider
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class CategoriasFragment : Fragment() {

    private lateinit var btnMenu: ImageButton
    private lateinit var btnCerrar: ImageButton
    private lateinit var recyclerCategorias: RecyclerView
    private lateinit var adapter: CategoriaAdapter
    private lateinit var etBuscar: TextInputEditText
    private lateinit var btnRegistrarProducto: MaterialButton
    private lateinit var btnRegistrarVenta: MaterialButton

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
        adapter = CategoriaAdapter(todasLasCategorias) { categoria ->
            abrirFragmentCategoria(categoria.nombre)
        }
        recyclerCategorias.adapter = adapter

        etBuscar.addTextChangedListener { texto ->
            val query = texto.toString().trim()
            val filtradas = if (query.isEmpty()) {
                todasLasCategorias
            } else {
                todasLasCategorias.filter {
                    it.nombre.contains(query, ignoreCase = true)
                }
            }
            adapter = CategoriaAdapter(filtradas) { categoria ->
                abrirFragmentCategoria(categoria.nombre)
            }
            recyclerCategorias.adapter = adapter
        }

        return view
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
