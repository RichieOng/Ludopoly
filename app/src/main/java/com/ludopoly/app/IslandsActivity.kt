package com.ludopoly.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ludopoly.app.databinding.ActivityIslandsBinding

class IslandsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityIslandsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIslandsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener {
            finish()
        }
    }
}
