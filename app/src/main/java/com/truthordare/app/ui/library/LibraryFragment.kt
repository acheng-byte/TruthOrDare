package com.truthordare.app.ui.library

import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.truthordare.app.R
import com.truthordare.app.data.model.*
import com.truthordare.app.databinding.FragmentLibraryBinding
import kotlinx.coroutines.launch

class LibraryFragment : Fragment() {
    private var _binding: FragmentLibraryBinding? = null
    private val binding get() = _binding!!
    private val viewModel: LibraryViewModel by viewModels()
    private lateinit var libraryAdapter: LibraryAdapter
    private lateinit var cardAdapter: CardAdapter

    private val importFileLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { readAndImportFile(it) }
    }

    private val exportFileLauncher = registerForActivityResult(ActivityResultContracts.CreateDocument("text/plain")) { uri ->
        uri?.let { writeExportFile(it) }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLibraryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupLibraryList()
        setupCardList()
        setupFab()
        observeViewModel()
    }

    private fun setupLibraryList() {
        libraryAdapter = LibraryAdapter(
            onSelect = { viewModel.selectLibrary(it.id) },
            onLongPress = { showLibraryOptions(it) }
        )
        binding.rvLibraries.apply {
            adapter = libraryAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupCardList() {
        cardAdapter = CardAdapter(
            onEdit = { showEditCardDialog(it) },
            onDelete = { viewModel.deleteCard(it) }
        )
        binding.rvCards.apply {
            adapter = cardAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupFab() {
        binding.fabNewLibrary.setOnClickListener { showCreateLibraryDialog() }
        binding.fabAddCard.setOnClickListener { showAddCardDialog() }
        binding.btnImport.setOnClickListener { showImportOptions() }
        binding.btnExport.setOnClickListener { exportCurrentLibrary() }
    }

    private fun observeViewModel() {
        viewModel.libraries.observe(viewLifecycleOwner) { libs ->
            libraryAdapter.submitList(libs)
            binding.tvEmptyLibrary.visibility = if (libs.isEmpty()) View.VISIBLE else View.GONE
        }
        viewModel.cardsForSelected.observe(viewLifecycleOwner) { cards ->
            cardAdapter.submitList(cards)
            binding.tvCardCount.text = "共 ${cards.size} 张"
            binding.tvEmptyCards.visibility = if (cards.isEmpty()) View.VISIBLE else View.GONE
        }
        viewModel.selectedLibraryId.observe(viewLifecycleOwner) { id ->
            val show = id > 0
            binding.layoutCardPanel.visibility = if (show) View.VISIBLE else View.GONE
        }
    }

    private fun showCreateLibraryDialog() {
        val view = layoutInflater.inflate(R.layout.dialog_create_library, null)
        val etName = view.findViewById<EditText>(R.id.etLibraryName)
        val etEmoji = view.findViewById<EditText>(R.id.etEmoji)
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("新建牌库")
            .setView(view)
            .setPositiveButton("创建") { _, _ ->
                val name = etName.text.toString().trim()
                val emoji = etEmoji.text.toString().trim().ifEmpty { "🎲" }
                if (name.isNotEmpty()) viewModel.createLibrary(name, emoji)
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun showLibraryOptions(library: CardLibrary) {
        val options = mutableListOf("重命名", "复制", "设为默认", "导出短码")
        if (!library.isDefault) options.add("删除")
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(library.emoji + " " + library.name)
            .setItems(options.toTypedArray()) { _, which ->
                when (options[which]) {
                    "重命名" -> showRenameDialog(library)
                    "复制" -> showCopyDialog(library)
                    "设为默认" -> { viewModel.setDefaultLibrary(library.id); Snackbar.make(binding.root, "已设为默认库", Snackbar.LENGTH_SHORT).show() }
                    "导出短码" -> showShortCode(library.id)
                    "删除" -> showDeleteConfirm(library)
                }
            }.show()
    }

    private fun showRenameDialog(library: CardLibrary) {
        val et = EditText(requireContext()).apply { setText(library.name) }
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("重命名")
            .setView(et)
            .setPositiveButton("确定") { _, _ -> viewModel.renameLibrary(library, et.text.toString().trim()) }
            .setNegativeButton("取消", null).show()
    }

    private fun showCopyDialog(library: CardLibrary) {
        val et = EditText(requireContext()).apply { setText("${library.name}（副本）") }
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("复制牌库")
            .setView(et)
            .setPositiveButton("复制") { _, _ -> viewModel.copyLibrary(library.id, et.text.toString().trim()) }
            .setNegativeButton("取消", null).show()
    }

    private fun showDeleteConfirm(library: CardLibrary) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("删除牌库")
            .setMessage("确定删除「${library.name}」及其所有卡牌？此操作不可撤销。")
            .setPositiveButton("删除") { _, _ -> viewModel.deleteLibrary(library) }
            .setNegativeButton("取消", null).show()
    }

    private fun showAddCardDialog() {
        val libId = viewModel.selectedLibraryId.value ?: return
        if (libId <= 0) { Toast.makeText(requireContext(), "请先选择牌库", Toast.LENGTH_SHORT).show(); return }
        showCardEditDialog(null, libId)
    }

    private fun showEditCardDialog(card: Card) {
        showCardEditDialog(card, card.libraryId)
    }

    private fun showCardEditDialog(card: Card?, libraryId: Long) {
        val view = layoutInflater.inflate(R.layout.dialog_edit_card, null)
        val etContent = view.findViewById<EditText>(R.id.etCardContent)
        val spinnerType = view.findViewById<Spinner>(R.id.spinnerCardType)
        val spinnerTag = view.findViewById<Spinner>(R.id.spinnerCardTag)
        val seekLevel = view.findViewById<android.widget.SeekBar>(R.id.seekBarLevel)
        val tvLevel = view.findViewById<TextView>(R.id.tvLevel)

        card?.let {
            etContent.setText(it.content)
            spinnerType.setSelection(CardType.values().indexOf(it.type))
            spinnerTag.setSelection(CardTag.values().indexOf(it.tag))
            seekLevel.progress = it.level - 1
            tvLevel.text = "Lv.${it.level}"
        }
        seekLevel.setOnSeekBarChangeListener(object : android.widget.SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(s: android.widget.SeekBar?, p: Int, f: Boolean) { tvLevel.text = "Lv.${p + 1}" }
            override fun onStartTrackingTouch(s: android.widget.SeekBar?) {}
            override fun onStopTrackingTouch(s: android.widget.SeekBar?) {}
        })

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(if (card == null) "添加卡牌" else "编辑卡牌")
            .setView(view)
            .setPositiveButton("保存") { _, _ ->
                val content = etContent.text.toString().trim()
                if (content.isEmpty()) return@setPositiveButton
                val type = CardType.values()[spinnerType.selectedItemPosition]
                val tag = CardTag.values()[spinnerTag.selectedItemPosition]
                val level = seekLevel.progress + 1
                if (card == null) viewModel.addCard(Card(libraryId = libraryId, type = type, content = content, level = level, tag = tag))
                else viewModel.updateCard(card.copy(type = type, content = content, level = level, tag = tag))
            }
            .setNegativeButton("取消", null).show()
    }

    private fun showImportOptions() {
        val opts = arrayOf("从文件导入（CSV/TXT）", "从短码导入")
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("导入牌库")
            .setItems(opts) { _, which ->
                when (which) {
                    0 -> importFileLauncher.launch("text/*")
                    1 -> showShortCodeImportDialog()
                }
            }.show()
    }

    private fun showShortCodeImportDialog() {
        val et = EditText(requireContext()).apply { hint = "粘贴短码" }
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("短码导入")
            .setView(et)
            .setPositiveButton("导入") { _, _ -> viewModel.importFromShortCode(et.text.toString().trim()) }
            .setNegativeButton("取消", null).show()
    }

    private fun showShortCode(libraryId: Long) {
        lifecycleScope.launch {
            val code = viewModel.exportShortCode(libraryId)
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("导出短码")
                .setMessage(code)
                .setPositiveButton("复制") { _, _ ->
                    val clipboard = requireContext().getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                    clipboard.setPrimaryClip(android.content.ClipData.newPlainText("short_code", code))
                    Snackbar.make(binding.root, "已复制到剪贴板", Snackbar.LENGTH_SHORT).show()
                }
                .setNegativeButton("关闭", null).show()
        }
    }

    private fun exportCurrentLibrary() {
        val libId = viewModel.selectedLibraryId.value ?: return
        if (libId <= 0) { Toast.makeText(requireContext(), "请先选择牌库", Toast.LENGTH_SHORT).show(); return }
        exportFileLauncher.launch("truthordare_library.csv")
    }

    private fun readAndImportFile(uri: Uri) {
        try {
            val content = requireContext().contentResolver.openInputStream(uri)?.bufferedReader()?.readText() ?: return
            val et = EditText(requireContext()).apply { hint = "牌库名称" }
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("导入为新库")
                .setView(et)
                .setPositiveButton("导入") { _, _ ->
                    val name = et.text.toString().trim().ifEmpty { "导入库_${System.currentTimeMillis()}" }
                    viewModel.importFromCsv(content, name)
                }
                .setNegativeButton("取消", null).show()
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "读取文件失败", Toast.LENGTH_SHORT).show()
        }
    }

    private fun writeExportFile(uri: Uri) {
        val libId = viewModel.selectedLibraryId.value ?: return
        lifecycleScope.launch {
            val cards = viewModel.cardsForSelected.value ?: return@launch
            val csv = "卡牌内容,类型,刺激等级,适用标签\n" + cards.joinToString("\n") {
                "${it.content},${it.type.label},${it.level},${it.tag.label}"
            }
            requireContext().contentResolver.openOutputStream(uri)?.use { it.write(csv.toByteArray()) }
            Snackbar.make(binding.root, "导出成功", Snackbar.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
