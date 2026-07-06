package com.truthordare.app.ui.game

import android.animation.AnimatorInflater
import android.animation.AnimatorSet
import android.content.Context
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.truthordare.app.R
import com.truthordare.app.data.model.CardTag
import com.truthordare.app.data.model.CardLibrary
import com.truthordare.app.databinding.FragmentGameBinding
import com.truthordare.app.ui.library.LibraryAdapter

class GameFragment : Fragment() {
    private var _binding: FragmentGameBinding? = null
    private val binding get() = _binding!!
    private val viewModel: GameViewModel by activityViewModels()
    private lateinit var libraryAdapter: LibraryChipAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentGameBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupLibraryChips()
        setupTagChips()
        setupLevelSlider()
        setupCardArea()
        setupBottomControls()
        observeViewModel()
    }

    private fun setupLibraryChips() {
        libraryAdapter = LibraryChipAdapter(
            onSelect = { library -> viewModel.selectLibrary(library.id) },
            onUnlockRequest = { library -> showUnlockDialog(library) }
        )
        binding.rvLibraries.adapter = libraryAdapter
        viewModel.libraries.observe(viewLifecycleOwner) { libs ->
            libraryAdapter.submitList(libs)
        }
    }

    private fun showUnlockDialog(library: CardLibrary) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("解锁 ${library.emoji} ${library.name}")
            .setMessage("该牌库内容较为刺激劲爆，确认后即可使用。")
            .setPositiveButton("确认解锁") { _, _ ->
                viewModel.unlockLibrary(library.id)
                Toast.makeText(requireContext(), "${library.name} 已解锁", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun setupTagChips() {
        binding.chipAll.setOnClickListener { viewModel.setTag(CardTag.ALL) }
        binding.chipCouple.setOnClickListener { viewModel.setTag(CardTag.COUPLE) }
        binding.chipDuo.setOnClickListener { viewModel.setTag(CardTag.DUO) }
        binding.chipParty.setOnClickListener { viewModel.setTag(CardTag.PARTY) }
    }

    private fun setupLevelSlider() {
        binding.sliderLevel.addOnChangeListener { _, value, fromUser ->
            if (fromUser) viewModel.setMaxLevel(value.toInt())
        }
    }

    private fun setupCardArea() {
        binding.cardFront.setOnClickListener { viewModel.draw() }
        binding.cardBack.setOnClickListener { viewModel.flipCard() }
        binding.btnDraw.setOnClickListener {
            vibrate()
            viewModel.draw()
        }
    }

    private fun setupBottomControls() {
        binding.btnToggleMode.setOnClickListener { viewModel.toggleGameMode() }
        binding.btnSaveSession.setOnClickListener { showSaveSessionDialog() }
        binding.btnViewHistory.setOnClickListener { showCurrentSessionHistory() }
    }

    private fun observeViewModel() {
        viewModel.currentCard.observe(viewLifecycleOwner) { card ->
            if (card == null) return@observe
            binding.cardBack.visibility = View.VISIBLE
            binding.cardFront.visibility = View.GONE
            val typeColor = when (card.type.name) {
                "TRUTH" -> requireContext().getColor(R.color.truth_color)
                "DARE" -> requireContext().getColor(R.color.dare_color)
                else -> requireContext().getColor(R.color.prop_color)
            }
            binding.tvCardType.text = card.type.label
            binding.tvCardContent.text = card.content
            binding.tvCardLevel.text = "Lv.\${card.level}"
            binding.cardBack.setCardBackgroundColor(typeColor)
        }

        viewModel.isFlipped.observe(viewLifecycleOwner) { flipped ->
            if (flipped) {
                binding.cardBack.visibility = View.GONE
                binding.cardFront.visibility = View.VISIBLE
            }
        }

        viewModel.gameMode.observe(viewLifecycleOwner) { mode ->
            binding.btnToggleMode.text = if (mode == GameMode.TURN_BASED) "轮流抽" else "指定抽"
        }

        viewModel.maxLevel.observe(viewLifecycleOwner) { level ->
            binding.sliderLevel.value = level.toFloat()
            binding.tvLevelLabel.text = "最高等级 Lv.$level"
        }

        viewModel.selectedTag.observe(viewLifecycleOwner) { tag ->
            binding.chipAll.isChecked = tag == CardTag.ALL
            binding.chipCouple.isChecked = tag == CardTag.COUPLE
            binding.chipDuo.isChecked = tag == CardTag.DUO
            binding.chipParty.isChecked = tag == CardTag.PARTY
        }

        viewModel.drawnRecords.observe(viewLifecycleOwner) { records ->
            binding.tvDrawCount.text = "本局 \${records.size} 次"
        }

        // 同步 chip 选中高亮
        viewModel.selectedLibraryId.observe(viewLifecycleOwner) { id ->
            libraryAdapter.setSelected(id)
        }
    }

    private fun showSaveSessionDialog() {
        val input = android.widget.EditText(requireContext())
        input.hint = "备注（可选）"
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("保存本局记录")
            .setView(input)
            .setPositiveButton("保存") { _, _ ->
                viewModel.saveCurrentSession(input.text.toString())
                Toast.makeText(requireContext(), "已保存到历史记录", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun showCurrentSessionHistory() {
        val records = viewModel.drawnRecords.value ?: emptyList()
        if (records.isEmpty()) {
            Toast.makeText(requireContext(), "本局暂无记录", Toast.LENGTH_SHORT).show()
            return
        }
        val text = records.joinToString("\n") { "• [\${it.type}] \${it.content}" }
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("本局流水账（\${records.size} 条）")
            .setMessage(text)
            .setPositiveButton("关闭", null)
            .setNeutralButton("清空") { _, _ -> viewModel.clearCurrentSession() }
            .show()
    }

    private fun vibrate() {
        val vibrator = requireContext().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
