package com.example.swindate

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.icu.text.SimpleDateFormat
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.webkit.*
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.core.view.GestureDetectorCompat
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList

class WebViewActivity : AppCompatActivity() {

    private val REQUEST_SELECT_FILE = 100
    private val CAMERA_REQUEST_CODE = 101
    private val STORAGE_REQUEST_CODE = 102
    private val CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 200

    private var mUploadMessage: ValueCallback<Array<Uri>>? = null
    private lateinit var webView: WebView
    private lateinit var progressBar: ProgressBar
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var gestureDetector: GestureDetectorCompat
    private lateinit var photoURI: Uri

    val Comfortaa = FontFamily(Font(R.font.comfortaa))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        updateUI(resources.configuration.orientation)

        gestureDetector = GestureDetectorCompat(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onDoubleTap(e: MotionEvent): Boolean {
                webView.reload()
                return true
            }
        })

        webView.setOnTouchListener { v, event ->
            v.parent.requestDisallowInterceptTouchEvent(true)
            false
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        updateUI(newConfig.orientation)
    }

    private fun updateUI(orientation: Int) {
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setContent {
                LandscapeSplashScreen()
            }
        } else {
            swipeRefresh = SwipeRefreshLayout(this)
            webView = WebView(this)
            progressBar = ProgressBar(this)
            progressBar.max = 100
            swipeRefresh.addView(webView)
            setContentView(swipeRefresh)

            swipeRefresh.setOnRefreshListener { webView.reload() }
            initializeWebView()
            loadContent()
        }
    }

    @Composable
    fun LandscapeSplashScreen() {
        Box(
            modifier = Modifier.fillMaxSize().background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.mipmap.image),
                    contentDescription = "App Icon",
                    modifier = Modifier.size(100.dp)
                )
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "Only available in portrait mode",
                    fontSize = 16.sp,
                    fontFamily = Comfortaa
                )
                Text(
                    text = "to enhance experience",
                    fontSize = 16.sp,
                    fontFamily = Comfortaa
                )
            }
        }
    }

    private fun initializeWebView() {
        checkAndRequestPermissions()

        webView.apply {
            settings.apply {
                domStorageEnabled = true
                javaScriptEnabled = true
                allowFileAccess = true
                allowContentAccess = true
                databaseEnabled = true
                loadsImagesAutomatically = true
                loadWithOverviewMode = true
                useWideViewPort = true

                cacheMode = if (isNetworkAvailable()) {
                    WebSettings.LOAD_DEFAULT
                } else {
                    WebSettings.LOAD_CACHE_ELSE_NETWORK
                }
            }
            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            webView.layoutParams = layoutParams

            scrollBarStyle = WebView.SCROLLBARS_OUTSIDE_OVERLAY
            isScrollbarFadingEnabled = true
            isNestedScrollingEnabled = true

        }

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                if (url == "https://swindate.com/") {
                    val intent = Intent(this@WebViewActivity, MainActivity::class.java)
                    startActivity(intent)
                    return true
                }
                if (url != null && !url.contains("swindate.com")) {
                    view?.loadUrl("https://swindate.com")
                    return true
                }
                return super.shouldOverrideUrlLoading(view, url)
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                super.onPageStarted(view, url, favicon)
                progressBar.visibility = android.view.View.VISIBLE
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                progressBar.visibility = android.view.View.GONE
                swipeRefresh.isRefreshing = false
            }

            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                super.onReceivedError(view, request, error)
                if (!isNetworkAvailable()) {
                    setContent {
                        NoInternetScreen()
                    }
                } else {
                    Toast.makeText(applicationContext, "Something went wrong", Toast.LENGTH_SHORT).show()
                }
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onShowFileChooser(
                webView: WebView,
                filePathCallback: ValueCallback<Array<Uri>>,
                fileChooserParams: FileChooserParams
            ): Boolean {
                mUploadMessage = filePathCallback
                val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                if (takePictureIntent.resolveActivity(packageManager) != null) {
                    var photoFile: File? = null
                    try {
                        photoFile = createImageFile()
                        takePictureIntent.putExtra(
                            MediaStore.EXTRA_OUTPUT,
                            FileProvider.getUriForFile(
                                applicationContext,
                                BuildConfig.APPLICATION_ID + ".provider",
                                photoFile
                            )
                        )
                        photoURI = FileProvider.getUriForFile(
                            applicationContext,
                            BuildConfig.APPLICATION_ID + ".provider",
                            photoFile
                        )
                    } catch (ex: Exception) {
                        Log.e("WebViewActivity", "Image file creation failed", ex)
                    }
                    if (photoFile != null) {
                        startActivityForResult(takePictureIntent, CAMERA_CAPTURE_IMAGE_REQUEST_CODE)
                    }
                }

                return true
            }

            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                progressBar.progress = newProgress
            }
        }

        webView.addJavascriptInterface(WebAppInterface(this), "AndroidFunction")
        webView.isVerticalScrollBarEnabled = true
        webView.isHorizontalScrollBarEnabled = true
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return gestureDetector.onTouchEvent(event!!) || super.onTouchEvent(event)
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }

    private fun checkAndRequestPermissions() {
        val cameraPermission = ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
        val storagePermission = ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)

        val listPermissionsNeeded = ArrayList<String>()
        if (cameraPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(android.Manifest.permission.CAMERA)
        }
        if (storagePermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(android.Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        if (listPermissionsNeeded.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toTypedArray(), STORAGE_REQUEST_CODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_SELECT_FILE) {
            if (mUploadMessage == null) return
            mUploadMessage?.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, data))
            mUploadMessage = null
        }

        if (requestCode == CAMERA_CAPTURE_IMAGE_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                mUploadMessage?.onReceiveValue(arrayOf(photoURI))
                mUploadMessage = null
            } else {
                mUploadMessage?.onReceiveValue(null)
                mUploadMessage = null
            }
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val imageFile = File.createTempFile(
            imageFileName,
            ".jpg",
            storageDir
        )
        return imageFile
    }

    inner class WebAppInterface(private val mContext: Context) {
        @JavascriptInterface
        fun showToast(toast: String) {
            Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show()
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }

    private fun loadContent() {
        val url = intent.getStringExtra("url")
        if (isNetworkAvailable() || webView.cacheHit) {
            url?.let { webView.loadUrl(it) }
        } else {
            setContent {
                NoInternetScreen()
            }
        }
    }

    @Composable
    fun NoInternetScreen() {
        Box(
            modifier = Modifier.fillMaxSize().background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.mipmap.image),
                    contentDescription = "App Icon",
                    modifier = Modifier.size(100.dp)
                )
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "Swindate requires internet",
                    fontSize = 16.sp,
                    fontFamily = Comfortaa
                )
            }
        }
    }

    val WebView.cacheHit: Boolean
        get() {
            return this.hitTestResult.type != WebView.HitTestResult.UNKNOWN_TYPE
        }
}