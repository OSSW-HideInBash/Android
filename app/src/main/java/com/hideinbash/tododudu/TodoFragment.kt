package com.hideinbash.tododudu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.hideinbash.tododudu.databinding.FragmentTodoBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TodoFragment : Fragment() {

    lateinit var binding: FragmentTodoBinding
    private lateinit var adapter: TodoAdapter

    // 현재 선택된 필터 상태
    private enum class FilterType { ALL, YET, DONE }
    private var currentFilter: FilterType = FilterType.YET  // 기본값

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
                        loadTodosFromRoom() // 리스트 새로고침
                    }
                }
            },
            onEdit = { todo ->
                // 수정 다이얼로그 표시
                TodoAddDialogFragment(
                    mode = TodoAddDialogFragment.Mode.EDIT,
                    todo = todo,
                    onComplete = { loadTodosFromRoom() } // DB 작업 후 UI 갱신
                ).show(parentFragmentManager, "TodoEditDialog")
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
                onComplete = { loadTodosFromRoom() } // DB 작업 후 UI 갱신
            ).show(parentFragmentManager, "TodoAddDialog")
        }

        // 초기 데이터 로드
        updateFilterUI()
        loadTodosFromRoom()
    }

    private fun loadTodosFromRoom() {
        // RoomDB에서 할 일 목록을 가져와서 어댑터에 설정
        CoroutineScope(Dispatchers.IO).launch {
            val db = TodoDatabase.getInstance(requireContext())
            val todos = when (currentFilter) {
                FilterType.ALL -> db.todoDao().getAllTodosRaw()
                FilterType.YET -> db.todoDao().getIncompleteTodos()
                FilterType.DONE -> db.todoDao().getCompletedTodos()
            }
            withContext(Dispatchers.Main) {
                adapter.updateList(todos)
            }
        }
    }

    private fun updateFilterUI() {
        binding.todoAllBtn.backgroundTintList = ContextCompat.getColorStateList(requireContext(),
            if (currentFilter == FilterType.ALL) R.color.selected else R.color.unselected)
        binding.todoYetBtn.backgroundTintList = ContextCompat.getColorStateList(requireContext(),
            if (currentFilter == FilterType.YET) R.color.selected else R.color.unselected)
        binding.todoDoneBtn.backgroundTintList = ContextCompat.getColorStateList(requireContext(),
            if (currentFilter == FilterType.DONE) R.color.selected else R.color.unselected)
    }
}