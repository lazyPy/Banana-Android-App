package com.example.bananadetection

// Import necessary Android libraries and components
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

// Suppress deprecation warnings
@Suppress("DEPRECATION")
// MainActivity class extending AppCompatActivity
class MainActivity : AppCompatActivity() {
    // Variables declaration
    private var filePathCallback: ValueCallback<Array<Uri>>? = null
    private lateinit var loadingSpinner: ProgressBar

    // Method called when the activity is created
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main) // Set the layout for this activity

        // Initialize WebView and ProgressBar
        val myWeb = findViewById<WebView>(R.id.MyWebView)
        loadingSpinner = findViewById(R.id.loadingSpinner)

        // Set loading spinner to be visible initially
        loadingSpinner.visibility = View.VISIBLE

        // Configure WebView behavior
        myWeb.webViewClient = object : WebViewClient() {
            // Show loading spinner when page starts loading
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                loadingSpinner.visibility = View.VISIBLE
            }

            // Hide loading spinner when page finishes loading
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                loadingSpinner.visibility = View.GONE
            }
        }

        // Load a specific URL in the WebView and configure its settings
        myWeb.apply {
            loadUrl("https://banana-detection.onrender.com")
            settings.javaScriptEnabled = true
            webChromeClient = object : WebChromeClient() {
                // Handle file choosing in the WebView
                override fun onShowFileChooser(
                    webView: WebView?,
                    callback: ValueCallback<Array<Uri>>?,
                    fileChooserParams: FileChooserParams?
                ): Boolean {
                    filePathCallback = callback
                    openImageChooser()
                    return true
                }
            }
        }
    }

    // Activity result launcher for image selection
    private val imageChooser =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val data: Intent? = result.data
            if (data != null && filePathCallback != null) {
                val selectedImage: Uri? = data.data
                val uris = if (selectedImage != null) {
                    arrayOf(selectedImage)
                } else {
                    val extras = data.extras
                    if (extras != null && extras.containsKey("data")) {
                        val imageBitmap = extras.get("data") as Bitmap
                        val uri = Uri.parse(MediaStore.Images.Media.insertImage(contentResolver, imageBitmap, "Title", null))
                        arrayOf(uri)
                    } else {
                        null
                    }
                }
                filePathCallback!!.onReceiveValue(uris)
                filePathCallback = null
            }
        }

    // Method to open the image chooser for selecting images
    private fun openImageChooser() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "image/*"

        val captureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val chooser = Intent.createChooser(intent, "Choose Image")
        chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(captureIntent))

        // Launch image chooser activity
        imageChooser.launch(chooser)
    }
}
