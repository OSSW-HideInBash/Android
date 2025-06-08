package com.hideinbash.tododudu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.hideinbash.tododudu.databinding.FragmentHomeBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class HomeFragment:Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private var currentDate: LocalDate = LocalDate.now()
    private val dbDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    private lateinit var monsterAdapter: MonsterAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadXpAndLevel()
        loadTodosFromRoom()

        monsterAdapter = MonsterAdapter(emptyList())
        binding.homeMonsterRv.layoutManager = LinearLayoutManager(requireContext()).apply {
            stackFromEnd = true     // 아이템을 아래에서부터 쌓기
            reverseLayout = false   // 데이터 순서는 그대로
        }
        binding.homeMonsterRv.adapter = monsterAdapter

        loadTodosAsMonsters()
    }

    private fun loadXpAndLevel() {
        val prefs = requireContext().getSharedPreferences("user_data", 0)
        val xp = prefs.getInt("xp", 0)
        val level = prefs.getInt("level", 1)
        var xpForNext = 100 + (level - 1) * 20 // 레벨업에 필요한 XP 계산

        binding.homeLevelTv.text = "Lv. $level"
        binding.homeXpPb.max = xpForNext
        binding.homeXpPb.progress = xp
    }

    private fun loadTodosFromRoom() {
        CoroutineScope(Dispatchers.IO).launch {
            val db = TodoDatabase.getInstance(requireContext())
            val dateStr = currentDate.format(dbDateFormatter)
            val todos = db.todoDao().getTodosByDate(dateStr) // 오늘 날짜 기준 할 일 전체

            val totalCount = todos.size
            val completedCount = todos.count { it.isCompleted }

            withContext(Dispatchers.Main) {
                // 진행률 업데이트
                binding.homeTodoTv.text = "$completedCount / $totalCount"
                binding.homeTodoPb.max = totalCount
                binding.homeTodoPb.progress = completedCount
            }
        }
    }

    private fun loadTodosAsMonsters() {
        CoroutineScope(Dispatchers.IO).launch {
            val db = TodoDatabase.getInstance(requireContext())
            val dateStr = currentDate.format(dbDateFormatter)
            val todos = db.todoDao().getYetTodosByDate(dateStr) // 오늘 날짜 기준 할 일 전체

            withContext(Dispatchers.Main) {
                monsterAdapter.updateList(todos)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadTodosAsMonsters()
    }
}