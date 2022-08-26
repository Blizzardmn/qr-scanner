package com.tools.easy.scanner.ui

import android.os.Bundle
import com.tools.easy.scanner.basic.BasicActivity
import com.tools.easy.scanner.databinding.ActivityCreateBinding

/**
 *  description :
 */
class CreateActivity: BasicActivity<ActivityCreateBinding>() {

    override fun vBinding(): ActivityCreateBinding {
        return ActivityCreateBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
}