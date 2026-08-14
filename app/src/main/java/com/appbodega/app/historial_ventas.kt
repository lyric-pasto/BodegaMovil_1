package com.appbodega.app

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.appbodega.Adapter.VentaAdapter
import com.appbodega.entity.venta
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.ChipGroup
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class historial_ventas : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var spinnerMetodo: Spinner
    private lateinit var tvDesde: TextView
    private lateinit var tvHasta: TextView
    private lateinit var tvTotalVentas: TextView
    private lateinit var tvTotalFiltrado: TextView
    private lateinit var chipGroupCategorias: ChipGroup
    private lateinit var rvHistorialVentas: RecyclerView
    private lateinit var tvEmptyVentas: TextView
    private lateinit var btnAtras: MaterialButton
    private lateinit var btnNuevaVenta: MaterialButton

    private lateinit var ventaAdapter: VentaAdapter
    private val todasLasVentas = mutableListOf<venta>()
    private val ventasFiltradas = mutableListOf<venta>()

    private val formatoFecha = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private var categoriaSeleccionada = "Todas"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_historial_ventas)

        initViews()
        setupRecyclerView()
        setupListeners()
        cargarMetodos()
        cargarVentasDesdeFirebase()
    }

    private fun initViews() {
        toolbar = findViewById(R.id.toolbarHistorial)
        spinnerMetodo = findViewById(R.id.spinnerMetodo)
        tvDesde = findViewById(R.id.tvDesde)
        tvHasta = findViewById(R.id.tvHasta)
        tvTotalVentas = findViewById(R.id.tvTotalVentas)
        tvTotalFiltrado = findViewById(R.id.tvTotalFiltrado)
        chipGroupCategorias = findViewById(R.id.chipGroupCategorias)
        rvHistorialVentas = findViewById(R.id.rvHistorialVentas)
        tvEmptyVentas = findViewById(R.id.tvEmptyVentas)
        btnAtras = findViewById(R.id.btnAtras)
        btnNuevaVenta = findViewById(R.id.btnNuevaVenta)

        // Fechas por defecto: 1er día del mes actual hasta fin de año
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, 1)
        tvDesde.text = formatoFecha.format(cal.time)

        cal.add(Calendar.MONTH, 11)
        cal.set(Calendar.DAY_OF_MONTH, 31)
        tvHasta.text = formatoFecha.format(cal.time)
    }

    private fun setupRecyclerView() {
        ventaAdapter = VentaAdapter(ventasFiltradas) { ventaSeleccionada ->
            mostrarDetalleVenta(ventaSeleccionada)
        }
        rvHistorialVentas.layoutManager = LinearLayoutManager(this)
        rvHistorialVentas.adapter = ventaAdapter
    }

    private fun setupListeners() {
        toolbar.setNavigationOnClickListener { finish() }
        btnAtras.setOnClickListener { finish() }

        btnNuevaVenta.setOnClickListener {
            startActivity(Intent(this, registro_ventas::class.java))
        }

        tvDesde.setOnClickListener { mostrarCalendario(tvDesde) }
        tvHasta.setOnClickListener { mostrarCalendario(tvHasta) }

        chipGroupCategorias.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isEmpty()) {
                categoriaSeleccionada = "Todas"
            } else {
                val chipId = checkedIds.first()
                categoriaSeleccionada = when (chipId) {
                    R.id.chipAbarrotes -> "Abarrotes"
                    R.id.chipBebidas -> "Bebidas"
                    R.id.chipLimpieza -> "Limpieza"
                    R.id.chipSnacks -> "Snacks"
                    R.id.chipAlcohol -> "Alcohol"
                    else -> "Todas"
                }
            }
            aplicarFiltros()
        }
    }

    private fun cargarMetodos() {
        val metodos = arrayOf("Todos", "Efectivo", "Yape", "Plin", "Tarjeta", "Transferencia")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, metodos)
        spinnerMetodo.adapter = adapter

        spinnerMetodo.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                aplicarFiltros()
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }
    }

    private fun cargarVentasDesdeFirebase() {
        val db = FirebaseDatabase.getInstance().getReference("ventas")
        db.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                todasLasVentas.clear()
                for (child in snapshot.children) {
                    val v = child.getValue(venta::class.java)
                    if (v != null) {
                        todasLasVentas.add(v)
                    }
                }
                todasLasVentas.reverse() // Más recientes primero
                aplicarFiltros()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@historial_ventas, "Error al cargar historial: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun aplicarFiltros() {
        val metodoSeleccionado = spinnerMetodo.selectedItem?.toString() ?: "Todos"
        val desdeStr = tvDesde.text.toString()
        val hastaStr = tvHasta.text.toString()

        val fechaDesde: Date? = try { formatoFecha.parse(desdeStr) } catch (e: Exception) { null }
        val fechaHasta: Date? = try { formatoFecha.parse(hastaStr) } catch (e: Exception) { null }

        ventasFiltradas.clear()

        var sumaHistorica = 0.0
        var sumaFiltrada = 0.0

        for (v in todasLasVentas) {
            sumaHistorica += v.total

            var cumple = true

            // Filtro Método
            if (metodoSeleccionado != "Todos" && !v.metodo.equals(metodoSeleccionado, ignoreCase = true)) {
                cumple = false
            }

            // Filtro Categoría
            if (categoriaSeleccionada != "Todas" && !v.categoria.equals(categoriaSeleccionada, ignoreCase = true)) {
                cumple = false
            }

            // Filtro Fecha
            val fechaVenta: Date? = try { formatoFecha.parse(v.fecha) } catch (e: Exception) { null }
            if (fechaVenta != null) {
                if (fechaDesde != null && fechaVenta.before(fechaDesde)) cumple = false
                if (fechaHasta != null && fechaVenta.after(fechaHasta)) cumple = false
            }

            if (cumple) {
                ventasFiltradas.add(v)
                sumaFiltrada += v.total
            }
        }

        tvTotalVentas.text = "S/ ${String.format(Locale.US, "%.2f", sumaHistorica)}"
        tvTotalFiltrado.text = "S/ ${String.format(Locale.US, "%.2f", sumaFiltrada)}"

        ventaAdapter.updateList(ventasFiltradas)

        if (ventasFiltradas.isEmpty()) {
            tvEmptyVentas.visibility = View.VISIBLE
        } else {
            tvEmptyVentas.visibility = View.GONE
        }
    }

    private fun mostrarCalendario(textView: TextView) {
        val calendario = Calendar.getInstance()
        val year = calendario.get(Calendar.YEAR)
        val month = calendario.get(Calendar.MONTH)
        val day = calendario.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(
            this,
            { _, y, m, d ->
                textView.text = "%02d/%02d/%04d".format(d, m + 1, y)
                aplicarFiltros()
            },
            year,
            month,
            day
        ).show()
    }

    private fun mostrarDetalleVenta(v: venta) {
        val detalle = """
            Código: ${v.codigo}
            Fecha: ${v.fecha}
            Cliente: ${v.nombreCliente} (${v.tipoCliente} ${v.documentoCliente})
            Producto: ${v.nombreProducto}
            Categoría: ${v.categoria}
            Cantidad: ${v.cantidad} unid.
            Precio Unitario: S/ ${String.format(Locale.US, "%.2f", v.precioUnitario)}
            Método de Pago: ${v.metodo}
            Total Cobrado: S/ ${String.format(Locale.US, "%.2f", v.total)}
        """.trimIndent()

        AlertDialog.Builder(this)
            .setTitle("Detalle de Venta")
            .setMessage(detalle)
            .setPositiveButton("Cerrar", null)
            .setNegativeButton("Anular Venta") { _, _ ->
                confirmarEliminarVenta(v)
            }
            .show()
    }

    private fun confirmarEliminarVenta(v: venta) {
        AlertDialog.Builder(this)
            .setTitle("¿Anular Venta?")
            .setMessage("¿Deseas anular esta venta? Se restaurará el stock de ${v.cantidad} unidades en el producto.")
            .setPositiveButton("Sí, Anular") { _, _ ->
                val db = FirebaseDatabase.getInstance().reference

                // 1. Eliminar de /ventas
                db.child("ventas").child(v.id).removeValue().addOnSuccessListener {
                    // 2. Restaurar stock
                    if (v.productoId.isNotEmpty()) {
                        db.child("productos").child(v.productoId).child("cantidad").get().addOnSuccessListener { snap ->
                            val stockActual = snap.getValue(Int::class.java) ?: 0
                            db.child("productos").child(v.productoId).child("cantidad").setValue(stockActual + v.cantidad)
                        }
                    }
                    Toast.makeText(this, "Venta anulada correctamente", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}
