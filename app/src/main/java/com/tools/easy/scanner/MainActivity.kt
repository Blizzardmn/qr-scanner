package com.tools.easy.scanner

import android.os.Bundle
import com.tools.easy.scanner.basic.BasicActivity
import com.tools.easy.scanner.databinding.ActivityMainBinding

class MainActivity : BasicActivity<ActivityMainBinding>() {

    override fun vBinding(): ActivityMainBinding {
        return ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }


}