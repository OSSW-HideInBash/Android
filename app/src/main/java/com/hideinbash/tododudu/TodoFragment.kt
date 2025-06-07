package com.hideinbash.tododudu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
        adapter = TodoAdapter(emptyList()) // 초기에는 빈 리스트로 설정
        binding.todoRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.todoRecyclerView.adapter = adapter
        binding.todoRecyclerView.addItemDecoration(ItemDecoration(50))  // 아이템 간격 설정

        loadTodosFromRoom()

        binding.todoAddBtn.setOnClickListener {
            TodoAddDialogFragment().show(parentFragmentManager, "TodoAddDialogFragment")
        }
    }

    private fun loadTodosFromRoom() {
        // RoomDB에서 할 일 목록을 가져와서 어댑터에 설정
        CoroutineScope(Dispatchers.IO).launch {
            val db = TodoDatabase.getInstance(requireContext())
            val todos = db.todoDao().getAllTodosRaw()
            withContext(Dispatchers.Main) {
                adapter.updateList(todos)
            }
        }
    }
}