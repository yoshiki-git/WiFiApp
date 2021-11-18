package com.example.wifiapp

import android.content.Context
import android.widget.Toast
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

class SendMailService(val fromUsername:String,val password:String,val toAddress:String,val filePath:String,val fileName:String) {
    fun sendOnce(context:Context){
        thread {
            val property = Properties()
            //SMTPを用いる IMAPとPOP3もいけるらしい
            property.put("mail.smtp.host", "smtp.gmail.com") //メールホストのサーバー　今回はgmailにしている
            property.put("mail.smtp.auth", "true") //AUTHコマンドを使用してユーザ認証を試みる
            property.put("mail.smtp.starttls.enable", "true") //TLSで通信？
            property.put("mail.smtp.port", "587") //接続するSMTPサーバーポート
            property.put("mail.smtp.debug", "true")

            val message: Message = MimeMessage(Session.getDefaultInstance(property, object: Authenticator() {
                override fun getPasswordAuthentication(): PasswordAuthentication {
                    //送信元アドレスとパスワードを引数に
                    return PasswordAuthentication(fromUsername, password)
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

            //送信元を引数に
            message.setFrom(InternetAddress(fromUsername, "Fromの表示名です"))
            //送信先を引数に
            message.setRecipient(Message.RecipientType.TO, InternetAddress(toAddress, "Toの表示名です"))
            message.subject = "Wifiログアプリ自動送信メール"
       //     message.setText("こんにちは")
            message.setContent(mp)
            Transport.send(message)
        }
    }
}