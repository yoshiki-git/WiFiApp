package com.example.wifiapp

import android.R.attr.button
import android.annotation.TargetApi
import android.app.ActivityManager
import android.app.AlarmManager
import android.content.*
import android.content.pm.PackageManager
import android.net.Uri
import android.net.wifi.ScanResult
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.*
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.WindowManager
import android.widget.Button
import android.widget.CheckBox
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.File
import java.lang.Exception
import java.time.LocalTime
import java.util.*


class MainActivity : AppCompatActivity() {
    //やること 複数人にメール送信　送信エラーでアプリ落ちないようにする
    //Wifiスキャン条件
    //Wifiオン、位置情報オン、画面点灯、2分につき4回までの制限がある BackGroundだと制限が30分に1回になる
    private val REQUEST_CODE : Int = 1000
    private val TAG ="MainActivity.kt"
    private val permissions = arrayOf(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    )
    @RequiresApi(Build.VERSION_CODES.Q)
    private val permissionsQ = arrayOf(
        android.Manifest.permission.ACCESS_FINE_LOCATION,
        android.Manifest.permission.ACCESS_COARSE_LOCATION,
        android.Manifest.permission.READ_EXTERNAL_STORAGE,
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
        android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
    )
    lateinit var context: Context
    lateinit var wifiManager: WifiManager
    //Wifimanager.WifiInfoの方
    lateinit var conWifi_Info: WifiInfo

    lateinit var wifiScanReceiver:BroadcastReceiver


    lateinit var recyclerView:RecyclerView
    //RecyclerViewのアダプター
    lateinit var mAdapter: CustomAdapter
    var  mWifiList = mutableListOf<Wifi_Info>()

    //オプションメニュー追加
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.option_menu_list,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        val itemId = item.itemId

        when(itemId){
            R.id.option_menu_1 -> {
                val intent = Intent(this,MailSettingActivity::class.java)
                startActivity(intent)
            }
            R.id.option_menu_2 -> {

            }
        }

        return super.onOptionsItemSelected(item)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.d(TAG,"onCreate")
        //画面を常時点灯設定にする。
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        //permissionチェック
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            checkPermission(permissions,REQUEST_CODE)
            checkLogPermission()
        }else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q){
            checkPermission(permissions,REQUEST_CODE)
        }else{
            checkPermission(permissions,REQUEST_CODE)
        }

        //ViewPagerの設定
        val viewPager2 :ViewPager2 = findViewById(R.id.pager)
        val pagerAdapter = SectionPagerAdapter(this)
        viewPager2.adapter = pagerAdapter


        //ログ保存のボタン処理
        val btn_ser_start:Button = findViewById(R.id.ser_start)
        val btn_ser_stop : Button = findViewById(R.id.ser_stop)
     //   val btn_mail_setting: Button = findViewById(R.id.btn_mailSetting)

        val pref = PreferenceManager.getDefaultSharedPreferences(this)
        val checkStatus = pref.getBoolean("checkStatus",false)

        //チェックボックス登録
        val checkBox : CheckBox = findViewById(R.id.timerCheckBox)
        checkBox.isChecked = checkStatus

        checkBox.setOnClickListener {
            pref.edit {
                putBoolean("checkStatus",checkBox.isChecked)
                Log.d(TAG,"CheckStatus:"+checkBox.isChecked)
            }
        }


        btn_ser_start.setOnClickListener {
            val intent = Intent(this,MyService::class.java)
            startForegroundService(intent)
            btn_ser_start.isEnabled = false
            btn_ser_stop.isEnabled = true
        }


        /*
        btn_ser_start.setOnClickListener {
            val getLogData = GetLogData(this)
            val getTimeData = GetTimeData()
            val file = getLogData.getFileStatus(getTimeData.getFileName()+".csv")

            val logData = "11:33:44,75498639717,Buffalo-A-B77E-4,c4:3c:ea:68:24:9e,false,N/A,N/A,[WPA-PSK-TKIP+CCMP][WPA2-PSK-TKIP+CCMP][RSN-PSK-TKIP+CCMP][ESS],-65,5180,80MHz\n"

            for (i in 0..1000003){
                Log.d(TAG,"現在${i}回目")
                getLogData.getLog(file,logData)
            }

        }

         */

        btn_ser_stop.setOnClickListener {
            val intent = Intent(this,MyService::class.java)
            stopService(intent)
            btn_ser_stop.isEnabled = false
            btn_ser_start.isEnabled = true
        }
/*
        btn_mail_setting.setOnClickListener {
            val intent = Intent(this,MailSettingActivity::class.java)
            startActivity(intent)
        }

 */


        context=applicationContext

        wifiScanReceiver = object : BroadcastReceiver() {

            override fun onReceive(context: Context, intent: Intent) {
                Log.d(TAG,"SCAN_RESULTS_AVAILABLE_ACTION Received")
                val success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false)
                if (success) {
                    Log.d(TAG,"wifi scan succeeded")
                } else {
                    Log.d(TAG,"wifi scan failed")
                }
            }
        }

        val intentFilter = IntentFilter()
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        context.registerReceiver(wifiScanReceiver, intentFilter)



        //RecyclerView取得
        recyclerView = findViewById(R.id.recycler_view)
        //境界線の設置
        val dividerItemDecoration = DividerItemDecoration(this, LinearLayoutManager(this).orientation)
        recyclerView.addItemDecoration(dividerItemDecoration)

        recyclerView.layoutManager = LinearLayoutManager(this)
        mAdapter = CustomAdapter(mWifiList)
        recyclerView.adapter = mAdapter



        //アプリ起動時に自動でWifiスキャンをする
        wifiManager=context.getSystemService(Context.WIFI_SERVICE) as WifiManager

        val creScanResult=wifiManager.startScan()

        if (creScanResult){
            Log.d(TAG,"wifi scan 成功")
            scanSuccess()
        }else{
            Log.d(TAG,"wifi scan 失敗")
            firstScanFailure()
        }
/*
        // CustomAdapterの生成と設定
        mAdapter = CustomAdapter(mWifiList)
        recyclerView.adapter = mAdapter

 */


        //Wifiビューの更新ボタン
        val wifi_scan: FloatingActionButton =findViewById(R.id.btn_wifi_scan)
        wifi_scan.setOnClickListener {
            //ボタンの連打防止処理　2秒使えないようにする
            wifi_scan.isEnabled = false
            Handler().postDelayed({ wifi_scan.isEnabled = true }, 2000L)

            if (mySerCheck()){
                Toast.makeText(this,"ログサービス起動中のため、直近のスキャン結果からビューを更新します",Toast.LENGTH_LONG).show()
                Log.d(TAG,"view create from serviceLog")
                scanSuccess()
                return@setOnClickListener
            }




            val scan_judgement=wifiManager.startScan()
            if (scan_judgement){
                //スキャン成功時にトーストで通知
                Toast.makeText(this,"wifi scan 成功", Toast.LENGTH_SHORT).show()
                Log.d(TAG,"wifi scan succeeded")
                scanSuccess()
            } else{
                Toast.makeText(this,"wifi scan 失敗", Toast.LENGTH_SHORT).show()
                Log.d(TAG,"wifi scan failed")
            }
        }





    }



    fun scanSuccess(){
        mWifiList.clear()
        Log.d(TAG,"mWifiList clear")
        val results:List<ScanResult> = wifiManager.scanResults
        conWifi_Info = wifiManager.connectionInfo
        Log.d(TAG,"results size:${results.size}")


        //BSSIDにより、ScanResultの中から、接続しているWifiを特定する
        val bssid_connected:String
        //何も接続していない場合
        if (conWifi_Info.bssid ==null){
            bssid_connected = "null"
            Log.d(TAG,"Do not connect")
            Log.d(TAG,"Connected: ${conWifi_Info.ssid}")
            Log.d(TAG,"linkspeed:${conWifi_Info.linkSpeed}")
        }else{
            //接続していた場合、BSSIDと回線速度をログで表示
            bssid_connected = conWifi_Info.bssid
            Log.d(TAG,"Connected: ${conWifi_Info.ssid}")
            Log.d(TAG,"linkspeed:${conWifi_Info.linkSpeed}")
        }

        for (i in results){
            if (bssid_connected ==i.BSSID){
                mWifiList.add(Wifi_Info(i.SSID,i.BSSID,i.level,i.frequency,R.drawable.wifi_con))
                Log.d(TAG,"mWifiList add")

            }else{
                mWifiList.add(Wifi_Info(i.SSID,i.BSSID,i.level,i.frequency,R.drawable.wifi))
                Log.d(TAG,"mWifiList add")
            }
        }
     //   Log.d(TAG,"mWifiList:$mWifiList")
        // CustomAdapterの生成と設定
        mAdapter = CustomAdapter(mWifiList)
        recyclerView.adapter = mAdapter
    }


    fun firstScanFailure(){
        //テキトーなデータ
        val df_wifiInfo = Wifi_Info("null","null",0,0,R.drawable.wifi_con)
        mWifiList .add(df_wifiInfo)
    //    Log.d(TAG,"mWifiList:$mWifiList")

        // CustomAdapterの生成と設定
        mAdapter = CustomAdapter(mWifiList)
        recyclerView.adapter = mAdapter
    }

    fun mySerCheck():Boolean{
        //ログ保存サービスが起動中かをチェック
        val am:ActivityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val serlist = am.getRunningServices(Integer.MAX_VALUE)
        for (info in serlist) {
            if (MyService::class.java.canonicalName.equals(info.service.getClassName())) {
                Log.d(TAG,"ser running")
                return true
            }
        }
        Log.d(TAG,"ser isn't ruunning")
        return false
    }



    override fun onDestroy() {
        Log.d(TAG,"onDestroy")
        super.onDestroy()
        context.unregisterReceiver(wifiScanReceiver)
    }

    //戻るボタンを押した際にダイアログを表示
    override fun onBackPressed() {
        val dialog = BackPressedDialogFragment()
        dialog.show(supportFragmentManager,"id")

    }


    //Permissionチェックのメソッド
    fun checkPermission(permissions: Array<String>?, request_code: Int) {
        // 許可されていないものだけダイアログが表示される
        ActivityCompat.requestPermissions(this, permissions!!, request_code)
    }

    // requestPermissionsのコールバック
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_CODE -> {
                var i = 0
                while (i < permissions.size) {
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        /*     Toast toast = Toast.makeText(this,
                                "Added Permission: " + permissions[i], Toast.LENGTH_SHORT);
                        toast.show(); */
                    } else {
                        val toast = Toast.makeText(this,
                                "設定より権限をオンにした後、アプリを再起動してください", Toast.LENGTH_LONG)
                        toast.show()
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        //Fragmentの場合はgetContext().getPackageName()
                        val uri = Uri.fromParts("package", packageName, null)
                        intent.data = uri
                        startActivity(intent)
                    }
                    i++
                }
            }
            else -> {
            }
        }
    }
    @TargetApi(Build.VERSION_CODES.R)
    private fun checkLogPermission(){
        if (Environment.isExternalStorageManager()){
            //todo when permission is granted
            Log.d(TAG,"MANAGE_EXTERNAL_STORAGE is Granted")
        }else{
            //request for the permission
            val logIntent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
            val uri = Uri.fromParts("package",packageName,null)
            logIntent.data = uri
            startActivity(logIntent)
        }
    }

}