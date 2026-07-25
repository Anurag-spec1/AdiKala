package com.devshiv.adikala.view.adapter



import com.devshiv.adikala.model.SortOption
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.devshiv.adikala.databinding.ItemSortOptionBinding

class SortAdapter(
    private val selected: SortOption,
    private val onPick: (SortOption) -> Unit
) : RecyclerView.Adapter<SortAdapter.VH>() {

    private val options = SortOption.values()

    inner class VH(val b: ItemSortOptionBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(opt: SortOption) {
            b.tvSortLabel.text = opt.label
            val isSel = opt == selected
            b.ivCheck.visibility = if (isSel) View.VISIBLE else View.INVISIBLE
            b.tvSortLabel.setTextColor(
                Color.parseColor(if (isSel) "#0D3238" else "#5C5C5C")
            )
            b.rowSort.setOnClickListener { onPick(opt) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemSortOptionBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(options[position])

    override fun getItemCount() = options.size
}