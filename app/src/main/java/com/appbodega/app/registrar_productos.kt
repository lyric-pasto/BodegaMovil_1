/*
package com.appbodega.app

import android.Manifest
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.appbodega.entity.Producto
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import java.io.ByteArrayOutputStream

class registrar_productos : AppCompatActivity() {

    // Estas son las variables que identificamos en el diseño, para luego usarlos aqui
    private lateinit var imagen: ImageView
    private lateinit var etNombre: TextInputEditText
    private lateinit var etCantidad: TextInputEditText
    private lateinit var etPrecioCompra: TextInputEditText
    private lateinit var etPrecioVenta: TextInputEditText
    private lateinit var etDescripcion: TextInputEditText
    private lateinit var spinnerCategorias: Spinner
    private lateinit var btnRegistrar: MaterialButton

    private var fotoProducto: Bitmap? = null // Guarda la foto tomada temporalmente

    // Lanzador para abrir la cámara y recibir la foto de perfil del producto
    private val camara = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { foto ->
        if (foto != null) {
            fotoProducto = foto
            imagen.setImageBitmap(foto) // Muestra la foto en pantalla
        }
    }

    // Lanzador para pedir permiso de cámara al usuario
    private val permiso = registerForActivityResult(ActivityResultContracts.RequestPermission()) { concedido ->
        if (concedido) camara.launch(null)
        else Toast.makeText(this, "Permiso denegado", Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_registrar_productos)

        // Enlazar las variables con los IDs del archivo XML
        imagen = findViewById(R.id.imagen_producto)
        etNombre = findViewById(R.id.nombre_nuevo_producto)
        etCantidad = findViewById(R.id.cantidad_nuevo_producto)
        etPrecioCompra = findViewById(R.id.precio_compra_nuevo_producto)
        etPrecioVenta = findViewById(R.id.precio_venta_nuevo_producto)
        etDescripcion = findViewById(R.id.descripcion_nuevo_producto)
        spinnerCategorias = findViewById(R.id.spinner_categorias)
        btnRegistrar = findViewById(R.id.btnAcceder)

        // Clic en la foto: revisa permisos y abre la cámara
        imagen.setOnClickListener {
            val tienePermiso = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            if (tienePermiso == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                camara.launch(null)
            } else {
                permiso.launch(Manifest.permission.CAMERA)
            }
        }

        // Clic en el botón Registrar Producto
        btnRegistrar.setOnClickListener {
            GuardarDatos()
        }
    }

    // Con esta funcion se guardan los datos escritos de los inputs en el obejeto producto
    private fun GuardarDatos() {
        val txtNombre = etNombre.text.toString()
        val txtCantidad = etCantidad.text.toString()
        val txtCompra = etPrecioCompra.text.toString()
        val txtVenta = etPrecioVenta.text.toString()
        val txtDesc = etDescripcion.text.toString()
        val txtCat = spinnerCategorias.selectedItem?.toString() ?: ""

        // Validación: Si falta algún campo obligatorio, frena el registro
        if (txtNombre.isEmpty() || txtCantidad.isEmpty() || txtCompra.isEmpty() || txtVenta.isEmpty()) {
            Toast.makeText(this, "Completa los campos obligatorios", Toast.LENGTH_SHORT).show()
            return
        }

        // Convierte la foto (Bitmap) a un arreglo de bytes comprimido
        var fotoEnBytes: ByteArray? = null
        if (fotoProducto != null) {
            val stream = ByteArrayOutputStream()
            fotoProducto!!.compress(Bitmap.CompressFormat.JPEG, 70, stream) // 70% calidad para que no pese
            fotoEnBytes = stream.toByteArray()
        }

        // Con esta funcion se guardan los datos escritos de los inputs en el obejeto producto
        val nuevoProducto = Producto(
            nombre = txtNombre,
            descripcion = txtDesc,
            cantidad = txtCantidad.toInt(),
            categoria = txtCat,
            precioCompra = txtCompra.toDouble(),
            precioVenta = txtVenta.toDouble(),
            imagenBytes = fotoEnBytes
        )


        val resultado = Intent()//Aqui se prepara el envio con el Intent
        resultado.putExtra("NUEVO_PRODUCTO", nuevoProducto)//Se guarda el paquete con una nueva etiqueta

        setResult(RESULT_OK, resultado) // Avisa que el registro tuvo exito
        finish() // Cierra esta pantalla y regresa al catálogo
    }
}*/
