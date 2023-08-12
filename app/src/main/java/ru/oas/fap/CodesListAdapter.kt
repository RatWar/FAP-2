package ru.oas.fap

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ru.oas.fap.room.CodesData

class CodesListAdapter internal constructor(context: Context): RecyclerView.Adapter<CodesListAdapter.CodesViewHolder>() {

    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private var codes = emptyList<CodesData>()

    private var onCodesClickListener: OnCodesClickListener? = null

    interface OnCodesClickListener {
        fun onCodesClick(codes: CodesData)
    }

    fun codesAdapter(onCodesClickListener: OnCodesClickListener) {
        this.onCodesClickListener = onCodesClickListener
    }

    inner class CodesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val codesItemView: TextView = itemView.findViewById(R.id.textView)
        val codesItemDelete: ImageView = itemView.findViewById(R.id.imageView)
    }

    internal fun setCodes(codesData: List<CodesData>) {
        this.codes = codesData
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CodesViewHolder {
        val itemView = inflater.inflate(R.layout.codeslist_view_item, parent, false)
        return CodesViewHolder(itemView)
    }

    override fun getItemCount(): Int  = codes.size

    override fun onBindViewHolder(holder: CodesViewHolder, position: Int) {
        val current = codes[position]
        holder.codesItemView.text = current.sgtin

        holder.codesItemView.setOnClickListener {
            val cod = codes[position]
            onCodesClickListener?.onCodesClick(cod)
        }

        holder.codesItemDelete.setOnClickListener {
            val cod = codes[position]
            onCodesClickListener?.onCodesClick(cod)
        }
    }

}