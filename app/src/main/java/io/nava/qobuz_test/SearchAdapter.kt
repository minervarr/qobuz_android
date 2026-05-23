package io.nava.qobuz_test

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SearchAdapter(
    private val items: List<SearchResult>,
    private val onDownload: (SearchResult) -> Unit,
) : RecyclerView.Adapter<SearchAdapter.VH>() {

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle:    TextView = view.findViewById(R.id.tvTitle)
        val tvSubtitle: TextView = view.findViewById(R.id.tvSubtitle)
        val btnDownload: Button  = view.findViewById(R.id.btnDownload)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(LayoutInflater.from(parent.context).inflate(R.layout.item_search_result, parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        holder.tvTitle.text    = item.title
        val sub = listOf(item.artist, item.year).filter { it.isNotBlank() }.joinToString(" · ")
        holder.tvSubtitle.text = sub
        holder.btnDownload.setOnClickListener { onDownload(item) }
    }

    override fun getItemCount() = items.size
}
