package io.nava.qobuz_test

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Prefs.hasCredentials(this)) {
            startMain()
            return
        }

        setContentView(R.layout.activity_login)

        val etAppId     = findViewById<EditText>(R.id.etAppId)
        val etAppSecret = findViewById<EditText>(R.id.etAppSecret)
        val etUserId    = findViewById<EditText>(R.id.etUserId)
        val etAuthToken = findViewById<EditText>(R.id.etAuthToken)
        val btnLogin    = findViewById<Button>(R.id.btnLogin)

        btnLogin.setOnClickListener {
            val appId     = etAppId.text.toString().trim()
            val appSecret = etAppSecret.text.toString().trim()
            val userId    = etUserId.text.toString().trim()
            val authToken = etAuthToken.text.toString().trim()

            if (appId.isEmpty() || appSecret.isEmpty() || userId.isEmpty() || authToken.isEmpty()) {
                Toast.makeText(this, "All fields required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Prefs.saveCredentials(this, appId, appSecret, userId, authToken)
            startMain()
        }
    }

    private fun startMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
