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
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

class MainActivity : AppCompatActivity() {


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
    lateinit var wifiManager:WifiManager
    lateinit var context: Context


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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
        wifiManager=context.getSystemService(Context.WIFI_SERVICE) as WifiManager

        val wifiScanReceiver = object : BroadcastReceiver(){

            override fun onReceive(context: Context, intent: Intent) {
                val success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false)
                if (success) {
                    scanSuccess()
                } else {
                    scanFailure()
                }
            }

        }

        val intentFilter = IntentFilter()
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        context.registerReceiver(wifiScanReceiver, intentFilter)

        val success = wifiManager.startScan()
        if (!success) {
            // scan failure handling
            scanFailure()
        }



        val wifi_scan: Button =findViewById(R.id.btn_wifi_scan)
        wifi_scan.setOnClickListener {
            val success = wifiManager.startScan()
            if (success) {
                // scan failure handling
                scanSuccess()
            }else{
                scanFailure()
            }
        }

        val wifi_result:TextView=findViewById(R.id.tv_wifi_result)


    }

    fun scanSuccess() {
        val results = wifiManager.scanResults
        Toast.makeText(this,"Scanしたよ",Toast.LENGTH_SHORT).show()
        Log.d(TAG,results.toString())
    }

    fun scanFailure() {
        // handle failure: new scan did NOT succeed
        // consider using old scan results: these are the OLD results!
        val results = wifiManager.scanResults
        Toast.makeText(this,"WifiScanが失敗しました",Toast.LENGTH_SHORT).show()
        Log.d(TAG,results.toString())

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