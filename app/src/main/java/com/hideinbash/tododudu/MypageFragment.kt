package com.hideinbash.tododudu

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.hideinbash.tododudu.databinding.FragmentMypageBinding
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

class MypageFragment:Fragment() {
    private var _binding: FragmentMypageBinding? = null
    private val binding get() = _binding!!



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMypageBinding.inflate(inflater, container, false)
        val prefs = requireContext().getSharedPreferences("user_info_data", 0)
        var nickname = prefs.getString("nickname", "투두두두")
        var character = prefs.getString("character","https://animatedoss.s3.amazonaws.com/fad01384-aeea-4539-946e-025387d43e81/video.gif")
        Glide.with(this)
            .load(character) // S3 이미지 URL
            .into(binding.defaultCharacterIv)
        binding.btnImageUpload.setOnClickListener {
            var dialog = DetailEditDialog(onComplete = { getImageByPref() })
            dialog.show(parentFragmentManager, "MyPageImageDialog")
        }
        binding.btnImageDefault.setOnClickListener {//기본 이미지 전환
            //uploadImageToFlask()
            val url = "https://animatedoss.s3.amazonaws.com/fad01384-aeea-4539-946e-025387d43e81/video.gif"
            Glide.with(this)
                .load(url) // S3 이미지 URL
                .into(binding.defaultCharacterIv)
            prefs.edit().putString("character",url).apply()
        }

        setTextListener()
        binding.nicknameTv.setText(nickname)

        return binding.root
    }

    fun setTextListener(){
        //닉네임 변경 버튼
        binding.btnNicknameChg.setOnClickListener {
            binding.nicknameTv.setText("")
            binding.nicknameTv.isEnabled = true
            binding.nicknameTv.isFocusableInTouchMode = true
            binding.nicknameTv.requestFocus()

            val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(binding.nicknameTv, InputMethodManager.SHOW_IMPLICIT)
        }
        binding.nicknameTv.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                // 완료 버튼 눌렀을 때 처리할 코드
                binding.nicknameTv.clearFocus()
                val prefs = requireContext().getSharedPreferences("user_info_data", 0)
                prefs.edit().putString("nickname",binding.nicknameTv.text.toString()).apply()
                binding.nicknameTv.isEnabled = false
                binding.nicknameTv.isFocusable = false
                // 키보드 내리기
                val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(binding.nicknameTv.windowToken, 0)

                true
            } else {
                false
            }
        }

        //닉네임 변경 버튼이 풀렸을 때의 로직
        binding.nicknameTv.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val prefs = requireContext().getSharedPreferences("user_info_data", 0)
                prefs.edit().putString("nickname",binding.nicknameTv.text.toString()).apply()
                binding.nicknameTv.isEnabled = false
                binding.nicknameTv.isFocusable = false

                // 키보드 내리기
                val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(binding.nicknameTv.windowToken, 0)
            }
        }

    }
    //이미리 저장된 이미지를 받아오기 위해 사용중, 참고로 manifest에 권한 설정을 해야한다.
    private lateinit var imageResultLauncher: ActivityResultLauncher<Intent>

    //이미지 갱신시, 다시 pref에서 이미지 url을 갖고오게 하는 함수
    fun getImageByPref(){
        val prefs = requireContext().getSharedPreferences("user_info_data", 0)
        var character = prefs.getString("character","https://animatedoss.s3.amazonaws.com/fad01384-aeea-4539-946e-025387d43e81/video.gif")
        Glide.with(this)
            .load(character) // S3 이미지 URL
            .into(binding.defaultCharacterIv)
    }




    override fun onResume() {
        super.onResume()
        // sharedprefrence에서 레벨 정보 가져와서 적용
        val prefs = requireContext().getSharedPreferences("user_data", 0)
        val level = prefs.getInt("level", 1)
        binding.levelTv.text = "Lv. $level"
    }

}