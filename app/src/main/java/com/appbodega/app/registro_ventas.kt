package com.appbodega.app

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Base64
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.appbodega.Adapter.ProductoSeleccionAdapter
import com.appbodega.entity.Producto
import com.appbodega.entity.venta
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class registro_ventas : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var tvCodigoGenerado: TextView
    private lateinit var btnAbrirCatalogoCategorias: MaterialButton
    private lateinit var spinnerCategoriasVenta: Spinner
    private lateinit var spinnerProductos: Spinner
    private lateinit var btnEscanearVenta: MaterialButton
    private lateinit var imgProductoPreview: ImageView
    private lateinit var tvNombreProductoSeleccionado: TextView
    private lateinit var tvCategoriaProductoSeleccionado: TextView
    private lateinit var tvStockProductoSeleccionado: TextView
    private lateinit var txtPrecioUnitario: TextView

    private lateinit var btnMenosCantidad: ImageButton
    private lateinit var btnMasCantidad: ImageButton
    private lateinit var txtCantidad: EditText
    private lateinit var spinnerMetodoPago: Spinner
    private lateinit var txtFechaVenta: TextView

    private lateinit var txtSubtotalCalculado: TextView
    private lateinit var txtIgvCalculado: TextView
    private lateinit var txtTotalCalculado: TextView
    private lateinit var btnRegistrarVenta: MaterialButton
    private lateinit var btnHistorialVentas: MaterialButton

    private val listaTodosLosProductos = mutableListOf<Producto>()
    private val listaProductosFiltrados = mutableListOf<Producto>()
    private val categorias = listOf("Todas", "Abarrotes", "Bebidas", "Limpieza", "Snacks", "Alcohol")
    private var categoriaActualSeleccionada = "Todas"

    private var productoSeleccionado: Producto? = null
    private var totalVentasRegistradas = 0
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
        cargarCategorias()
        cargarMetodosPago()
        cargarProductosDesdeFirebase()
        contarVentasParaCodigo()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun initViews() {
        toolbar = findViewById(R.id.toolbarVentas)
        tvCodigoGenerado = findViewById(R.id.tvCodigoGenerado)
        btnAbrirCatalogoCategorias = findViewById(R.id.btnAbrirCatalogoCategorias)
        spinnerCategoriasVenta = findViewById(R.id.spinnerCategoriasVenta)
        spinnerProductos = findViewById(R.id.spinnerProductos)
        btnEscanearVenta = findViewById(R.id.btnEscanearVenta)
        imgProductoPreview = findViewById(R.id.imgProductoPreview)
        tvNombreProductoSeleccionado = findViewById(R.id.tvNombreProductoSeleccionado)
        tvCategoriaProductoSeleccionado = findViewById(R.id.tvCategoriaProductoSeleccionado)
        tvStockProductoSeleccionado = findViewById(R.id.tvStockProductoSeleccionado)
        txtPrecioUnitario = findViewById(R.id.txtPrecioUnitario)

        btnMenosCantidad = findViewById(R.id.btnMenosCantidad)
        btnMasCantidad = findViewById(R.id.btnMasCantidad)
        txtCantidad = findViewById(R.id.txtCantidad)
        spinnerMetodoPago = findViewById(R.id.spinnerMetodoPago)
        txtFechaVenta = findViewById(R.id.txtFechaVenta)

        txtSubtotalCalculado = findViewById(R.id.txtSubtotalCalculado)
        txtIgvCalculado = findViewById(R.id.txtIgvCalculado)
        txtTotalCalculado = findViewById(R.id.txtTotalCalculado)
        btnRegistrarVenta = findViewById(R.id.btnRegistrarVenta)
        btnHistorialVentas = findViewById(R.id.btnHistorialVentas)

        txtFechaVenta.text = fechaSeleccionada
        actualizarCodigoVista()
    }

    private fun setupListeners() {
        toolbar.setNavigationOnClickListener { finish() }

        txtFechaVenta.setOnClickListener {
            mostrarSelectorFecha()
        }

        btnAbrirCatalogoCategorias.setOnClickListener {
            mostrarDialogoSeleccionarPorCategoria()
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

    private fun cargarCategorias() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, categorias)
        spinnerCategoriasVenta.adapter = adapter

        spinnerCategoriasVenta.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                categoriaActualSeleccionada = categorias[position]
                filtrarProductosPorCategoria(categoriaActualSeleccionada)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun contarVentasParaCodigo() {
        FirebaseDatabase.getInstance().getReference("ventas")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    totalVentasRegistradas = snapshot.childrenCount.toInt()
                    actualizarCodigoVista()
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun generarCodigoVenta(): String {
        val num = totalVentasRegistradas + 1
        return "V-%03d".format(num)
    }

    private fun actualizarCodigoVista() {
        tvCodigoGenerado.text = "Código: ${generarCodigoVenta()}"
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
                listaTodosLosProductos.clear()

                for (child in snapshot.children) {
                    val p = child.getValue(Producto::class.java)
                    if (p != null) {
                        listaTodosLosProductos.add(p)
                    }
                }

                filtrarProductosPorCategoria(categoriaActualSeleccionada)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@registro_ventas, "Error al cargar productos: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun filtrarProductosPorCategoria(cat: String) {
        listaProductosFiltrados.clear()
        if (cat.equals("Todas", ignoreCase = true)) {
            listaProductosFiltrados.addAll(listaTodosLosProductos)
        } else {
            listaProductosFiltrados.addAll(
                listaTodosLosProductos.filter { it.categoria.equals(cat, ignoreCase = true) }
            )
        }

        val nombres = mutableListOf("Seleccione un producto...")
        for (p in listaProductosFiltrados) {
            val barcodeInfo = if (p.codigoBarras.isNotEmpty()) "[${p.codigoBarras}] " else ""
            nombres.add("$barcodeInfo${p.nombre} (Stock: ${p.cantidad}) - S/ ${String.format(Locale.US, "%.2f", p.precioVenta)}")
        }

        val adapter = ArrayAdapter(this@registro_ventas, android.R.layout.simple_spinner_dropdown_item, nombres)
        spinnerProductos.adapter = adapter

        spinnerProductos.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position > 0 && position - 1 < listaProductosFiltrados.size) {
                    actualizarProductoSeleccionado(listaProductosFiltrados[position - 1])
                } else {
                    actualizarProductoSeleccionado(null)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                actualizarProductoSeleccionado(null)
            }
        }
    }

    private fun mostrarDialogoSeleccionarPorCategoria() {
        val dialog = Dialog(this, android.R.style.Theme_Material_Light_NoActionBar_Fullscreen)
        dialog.setContentView(R.layout.dialog_seleccionar_producto)

        val btnCerrarDialogo = dialog.findViewById<ImageButton>(R.id.btnCerrarDialogo)
        val etBuscarProductoDialog = dialog.findViewById<TextInputEditText>(R.id.etBuscarProductoDialog)
        val chipGroup = dialog.findViewById<ChipGroup>(R.id.chipGroupCategoriasDialog)
        val rvProductos = dialog.findViewById<RecyclerView>(R.id.rvProductosDialog)
        val layoutVacio = dialog.findViewById<View>(R.id.layoutVacioDialog)

        rvProductos.layoutManager = LinearLayoutManager(this)

        var categoriaFiltro = categoriaActualSeleccionada
        val itemsFiltrados = mutableListOf<Producto>()

        fun actualizarListaModal() {
            val query = etBuscarProductoDialog.text.toString().trim()
            itemsFiltrados.clear()

            for (p in listaTodosLosProductos) {
                val coincideCategoria = categoriaFiltro.equals("Todas", ignoreCase = true) ||
                        p.categoria.equals(categoriaFiltro, ignoreCase = true)

                val coincideBusqueda = query.isEmpty() ||
                        p.nombre.contains(query, ignoreCase = true) ||
                        p.codigoBarras.contains(query, ignoreCase = true) ||
                        p.descripcion.contains(query, ignoreCase = true) ||
                        p.categoria.contains(query, ignoreCase = true)

                if (coincideCategoria && coincideBusqueda) {
                    itemsFiltrados.add(p)
                }
            }

            val adapterModal = ProductoSeleccionAdapter(itemsFiltrados) { productoElegido ->
                // Selecciona el producto y sincroniza la UI principal
                val catIndex = categorias.indexOfFirst { it.equals(productoElegido.categoria, ignoreCase = true) }
                if (catIndex != -1) {
                    spinnerCategoriasVenta.setSelection(catIndex)
                } else {
                    spinnerCategoriasVenta.setSelection(0)
                }

                actualizarProductoSeleccionado(productoElegido)
                dialog.dismiss()
            }
            rvProductos.adapter = adapterModal

            if (itemsFiltrados.isEmpty()) {
                layoutVacio.visibility = View.VISIBLE
                rvProductos.visibility = View.GONE
            } else {
                layoutVacio.visibility = View.GONE
                rvProductos.visibility = View.VISIBLE
            }
        }

        // Sincronizar chip inicial
        when (categoriaFiltro) {
            "Abarrotes" -> chipGroup.check(R.id.chipDialogAbarrotes)
            "Bebidas" -> chipGroup.check(R.id.chipDialogBebidas)
            "Limpieza" -> chipGroup.check(R.id.chipDialogLimpieza)
            "Snacks" -> chipGroup.check(R.id.chipDialogSnacks)
            "Alcohol" -> chipGroup.check(R.id.chipDialogAlcohol)
            else -> chipGroup.check(R.id.chipDialogTodas)
        }

        chipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            categoriaFiltro = if (checkedIds.isEmpty()) {
                "Todas"
            } else {
                when (checkedIds.first()) {
                    R.id.chipDialogAbarrotes -> "Abarrotes"
                    R.id.chipDialogBebidas -> "Bebidas"
                    R.id.chipDialogLimpieza -> "Limpieza"
                    R.id.chipDialogSnacks -> "Snacks"
                    R.id.chipDialogAlcohol -> "Alcohol"
                    else -> "Todas"
                }
            }
            actualizarListaModal()
        }

        etBuscarProductoDialog.addTextChangedListener {
            actualizarListaModal()
        }

        btnCerrarDialogo.setOnClickListener {
            dialog.dismiss()
        }

        actualizarListaModal()
        dialog.show()
    }

    private fun seleccionarProductoPorCodigo(codigo: String) {
        val index = listaTodosLosProductos.indexOfFirst { it.codigoBarras == codigo || it.id == codigo }
        if (index != -1) {
            val prod = listaTodosLosProductos[index]
            val catIndex = categorias.indexOfFirst { it.equals(prod.categoria, ignoreCase = true) }
            if (catIndex != -1) {
                spinnerCategoriasVenta.setSelection(catIndex)
            }
            actualizarProductoSeleccionado(prod)
            Toast.makeText(this, "Producto escaneado: ${prod.nombre}", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "No se encontró producto con código: $codigo", Toast.LENGTH_LONG).show()
        }
    }

    private fun actualizarProductoSeleccionado(prod: Producto?) {
        productoSeleccionado = prod
        if (prod != null) {
            tvNombreProductoSeleccionado.text = prod.nombre
            tvCategoriaProductoSeleccionado.text = "Categoría: ${prod.categoria}"
            tvStockProductoSeleccionado.text = "Stock disponible: ${prod.cantidad} unid."
            txtPrecioUnitario.text = "P. Unit: S/ ${String.format(Locale.US, "%.2f", prod.precioVenta)}"

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
            tvNombreProductoSeleccionado.text = "Selecciona o busca un producto"
            tvCategoriaProductoSeleccionado.text = "Categoría: -"
            tvStockProductoSeleccionado.text = "Stock: -"
            txtPrecioUnitario.text = "P. Unit: S/ 0.00"
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
            Toast.makeText(this, "Stock insuficiente. Solo quedan ${prod.cantidad} unidades.", Toast.LENGTH_LONG).show()
            return
        }

        val metodoPago = spinnerMetodoPago.selectedItem?.toString() ?: "Efectivo"
        val total = cantidad * prod.precioVenta

        val dbRef = FirebaseDatabase.getInstance().getReference("ventas")
        val key = dbRef.push().key ?: System.currentTimeMillis().toString()
        val codigoVenta = generarCodigoVenta()

        val nuevaVenta = venta(
            id = key,
            codigo = codigoVenta,
            fecha = fechaSeleccionada,
            cantidad = cantidad,
            metodo = metodoPago,
            categoria = prod.categoria,
            total = total,
            productoId = prod.id
        )

        btnRegistrarVenta.isEnabled = false

        // 1. Guardar Venta en /ventas
        dbRef.child(key).setValue(nuevaVenta)
            .addOnSuccessListener {
                // 2. Descontar Stock del Producto en /productos/{id}/cantidad
                val nuevoStock = prod.cantidad - cantidad
                FirebaseDatabase.getInstance().getReference("productos")
                    .child(prod.id)
                    .child("cantidad")
                    .setValue(nuevoStock)

                AlertDialog.Builder(this)
                    .setTitle("¡Venta Registrada Exitosamente!")
                    .setMessage(
                        "Código: $codigoVenta\n" +
                        "Producto: ${prod.nombre}\n" +
                        "Categoría: ${prod.categoria}\n" +
                        "Cantidad: $cantidad unid.\n" +
                        "Método: $metodoPago\n" +
                        "Total: S/ ${String.format(Locale.US, "%.2f", total)}\n" +
                        "Stock restante: $nuevoStock unid."
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
                Toast.makeText(this, "Error al registrar: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun limpiarFormulario() {
        txtCantidad.setText("1")
        if (spinnerProductos.adapter != null && spinnerProductos.adapter.count > 0) {
            spinnerProductos.setSelection(0)
        }
        actualizarProductoSeleccionado(null)
    }
}
