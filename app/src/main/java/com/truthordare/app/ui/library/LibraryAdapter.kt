package com.truthordare.app.ui.library

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.truthordare.app.data.model.CardLibrary
import com.truthordare.app.databinding.ItemLibraryBinding

class LibraryAdapter(
    private val onSelect: (CardLibrary) -> Unit,
    private val onLongPress: (CardLibrary) -> Unit
) : ListAdapter<CardLibrary, LibraryAdapter.VH>(DIFF) {

    private var selectedId: Long = -1L

    inner class VH(val binding: ItemLibraryBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(ItemLibraryBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = getItem(position)
        with(holder.binding) {
            tvEmoji.text = item.emoji
            tvName.text = item.name
            tvDesc.text = item.description
            tvCardCount.text = ""
            ivLock.visibility = if (item.isLocked) android.view.View.VISIBLE else android.view.View.GONE
            ivDefault.visibility = if (item.isUserDefault) android.view.View.VISIBLE else android.view.View.GONE
            root.isSelected = item.id == selectedId
            root.setOnClickListener {
                selectedId = item.id
                notifyDataSetChanged()
                onSelect(item)
            }
            root.setOnLongClickListener {
                onLongPress(item)
                true
            }
        }
    }

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<CardLibrary>() {
            override fun areItemsTheSame(a: CardLibrary, b: CardLibrary) = a.id == b.id
            override fun areContentsTheSame(a: CardLibrary, b: CardLibrary) = a == b
        }
    }
}
