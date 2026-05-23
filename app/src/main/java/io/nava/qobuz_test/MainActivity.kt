package io.nava.qobuz_test

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "QobuzApp"
    }

    private val executor = Executors.newSingleThreadExecutor()

    private lateinit var searchView: SearchView
    private lateinit var spinner: Spinner
    private lateinit var recycler: RecyclerView
    private lateinit var progress: ProgressBar
    private lateinit var tvStatus: TextView

    private val searchTypes = listOf("albums", "artists", "tracks")
    private val searchLabels = listOf("Albums", "Artists", "Tracks")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        searchView = findViewById(R.id.searchView)
        spinner    = findViewById(R.id.spinnerType)
        recycler   = findViewById(R.id.recycler)
        progress   = findViewById(R.id.progress)
        tvStatus   = findViewById(R.id.tvStatus)

        spinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, searchLabels)
        recycler.layoutManager = LinearLayoutManager(this)

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.takeIf { it.isNotBlank() }?.let { doSearch(it) }
                return true
            }
            override fun onQueryTextChange(newText: String?) = false
        })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_settings) {
            startActivity(Intent(this, SettingsActivity::class.java))
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun doSearch(query: String) {
        val typeIndex = spinner.selectedItemPosition
        val searchType = searchTypes[typeIndex]

        progress.visibility = View.VISIBLE
        tvStatus.text = "Searching…"
        recycler.adapter = null
        Log.i(TAG, "search: \"$query\" [$searchType]")

        executor.submit {
            val result = QobuzNative.search(
                Prefs.appId(this), Prefs.appSecret(this),
                Prefs.userId(this), Prefs.authToken(this),
                query, searchType,
            )
            runOnUiThread {
                progress.visibility = View.GONE
                result.fold(
                    onSuccess = { items ->
                        Log.i(TAG, "search: ${items.size} results")
                        tvStatus.text = "${items.size} result(s)"
                        recycler.adapter = SearchAdapter(items) { item ->
                            doDownload(item, searchType)
                        }
                    },
                    onFailure = { e ->
                        Log.i(TAG, "search error: ${e.message}")
                        tvStatus.text = "Error: ${e.message}"
                    }
                )
            }
        }
    }

    private fun doDownload(item: SearchResult, searchType: String) {
        val itemType = if (searchType == "tracks") "track" else "album"
        val outDir   = Prefs.downloadDir(this)
        val quality  = Prefs.quality(this)

        File(outDir).mkdirs()

        tvStatus.text = "Downloading \"${item.title}\"…"
        progress.visibility = View.VISIBLE
        Log.i(TAG, "download: id=${item.id} type=$itemType quality=$quality")

        executor.submit {
            val result = QobuzNative.download(
                Prefs.appId(this), Prefs.appSecret(this),
                Prefs.userId(this), Prefs.authToken(this),
                item.id, itemType, outDir, quality,
            )
            runOnUiThread {
                progress.visibility = View.GONE
                result.fold(
                    onSuccess = {
                        Log.i(TAG, "download ok: ${item.title}")
                        tvStatus.text = "Downloaded \"${item.title}\""
                    },
                    onFailure = { e ->
                        Log.i(TAG, "download error: ${e.message}")
                        tvStatus.text = "Error: ${e.message}"
                    }
                )
            }
        }
    }
}
