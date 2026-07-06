package com.truthordare.app.ui.library

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.truthordare.app.R
import com.truthordare.app.data.model.Card
import com.truthordare.app.data.model.CardType
import com.truthordare.app.databinding.ItemCardBinding

class CardAdapter(
    private val onEdit: (Card) -> Unit,
    private val onDelete: (Card) -> Unit
) : ListAdapter<Card, CardAdapter.VH>(DIFF) {

    inner class VH(val binding: ItemCardBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(ItemCardBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val card = getItem(position)
        with(holder.binding) {
            tvContent.text = card.content
            tvType.text = card.type.label
            tvLevel.text = "Lv.${card.level}"
            tvTag.text = card.tag.label
            val colorRes = when (card.type) {
                CardType.TRUTH -> R.color.truth_color
                CardType.DARE -> R.color.dare_color
                CardType.PROP -> R.color.prop_color
            }
            tvType.setTextColor(root.context.getColor(colorRes))
            btnEdit.setOnClickListener { onEdit(card) }
            btnDelete.setOnClickListener { onDelete(card) }
        }
    }

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<Card>() {
            override fun areItemsTheSame(a: Card, b: Card) = a.id == b.id
            override fun areContentsTheSame(a: Card, b: Card) = a == b
        }
    }
}
