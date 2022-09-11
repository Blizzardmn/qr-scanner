package com.tools.easy.scanner.basic

import android.os.Bundle
import android.widget.Toast
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
        resources.displayMetrics.apply {
            val finalHeight = heightPixels / 750f
            density = finalHeight
            scaledDensity = finalHeight
            densityDpi = (160 * finalHeight).toInt()
        }

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

    protected fun toastLong(toast: String) {
        Toast.makeText(applicationContext, toast, Toast.LENGTH_LONG).show()
    }

    protected fun toastShort(toast: String) {
        Toast.makeText(applicationContext, toast, Toast.LENGTH_SHORT).show()
    }

}