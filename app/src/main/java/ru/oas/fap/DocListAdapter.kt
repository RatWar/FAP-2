package ru.oas.fap

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ru.oas.fap.room.DocumentData

class DocListAdapter internal constructor(context: Context): RecyclerView.Adapter<DocListAdapter.DocViewHolder>() {

    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private var docs = emptyList<DocumentData>()

    private var onDocClickListener: OnDocClickListener? = null

    interface OnDocClickListener {
        fun onDocClick(docs: DocumentData, del: Boolean)
    }

    fun docAdapter(onDocClickListener: OnDocClickListener) {
        this.onDocClickListener = onDocClickListener
    }

    inner class DocViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val docItemView: TextView = itemView.findViewById(R.id.textView)
    }

    internal fun setDocs(docData: List<DocumentData>) {
        this.docs = docData
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DocViewHolder {
        val itemView = inflater.inflate(R.layout.doclist_view_item, parent, false)
        return DocViewHolder(itemView)
    }

    override fun getItemCount(): Int  = docs.size

    override fun onBindViewHolder(holder: DocViewHolder, position: Int) {
        val current = docs[position]
        holder.docItemView.text = current.dateTime

        holder.docItemView.setOnClickListener {
            val doc = docs[position]
            onDocClickListener?.onDocClick(doc, false)
        }

    }
}