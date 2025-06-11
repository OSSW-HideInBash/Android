package com.hideinbash.tododudu

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.hideinbash.tododudu.databinding.DetailEditDialogBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

class DetailEditDialog(
    private val onComplete: (() -> Unit)? = null
) : DialogFragment() {

    private lateinit var binding: DetailEditDialogBinding
    var url : String? = null
    //이미지 전송할때, requestbody용
    private var body : MultipartBody.Part? = null

    var editMode : Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DetailEditDialogBinding.inflate(LayoutInflater.from(context))

        binding.imgUploadBtn.setOnClickListener {
            selectGallery()
        }

        binding.chgSaveBtnIv.setOnClickListener {
            saveAtPref()//pref에 저장하고 dialog 종료
        }

        binding.editDialogCloseBtn.setOnClickListener {
            dismiss()
        }
        binding.detailEditBtn.setOnClickListener {
            startDetailEdit()
        }
        binding.animChgBtn.setOnClickListener {
            val json : JSONObject = binding.skeletonview.getSkeletonJson()
            Log.d("json출력","${json}")
            uploadCustomImageToFlask(json)
        }


        if(uri != null){
            binding.detailImageIv.setImageURI(uri)
        }
        return binding.root
    }

    //갤러리에서 이미지가 선택됐을때 실행되는 코드
    private lateinit var imageResultLauncher: ActivityResultLauncher<Intent>
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
                    Glide.with(requireContext())
                        .load(uri)
                        .into(binding.detailImageIv)
                    uploadImageToFlask()

                    //이미지의 크기를 직접 가져오는 작업
                    val inputStream = requireContext().contentResolver.openInputStream(uri!!)
                    val options = BitmapFactory.Options()
                    options.inJustDecodeBounds = true // 메모리에 로드하지 않고 정보만 가져옴

                    BitmapFactory.decodeStream(inputStream, null, options)

                    val width = options.outWidth
                    val height = options.outHeight

                    binding.skeletonview.setOriginalImageSize(width, height)

                }
                Log.d("이미지 변환", "${body}")

            }
        }
    }

    //갤러리에서 이미지를 받아올 때, uri로 변경하는 함수
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

    //이미지 갤러리에서 갖고오는 함수(permission을 받아옴
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

    //갤러리에서 이미지를 받아온 뒤, 실행하는 api함수
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

            var index = 1

            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addPart(body!!)
                .addFormDataPart("index", index.toString())
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
                        .into(binding.detailImageIv)
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

    fun uploadCustomImageToFlask(jsonObject: JSONObject) {
        CoroutineScope(Dispatchers.Main).launch {
            val loadingDialog = LoadingDialog(requireContext()) // 로딩창 생성
            loadingDialog.show()

            if (body == null) {
                loadingDialog.dismiss()
                Toast.makeText(requireContext(), "사진이 받아와지지 않았습니다", Toast.LENGTH_SHORT).show()
                return@launch
            }

            val client = OkHttpClient.Builder()
                .connectTimeout(300, TimeUnit.SECONDS)
                .readTimeout(300, TimeUnit.SECONDS)
                .writeTimeout(300, TimeUnit.SECONDS)
                .build()

            var index = 1

            // 1. JSONObject → String → RequestBody
            val jsonRequestBody = jsonObject.toString()
                .toRequestBody("application/json".toMediaType())

            // 2. JSON을 파일처럼 MultipartBody.Part 생성
            val jsonPart = MultipartBody.Part.createFormData(
                "skeleton_json", // key
                "skeleton_json", // 파일 이름
                jsonRequestBody  // 본문
            )

            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addPart(body!!)
                .addPart(jsonPart)
                .addFormDataPart("index", index.toString())
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
                        .into(binding.detailImageIv)
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



    //변경하기 버튼을 눌렀을 때, 저장되는 함수
    fun saveAtPref(){
        if(url == null){
            Toast.makeText(requireContext(), "애니메이션이 전환되지 않았습니다", Toast.LENGTH_SHORT).show()
            return
        }
        val prefs = requireContext().getSharedPreferences("user_info_data", 0)
        prefs.edit().putString("character",url).apply()
        onComplete?.invoke()
        dismiss()
    }

    fun startDetailEdit(){
        if(!editMode){//editMode 실행
            if (uri == null){
                Toast.makeText(requireContext(), "사진이 받아와지지 않았습니다", Toast.LENGTH_SHORT).show()
            }
            editMode = true
            binding.skeletonview.visibility = View.VISIBLE
            binding.animChgBtn.visibility = View.VISIBLE


        }else{//editMode 실행 취소
            editMode = false
            binding.skeletonview.visibility = View.GONE
            binding.animChgBtn.visibility = View.GONE
        }
    }

}