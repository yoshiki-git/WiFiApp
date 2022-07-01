package com.example.wifiapp

import android.content.Context
import android.os.AsyncTask
import android.util.Log
import android.widget.Toast
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import java.io.File
import java.lang.Exception
import java.util.*
import javax.activation.DataHandler
import javax.activation.FileDataSource
import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMessage
import javax.mail.internet.MimeMultipart
import kotlin.concurrent.thread


/*
受信メール（IMAP）サーバー	imap.mail.yahoo.co.jp
受信メール（IMAP）通信方法	SSL
受信メール（IMAP）ポート番号	993
送信メール（SMTP）サーバー	smtp.mail.yahoo.co.jp
送信メール（SMTP）認証方式	SMTP_AUTH
送信メール（SMTP）通信方法	SSL
送信メール（SMTP）ポート番号	465
アカウント名/ログイン名	Yahoo! JAPAN ID
※ログイン時にシークレットIDを使用する設定にしている場合は、シークレットIDを入力してください。
メールアドレス	zrnuruwf@yahoo.co.jp
パスワード	Yoshiki32@ntct
 */

class SendMailService(val fromUsername:String,val password:String,val toAddress:String,val filePath:String,val fileName:String){
    private val TAG = "SendMailService.kt"

    @Suppress("BlockingMethodInNonBlockingContext")
    fun sendOnce():Deferred<String?> = GlobalScope.async {
        Log.d(TAG,fromUsername)
        Log.d(TAG,password)
        Log.d(TAG,toAddress)
        Log.d(TAG,filePath)
        Log.d(TAG,fileName)

        //YahooMail
        //zrnuruwf@yahoo.co.jp
        //Yoshiki32@ntct


        val property = Properties()
        //SMTPを用いる IMAPとPOP3もいけるらしい
        //Yahooメール用
        property.put("mail.smtp.host", "smtp.mail.yahoo.co.jp") //メールホストのサーバ
        property.put("mail.smtp.auth", "true") //AUTHコマンドを使用してユーザ認証を試みる
        property.put("mail.transport.protocol","smtp")
        property.put("mail.smtp.ssl.enable","true") //SSLで通信するときにいる
        property.put("mail.smtp.port", "465") //接続するSMTPサーバーポート 465? 587?
        property.put("mail.smtp.debug", "true")

        /*
        Gmail 2022.5.30より使用不可になった
        property.put("mail.smtp.host", "smtp.gmail.com") //メールホストのサーバー　今回はgmailにしている
        property.put("mail.smtp.auth", "true") //AUTHコマンドを使用してユーザ認証を試みる
        property.put("mail.smtp.starttls.enable", "true") //TLSで通信？
        property.put("mail.smtp.port", "587") //接続するSMTPサーバーポート
        property.put("mail.smtp.debug", "true")

         */

        val message: Message = MimeMessage(Session.getDefaultInstance(property, object: Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication {
                //送信元アドレスとパスワードを引数に
                return PasswordAuthentication("zrnuruwf@yahoo.co.jp", "Yoshiki32@ntct")
               // return PasswordAuthentication(fromUsername, password)
            }
        }))

        //メール本文の追加
        val txtPart = MimeBodyPart()
        txtPart.setText("メール本文","utf-8")

        //添付ファイルの追加
        val filePart = MimeBodyPart()
        val file = File(filePath)
        val fds = FileDataSource(file)
        val data = DataHandler(fds)
        filePart.dataHandler = data

        filePart.fileName = fileName

        val mp = MimeMultipart()
        mp.addBodyPart(txtPart)
        mp.addBodyPart(filePart)

        try{
            //送信元を引数に
            message.setFrom(InternetAddress("zrnuruwf@yahoo.co.jp", "Fromの表示名です"))
            //送信先を引数に
            message.setRecipient(Message.RecipientType.TO, InternetAddress(toAddress, "Toの表示名です"))
            message.subject = "Wifiログアプリ自動送信メール"
            //     message.setText("こんにちは")
            message.setContent(mp)
            Transport.send(message)
            Log.d(TAG,"送信成功")
            return@async "送信成功"
        }catch(e:Exception){
            e.printStackTrace()
            Log.d(TAG,"送信失敗")
            Log.d(TAG,e.toString())
            return@async "送信失敗"
        }

    }



}