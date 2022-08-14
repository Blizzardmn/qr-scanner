package com.tools.easy.scanner.ui

import android.os.Bundle
import com.tools.easy.scanner.basic.BasicActivity
import com.tools.easy.scanner.databinding.ActivityScanBinding

/**
 *  description :
 */
class ScanActivity: BasicActivity<ActivityScanBinding>() {

    override fun vBinding(): ActivityScanBinding {
        return ActivityScanBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }
}