package com.hideinbash.tododudu

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
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

    // 완료 예정인 할 일들을 추적하는 Set
    private val completingTodos = mutableSetOf<Long>()

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
                HomeDetailDialogFragment.newInstance(
                    todo = todo,
                    onComplete = { todoId ->
                        handleTodoCompletion(todoId)
                    },
                    onUpdate = { todoId ->
                        // 수정 완료 시 데이터만 새로고침 (완료 처리 X)
                        loadAllData()
                    }
                ).show(parentFragmentManager, "HomeDetailDialog")
            }
        )

        setupRecyclerView()
        setupAddButton()
        loadAllData()
    }

    private fun setupRecyclerView() {
        // 패턴 정의
        val pattern = listOf(
            0.65f to 0.9f,
            0.35f to 0.75f,
            0.85f to 0.6f,
            0.2f to 0.45f,
            0.55f to 0.3f
        )
        val itemPx = (100 * resources.displayMetrics.density).toInt()   // 아이템 크기 (px 계산)
        binding.homeMonsterRv.layoutManager = MonsterPatternLayoutManager(pattern, itemPx, itemPx)
        binding.homeMonsterRv.adapter = monsterAdapter
    }

    private fun setupAddButton() {
        binding.homeAddBtn.setOnClickListener {
            TodoAddDialogFragment(
                mode = TodoAddDialogFragment.Mode.CREATE,
                onComplete = { loadAllData() },
                date = currentDate.format(dbDateFormatter)
            ).show(parentFragmentManager, "TodoAddDialog")
        }
    }

    private fun loadAllData() {
        CoroutineScope(Dispatchers.IO).launch {

            // 1. XP & 레벨 데이터 (SharedPreferences)
            val prefs = requireContext().getSharedPreferences("user_data", 0)
            val xp = prefs.getInt("xp", 0)
            val level = prefs.getInt("level", 1)
            val xpForNext = 100 + (level - 1) * 20

            // 1-1. 마이페이지의 닉네임과 캐릭터 정보
            val info_prefs = requireContext().getSharedPreferences("user_info_data", 0)
            val character = info_prefs.getString("character","https://animatedoss.s3.amazonaws.com/fad01384-aeea-4539-946e-025387d43e81/video.gif")

            // 2. 할 일 데이터 (RoomDB)
            val db = TodoDatabase.getInstance(requireContext().applicationContext)
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

                //메인 캐릭터 이미지
                Glide.with(requireContext())
                    .load(character) // S3 이미지 URL
                    .into(binding.homeMainCharacterIv)
            }
        }
    }

    // 할 일 완료 처리 로직
    private fun handleTodoCompletion(todoId: Long) {
        // 1. 완료 예정 상태로 추가
        completingTodos.add(todoId)

        // 2. 즉시 이미지 변경
        monsterAdapter.setItemCompleting(todoId)

        // 3. 2초 후 실제 DB 업데이트
        CoroutineScope(Dispatchers.IO).launch {
            kotlinx.coroutines.delay(2000)

            try {
                val db = TodoDatabase.getInstance(requireContext().applicationContext)

                // 현재 할 일 정보 가져오기
                val currentTodo = db.todoDao().getTodoById(todoId)
                if (currentTodo != null && !currentTodo.isCompleted) {
                    // 완료 상태로 업데이트
                    val updatedTodo = currentTodo.copy(isCompleted = true)
                    db.todoDao().updateTodo(updatedTodo)

                    Log.d("HomeFragment", "Todo $todoId completed successfully")

                    // UI 업데이트 (메인 스레드에서)
                    withContext(Dispatchers.Main) {
                        addXp(20)
                        completingTodos.remove(todoId)
                        loadAllData() // 데이터 새로고침
                    }
                }
            } catch (e: Exception) {
                Log.e("HomeFragment", "Error completing todo $todoId: ${e.message}")
                // 에러 발생 시 완료 예정 상태 해제
                withContext(Dispatchers.Main) {
                    completingTodos.remove(todoId)
                    monsterAdapter.removeCompletingItem(todoId)
                }
            }
        }
    }

    private fun addXp(amount: Int) {
        val prefs = requireContext().getSharedPreferences("user_data", 0)
        var xp = prefs.getInt("xp", 0)
        var level = prefs.getInt("level", 1)
        var xpForNext = 100 + (level - 1) * 20 // 레벨업에 필요한 XP 계산

        xp += amount

        // 레벨업
        while (xp >= xpForNext) {
            xp -= xpForNext
            level++
            xpForNext = 100 + (level - 1) * 20 // 다음 레벨업에 필요한 XP 계산
        }

        // 레벨다운
        while (xp < 0 && level > 1) {
            level--
            xpForNext = 100 + (level - 1) * 20 // 이전 레벨업에 필요한 XP 계산
            xp += xpForNext
        }

        // 최소값 보정
        if (xp < 0) xp = 0
        if (level < 1) level = 1

        prefs.edit()
            .putInt("xp", xp)
            .putInt("level", level)
            .putInt("next_level_xp", xpForNext)
            .apply()
    }

    override fun onResume() {
        super.onResume()
        loadAllData()
    }
}