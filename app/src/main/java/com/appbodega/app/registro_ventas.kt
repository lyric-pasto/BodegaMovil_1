package com.appbodega.app

import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Base64
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.appbodega.entity.Producto
import com.appbodega.entity.venta
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

class registro_ventas : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var rgTipoCliente: RadioGroup
    private lateinit var rbClienteNormal: RadioButton
    private lateinit var rbClienteRuc: RadioButton
    private lateinit var tilDocumento: TextInputLayout
    private lateinit var txtClienteDocumento: TextInputEditText
    private lateinit var tilNombre: TextInputLayout
    private lateinit var txtClienteNombre: TextInputEditText
    private lateinit var txtFechaVenta: TextView
    private lateinit var spinnerMetodoPago: Spinner

    private lateinit var spinnerProductos: Spinner
    private lateinit var btnEscanearVenta: MaterialButton
    private lateinit var btnMenosCantidad: ImageButton
    private lateinit var btnMasCantidad: ImageButton
    private lateinit var txtCantidad: EditText
    private lateinit var txtPrecioUnitario: TextView

    private lateinit var imgProductoPreview: ImageView
    private lateinit var tvNombreProductoSeleccionado: TextView
    private lateinit var tvCategoriaProductoSeleccionado: TextView
    private lateinit var tvStockProductoSeleccionado: TextView
    private lateinit var txtSubtotalCalculado: TextView
    private lateinit var txtIgvCalculado: TextView
    private lateinit var txtTotalCalculado: TextView
    private lateinit var btnRegistrarVenta: MaterialButton
    private lateinit var btnHistorialVentas: MaterialButton

    private val listaProductos = mutableListOf<Producto>()
    private var productoSeleccionado: Producto? = null
    private var fechaSeleccionada = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())

    private val scannerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val codigoEscaneado = result.data?.getStringExtra("codigo_escaneado")
                if (!codigoEscaneado.isNullOrEmpty()) {
                    seleccionarProductoPorCodigo(codigoEscaneado)
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_registro_ventas)

        initViews()
        setupListeners()
        cargarMetodosPago()
        cargarProductosDesdeFirebase()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun initViews() {
        toolbar = findViewById(R.id.toolbarVentas)
        rgTipoCliente = findViewById(R.id.rgTipoCliente)
        rbClienteNormal = findViewById(R.id.rbClienteNormal)
        rbClienteRuc = findViewById(R.id.rbClienteRuc)
        tilDocumento = findViewById(R.id.tilDocumento)
        txtClienteDocumento = findViewById(R.id.txtClienteDocumento)
        tilNombre = findViewById(R.id.tilNombre)
        txtClienteNombre = findViewById(R.id.txtClienteNombre)
        txtFechaVenta = findViewById(R.id.txtFechaVenta)
        spinnerMetodoPago = findViewById(R.id.spinnerMetodoPago)

        spinnerProductos = findViewById(R.id.spinnerProductos)
        btnEscanearVenta = findViewById(R.id.btnEscanearVenta)
        btnMenosCantidad = findViewById(R.id.btnMenosCantidad)
        btnMasCantidad = findViewById(R.id.btnMasCantidad)
        txtCantidad = findViewById(R.id.txtCantidad)
        txtPrecioUnitario = findViewById(R.id.txtPrecioUnitario)

        imgProductoPreview = findViewById(R.id.imgProductoPreview)
        tvNombreProductoSeleccionado = findViewById(R.id.tvNombreProductoSeleccionado)
        tvCategoriaProductoSeleccionado = findViewById(R.id.tvCategoriaProductoSeleccionado)
        tvStockProductoSeleccionado = findViewById(R.id.tvStockProductoSeleccionado)
        txtSubtotalCalculado = findViewById(R.id.txtSubtotalCalculado)
        txtIgvCalculado = findViewById(R.id.txtIgvCalculado)
        txtTotalCalculado = findViewById(R.id.txtTotalCalculado)
        btnRegistrarVenta = findViewById(R.id.btnRegistrarVenta)
        btnHistorialVentas = findViewById(R.id.btnHistorialVentas)

        txtFechaVenta.text = fechaSeleccionada
    }

    private fun setupListeners() {
        toolbar.setNavigationOnClickListener { finish() }

        rgTipoCliente.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == R.id.rbClienteRuc) {
                tilDocumento.hint = "RUC (11 dígitos)"
                txtClienteDocumento.hint = "Ingrese RUC de la empresa"
                tilNombre.hint = "Razón Social de la Empresa"
            } else {
                tilDocumento.hint = "DNI del Cliente (8 dígitos)"
                txtClienteDocumento.hint = "Ingrese DNI del cliente"
                tilNombre.hint = "Nombre del Cliente"
            }
        }

        txtFechaVenta.setOnClickListener {
            mostrarSelectorFecha()
        }

        btnEscanearVenta.setOnClickListener {
            val intent = Intent(this, RegistroProductoActivity::class.java)
            scannerLauncher.launch(intent)
        }

        btnMenosCantidad.setOnClickListener {
            val actual = txtCantidad.text.toString().toIntOrNull() ?: 1
            if (actual > 1) {
                txtCantidad.setText((actual - 1).toString())
            }
        }

        btnMasCantidad.setOnClickListener {
            val actual = txtCantidad.text.toString().toIntOrNull() ?: 0
            val stock = productoSeleccionado?.cantidad ?: 999
            if (actual < stock) {
                txtCantidad.setText((actual + 1).toString())
            } else {
                Toast.makeText(this, "Stock máximo alcanzado ($stock unid.)", Toast.LENGTH_SHORT).show()
            }
        }

        txtCantidad.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                calcularTotal()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        btnRegistrarVenta.setOnClickListener {
            validarYRegistrarVenta()
        }

        btnHistorialVentas.setOnClickListener {
            startActivity(Intent(this, historial_ventas::class.java))
        }
    }

    private fun cargarMetodosPago() {
        val metodos = listOf("Efectivo", "Yape")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, metodos)
        spinnerMetodoPago.adapter = adapter
    }

    private fun cargarProductosDesdeFirebase() {
        val db = FirebaseDatabase.getInstance().getReference("productos")
        db.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listaProductos.clear()
                val nombres = mutableListOf("Seleccione un producto...")

                for (child in snapshot.children) {
                    val p = child.getValue(Producto::class.java)
                    if (p != null) {
                        listaProductos.add(p)
                        val barcodeInfo = if (p.codigoBarras.isNotEmpty()) "[${p.codigoBarras}] " else ""
                        nombres.add("$barcodeInfo${p.nombre} (Stock: ${p.cantidad}) - S/ ${String.format(Locale.US, "%.2f", p.precioVenta)}")
                    }
                }

                val adapter = ArrayAdapter(this@registro_ventas, android.R.layout.simple_spinner_dropdown_item, nombres)
                spinnerProductos.adapter = adapter

                spinnerProductos.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                        if (position > 0 && position - 1 < listaProductos.size) {
                            actualizarProductoSeleccionado(listaProductos[position - 1])
                        } else {
                            actualizarProductoSeleccionado(null)
                        }
                    }

                    override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {
                        actualizarProductoSeleccionado(null)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@registro_ventas, "Error al cargar productos: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun seleccionarProductoPorCodigo(codigo: String) {
        val index = listaProductos.indexOfFirst { it.codigoBarras == codigo || it.id == codigo }
        if (index != -1) {
            spinnerProductos.setSelection(index + 1)
            actualizarProductoSeleccionado(listaProductos[index])
            Toast.makeText(this, "Producto encontrado: ${listaProductos[index].nombre}", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "No se encontró ningún producto con código: $codigo", Toast.LENGTH_LONG).show()
        }
    }

    private fun actualizarProductoSeleccionado(prod: Producto?) {
        productoSeleccionado = prod
        if (prod != null) {
            tvNombreProductoSeleccionado.text = prod.nombre
            tvCategoriaProductoSeleccionado.text = "Categoría: ${prod.categoria}"
            tvStockProductoSeleccionado.text = "Stock disponible: ${prod.cantidad} unid."
            txtPrecioUnitario.text = "S/ ${String.format(Locale.US, "%.2f", prod.precioVenta)}"

            if (prod.imagenBase64.isNotEmpty()) {
                try {
                    val bytes = Base64.decode(prod.imagenBase64, Base64.DEFAULT)
                    val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    imgProductoPreview.setImageBitmap(bmp)
                } catch (e: Exception) {
                    imgProductoPreview.setImageResource(R.drawable.icono_camara)
                }
            } else {
                imgProductoPreview.setImageResource(R.drawable.icono_camara)
            }
        } else {
            tvNombreProductoSeleccionado.text = "Selecciona o escanea un producto"
            tvCategoriaProductoSeleccionado.text = "Categoría: -"
            tvStockProductoSeleccionado.text = "Stock disponible: -"
            txtPrecioUnitario.text = "S/ 0.00"
            imgProductoPreview.setImageResource(R.drawable.icono_camara)
        }
        calcularTotal()
    }

    private fun calcularTotal() {
        val cant = txtCantidad.text.toString().toIntOrNull() ?: 0
        val precio = productoSeleccionado?.precioVenta ?: 0.0
        val total = cant * precio
        val subtotal = if (total > 0.0) total / 1.18 else 0.0
        val igv = if (total > 0.0) total - subtotal else 0.0

        txtSubtotalCalculado.text = "S/ ${String.format(Locale.US, "%.2f", subtotal)}"
        txtIgvCalculado.text = "S/ ${String.format(Locale.US, "%.2f", igv)}"
        txtTotalCalculado.text = "S/ ${String.format(Locale.US, "%.2f", total)}"
    }

    private fun mostrarSelectorFecha() {
        val cal = Calendar.getInstance()
        val dpd = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                cal.set(Calendar.YEAR, year)
                cal.set(Calendar.MONTH, month)
                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                fechaSeleccionada = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(cal.time)
                txtFechaVenta.text = fechaSeleccionada
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        )
        dpd.show()
    }

    private fun validarYRegistrarVenta() {
        val prod = productoSeleccionado
        if (prod == null) {
            Toast.makeText(this, "Debe seleccionar o escanear un producto", Toast.LENGTH_SHORT).show()
            return
        }

        val cantidad = txtCantidad.text.toString().toIntOrNull() ?: 0
        if (cantidad <= 0) {
            Toast.makeText(this, "Ingrese una cantidad válida mayor a 0", Toast.LENGTH_SHORT).show()
            return
        }

        if (cantidad > prod.cantidad) {
            Toast.makeText(this, "Stock insuficiente. Solo hay ${prod.cantidad} unidades disponibles.", Toast.LENGTH_LONG).show()
            return
        }

        val docCliente = txtClienteDocumento.text.toString().trim()
        val nomCliente = txtClienteNombre.text.toString().trim()
        val tipoCliente = if (rbClienteRuc.isChecked) "RUC" else "Normal"
        val metodoPago = spinnerMetodoPago.selectedItem?.toString() ?: "Efectivo"
        val total = cantidad * prod.precioVenta

        val idVenta = UUID.randomUUID().toString()
        val codigoVenta = "V-" + SimpleDateFormat("yyMMdd-HHmmss", Locale.getDefault()).format(Date())

        val nuevaVenta = venta(
            id = idVenta,
            codigo = codigoVenta,
            fecha = fechaSeleccionada,
            cantidad = cantidad,
            metodo = metodoPago,
            categoria = prod.categoria,
            total = total,
            productoId = prod.id,
            nombreProducto = prod.nombre,
            tipoCliente = tipoCliente,
            documentoCliente = docCliente,
            nombreCliente = nomCliente.ifEmpty { if (tipoCliente == "RUC") "Cliente RUC" else "Cliente General" },
            precioUnitario = prod.precioVenta
        )

        val db = FirebaseDatabase.getInstance().reference
        btnRegistrarVenta.isEnabled = false

        // 1. Guardar Venta en /ventas
        db.child("ventas").child(idVenta).setValue(nuevaVenta)
            .addOnSuccessListener {
                // 2. Descontar Stock del Producto en /productos/{id}/cantidad
                val nuevoStock = prod.cantidad - cantidad
                db.child("productos").child(prod.id).child("cantidad").setValue(nuevoStock)

                AlertDialog.Builder(this)
                    .setTitle("¡Venta Registrada!")
                    .setMessage(
                        "Código: $codigoVenta\n" +
                        "Producto: ${prod.nombre}\n" +
                        "Cantidad: $cantidad unid.\n" +
                        "Total: S/ ${String.format(Locale.US, "%.2f", total)}\n" +
                        "Método: $metodoPago\n" +
                        "Nuevo Stock: $nuevoStock unid."
                    )
                    .setPositiveButton("Aceptar") { _, _ ->
                        limpiarFormulario()
                    }
                    .setNeutralButton("Ver Historial") { _, _ ->
                        limpiarFormulario()
                        startActivity(Intent(this, historial_ventas::class.java))
                    }
                    .show()

                btnRegistrarVenta.isEnabled = true
            }
            .addOnFailureListener { e ->
                btnRegistrarVenta.isEnabled = true
                Toast.makeText(this, "Error al registrar la venta: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun limpiarFormulario() {
        txtCantidad.setText("1")
        txtClienteDocumento.setText("")
        txtClienteNombre.setText("")
        spinnerProductos.setSelection(0)
        actualizarProductoSeleccionado(null)
    }
}