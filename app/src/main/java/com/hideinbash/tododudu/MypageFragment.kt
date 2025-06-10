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
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.hideinbash.tododudu.databinding.FragmentMypageBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

class MypageFragment:Fragment() {
    private var _binding: FragmentMypageBinding? = null
    private val binding get() = _binding!!
    //이미지 전송할때, requestbody용
    private var body : MultipartBody.Part? = null

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
            selectGallery()
        }
        binding.btnImageDefault.setOnClickListener {//기본 이미지 전환
            //uploadImageToFlask()
            val url = "https://animatedoss.s3.amazonaws.com/fad01384-aeea-4539-946e-025387d43e81/video.gif"
            Glide.with(this)
                .load(url) // S3 이미지 URL
                .into(binding.defaultCharacterIv)
            prefs.edit().putString("character",url).apply()
        }
        binding.btnDetailEdit.setOnClickListener {//스켈레톤 뼈대 커스텀 설정
            /*if(uri == null){
                Toast.makeText(requireContext(),"기본 이미지 입니다.",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }*/
            var dialog = DetailEditDialog(requireContext(),uri)
            dialog.show()
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

    private var uri: Uri? = null
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        imageResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val imageUri = result.data?.data
                imageUri?.let {
                    // 이미지를 ImageView에 맞게 로드
                    //binding.defaultCharacterIv.setImageURI(it)
                    //아래의 코드로 이제 서버쪽으로 이미지를 보낼 수 있게 해줌.

                    uri = it//finish dialog로 사진 정보 넘겨줘야함
                    //갤러리에서 이미지를 받아오면 서버에 넘길 수 있게 바로 제작
                    body = createMultipartBodyFromUri(it, requireContext())

                }
                Log.d("이미지 변환", "${body}")
                CharacterEditDialog(
                    uri,
                    body,
                    onComplete = { getImageByPref() }
                ).show(parentFragmentManager, "MyPageImageDialog")

            }
        }
    }
    fun createMultipartBodyFromUri(uri: Uri, context: Context): MultipartBody.Part? {
        // Uri에서 MIME 타입을 가져옵니다.
        val mimeType = context.contentResolver.getType(uri) ?: "image/jpeg" // 기본값을 설정할 수 있습니다.

        // Uri에서 InputStream을 가져옵니다.
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null

        // 임시 파일을 생성합니다.
        val tempFile = File(context.cacheDir, "tempImageFile${System.currentTimeMillis()}.${mimeType.substringAfter("/")}")

        // InputStream의 데이터를 임시 파일로 복사합니다.
        inputStream.use { input ->
            FileOutputStream(tempFile).use { output ->
                input.copyTo(output)
            }
        }

        // RequestBody를 생성합니다.
        val requestFile = tempFile.asRequestBody(mimeType.toMediaTypeOrNull())

        // MultipartBody.Part를 생성합니다.
        val multipartBody = MultipartBody.Part.createFormData("image", tempFile.name, requestFile)

        // 임시 파일 삭제 (선택 사항: 파일 사용 후 삭제)
        tempFile.deleteOnExit()

        return multipartBody
    }


    private fun selectGallery() {
        // Android 버전에 따른 권한 확인x`
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13 (API 33) 이상
            requestPermission(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            // Android 12 (API 32) 이하
            requestPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    private fun requestPermission(permission: String) {
        val permissionStatus = ContextCompat.checkSelfPermission(requireContext(), permission)

        if (permissionStatus == PackageManager.PERMISSION_DENIED) {
            // 권한 요청
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(permission),
                REQ_GALLERY
            )
        } else {
            // 권한이 있는 경우 갤러리 실행
            openGallery()
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        imageResultLauncher.launch(intent)
    }
    companion object {
        private const val REQ_GALLERY = 1
    }

    override fun onResume() {
        super.onResume()
        // sharedprefrence에서 레벨 정보 가져와서 적용
        val prefs = requireContext().getSharedPreferences("user_data", 0)
        val level = prefs.getInt("level", 1)
        binding.levelTv.text = "Lv. $level"
    }

}