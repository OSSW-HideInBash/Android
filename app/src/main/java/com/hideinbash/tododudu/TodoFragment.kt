package com.hideinbash.tododudu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.hideinbash.tododudu.databinding.FragmentTodoBinding

class TodoFragment : Fragment() {

    lateinit var binding: FragmentTodoBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTodoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 예시 데이터
        val todoList = listOf(
            Todo(title = "운동하기", description = "헬스장 가기", priority = 1, date = "2025-06-08"),
            Todo(title = "공부하기", description = "수학 문제 풀기", priority = 2, date = "2025-06-08"),
            Todo(title = "장보기", description = "계란, 우유, 빵 사기", priority = 3, date = "2025-06-08"),
            Todo(title = "장보기1", description = "계란, 우유, 빵 사기", priority = 3, date = "2025-06-08"),
            Todo(title = "장보기2", description = "계란, 우유, 빵 사기", priority = 3, date = "2025-06-08"),
            Todo(title = "장보기3", description = "계란, 우유, 빵 사기", priority = 3, date = "2025-06-08"),
            Todo(title = "장보기4", description = "계란, 우유, 빵 사기", priority = 3, date = "2025-06-08")
        )

        val adapter = TodoAdapter(todoList)
        binding.todoRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.todoRecyclerView.adapter = adapter
        binding.todoRecyclerView.addItemDecoration(ItemDecoration(50))  // 아이템 간격 설정

        binding.todoAddBtn.setOnClickListener {
            TodoAddDialogFragment().show(parentFragmentManager, "TodoAddDialogFragment")
        }
    }
}