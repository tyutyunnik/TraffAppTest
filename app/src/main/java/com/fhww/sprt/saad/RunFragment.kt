package com.fhww.sprt.saad

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import android.webkit.*
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.fhww.sprt.saad.databinding.FragmentRunBinding

class RunFragment : Fragment(R.layout.fragment_run) {
    private lateinit var binding: FragmentRunBinding
    private lateinit var shaPr: SharedPreferences
    private var lnk = ""
    private var isLnkRwrt = true
    private var appsF = ""

    private var mCameraPhotoUri: Uri? = null
    private var mFilePathCallback: ValueCallback<Array<Uri>>? = null

    private lateinit var resultLauncher: ActivityResultLauncher<Intent>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentRunBinding.bind(view)

        shaPr =
            requireActivity().getSharedPreferences("shaPr", AppCompatActivity.MODE_PRIVATE)

        lnk = shaPr.getString("lnk", "").toString()
        isLnkRwrt = shaPr.getBoolean("isLnkRwrt", true)
        appsF = shaPr.getString("appsF", "").toString()
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        setSettings()
        initResultLauncher()

        binding.webView.webViewClient = object : WebViewClient() {

            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest
            ): Boolean {
                return rewriteLink(view!!, request.url.toString())
            }

            private fun rewriteLink(view: WebView, url: String): Boolean {
                return if (url.startsWith("mailto:")) {
                    startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse(url)))
                    true
                } else if (url.startsWith("tg:") || url.startsWith("https://t.me")
                    || url.startsWith("https://telegram.me")
                ) {
                    try {
                        view.context.startActivity(
                            Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse(view.hitTestResult.extra)
                            )
                        )
                    } catch (_: Exception) {
                    }
                    true
                } else {
                    false
                }
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                if (isLnkRwrt) {
                    if (url != null) {
                        shaPr.edit().putString("lnk", url).apply()
                    }
                    shaPr.edit().putBoolean("isLnkRwrt", false).apply()
                    CookieManager.getInstance().flush()
                }
                CookieManager.getInstance().flush()
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
            }
        }

        binding.webView.webChromeClient = object : WebChromeClient() {
            fun checkPermissions() {
                ActivityCompat.requestPermissions(
                    requireActivity(), arrayOf(
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        android.Manifest.permission.READ_EXTERNAL_STORAGE,
                        android.Manifest.permission.CAMERA
                    ), 1
                )
            }

            @SuppressLint("QueryPermissionsNeeded")
            override fun onShowFileChooser(
                webView: WebView?,
                filePathCallback: ValueCallback<Array<Uri>>?,
                fileChooserParams: FileChooserParams?
            ): Boolean {
                val permStat = ContextCompat.checkSelfPermission(
                    requireActivity(),
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                )
                if (permStat == PackageManager.PERMISSION_GRANTED) {
                    mFilePathCallback?.onReceiveValue(null)
                    mFilePathCallback = filePathCallback

                    val values = ContentValues()
                    values.put(MediaStore.Images.Media.TITLE, "New Picture")
                    values.put(MediaStore.Images.Media.DESCRIPTION, "From Camera")
                    mCameraPhotoUri = requireContext().contentResolver.insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        values
                    )

                    val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mCameraPhotoUri)

                    val contentSelectionIntent = Intent(Intent.ACTION_GET_CONTENT)
                    contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE)
                    contentSelectionIntent.type = "image/*"

                    val intentArray: Array<Intent?> = arrayOf(takePictureIntent)

                    val chooserIntent = Intent(Intent.ACTION_CHOOSER)
                    chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent)
                    chooserIntent.putExtra(Intent.EXTRA_TITLE, "Take Photo")
                    chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray)

                    resultLauncher.launch(chooserIntent)

                    return true
                } else checkPermissions()
                return false
            }

            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                binding.progressBar.isActivated = true
                binding.progressBar.visibility = View.VISIBLE
                binding.progressBar.progress = newProgress
                if (newProgress == 100) {
                    binding.progressBar.visibility = View.GONE
                    binding.progressBar.isActivated = false
                }
            }
        }
        binding.webView.setDownloadListener { url, _, _, _, _ ->
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            startActivity(intent)
        }
        binding.webView.loadUrl(lnk)
        Log.d("appsF", appsF)

        requireActivity().onBackPressedDispatcher.addCallback(requireActivity(), object :
            OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.webView.canGoBack()) {
                    binding.webView.goBack()
                } else {
                    AlertDialog.Builder(requireContext())
                        .setTitle("Quit")
                        .setMessage("Are you sure?")
                        .setNegativeButton("No", null)
                        .setPositiveButton("Yes") { _, _ ->
                            requireActivity().finish()
                        }
                        .create()
                        .show()
                }
            }
        })
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setSettings() {
        binding.webView.apply {
            settings.apply {
                userAgentString = binding.webView.settings.userAgentString
                javaScriptEnabled = true
                cacheMode = WebSettings.LOAD_NO_CACHE
                domStorageEnabled = true
                databaseEnabled = true
                setSupportZoom(false)
                allowFileAccess = true
                allowContentAccess = true
                loadWithOverviewMode = true
                useWideViewPort = true
                javaScriptCanOpenWindowsAutomatically = true
            }
        }
        binding.webView.scrollBarStyle = View.SCROLLBARS_INSIDE_OVERLAY
        binding.webView.requestFocus(View.FOCUS_DOWN)
        binding.webView.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        val cookieManager: CookieManager = CookieManager.getInstance()
        cookieManager.setAcceptCookie(true)
        cookieManager.acceptCookie()
        cookieManager.setAcceptThirdPartyCookies(binding.webView, true)
        cookieManager.flush()
    }

    private fun initResultLauncher() {
        resultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val data: Intent? = result.data
                    var results: Array<Uri>? = null
                    if (data == null || data.data == null) {
                        if (mCameraPhotoUri != null) {
                            results = arrayOf((mCameraPhotoUri!!))
                        }
                    } else {
                        val dataString: String? = data.dataString
                        if (dataString != null) {
                            results = arrayOf(Uri.parse(dataString))
                        }
                    }
                    mFilePathCallback!!.onReceiveValue(results)
                    mFilePathCallback = null
                }
            }
    }

    override fun onPause() {
        super.onPause()
        CookieManager.getInstance().flush()
    }

    override fun onResume() {
        super.onResume()
        CookieManager.getInstance().flush()
    }
}