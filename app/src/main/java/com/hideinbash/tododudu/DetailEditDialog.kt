package com.hideinbash.tododudu

import android.app.Dialog
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import com.hideinbash.tododudu.databinding.DetailEditDialogBinding

class DetailEditDialog(context: Context,val uri: Uri?):Dialog(context) {
    private lateinit var binding: DetailEditDialogBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DetailEditDialogBinding.inflate(LayoutInflater.from(context))
        setContentView(binding.root)
        window?.setLayout(
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                400f,
                context.resources.displayMetrics
            ).toInt(),
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                400f,
                context.resources.displayMetrics
            ).toInt()
        )
        if(uri != null){
            binding.editImageIv.setImageURI(uri)
        }

    }
}