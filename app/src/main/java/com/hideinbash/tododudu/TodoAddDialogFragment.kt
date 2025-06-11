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

class TodoAddDialogFragment(
    private val mode: Mode = Mode.CREATE,
    private val todo: Todo? = null, // 수정 모드일 때 기존 Todo 객체를 전달
    private val onComplete: (() -> Unit)? = null, // DB 작업 후 UI 갱신용 콜백
    private val date: String
) : DialogFragment() {

    lateinit var binding: FragmentTodoAddDialogBinding
    enum class Mode { CREATE, EDIT }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // DialogFragment의 배경을 투명하게 설정
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTodoAddDialogBinding.inflate(inflater, container, false)

        // 닫기 버튼
        binding.todoaddCloseBtn.setOnClickListener{
            dismiss()
        }

        // 모드에 따라 UI 설정
        if (mode == Mode.EDIT && todo != null) {
            binding.todoaddTitleTv.text = "할 일 수정"
            binding.todoaddCreateBtn.text = "수정하기"
            binding.todoaddTitleInput.setText(todo.title)
            binding.todoaddDescInput.setText(todo.description)
            when (todo.priority) {
                1 -> binding.todoaddPriority1.isChecked = true
                2 -> binding.todoaddPriority2.isChecked = true
                3 -> binding.todoaddPriority3.isChecked = true
            }
        } else {
            binding.todoaddTitleTv.text = "할 일 생성"
            binding.todoaddCreateBtn.text = "생성하기"
        }

        // 생성,수정 버튼 클릭 리스너
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

            val todoDate = if (mode == Mode.EDIT && todo != null) {
                todo.date // 수정 모드면 기존 날짜 유지
            } else {
                date // 생성 모드면 전달받은 날짜 사용
            }

            // RoomDB에 저장
            CoroutineScope(Dispatchers.IO).launch {
                val db = TodoDatabase.getInstance(requireContext())
                if (mode == Mode.EDIT && todo != null) {
                    // 수정 모드: 기존 id로 업데이트
                    val updated = todo.copy(
                        title = title,
                        description = desc,
                        priority = priority
                    )
                    db.todoDao().updateTodo(updated)
                } else {
                    // 생성 모드
                    val newTodo = Todo(
                        title = title,
                        description = desc,
                        priority = priority,
                        date = todoDate
                    )
                    db.todoDao().insertTodo(newTodo)
                }
                onComplete?.invoke() // DB 작업 후 UI 갱신 콜백
                dismiss()
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