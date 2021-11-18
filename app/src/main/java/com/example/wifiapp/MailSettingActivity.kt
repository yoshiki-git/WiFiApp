package com.example.wifiapp

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import androidx.security.crypto.MasterKeys
import com.example.wifiapp.databinding.ActivityMailSettingBinding
import java.lang.Exception
import java.net.URLDecoder

class MailSettingActivity : AppCompatActivity() {

    val TAG = "メールアクティビティ"
    val READ_REQUEST_CODE:Int = 9999

    companion object{
        const val PREF_NAME = "encrypted_prefs"
    }

    private lateinit var binding: ActivityMailSettingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMailSettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mailKey = MasterKey.Builder(applicationContext)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        val pref = PreferenceManager.getDefaultSharedPreferences(this)

        //Android9と10だとクラッシュする
/*
        //EncryptedSharedPreferenceを使用する　セキュリティの観点のため
        val pref = EncryptedSharedPreferences.create(
            applicationContext,
            PREF_NAME,
            mailKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

 */

        //前回保存した値を読み出して表示する
        val motoadd = pref.getString("MOTOADD","non")
        val motopass = pref.getString("PASS","non")
        val ateadd = pref.getString("ATEADD","non")
        val interval = pref.getInt("INTERVAL",15)
        val filePath = pref.getString("FILEPATH","添付ファイルのパスがここに表示される")
        val fileName = pref.getString("FILENAME","ファイル名がここに表示される")

        binding.editMotoAdress.setText(motoadd)
        binding.editMotoPass.setText(motopass)
        binding.editAtesakiAddress.setText(ateadd)
        binding.editSendInterval.setText(interval.toString())
        binding.editFilepath.setText(filePath)
        binding.editFileName.setText(fileName)



        binding.btnSearchFile.setOnClickListener {
            //添付ファイルを検索し、パスを保存する
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                setType("*/*")
            }
            startActivityForResult(intent,READ_REQUEST_CODE)
        }

        binding.btnSaveInputs.setOnClickListener {
           val i = Integer.parseInt(binding.editSendInterval.text.toString())
            //入力値をプリファレンスに保存する
            pref.edit {
                putString("MOTOADD",binding.editMotoAdress.text.toString())
                putString("PASS",binding.editMotoPass.text.toString())
                putString("ATEADD",binding.editAtesakiAddress.text.toString())
                putInt("INTERVAL",i)
                putString("FILEPATH",binding.editFilepath.text.toString())
                putString("FILENAME",binding.editFileName.text.toString())
            }
            Toast.makeText(this,"入力値を保存しました",Toast.LENGTH_SHORT).show()
        }



        binding.btnSendOnce.setOnClickListener {
            //1回だけメール送信
            val motoadd1 = binding.editMotoAdress.text.toString()
            val motopass1 = binding.editMotoPass.text.toString()
            val ateadd1 = binding.editAtesakiAddress.text.toString()
            val filePath1 = binding.editFilepath.text.toString()
            val fileName1 = binding.editFileName.text.toString()
            val sendMailService = SendMailService(motoadd1,motopass1,ateadd1,filePath1,fileName1)
            sendMailService.sendOnce(applicationContext)
        }

        binding.btnSendRepeat.setOnClickListener {
            //指定間隔メール送信
            val intent = Intent(this,RepeatMailService::class.java)
            startForegroundService(intent)
        }
        binding.btnSendStop.setOnClickListener {
            //指定間隔にメール送信を停止する
            val intent = Intent(this,RepeatMailService::class.java)
            stopService(intent)
        }

    }

    //ファイルの検索結果を処理する
    @SuppressLint("sdCardPath")
    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)
        Log.d(TAG,"onActivityResult")
        if(requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK){


            //URIからファイル名を取得して、ビューに反映
            resultData?.data?.let { returnUri ->
                contentResolver.query(returnUri,null,null,null,null)
            }?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                cursor.moveToFirst()
                Log.d(TAG,cursor.getString(nameIndex))
                binding.editFileName.setText(cursor.getString(nameIndex))
            }

            //ファイルパス取得
            val filePath = resultData?.dataString

            //パスをUTF-8にデコード
            val decodedPath = URLDecoder.decode(filePath,"utf-8")
            Log.d(TAG,decodedPath)

            try {
                val resultPass =decodedPath.replace("content://com.android.externalstorage.documents/document/primary:",
                "/sdcard/")
                binding.editFilepath.setText(resultPass)
                Log.d(TAG,resultPass)
            }catch (e:Exception){
                e.printStackTrace()
                Log.d(TAG,"Exception発生:$e")
                binding.editFilepath.setText(decodedPath)
            }
        }
    }


}