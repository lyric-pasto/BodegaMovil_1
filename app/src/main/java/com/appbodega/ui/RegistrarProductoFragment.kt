package com.appbodega.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.appbodega.app.R
import com.appbodega.app.RegistroProductoActivity
import com.appbodega.entity.Producto
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.io.ByteArrayOutputStream
import java.util.UUID

class RegistrarProductoFragment :
    Fragment(R.layout.fragment_registrar_productos) {

    private lateinit var imagen: ImageView
    private lateinit var etCodigoBarras: TextInputEditText
    private lateinit var btnEscanearCodigo: MaterialButton
    private lateinit var etNombre: TextInputEditText
    private lateinit var etCantidad: TextInputEditText
    private lateinit var etPrecioCompra: TextInputEditText
    private lateinit var etPrecioVenta: TextInputEditText
    private lateinit var etDescripcion: TextInputEditText
    private lateinit var spinnerCategorias: Spinner
    private lateinit var btnRegistrar: MaterialButton
    private lateinit var btnCancelar: MaterialButton

    private var fotoProducto: Bitmap? = null
    private var productoExistenteId: String? = null

    private val camara =
        registerForActivityResult(
            ActivityResultContracts.TakePicturePreview()
        ) { foto ->
            if (foto != null) {
                fotoProducto = foto
                imagen.setImageBitmap(foto)
            }
        }

    private val scannerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                val codigo = result.data?.getStringExtra("codigo_escaneado")
                if (!codigo.isNullOrEmpty()) {
                    etCodigoBarras.setText(codigo)
                    buscarProductoPorCodigo(codigo)
                }
            }
        }

    private val permiso =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            if (granted) {
                camara.launch(null)
            } else {
                Toast.makeText(
                    requireContext(),
                    "Permiso denegado",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        imagen = view.findViewById(R.id.imagen_producto)
        etCodigoBarras = view.findViewById(R.id.codigo_barras_nuevo_producto)
        btnEscanearCodigo = view.findViewById(R.id.btnEscanearCodigo)
        etNombre = view.findViewById(R.id.nombre_nuevo_producto)
        etCantidad = view.findViewById(R.id.cantidad_nuevo_producto)
        etPrecioCompra = view.findViewById(R.id.precio_compra_nuevo_producto)
        etPrecioVenta = view.findViewById(R.id.precio_venta_nuevo_producto)
        etDescripcion = view.findViewById(R.id.descripcion_nuevo_producto)
        spinnerCategorias = view.findViewById(R.id.spinner_categorias)

        btnRegistrar = view.findViewById(R.id.btnAcceder)
        btnCancelar = view.findViewById(R.id.btnCancelar)

        btnEscanearCodigo.setOnClickListener {
            val intent = Intent(requireContext(), RegistroProductoActivity::class.java)
            scannerLauncher.launch(intent)
        }

        imagen.setOnClickListener {
            val permisoCamara =
                ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.CAMERA
                )

            if (permisoCamara == PackageManager.PERMISSION_GRANTED) {
                camara.launch(null)
            } else {
                permiso.launch(Manifest.permission.CAMERA)
            }
        }

        btnRegistrar.setOnClickListener {
            guardarDatos()
        }

        btnCancelar.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun buscarProductoPorCodigo(codigo: String) {
        val db = FirebaseDatabase.getInstance().getReference("productos")
        db.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (child in snapshot.children) {
                    val p = child.getValue(Producto::class.java)
                    if (p != null && (p.codigoBarras == codigo || p.id == codigo)) {
                        productoExistenteId = p.id
                        etNombre.setText(p.nombre)
                        etCantidad.setText(p.cantidad.toString())
                        etPrecioCompra.setText(p.precioCompra.toString())
                        etPrecioVenta.setText(p.precioVenta.toString())
                        etDescripcion.setText(p.descripcion)
                        seleccionarCategoria(p.categoria)

                        if (p.imagenBase64.isNotEmpty()) {
                            try {
                                val bytes = Base64.decode(p.imagenBase64, Base64.DEFAULT)
                                val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                                fotoProducto = bmp
                                imagen.setImageBitmap(bmp)
                            } catch (e: Exception) {
                                // ignore
                            }
                        }

                        Toast.makeText(
                            requireContext(),
                            "Producto encontrado. Puedes editar sus datos o stock.",
                            Toast.LENGTH_SHORT
                        ).show()
                        return
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun seleccionarCategoria(categoria: String) {
        val count = spinnerCategorias.adapter?.count ?: 0
        for (i in 0 until count) {
            if (spinnerCategorias.getItemAtPosition(i).toString().equals(categoria, ignoreCase = true)) {
                spinnerCategorias.setSelection(i)
                break
            }
        }
    }

    private fun guardarDatos() {
        val txtCodigo = etCodigoBarras.text.toString().trim()
        val txtNombre = etNombre.text.toString().trim()
        val txtCantidad = etCantidad.text.toString().trim()
        val txtCompra = etPrecioCompra.text.toString().trim()
        val txtVenta = etPrecioVenta.text.toString().trim()
        val txtDesc = etDescripcion.text.toString().trim()
        val txtCat = spinnerCategorias.selectedItem?.toString() ?: ""

        if (
            txtNombre.isEmpty() ||
            txtCantidad.isEmpty() ||
            txtCompra.isEmpty() ||
            txtVenta.isEmpty()
        ) {
            Toast.makeText(
                requireContext(),
                "Completa todos los campos obligatorios",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val cantidad = txtCantidad.toIntOrNull()
        val compra = txtCompra.toDoubleOrNull()
        val venta = txtVenta.toDoubleOrNull()

        if (cantidad == null || compra == null || venta == null) {
            Toast.makeText(
                requireContext(),
                "Ingrese valores numéricos válidos",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        var fotoBytes: ByteArray? = null
        fotoProducto?.let {
            val stream = ByteArrayOutputStream()
            it.compress(Bitmap.CompressFormat.JPEG, 70, stream)
            fotoBytes = stream.toByteArray()
        }

        var imagenBase64 = ""
        fotoBytes?.let {
            imagenBase64 = Base64.encodeToString(it, Base64.DEFAULT)
        }

        val idProducto = productoExistenteId ?: UUID.randomUUID().toString()

        val nuevoProducto = Producto(
            id = idProducto,
            nombre = txtNombre,
            descripcion = txtDesc,
            cantidad = cantidad,
            categoria = txtCat,
            precioCompra = compra,
            precioVenta = venta,
            imagenBase64 = imagenBase64,
            codigoBarras = txtCodigo
        )

        val db = FirebaseDatabase.getInstance().reference
        db.child("productos")
            .child(idProducto)
            .setValue(nuevoProducto)
            .addOnSuccessListener {
                Toast.makeText(
                    requireContext(),
                    "Producto registrado correctamente",
                    Toast.LENGTH_SHORT
                ).show()

                limpiarCampos()
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    requireContext(),
                    e.message,
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun limpiarCampos() {
        productoExistenteId = null
        etCodigoBarras.setText("")
        etNombre.setText("")
        etCantidad.setText("")
        etPrecioCompra.setText("")
        etPrecioVenta.setText("")
        etDescripcion.setText("")

        spinnerCategorias.setSelection(0)
        fotoProducto = null
        imagen.setImageResource(R.drawable.icono_camara)
    }
}