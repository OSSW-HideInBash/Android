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
    private var isEditMode = false

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
        setDefaultUI()

        // 배경 투명하게 설정
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // 수정 버튼 클릭 시 수정 모드 진입
        binding.detailEditBtn.setOnClickListener { enterEditMode() }

        // 수정모드에서 '수정' 버튼
        binding.detailModifyBtn.setOnClickListener { saveEdit() }

        // 수정모드에서 '취소' 버튼
        binding.detailEditCancelBtn.setOnClickListener {
            // 키보드 자동 내림
            val imm = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
            imm.hideSoftInputFromWindow(binding.detailTitleEdit.windowToken, 0)

            exitEditMode()
        }

        // 완료 버튼
        binding.detailCompleteBtn.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                val db = TodoDatabase.getInstance(requireContext())
                val updatedTodo = todo.copy(isCompleted = true)
                db.todoDao().updateTodo(updatedTodo)
                withContext(Dispatchers.Main) {
                    addXp(20)
                    onComplete?.invoke()
                    dismiss()
                }
            }
        }

        // 취소(닫기) 버튼
        binding.detailCloseBtn.setOnClickListener {
            dismiss()
        }
    }

    private fun setDefaultUI() {
        isEditMode = false
        // 제목/설명 TextView 보이기, EditText 숨기기
        binding.detailTitleTv.text = todo.title
        binding.detailDescTv.text = todo.description
        binding.detailTitleTv.visibility = View.VISIBLE
        binding.detailDescTv.visibility = View.VISIBLE
        binding.detailTitleEdit.visibility = View.GONE
        binding.detailDescEdit.visibility = View.GONE

        // 우선순위 아이콘 표시, RadioGroup 숨김
        binding.detailOrderIv.visibility = View.VISIBLE
        binding.detailPriorityGroup.visibility = View.GONE
        val colorRes = when (todo.priority) {
            1 -> R.color.first_red
            2 -> R.color.second_blue
            3 -> R.color.third_yellow
            else -> R.color.third_yellow
        }
        binding.detailOrderIv.setColorFilter(ContextCompat.getColor(requireContext(), colorRes))

        // 버튼 표시
        binding.detailEditBtn.visibility = View.VISIBLE
        binding.detailCompleteBtn.visibility = View.VISIBLE
        binding.detailCloseBtn.visibility = View.VISIBLE
        binding.detailEditCancelBtn.visibility = View.GONE
        binding.detailModifyBtn.visibility = View.GONE
    }

    private fun enterEditMode() {
        isEditMode = true
        // EditText에 기존 값 세팅
        binding.detailTitleEdit.setText(todo.title)
        binding.detailDescEdit.setText(todo.description)
        binding.detailTitleTv.visibility = View.GONE
        binding.detailDescTv.visibility = View.GONE
        binding.detailTitleEdit.visibility = View.VISIBLE
        binding.detailDescEdit.visibility = View.VISIBLE

        // 우선순위 RadioGroup 표시, 아이콘 숨김
        binding.detailOrderIv.visibility = View.GONE
        binding.detailPriorityGroup.visibility = View.VISIBLE
        when (todo.priority) {
            1 -> binding.detailPriority1.isChecked = true
            2 -> binding.detailPriority2.isChecked = true
            3 -> binding.detailPriority3.isChecked = true
        }

        // 버튼 전환
        binding.detailEditBtn.visibility = View.GONE
        binding.detailCompleteBtn.visibility = View.GONE
        binding.detailCloseBtn.visibility = View.GONE
        binding.detailEditCancelBtn.visibility = View.VISIBLE
        binding.detailModifyBtn.visibility = View.VISIBLE
    }

    private fun exitEditMode() {
        setDefaultUI()
    }

    private fun saveEdit() {
        val newTitle = binding.detailTitleEdit.text.toString()
        val newDesc = binding.detailDescEdit.text.toString()
        val checkedId = binding.detailPriorityGroup.checkedRadioButtonId
        val newPriority = when (checkedId) {
            R.id.detail_priority1 -> 1
            R.id.detail_priority2 -> 2
            R.id.detail_priority3 -> 3
            else -> todo.priority
        }
        val updatedTodo = todo.copy(
            title = newTitle,
            description = newDesc,
            priority = newPriority
        )
        CoroutineScope(Dispatchers.IO).launch {
            val db = TodoDatabase.getInstance(requireContext())
            db.todoDao().updateTodo(updatedTodo)
            withContext(Dispatchers.Main) {
                // 키보드 자동 내림
                val imm = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
                imm.hideSoftInputFromWindow(binding.detailTitleEdit.windowToken, 0)

                onComplete?.invoke()
                dismiss()
            }
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