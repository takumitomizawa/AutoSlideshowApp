package jp.techacademy.takumi.tomizawa.autoslideshowapp

import android.content.ContentUris
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.webkit.PermissionRequest
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import jp.techacademy.takumi.tomizawa.autoslideshowapp.databinding.ActivityMainBinding
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val PERMISSIONS_REQUEST_CODE = 100

    private var pictures = mutableListOf<Uri>()

    private var i1 = 0

    private var timer: Timer? =null

    private var handler = Handler(Looper.getMainLooper())


    // APIレベルによって許可が必要なパーミッションを切り替える
    private val readImagesPermission =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) android.Manifest.permission.READ_MEDIA_IMAGES
        else android.Manifest.permission.READ_EXTERNAL_STORAGE


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        var buttonTap = false

        // パーミッションの許可状態を確認する
        if (checkSelfPermission(readImagesPermission) == PackageManager.PERMISSION_GRANTED) {
            // 許可されている
            getContentsInfo()
        } else {
            // 許可されていないので許可ダイアログを表示する
            requestPermissions(
                arrayOf(readImagesPermission),
                PERMISSIONS_REQUEST_CODE
            )
        }

        binding.button.setOnClickListener{
            // パーミッションの許可状態を確認する
            if (checkSelfPermission(readImagesPermission) == PackageManager.PERMISSION_GRANTED) {
                // 許可されている
                val length = pictures.size - 1

                if (i1 == length){
                    i1 = 0
                    binding.imageView.setImageURI(pictures[i1])
                }else{
                    i1++
                    binding.imageView.setImageURI(pictures[i1])
                }
            } else {
                // 許可されていないので許可ダイアログを表示する
                requestPermissions(
                    arrayOf(readImagesPermission),
                    PERMISSIONS_REQUEST_CODE
                )
            }
        }

        binding.button2.setOnClickListener{
            if (checkSelfPermission(readImagesPermission) == PackageManager.PERMISSION_GRANTED) {
                // 許可されている
                val length = pictures.size - 1

                if (i1 == 0){
                    i1 = length
                    binding.imageView.setImageURI(pictures[i1])
                }else{
                    i1--
                    binding.imageView.setImageURI(pictures[i1])
                }
            } else {
                // 許可されていないので許可ダイアログを表示する
                requestPermissions(
                    arrayOf(readImagesPermission),
                    PERMISSIONS_REQUEST_CODE
                )
            }
        }

        binding.button3.setOnClickListener {

            if (checkSelfPermission(readImagesPermission) == PackageManager.PERMISSION_GRANTED) {
                binding.button.isEnabled = false
                binding.button2.isEnabled = false
                moveSlideShow()

                if (buttonTap){
                    binding.button3.text = getString(R.string.play)
                    binding.button.isEnabled = true
                    binding.button2.isEnabled = true
                    timer!!.cancel()
                    buttonTap = false

                }else{
                    binding.button3.text = getString(R.string.stop)
                    buttonTap = true
                    moveSlideShow()
                }
            } else {
                // 許可されていないので許可ダイアログを表示する
                requestPermissions(
                    arrayOf(readImagesPermission),
                    PERMISSIONS_REQUEST_CODE
                )
            }
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.d("test",PERMISSIONS_REQUEST_CODE.toString())
        Log.d("test",PackageManager.PERMISSION_GRANTED.toString())
        Log.d("test",grantResults[0].toString())
        when (requestCode) {
            PERMISSIONS_REQUEST_CODE ->
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getContentsInfo()
                }
        }
    }

    private fun moveSlideShow(){
        if (timer == null) {
            timer = Timer()
            timer!!.schedule(object : TimerTask() {
                override fun run() {
                    handler.post {
                        val length = pictures.size - 1

                        if (i1 == length) {
                            i1 = 0
                            binding.imageView.setImageURI(pictures[i1])
                        } else {
                            i1++
                            binding.imageView.setImageURI(pictures[i1])
                        }
                    }
                }
            }, 2000, 2000)  // 最初に始動させるまで2000ミリ秒、ループの間隔を2000ミリ秒 に設定
        }
    }

    private fun getContentsInfo() {
        // 画像の情報を取得する
        val resolver = contentResolver
        val cursor = resolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // データの種類
            null, // 項目（null = 全項目）
            null, // フィルタ条件（null = フィルタなし）
            null, // フィルタ用パラメータ
            null // ソート (nullソートなし）
        )

        if (cursor!!.moveToFirst()) {
            do {
                // indexからIDを取得し、そのIDから画像のURIを取得する
                val fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID)
                val id = cursor.getLong(fieldIndex)
                val imageUri =
                    ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)

                pictures.add(imageUri)

                Log.d("ANDROID", "URI : $imageUri")

            } while (cursor.moveToNext())
        }
        binding.imageView.setImageURI(pictures.first())
        cursor.close()
    }
}