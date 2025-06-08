package com.hideinbash.tododudu

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.hideinbash.tododudu.databinding.FragmentHomeBinding

class HomeFragment:Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        // 경험치 로딩
        val prefs = requireContext().getSharedPreferences("user_data", 0)
        val xp = prefs.getInt("xp", 0)
        val level = prefs.getInt("level", 1)
        val nextLevelXp = prefs.getInt("next_level_xp", 100)

        binding.homeLevelTv.text = "Lv. $level"
        binding.homeXpPb.progress = ((xp.toFloat()/nextLevelXp.toFloat()) * 100.0).toInt()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}