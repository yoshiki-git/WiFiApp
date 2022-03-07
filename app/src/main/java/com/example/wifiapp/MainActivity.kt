package com.example.wifiapp

import android.annotation.TargetApi
import android.content.*
import android.content.pm.PackageManager
import android.net.Uri
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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import androidx.viewpager2.widget.ViewPager2
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
    //BACKGROUND_LOCATIONがうまく機能してないので現状使っていない
    @RequiresApi(Build.VERSION_CODES.Q)
    private val permissionsQ = arrayOf(
        android.Manifest.permission.ACCESS_FINE_LOCATION,
        android.Manifest.permission.ACCESS_COARSE_LOCATION,
        android.Manifest.permission.READ_EXTERNAL_STORAGE,
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
        android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
    )
    @RequiresApi(Build.VERSION_CODES.S)
    private val permissionsS = arrayOf(
        android.Manifest.permission.ACCESS_FINE_LOCATION,
        android.Manifest.permission.ACCESS_COARSE_LOCATION,
        android.Manifest.permission.READ_EXTERNAL_STORAGE,
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
        android.Manifest.permission.BLUETOOTH_SCAN,
        android.Manifest.permission.BLUETOOTH_CONNECT
    )
    lateinit var context: Context

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
            R.id.option_menu_3->{
                val intent = Intent(this,LogItemSettingActivity::class.java)
                startActivity(intent)
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
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
          checkPermission(permissionsS,REQUEST_CODE)
          checkLogPermission()
        } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.R){
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
    }


    override fun onDestroy() {
        Log.d(TAG,"onDestroy")
        super.onDestroy()
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
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
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