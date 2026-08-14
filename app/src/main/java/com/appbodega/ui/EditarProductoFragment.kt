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
import android.widget.TextView
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
import com.google.firebase.database.FirebaseDatabase
import java.io.ByteArrayOutputStream

class EditarProductoFragment : Fragment(R.layout.fragment_registrar_productos) {

    private lateinit var imagen: ImageView
    private lateinit var etCodigoBarras: TextInputEditText
    private lateinit var btnEscanearCodigo: MaterialButton
    private lateinit var etNombre: TextInputEditText
    private lateinit var etDescripcion: TextInputEditText
    private lateinit var etCantidad: TextInputEditText
    private lateinit var etPrecioCompra: TextInputEditText
    private lateinit var etPrecioVenta: TextInputEditText
    private lateinit var spinnerCategorias: Spinner
    private lateinit var btnGuardar: MaterialButton
    private lateinit var btnCancelar: MaterialButton

    private var productoActual: Producto? = null
    private var fotoNueva: Bitmap? = null

    private val camara =
        registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { foto ->
            if (foto != null) {
                fotoNueva = foto
                imagen.setImageBitmap(foto)
            }
        }

    private val scannerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                val codigo = result.data?.getStringExtra("codigo_escaneado")
                if (!codigo.isNullOrEmpty()) {
                    etCodigoBarras.setText(codigo)
                    Toast.makeText(requireContext(), "Código de barras asignado: $codigo", Toast.LENGTH_SHORT).show()
                }
            }
        }

    private val permiso =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) camara.launch(null)
            else Toast.makeText(requireContext(), "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        @Suppress("DEPRECATION")
        productoActual = arguments?.getSerializable("producto") as? Producto

        // Cambiar título a "Editar Producto"
        view.findViewById<TextView>(R.id.tvTituloFormulario)?.text = "Editar Producto"

        imagen            = view.findViewById(R.id.imagen_producto)
        etCodigoBarras    = view.findViewById(R.id.codigo_barras_nuevo_producto)
        btnEscanearCodigo = view.findViewById(R.id.btnEscanearCodigo)
        etNombre          = view.findViewById(R.id.nombre_nuevo_producto)
        etDescripcion     = view.findViewById(R.id.descripcion_nuevo_producto)
        etCantidad        = view.findViewById(R.id.cantidad_nuevo_producto)
        etPrecioCompra    = view.findViewById(R.id.precio_compra_nuevo_producto)
        etPrecioVenta     = view.findViewById(R.id.precio_venta_nuevo_producto)
        spinnerCategorias = view.findViewById(R.id.spinner_categorias)
        btnGuardar        = view.findViewById(R.id.btnAcceder)
        btnCancelar       = view.findViewById(R.id.btnCancelar)

        // Cambiar texto del botón
        btnGuardar.text = "Guardar Cambios"

        // Precargar datos del producto
        productoActual?.let { p ->
            etCodigoBarras.setText(p.codigoBarras)
            etNombre.setText(p.nombre)
            etDescripcion.setText(p.descripcion)
            etCantidad.setText(p.cantidad.toString())
            etPrecioCompra.setText(p.precioCompra.toString())
            etPrecioVenta.setText(p.precioVenta.toString())

            // Cargar categoría en el spinner
            val count = spinnerCategorias.adapter?.count ?: 0
            for (i in 0 until count) {
                if (spinnerCategorias.getItemAtPosition(i)
                        .toString().equals(p.categoria, ignoreCase = true)) {
                    spinnerCategorias.setSelection(i)
                    break
                }
            }

            // Cargar imagen existente
            if (p.imagenBase64.isNotEmpty()) {
                try {
                    val bytes = Base64.decode(p.imagenBase64, Base64.DEFAULT)
                    val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    imagen.setImageBitmap(bmp)
                } catch (e: Exception) { }
            }
        }

        // Escanear código nuevo
        btnEscanearCodigo.setOnClickListener {
            val intent = Intent(requireContext(), RegistroProductoActivity::class.java)
            scannerLauncher.launch(intent)
        }

        // Tocar imagen abre cámara
        imagen.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    requireContext(), Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                camara.launch(null)
            } else {
                permiso.launch(Manifest.permission.CAMERA)
            }
        }

        btnGuardar.setOnClickListener { guardarCambios() }
        btnCancelar.setOnClickListener { parentFragmentManager.popBackStack() }
    }

    private fun guardarCambios() {
        val id = productoActual?.id ?: return

        val nombre       = etNombre.text.toString().trim()
        val descripcion  = etDescripcion.text.toString().trim()
        val codigoBarras = etCodigoBarras.text.toString().trim()
        val cantidad     = etCantidad.text.toString().toIntOrNull() ?: 0
        val precioCompra = etPrecioCompra.text.toString().toDoubleOrNull() ?: 0.0
        val precioVenta  = etPrecioVenta.text.toString().toDoubleOrNull() ?: 0.0
        val categoria    = spinnerCategorias.selectedItem?.toString() ?: ""

        if (nombre.isEmpty()) {
            etNombre.error = "El nombre es obligatorio"
            etNombre.requestFocus()
            return
        }

        if (cantidad <= 0) {
            etCantidad.error = "La cantidad debe ser mayor a 0"
            etCantidad.requestFocus()
            return
        }

        // Si tomó foto nueva la convierte a Base64, sino mantiene la original
        val imagenBase64 = if (fotoNueva != null) {
            val stream = ByteArrayOutputStream()
            fotoNueva!!.compress(Bitmap.CompressFormat.JPEG, 70, stream)
            Base64.encodeToString(stream.toByteArray(), Base64.DEFAULT)
        } else {
            productoActual?.imagenBase64 ?: ""
        }

        val productoActualizado = productoActual!!.copy(
            nombre       = nombre,
            descripcion  = descripcion,
            cantidad     = cantidad,
            precioCompra = precioCompra,
            precioVenta  = precioVenta,
            categoria    = categoria,
            codigoBarras = codigoBarras,
            imagenBase64 = imagenBase64
        )

        btnGuardar.isEnabled = false

        FirebaseDatabase.getInstance()
            .getReference("productos")
            .child(id)
            .setValue(productoActualizado)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Producto actualizado ✓", Toast.LENGTH_SHORT).show()
                parentFragmentManager.popBackStack()
            }
            .addOnFailureListener { e ->
                btnGuardar.isEnabled = true
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    companion object {
        fun newInstance(producto: Producto): EditarProductoFragment {
            val fragment = EditarProductoFragment()
            val args = Bundle()
            args.putSerializable("producto", producto)
            fragment.arguments = args
            return fragment
        }
    }
}
