package com.hideinbash.tododudu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.hideinbash.tododudu.databinding.FragmentTodoAddDialogBinding

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