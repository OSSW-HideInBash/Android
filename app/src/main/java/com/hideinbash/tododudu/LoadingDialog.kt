package com.hideinbash.tododudu

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import com.bumptech.glide.Glide
import com.hideinbash.tododudu.databinding.LoadingDialogBinding

class LoadingDialog(context:Context):Dialog(context) {
    private lateinit var binding : LoadingDialogBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = LoadingDialogBinding.inflate(LayoutInflater.from(context))
        setContentView(binding.root)

        // 다이얼로그 자체를 투명하게
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // 배경 흐림 정도 (선택)
        window?.attributes = window?.attributes?.apply {
            dimAmount = 0.4f
        }

        setCancelable(false)
        Glide.with(context)
            .asGif()
            .load(R.drawable.hourglass_time)
            .into(binding.loadingIv)
    }
}