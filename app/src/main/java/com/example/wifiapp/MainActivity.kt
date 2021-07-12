package com.example.wifiapp

import android.annotation.TargetApi
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {
    //Wifiスキャン条件
    //Wifiオン、位置情報オン、画面点灯、2分につき4回までの制限がある
    private val REQUEST_CODE : Int = 1000
    private val TAG ="TestApp"
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

    //RecyclerViewのアダプター
    lateinit var mAdapter: CustomAdapter
    lateinit var  mWifiList: ArrayList<Wifi_Info>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //画面を常時点灯設定にする。
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        //permissionチェック
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            checkPermission(permissionsQ,REQUEST_CODE)
            checkLogPermission()
        }else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q){
            checkPermission(permissionsQ,REQUEST_CODE)
        }else{
            checkPermission(permissions,REQUEST_CODE)
        }


        context=applicationContext



        //アクティビティ上にWifiを表示するボタン
        val wifi_scan: Button =findViewById(R.id.btn_wifi_scan)


        val btn_ser_start:Button = findViewById(R.id.ser_start)
        val btn_ser_stop : Button = findViewById(R.id.ser_stop)

        btn_ser_start.setOnClickListener {
            val intent = Intent(this,MyService::class.java)
            startForegroundService(intent)
            btn_ser_start.isEnabled = false
            btn_ser_stop.isEnabled = true
        }
        btn_ser_stop.setOnClickListener {
            val intent = Intent(this,MyService::class.java)
            stopService(intent)
            btn_ser_stop.isEnabled = false
            btn_ser_start.isEnabled = true
        }

        //テキトーなデータ
        val df_wifiInfo = Wifi_Info("null","null",0,0,R.drawable.wifi_con)
        mWifiList = arrayListOf(df_wifiInfo)

        //RecyclerView取得
        val recyclerView = findViewById<RecyclerView>(R.id.recycler_view)

        //境界線の設置
        val dividerItemDecoration = DividerItemDecoration(this, LinearLayoutManager(this).orientation)
        recyclerView.addItemDecoration(dividerItemDecoration)

        recyclerView.layoutManager = LinearLayoutManager(this)

        // CustomAdapterの生成と設定
        mAdapter = CustomAdapter(mWifiList)
        recyclerView.adapter = mAdapter
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
            val logIntent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
            val uri = Uri.fromParts("package",packageName,null)
            logIntent.data = uri
            startActivity(logIntent)
        }
    }

}