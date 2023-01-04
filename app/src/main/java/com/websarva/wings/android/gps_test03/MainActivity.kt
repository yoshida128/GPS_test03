package com.websarva.wings.android.gps_test03

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.*

class MainActivity : AppCompatActivity(), LocationListener {
    private var _ido = 0.0     //緯度
    private var _keido = 0.0   //経度
    private var _zoom = 17     //ズーム（最大：20まで）
    private var _GPS_flg = 0   //GPS起動済フラグ

    private lateinit var locationManager: LocationManager
    private val requestPermissionLauncher = registerForActivityResult(

        RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // 使用が許可された場合
            locationStart()
        } else {
            // それでも拒否された場合
            val toast = Toast.makeText(
                this,
                "これ以上なにもできません。", Toast.LENGTH_SHORT
            )
            toast.show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            locationStart()
        }
    }

    private fun locationStart() {
        Log.d("debug", "locationStart()")
        // Instances of LocationManager class must be obtained using Context.getSystemService(Class)
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Log.d("debug", "location manager Enabled")
        } else {
            val settingsIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(settingsIntent)
            Log.d("debug", "not gpsEnable, startActivity")
        }
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1000
            )
            Log.d("debug", "checkSelfPermission false")
            return
        }
        locationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            1000,
            50f,
            this
        )
    }

    override fun onLocationChanged(location: Location) {
        if (_GPS_flg.equals(0)) {
            _ido = location.latitude
            _keido = location.longitude
            _GPS_flg = 1

            //出発地　★東京タワー（仮）
            var sta_lat = _ido;
            var sta_ltg = _keido;
            //目的地　★御成門小学校前バス停（仮）
            //最終的に店情報の緯度と経度を設定する。仮で出発地から引いた値を設定。
            var end_lat = _ido - 0.0019312;
            var end_ltg = _keido - 0.0036184;
            intent.setAction(Intent.ACTION_VIEW)
            intent.setClassName("com.google.android.apps.maps",
                "com.google.android.maps.MapsActivity");
            var str = String.format(
                Locale.US,
                "http://maps.google.com/maps?saddr=%s,%s&daddr=%s,%s",
                sta_lat,sta_ltg,end_lat,end_ltg);
            intent.setData(Uri.parse(str))
            startActivity(intent)
        }
    }

    override fun onProviderEnabled(provider: String) {
    }

    override fun onProviderDisabled(provider: String) {
    }
}
