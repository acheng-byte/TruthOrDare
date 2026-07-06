package com.truthordare.app.ui.history

import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.truthordare.app.databinding.FragmentHistoryBinding
import kotlinx.coroutines.launch

class HistoryFragment : Fragment() {
    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HistoryViewModel by viewModels()
    private lateinit var adapter: SessionAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupSearch()
        setupClearButton()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        adapter = SessionAdapter(
            onDelete = { session ->
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("删除记录")
                    .setMessage("确定删除「${session.memo.ifEmpty { session.libraryName }}」的记录？")
                    .setPositiveButton("删除") { _, _ -> viewModel.deleteSession(session) }
                    .setNegativeButton("取消", null).show()
            },
            onExpand = { session ->
                lifecycleScope.launch {
                    val records = viewModel.getRecords(session.id)
                    val text = if (records.isEmpty()) "暂无抽卡记录" else
                        records.joinToString("\n") { "• [${it.type}] ${it.content}" }
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle("${session.libraryName} · 抽卡流水（${records.size} 条）")
                        .setMessage(text)
                        .setPositiveButton("关闭", null).show()
                }
            }
        )
        binding.rvSessions.apply {
            adapter = this@HistoryFragment.adapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupSearch() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(q: String?) = true.also { viewModel.search(q ?: "") }
            override fun onQueryTextChange(q: String?) = true.also { viewModel.search(q ?: "") }
        })
    }

    private fun setupClearButton() {
        binding.fabClearAll.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("清空全部历史")
                .setMessage("确定删除所有已保存的对局记录？")
                .setPositiveButton("清空") { _, _ -> viewModel.clearAllHistory() }
                .setNegativeButton("取消", null).show()
        }
    }

    private fun observeViewModel() {
        viewModel.filteredSessions.observe(viewLifecycleOwner) { sessions ->
            adapter.submitList(sessions)
            binding.tvEmpty.visibility = if (sessions.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
