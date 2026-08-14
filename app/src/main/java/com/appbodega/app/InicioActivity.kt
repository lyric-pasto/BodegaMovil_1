package com.appbodega.app

import android.content.Intent
import android.os.Bundle
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.appbodega.ui.CategoriasFragment
import com.google.android.material.navigation.NavigationView
import kotlin.jvm.java

class InicioActivity : AppCompatActivity() {

    private lateinit var dlaymenu: DrawerLayout
    private lateinit var nvMenu: NavigationView
    private lateinit var ivMenu: ImageView
    private lateinit var flayContenedor: FrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_inicio)

        dlaymenu = findViewById(R.id.dlaymenu)
        nvMenu = findViewById(R.id.nvMenu)
        ivMenu = findViewById(R.id.ivMenu)
        flayContenedor = findViewById(R.id.flayContenedor)

        ivMenu.setOnClickListener { dlaymenu.openDrawer(GravityCompat.START) }

        supportFragmentManager.beginTransaction()
            .replace(R.id.flayContenedor, CategoriasFragment())
            .commit()

        nvMenu.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.itInicio ->
                    supportFragmentManager.beginTransaction()
                    .replace(R.id.flayContenedor, CategoriasFragment())
                    .commit()
                R.id.itCatalogo ->
                    supportFragmentManager.beginTransaction()
                    .replace(R.id.flayContenedor, CategoriasFragment())
                    .commit()
                R.id.itHistorial -> startActivity(Intent(this, historial_ventas::class.java))
                R.id.itCerrarSesion -> startActivity(Intent(this, inicio_sesion::class.java))
            }

            cerrarMenu()
            true
        }



        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.dlaymenu)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    fun abrirMenu() {
        dlaymenu.openDrawer(GravityCompat.START)
    }

    fun cerrarMenu() {
        dlaymenu.closeDrawer(GravityCompat.START)
    }

}
