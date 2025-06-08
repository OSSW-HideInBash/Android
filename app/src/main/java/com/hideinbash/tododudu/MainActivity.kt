package com.hideinbash.tododudu

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.hideinbash.tododudu.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        /*enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }*/
        if (savedInstanceState == null) {

            replaceFragment(HomeFragment())
            binding.bottomNavigationView.selectedItemId = R.id.nav_home
        }

        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            Log.d("BottomNav", "Clicked item: ${item.itemId}") // ← 로그 추가
            when (item.itemId) {
                R.id.nav_mypage -> replaceFragment(MypageFragment())
                R.id.nav_todo-> replaceFragment(TodoFragment()) // 습관
                R.id.nav_home -> replaceFragment(HomeFragment()) // 홈

            }
            true
        }

    }
    fun replaceFragment(fragment: Fragment, bundle: Bundle? =null) {
        supportFragmentManager.beginTransaction()
            .replace(binding.mainContainer.id, fragment)
            .commit()
    }
}