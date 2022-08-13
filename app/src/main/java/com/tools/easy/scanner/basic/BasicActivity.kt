package com.tools.easy.scanner.basic

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding

/**
 *  description :
 */
abstract class BasicActivity<T: ViewBinding>: AppCompatActivity() {

    protected lateinit var binding: T

    abstract fun vBinding(): T

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = vBinding()
        setContentView(binding.root)
    }

    private var isPaused = false

    override fun onStart() {
        super.onStart()
        isPaused = false
    }

    override fun onPause() {
        super.onPause()
        isPaused = true
    }

    override fun onResume() {
        super.onResume()
        isPaused = false
    }

    open fun isActivityPaused(): Boolean {
        return isPaused
    }
}