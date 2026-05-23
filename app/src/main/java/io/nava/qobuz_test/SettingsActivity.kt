package io.nava.qobuz_test

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {

    private val qualityKeys    = listOf("mp3", "flac", "flac-hi", "flac-ultra")
    private val qualityLabels  = listOf("MP3 320", "FLAC Lossless", "FLAC Hi-Res", "FLAC Ultra Hi-Res")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val spinnerQuality = findViewById<Spinner>(R.id.spinnerQuality)
        val etDir          = findViewById<EditText>(R.id.etDownloadDir)
        val btnSave        = findViewById<Button>(R.id.btnSave)
        val btnLogout      = findViewById<Button>(R.id.btnLogout)

        spinnerQuality.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, qualityLabels)

        val currentQuality = Prefs.quality(this)
        spinnerQuality.setSelection(qualityKeys.indexOf(currentQuality).coerceAtLeast(0))
        etDir.setText(Prefs.downloadDir(this))

        btnSave.setOnClickListener {
            val quality = qualityKeys[spinnerQuality.selectedItemPosition]
            val dir     = etDir.text.toString().trim().ifBlank { "/sdcard/Music/Qobuz" }
            Prefs.saveSettings(this, quality, dir)
            Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show()
        }

        btnLogout.setOnClickListener {
            Prefs.saveCredentials(this, "", "", "", "")
            Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
