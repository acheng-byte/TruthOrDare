package com.truthordare.app.ui.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.truthordare.app.data.model.GameSession
import com.truthordare.app.databinding.ItemSessionBinding
import java.text.SimpleDateFormat
import java.util.*

class SessionAdapter(
    private val onDelete: (GameSession) -> Unit,
    private val onExpand: (GameSession) -> Unit
) : ListAdapter<GameSession, SessionAdapter.VH>(DIFF) {

    private val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

    inner class VH(val binding: ItemSessionBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(ItemSessionBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val session = getItem(position)
        with(holder.binding) {
            tvLibraryName.text = session.libraryName
            tvMemo.text = session.memo.ifEmpty { "无备注" }
            tvStats.text = "抽了 ${session.totalDraws} 次 · 最高 Lv.${session.maxLevel}"
            tvDate.text = sdf.format(Date(session.savedAt))
            btnDelete.setOnClickListener { onDelete(session) }
            root.setOnClickListener { onExpand(session) }
        }
    }

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<GameSession>() {
            override fun areItemsTheSame(a: GameSession, b: GameSession) = a.id == b.id
            override fun areContentsTheSame(a: GameSession, b: GameSession) = a == b
        }
    }
}
