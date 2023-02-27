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
import android.os.Handler
import android.provider.Settings
import android.text.util.Linkify
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.annotation.UiThread
import androidx.annotation.WorkerThread
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.os.HandlerCompat
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.AdRequest
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.lang.StringBuilder
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import java.util.*
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity(), LocationListener {
    var shopName01 = ""
    var shopAddr01 = ""
    var shopido01 = ""
    var shopkeido01 = ""
    var shopurls01 = ""
    var shopName02 = ""
    var shopAddr02 = ""
    var shopido02 = ""
    var shopkeido02 = ""
    var shopurls02 = ""
    var shopName03 = ""
    var shopAddr03 = ""
    var shopido03 = ""
    var shopkeido03 = ""
    var shopurls03 = ""

    private var _ido = 0.0     //緯度
    private var _keido = 0.0   //経度
    private var _GPS_flg = 0   //GPS起動済フラグ
    private var _KensakuSumi_flg = 0   //検索済フラグ

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
                "これ以上なにもできません", Toast.LENGTH_SHORT
            )
            toast.show()
        }
    }
    // 店情報の取得処理を行うメソッド
    private fun receiveShopInfo(urlFull: String){
        val handler = HandlerCompat.createAsync(mainLooper)
        val backgroundReceiver = ShopInfoBackgroundReceiver(handler,urlFull)
        val executeService = Executors.newSingleThreadExecutor()
        executeService.submit(backgroundReceiver)
    }
    // 非同期で店情報取得APIにアクセスする為のクラス
    private inner class ShopInfoBackgroundReceiver(handler: Handler, url: String): Runnable{
        // ハンドラオブジェクト
        private val _handler = handler
        // 店情報を取得するURL
        private val _url = url
        @WorkerThread
        override fun run() {
            // 店情報取得サービスから取得したJSON文字列。店情報を格納。
            var result = ""
            // URIオブジェクトを生成
            val url = URL(_url)
            // URLオブジェクトからHttpURLConnectionオブジェクトを取得
            val con = url.openConnection() as? HttpURLConnection
            // con がnullでない場合
            con?.let {
                try {
                    it.connectTimeout = 1000
                    it.readTimeout = 1000
                    it.requestMethod = "GET"
                    it.connect()
                    val stream = it.inputStream
                    result = is2String(stream)
                    stream.close()
                }
                catch(ex: SocketTimeoutException){
                }
                it.disconnect()
            }
            // ここに店情報取得APIにアクセスするコードを記述
            val postExecutor = ShopInfoPostExecutor(result)
            _handler.post(postExecutor)
        }

        private fun is2String(stream: InputStream): String {
            val sb = StringBuilder()
            val reader = BufferedReader(InputStreamReader(stream, "UTF-8"))
            var line = reader.readLine()
            while(line != null) {
                sb.append(line)
                line = reader.readLine()
            }
            reader.close()
            return sb.toString()
        }
    }
    // 非同期で店情報を取得した後にUIスレッドでその情報を表示するためのクラス
    private inner class ShopInfoPostExecutor(result: String): Runnable {
        private val _result = result
        @UiThread
        override fun run() {
            // ホットペツパー情報の取得と表示
            val rootJSON = JSONObject(_result)
            val resultsJSON = rootJSON.getJSONObject("results")
            //1件目の店名取得
            val shopJSONArray = resultsJSON.getJSONArray("shop")
            val shopJSON01 = shopJSONArray.getJSONObject(0)
            shopName01 = shopJSON01.getString("name")
            val textView0101: TextView = findViewById(R.id.tvShopName01)
            textView0101.text = shopName01
            //1件目の住所取得
            shopAddr01 = shopJSON01.getString("address")
            val textView0102: TextView = findViewById(R.id.tvShopAddr01)
            textView0102.text = shopAddr01
            //1件目の緯度取得
            shopido01 = shopJSON01.getString("lat")
            val textView0103: TextView = findViewById(R.id.tvShopIdo01)
            textView0103.text = shopido01
            //1件目の経度取得
            shopkeido01 = shopJSON01.getString("lng")
            val textView0104: TextView = findViewById(R.id.tvShopKeido01)
            textView0104.text = shopkeido01
            //1件目のURL取得
            val urlsJSON01 = shopJSON01.getJSONObject("urls")
            shopurls01 = urlsJSON01.getString("pc")
            val textView0105: TextView = findViewById(R.id.tvShopUrl01)
            textView0105.setAutoLinkMask(Linkify.WEB_URLS);
            textView0105.text = shopurls01 + "#detailInfo"

            //2件目の店名取得
            val shopJSON02 = shopJSONArray.getJSONObject(1)
            shopName02 = shopJSON02.getString("name")
            val textView0201: TextView = findViewById(R.id.tvShopName02)
            textView0201.text = shopName02
            //2件目の住所取得
            shopAddr02 = shopJSON02.getString("address")
            val textView0202: TextView = findViewById(R.id.tvShopAddr02)
            textView0202.text = shopAddr02
            //2件目の緯度経度取得
            shopido02 = shopJSON02.getString("lat")
            shopkeido02 = shopJSON02.getString("lng")
            //2件目の緯度取得
            shopido02 = shopJSON02.getString("lat")
            val textView0203: TextView = findViewById(R.id.tvShopIdo02)
            textView0203.text = shopido02
            //2件目の経度取得
            shopkeido02 = shopJSON02.getString("lng")
            val textView0204: TextView = findViewById(R.id.tvShopKeido02)
            textView0204.text = shopkeido02
            //2件目のURL取得
            val urlsJSON02 = shopJSON02.getJSONObject("urls")
            shopurls02 = urlsJSON02.getString("pc")
            val textView0205: TextView = findViewById(R.id.tvShopUrl02)
            textView0205.setAutoLinkMask(Linkify.WEB_URLS)
            textView0205.text = shopurls02 + "#detailInfo"

            //3件目の店名取得
            val shopJSON03 = shopJSONArray.getJSONObject(2)
            shopName03 = shopJSON03.getString("name")
            val textView0301: TextView = findViewById(R.id.tvShopName03)
            textView0301.text = shopName03
            //3件目の住所取得
            shopAddr03 = shopJSON03.getString("address")
            val textView0302: TextView = findViewById(R.id.tvShopAddr03)
            textView0302.text = shopAddr03
            //3件目の緯度経度取得
            shopido03 = shopJSON03.getString("lat")
            shopkeido03 = shopJSON03.getString("lng")
            //3件目の緯度取得
            shopido03 = shopJSON03.getString("lat")
            val textView0303: TextView = findViewById(R.id.tvShopIdo03)
            textView0303.text = shopido03
            //3件目の経度取得
            shopkeido03 = shopJSON03.getString("lng")
            val textView0304: TextView = findViewById(R.id.tvShopKeido03)
            textView0304.text = shopkeido03
            //3件目のURL取得
            val urlsJSON03 = shopJSON03.getJSONObject("urls")
            shopurls03 = urlsJSON03.getString("pc")
            val textView0305: TextView = findViewById(R.id.tvShopUrl03)
            textView0305.setAutoLinkMask(Linkify.WEB_URLS)
            textView0305.text = shopurls03 + "#detailInfo"
            //検索済フラグをオンにする
            _KensakuSumi_flg = 1
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        lateinit var mAdView : AdView
        MobileAds.initialize(this) {}

        mAdView = findViewById(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)

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

            // 現在の緯度と経度を取得
            val lat = _ido
            val ltg = _keido

            // ホットペツパーAPIを起動する為のURL　検索対象は固定値3件
            // rangeは3：1000m以内　、orderは4：おすすめ順
            val urlFull = String.format(
                Locale.US,
                "https://webservice.recruit.co.jp/hotpepper/gourmet/v1/?key=b2dd6f39b13b34bd&lat=%s&lng=%s&range=3&order=4&count=3&format=json",
                lat, ltg
            )
            receiveShopInfo(urlFull)
        }
    }

    override fun onProviderEnabled(provider: String) {
    }

    override fun onProviderDisabled(provider: String) {
    }

    //１件目店舗までのルート検索ボタンタップ時の処理
    fun onRouteSearchButtonClick01(view: View) {
        if (_KensakuSumi_flg.equals(1)) {
            //出発地（現在地）
            var sta_lat01 = _ido;
            var sta_ltg01 = _keido;
            //目的地
            var end_lat01 = shopido01;
            var end_ltg01 = shopkeido01;
            intent.setAction(Intent.ACTION_VIEW)
            intent.setClassName(
                "com.google.android.apps.maps",
                "com.google.android.maps.MapsActivity"
            );
            var str01 = String.format(
                Locale.US,
                "http://maps.google.com/maps?saddr=%s,%s&daddr=%s,%s",
                sta_lat01, sta_ltg01, end_lat01, end_ltg01
            );
            intent.setData(Uri.parse(str01))
            startActivity(intent)
        }
    }

    //２件目店舗までのルート検索ボタンタップ時の処理
    fun onRouteSearchButtonClick02(view: View) {
        if (_KensakuSumi_flg.equals(1)) {
            //出発地（現在地）
            var sta_lat02 = _ido;
            var sta_ltg02 = _keido;
            //目的地
            var end_lat02 = shopido02;
            var end_ltg02 = shopkeido02;
            intent.setAction(Intent.ACTION_VIEW)
            intent.setClassName(
                "com.google.android.apps.maps",
                "com.google.android.maps.MapsActivity"
            );
            var str02 = String.format(
                Locale.US,
                "http://maps.google.com/maps?saddr=%s,%s&daddr=%s,%s",
                sta_lat02, sta_ltg02, end_lat02, end_ltg02
            );
            intent.setData(Uri.parse(str02))
            startActivity(intent)
        }
    }
    //３件目店舗までのルート検索ボタンタップ時の処理
    fun onRouteSearchButtonClick03(view: View) {
        if (_KensakuSumi_flg.equals(1)) {
            //出発地（現在地）
        var sta_lat03 = _ido;
        var sta_ltg03 = _keido;
        //目的地
        var end_lat03 = shopido03;
        var end_ltg03 = shopkeido03;
        intent.setAction(Intent.ACTION_VIEW)
        intent.setClassName("com.google.android.apps.maps",
            "com.google.android.maps.MapsActivity");
        var str03 = String.format(
            Locale.US,
            "http://maps.google.com/maps?saddr=%s,%s&daddr=%s,%s",
            sta_lat03,sta_ltg03,end_lat03,end_ltg03);
        intent.setData(Uri.parse(str03))
        startActivity(intent)
        }
    }
}
