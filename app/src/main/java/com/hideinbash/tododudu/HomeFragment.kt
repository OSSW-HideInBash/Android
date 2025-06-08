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
        loadAllData()

        monsterAdapter = MonsterAdapter(
            items = emptyList(),
            onItemClick = { todo ->
                // 할 일 아이템 클릭 시 처리
                HomeDetailDialogFragment.newInstance(todo) {
                    loadAllData()
                }.show(parentFragmentManager, "HomeDetailDialog")
            }
        )
        binding.homeMonsterRv.layoutManager = LinearLayoutManager(requireContext()).apply {
            stackFromEnd = true     // 아이템을 아래에서부터 쌓기
            reverseLayout = false   // 데이터 순서는 그대로
        }
        binding.homeMonsterRv.adapter = monsterAdapter

        loadAllData()
    }

    private fun loadAllData() {
        CoroutineScope(Dispatchers.IO).launch {
            // 1. XP & 레벨 데이터 (SharedPreferences)
            val prefs = requireContext().getSharedPreferences("user_data", 0)
            val xp = prefs.getInt("xp", 0)
            val level = prefs.getInt("level", 1)
            val xpForNext = 100 + (level - 1) * 20

            // 2. 할 일 데이터 (RoomDB)
            val db = TodoDatabase.getInstance(requireContext())
            val dateStr = currentDate.format(dbDateFormatter)
            val allTodos = db.todoDao().getTodosByDate(dateStr)
            val yetTodos = db.todoDao().getYetTodosByDate(dateStr)

            // 3. 메인 스레드에서 UI 업데이트
            withContext(Dispatchers.Main) {
                // XP & 레벨
                binding.homeLevelTv.text = "Lv. $level"
                binding.homeXpPb.max = xpForNext
                binding.homeXpPb.progress = xp

                // 할 일 진행률
                binding.homeTodoTv.text = "${allTodos.count { it.isCompleted }} / ${allTodos.size}"
                binding.homeTodoPb.max = allTodos.size
                binding.homeTodoPb.progress = allTodos.count { it.isCompleted }

                // 몬스터 리스트
                monsterAdapter.updateList(yetTodos)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadAllData()
    }
}