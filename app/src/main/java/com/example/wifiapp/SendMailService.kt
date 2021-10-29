package com.example.wifiapp

import java.util.*
import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage
import kotlin.concurrent.thread

class SendMailService {
    fun send(){
        thread {
            val property = Properties()
            property.put("mail.smtp.host", "smtp.gmail.com")
            property.put("mail.smtp.auth", "true")
            property.put("mail.smtp.starttls.enable", "true")
            property.put("mail.smtp.port", "587")
            property.put("mail.smtp.debug", "true")

            val message: Message = MimeMessage(Session.getDefaultInstance(property, object: Authenticator() {
                override fun getPasswordAuthentication(): PasswordAuthentication {
                    return PasswordAuthentication("ntct08013950543@gmail.com", "Yoshiki32@ntct")
                }
            }))

            message.setFrom(InternetAddress("judging5@gmail.com", "[表示名]"))
            message.setRecipient(Message.RecipientType.TO, InternetAddress("yonshiki0425@yahoo.co.jp", "[表示名]"))
            message.subject = "タイトル"
            message.setText("こんにちは")
            Transport.send(message)
        }
    }
}