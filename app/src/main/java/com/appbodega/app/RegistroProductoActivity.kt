package com.appbodega.app

import android.Manifest
import android.os.Bundle
import android.util.Size
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.appbodega.app.databinding.ActivityScannerBinding
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class RegistroProductoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityScannerBinding
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var barcodeScanner: BarcodeScanner

    private var lastScannedCode: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScannerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        cameraExecutor = Executors.newSingleThreadExecutor()
        barcodeScanner = BarcodeScanning.getClient()

        binding.btnGuardar.setOnClickListener {
            lastScannedCode?.let { code ->
                val resultIntent = android.content.Intent().apply {
                    putExtra("codigo_escaneado", code)
                }
                setResult(RESULT_OK, resultIntent)
                finish()
            }
        }

        binding.btnLimpiar.setOnClickListener {
            lastScannedCode = null
            binding.resultTextView.text = "Esperando escaneo…"
            binding.btnGuardar.isEnabled = false
            binding.codeTypeChip.visibility = android.view.View.GONE
        }

        val requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (isGranted) {
                    startCamera()
                } else {
                    binding.resultTextView.text = "Permisos de cámara necesarios"
                }
            }

        requestPermissionLauncher.launch(Manifest.permission.CAMERA)
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        val screenSize = Size(1280, 720)
        val resolutionSelector = ResolutionSelector.Builder().setResolutionStrategy(
            ResolutionStrategy(screenSize, ResolutionStrategy.FALLBACK_RULE_NONE)
        ).build()
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().setResolutionSelector(resolutionSelector)
                .build()
                .also {
                    it.setSurfaceProvider(binding.previewView.surfaceProvider)
                }
            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, { imageProxy ->
                        processImageProxy(imageProxy)
                    })
                }
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalyzer)
        }, ContextCompat.getMainExecutor(this))
    }

    @OptIn(ExperimentalGetImage::class)
    private fun processImageProxy(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image

        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

            barcodeScanner.process(image)
                .addOnSuccessListener { barcodes ->
                    for (barcode in barcodes){
                        handleBarcode(barcode)
                    }
                }
                .addOnFailureListener{
                    binding.resultTextView.text = "Fallo al escanear el codigo"
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }

        }
    }

    private fun handleBarcode(barcode: Barcode) {
        val txt = barcode.rawValue ?: barcode.url?.url ?: barcode.displayValue
        if (!txt.isNullOrEmpty()) {
            lastScannedCode = txt
            binding.resultTextView.text = txt
            binding.btnGuardar.isEnabled = true
            binding.btnGuardar.text = "Usar Código: $txt"
            binding.codeTypeChip.visibility = android.view.View.VISIBLE
            binding.codeTypeChip.text = when (barcode.format) {
                Barcode.FORMAT_QR_CODE -> "Código QR"
                Barcode.FORMAT_EAN_13 -> "EAN-13"
                Barcode.FORMAT_EAN_8 -> "EAN-8"
                Barcode.FORMAT_UPC_A -> "UPC-A"
                Barcode.FORMAT_UPC_E -> "UPC-E"
                Barcode.FORMAT_CODE_128 -> "Code 128"
                else -> "Código de Barras"
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}