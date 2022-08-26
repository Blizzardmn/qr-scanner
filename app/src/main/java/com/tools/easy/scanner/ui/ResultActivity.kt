package com.tools.easy.scanner.ui

import android.os.Bundle
import com.tools.easy.scanner.basic.BasicActivity
import com.tools.easy.scanner.databinding.ActivityResultBinding

/**
 *  description :
 */
class ResultActivity: BasicActivity<ActivityResultBinding>() {

    override fun vBinding(): ActivityResultBinding {
        return ActivityResultBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

}