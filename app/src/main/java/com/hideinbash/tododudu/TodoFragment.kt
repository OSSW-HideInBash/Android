package com.hideinbash.tododudu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.hideinbash.tododudu.databinding.FragmentTodoBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class TodoFragment : Fragment() {

    lateinit var binding: FragmentTodoBinding
    private lateinit var adapter: TodoAdapter

    // 현재 선택된 필터 상태
    private enum class FilterType { ALL, YET, DONE }
    private var currentFilter: FilterType = FilterType.YET  // 기본값

    // 날짜 설정
    private var currentDate: LocalDate = LocalDate.now()
    private val dateFormatter = DateTimeFormatter.ofPattern("M월 d일")
    private val dbDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTodoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // RoomDB에서 할 일 목록을 가져오기
        adapter = TodoAdapter(
            items = emptyList(),
            onToggleComplete = { todo ->
                val updatedTodo = todo.copy(isCompleted = !todo.isCompleted)
                CoroutineScope(Dispatchers.IO).launch {
                    val db = TodoDatabase.getInstance(requireContext())
                    db.todoDao().updateTodo(updatedTodo)
                    withContext(Dispatchers.Main) {
                        val msg = if (updatedTodo.isCompleted) "완료로 변경되었습니다." else "예정으로 변경되었습니다."
                        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()

                        // 경험치 추가
                        if(updatedTodo.isCompleted) {
                            addXp(20) // 완료 시 20 XP 추가
                        } else {
                            addXp(-20) // 예정으로 변경 시 20 XP 차감
                        }

                        loadTodosFromRoom() // 리스트 새로고침
                    }
                }
            },
            onEdit = { todo ->
                // 수정 다이얼로그 표시
                TodoAddDialogFragment(
                    mode = TodoAddDialogFragment.Mode.EDIT,
                    todo = todo,
                    onComplete = { loadTodosFromRoom() }, // DB 작업 후 UI 갱신
                    date = currentDate.format(dbDateFormatter) // 현재 날짜 전달
                ).show(parentFragmentManager, "TodoEditDialog")
            },
            onDelete = { todo ->
                // 삭제 확인 다이얼로그
                AlertDialog.Builder(requireContext())
                    .setTitle("삭제 확인")
                    .setMessage("정말 삭제하시겠습니까?")
                    .setPositiveButton("삭제") { dialog, _ ->
                        CoroutineScope(Dispatchers.IO).launch {
                            val db = TodoDatabase.getInstance(requireContext())
                            db.todoDao().deleteTodo(todo)
                            withContext(Dispatchers.Main) {
                                if(todo.isCompleted) {
                                    addXp(-20) // 완료된 할 일 삭제 시 20 XP 차감
                                }
                                Toast.makeText(requireContext(), "할 일이 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                                loadTodosFromRoom() // 리스트 새로고침
                            }
                        }
                        dialog.dismiss()
                    }
                    .setNegativeButton("취소") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
            }
        )
        binding.todoRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.todoRecyclerView.adapter = adapter
        binding.todoRecyclerView.addItemDecoration(ItemDecoration(50))  // 아이템 간격 설정

        // 필터 버튼 클릭 리스너 설정
        binding.todoAllBtn.setOnClickListener{
            if(currentFilter != FilterType.ALL) {
                currentFilter = FilterType.ALL
                updateFilterUI()
                loadTodosFromRoom()
            }
        }
        binding.todoYetBtn.setOnClickListener{
            if(currentFilter != FilterType.YET) {
                currentFilter = FilterType.YET
                updateFilterUI()
                loadTodosFromRoom()
            }
        }
        binding.todoDoneBtn.setOnClickListener{
            if(currentFilter != FilterType.DONE) {
                currentFilter = FilterType.DONE
                updateFilterUI()
                loadTodosFromRoom()
            }
        }

        // 할 일 추가 버튼
        binding.todoAddBtn.setOnClickListener {
            TodoAddDialogFragment(
                mode = TodoAddDialogFragment.Mode.CREATE,
                onComplete = { loadTodosFromRoom() }, // DB 작업 후 UI 갱신
                date = currentDate.format(dbDateFormatter)
            ).show(parentFragmentManager, "TodoAddDialog")
        }

        // 초기 데이터 로드
        updateFilterUI()
        loadTodosFromRoom()

        // 날짜 기본값 오늘로 설정
        updateDateUI()

        // 좌우 화살표 클릭 시 날짜 이동
        binding.todoArrowLeftIv.setOnClickListener {
            currentDate = currentDate.minusDays(1)
            updateDateUI()
            loadTodosFromRoom()
        }
        binding.todoArrowRightIv.setOnClickListener {
            currentDate = currentDate.plusDays(1)
            updateDateUI()
            loadTodosFromRoom()
        }

        // 날짜 클릭 시 날짜 선택 다이얼로그 표시
        binding.todoDateTv.setOnClickListener {
            CalendarDialogFragment(
                selectedDate = currentDate,
                onDateSelected = { year, month, day ->
                    currentDate = LocalDate.of(year, month, day)
                    updateDateUI()
                    loadTodosFromRoom()
                }
            ).show(parentFragmentManager, "CalendarDialog")
        }
    }

    private fun loadTodosFromRoom() {
        // RoomDB에서 할 일 목록을 가져와서 어댑터에 설정
        CoroutineScope(Dispatchers.IO).launch {
            val db = TodoDatabase.getInstance(requireContext())
            val dateStr = currentDate.format(dbDateFormatter)
            val todos = when (currentFilter) {
                FilterType.ALL -> db.todoDao().getTodosByDate(dateStr)
                FilterType.YET -> db.todoDao().getTodosByDate(dateStr).filter { !it.isCompleted }
                FilterType.DONE -> db.todoDao().getTodosByDate(dateStr).filter { it.isCompleted }
            }
            withContext(Dispatchers.Main) {
                adapter.updateList(todos)

                // 진행률 업데이트
                val allTodos = db.todoDao().getTodosByDate(dateStr)
                updateProgressBar(allTodos)
            }
        }
    }

    private fun updateFilterUI() {
        binding.todoAllBtn.backgroundTintList = ContextCompat.getColorStateList(requireContext(),
            if (currentFilter == FilterType.ALL) R.color.button_yellow else R.color.white)
        binding.todoYetBtn.backgroundTintList = ContextCompat.getColorStateList(requireContext(),
            if (currentFilter == FilterType.YET) R.color.button_yellow else R.color.white)
        binding.todoDoneBtn.backgroundTintList = ContextCompat.getColorStateList(requireContext(),
            if (currentFilter == FilterType.DONE) R.color.button_yellow else R.color.white)
    }

    private fun updateDateUI() {
        binding.todoDateTv.text = currentDate.format(dateFormatter)
    }

    private fun updateProgressBar(todos: List<Todo>) {
        val totalCount = todos.size
        val completedCount = todos.count { it.isCompleted }

        binding.todoPbTv.text = "$completedCount / $totalCount"

        val percent = if (totalCount == 0) 0 else (completedCount * 100 / totalCount)
        binding.todoPb.progress = percent
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
}