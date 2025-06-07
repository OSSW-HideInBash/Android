package com.hideinbash.tododudu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.hideinbash.tododudu.databinding.FragmentTodoAddDialogBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TodoAddDialogFragment : DialogFragment() {

    lateinit var binding: FragmentTodoAddDialogBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTodoAddDialogBinding.inflate(inflater, container, false)

        binding.todoaddCloseBtn.setOnClickListener{
            dismiss()
        }

        binding.todoaddCreateBtn.setOnClickListener {
            val title = binding.todoaddTitleInput.text.toString()
            val desc = binding.todoaddDescInput.text.toString()

            val checkedId = binding.todoaddPriorityGroup.checkedRadioButtonId
            val priority = when (checkedId) {
                R.id.todoadd_priority1 -> 1
                R.id.todoadd_priority2 -> 2
                R.id.todoadd_priority3 -> 3
                else -> 3 // 기본값으로 최하위 우선순위 설정
            }

            val date = "2025-06-08" // 예시 날짜, 실제로는 현재 날짜로 설정해야 함

            val todo = Todo(
                title = title,
                description = desc,
                priority = priority,
                date = date
            )

            // RoomDB에 저장
            CoroutineScope(Dispatchers.IO).launch {
                val db = TodoDatabase.getInstance(requireContext())
                db.todoDao().insertTodo(todo)
                dismiss()   // 다이얼로그 닫기
            }
        }

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.9).toInt(), // 화면의 90% 너비
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }
}