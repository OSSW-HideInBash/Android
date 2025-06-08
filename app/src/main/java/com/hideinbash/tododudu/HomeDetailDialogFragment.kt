package com.hideinbash.tododudu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.hideinbash.tododudu.databinding.FragmentHomeDetailDialogBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeDetailDialogFragment() : DialogFragment() {

    companion object {
        private const val ARG_TODO = "arg_todo"
        fun newInstance(todo: Todo, onComplete: (() -> Unit)? = null): HomeDetailDialogFragment {
            val fragment = HomeDetailDialogFragment()
            val args = Bundle()
            args.putSerializable(ARG_TODO, todo)
            fragment.arguments = args
            fragment.onComplete = onComplete
            return fragment
        }
    }

    private var onComplete: (() -> Unit)? = null
    private lateinit var todo: Todo
    private var _binding: FragmentHomeDetailDialogBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        todo = requireArguments().getSerializable(ARG_TODO) as Todo
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        _binding = FragmentHomeDetailDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.detailTitleTv.text = todo.title
        binding.detailDescTv.text = todo.description

        // priority에 따라 색 설정
        when (todo.priority) {
            1 -> { binding.detailOrderIv.setColorFilter(ContextCompat.getColor(requireContext(), R.color.first_red)) }
            2 -> { binding.detailOrderIv.setColorFilter(ContextCompat.getColor(requireContext(), R.color.second_blue)) }
            3 -> { binding.detailOrderIv.setColorFilter(ContextCompat.getColor(requireContext(), R.color.third_yellow)) }
        }

        // 수정 버튼
        // 로직구현

        // 완료 버튼
        binding.detailCompleteBtn.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                val db = TodoDatabase.getInstance(requireContext())
                // isCompleted만 true로 바꾼 새 객체 생성
                val updatedTodo = todo.copy(isCompleted = true)
                db.todoDao().updateTodo(updatedTodo) // RoomDB 업데이트

                withContext(Dispatchers.Main) {
                    addXp(20)
                    onComplete?.invoke() // UI 갱신 콜백 호출
                    dismiss()
                }
            }
        }

        // 취소 버튼
        binding.detailCancleBtn.setOnClickListener {
            dismiss()
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.9).toInt(), // 화면의 90% 너비
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
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