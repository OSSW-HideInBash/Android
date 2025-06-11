package com.hideinbash.tododudu

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.hideinbash.tododudu.databinding.CharacterEditDialogBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit


class CharacterEditDialog(
    private val uri: Uri?,
    private var body : MultipartBody.Part?,
    private val onComplete: (() -> Unit)? = null
) : DialogFragment() {
    lateinit var binding: CharacterEditDialogBinding
    var url : String? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = CharacterEditDialogBinding.inflate(inflater, container, false)



        if(uri != null){
            binding.defaultCharacterIv.setImageURI(uri)
        }
        binding.btnCharConversionBtn.setOnClickListener {
            uploadImageToFlask()
        }
        binding.btnCharSaveBtn.setOnClickListener {
            //saveAtPref()
        }
        binding.editDialogCloseBtn.setOnClickListener {
            dismiss()
        }

        return binding.root
    }



    fun uploadImageToFlask() {
        CoroutineScope(Dispatchers.Main).launch {
            val loadingDialog = LoadingDialog(requireContext()) // 로딩창 생성
            loadingDialog.show()

            if (body == null) {
                loadingDialog.dismiss()
                Toast.makeText(requireContext(), "사진이 받아와지지 않았습니다", Toast.LENGTH_SHORT).show()
                return@launch
            }

            val client = OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build()

            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addPart(body!!)
                .build()

            val request = Request.Builder()
                .url("http://13.55.149.37:5000/gif/inside")
                .post(requestBody)
                .build()

            try {
                // 백그라운드 처리
                val response = withContext(Dispatchers.IO) {
                    client.newCall(request).execute()
                }

                val responseBody = response.body?.string()
                if (responseBody != null) {
                    val gifUrl = JSONObject(responseBody).getString("gif_url")
                    Log.d("gif-test", "응답 성공, $gifUrl")
                    url = gifUrl
                    Glide.with(requireContext())
                        .load(gifUrl)
                        .into(binding.defaultCharacterIv)
                } else {
                    Log.d("gif-test", "응답 본문이 null입니다.")
                }

            } catch (e: Exception) {
                Log.e("gif-test", "에러 발생: ${e.message}")
            } finally {
                loadingDialog.dismiss() // 무조건 로딩창 닫기
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
}