package com.truthordare.app.ui.settings

import android.content.SharedPreferences
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.truthordare.app.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var prefs: SharedPreferences

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())

        binding.switchVibration.isChecked = prefs.getBoolean("vibration", true)
        binding.switchVibration.setOnCheckedChangeListener { _, checked ->
            prefs.edit().putBoolean("vibration", checked).apply()
        }

        binding.switchDarkMode.isChecked = prefs.getBoolean("dark_mode", false)
        binding.switchDarkMode.setOnCheckedChangeListener { _, checked ->
            prefs.edit().putBoolean("dark_mode", checked).apply()
            val mode = if (checked) androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
                       else androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
            androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(mode)
        }

        binding.tvVersion.text = "版本 1.0.0"
        binding.tvAbout.text = "真心话大冒险·无限库\n轻量化 · 库管理 · 历史存档"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
