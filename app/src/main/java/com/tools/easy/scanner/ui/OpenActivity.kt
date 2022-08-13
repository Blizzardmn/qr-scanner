package com.tools.easy.scanner.ui

import android.os.Bundle
import com.tools.easy.scanner.basic.BasicActivity
import com.tools.easy.scanner.databinding.ActivityOpenBinding

/**
 *  description :
 */
class OpenActivity: BasicActivity<ActivityOpenBinding>() {

    override fun vBinding(): ActivityOpenBinding {
        return ActivityOpenBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }
}