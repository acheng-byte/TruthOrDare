package com.truthordare.app.ui.game

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.truthordare.app.data.model.CardLibrary
import com.truthordare.app.databinding.ItemLibraryChipBinding

class LibraryChipAdapter(
    private val onSelect: (CardLibrary) -> Unit,
    private val onUnlockRequest: (CardLibrary) -> Unit = {}
) : ListAdapter<CardLibrary, LibraryChipAdapter.VH>(DIFF) {

    private var selectedId: Long = -1L

    inner class VH(val binding: ItemLibraryChipBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemLibraryChipBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = getItem(position)
        with(holder.binding) {
            tvEmoji.text = item.emoji
            tvName.text = item.name
            val isSelected = item.id == selectedId
            root.isSelected = isSelected
            root.strokeWidth = if (isSelected) 3 else 1
            root.setStrokeColor(
                android.content.res.ColorStateList.valueOf(
                    if (isSelected)
                        root.context.getColor(com.truthordare.app.R.color.secondary)
                    else
                        root.context.getColor(com.truthordare.app.R.color.divider)
                )
            )
            root.alpha = if (item.isLocked && !isSelected) 0.6f else 1f

            root.setOnClickListener {
                if (item.isLocked) {
                    onUnlockRequest(item)
                    return@setOnClickListener
                }
                selectedId = item.id
                notifyDataSetChanged()
                onSelect(item)
            }
        }
    }

    fun setSelected(id: Long) {
        selectedId = id
        notifyDataSetChanged()
    }

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<CardLibrary>() {
            override fun areItemsTheSame(a: CardLibrary, b: CardLibrary) = a.id == b.id
            override fun areContentsTheSame(a: CardLibrary, b: CardLibrary) = a == b
        }
    }
}
