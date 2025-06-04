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
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.hideinbash.tododudu.databinding.FragmentMypageBinding
import com.hideinbash.tododudu.databinding.FragmentTodoBinding
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

        binding.btnImageUpload.setOnClickListener {
            selectGallery()
        }

        return binding.root
    }


    //이미리 저장된 이미지를 받아오기 위해 사용중, 참고로 manifest에 권한 설정을 해야한다.
    private lateinit var imageResultLauncher: ActivityResultLauncher<Intent>

    //이미지 전송할때, requestbody용
    private var body : MultipartBody.Part? = null

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
                    binding.defaultCharacterIv.setImageURI(it)
                    //아래의 코드로 이제 서버쪽으로 이미지를 보낼 수 있게 해줌.

                    uri = it//finish dialog로 사진 정보 넘겨줘야함
                    //갤러리에서 이미지를 받아오면 서버에 넘길 수 있게 바로 제작
                    body = createMultipartBodyFromUri(it, requireContext())
                }
                Log.d("이미지 변환", "${body}")


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
        // Android 버전에 따른 권한 확인
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
}