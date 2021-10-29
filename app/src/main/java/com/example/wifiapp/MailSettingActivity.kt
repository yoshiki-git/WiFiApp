package com.example.wifiapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.preference.PreferenceManager
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.wifiapp.databinding.ActivityMailSettingBinding

class MailSettingActivity : AppCompatActivity() {

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

        val pref = EncryptedSharedPreferences.create(
            applicationContext,
            PREF_NAME,
            mailKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        val motoadd = pref.getString("MOTOADD","non")
        val motopass = pref.getString("PASS","non")
        val ateadd = pref.getString("ATEADD","non")
        val interval = pref.getInt("INTERVAL",15)

        binding.editMotoAdress.setText(motoadd)
        binding.editMotoPass.setText(motopass)
        binding.editAtesakiAddress.setText(ateadd)
        binding.editSendInterval.setText(interval.toString())

        val butn:Button = findViewById(R.id.btn_save_inputs)




    }


}