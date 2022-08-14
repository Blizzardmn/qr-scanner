package com.tools.easy.scanner.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.annotation.DrawableRes
import androidx.recyclerview.widget.GridLayoutManager
import com.tools.easy.scanner.R
import com.tools.easy.scanner.basic.BasicActivity
import com.tools.easy.scanner.databinding.ActivityMainBinding
import com.tools.easy.scanner.ui.adapter.CardAdapter

class MainActivity : BasicActivity<ActivityMainBinding>(), View.OnClickListener {

    override fun vBinding(): ActivityMainBinding {
        return ActivityMainBinding.inflate(layoutInflater)
    }

    data class TypeItem(@DrawableRes val icon: Int, val name: Int)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initList()

        binding.main.apply {
            imgScan.setOnClickListener(this@MainActivity)
            imgSettings.setOnClickListener(this@MainActivity)
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.img_scan -> {
                startActivity(Intent(this, ScanActivity::class.java))
            }

            R.id.img_settings -> {
                binding.drawerLayout.openDrawer(binding.navView)
            }
        }
    }

    override fun onBackPressed() {
        if (closeDrawer()) return
        super.onBackPressed()
    }

    private fun closeDrawer(): Boolean {
        if (binding.drawerLayout.isDrawerOpen(binding.navView)) {
            binding.drawerLayout.closeDrawer(binding.navView)
            return true
        }
        return false
    }

    private fun initList() {
        val list = arrayListOf(
            TypeItem(R.mipmap.ic_home_instagram, R.mipmap.hm_instagram),
            TypeItem(R.mipmap.ic_home_facebook, R.mipmap.hm_facebook),
            TypeItem(R.mipmap.ic_home_twitter, R.mipmap.hm_twitter),
            TypeItem(R.mipmap.ic_home_youtube, R.mipmap.hm_youtube),
            TypeItem(R.mipmap.ic_home_whatsapp, R.mipmap.hm_whatsapp),
            TypeItem(R.mipmap.ic_home_tiktok, R.mipmap.hm_tiktok),
            TypeItem(R.mipmap.ic_home_clipboard, R.mipmap.hm_clipboard),
            TypeItem(R.mipmap.ic_home_email, R.mipmap.hm_email),
            TypeItem(R.mipmap.ic_home_website, R.mipmap.hm_websites),
            TypeItem(R.mipmap.ic_home_text, R.mipmap.hm_text),
            TypeItem(R.mipmap.ic_home_contacts, R.mipmap.hm_contacts),
            TypeItem(R.mipmap.ic_home_wifi, R.mipmap.hm_wifi)
        )
        binding.main.recyclerView.layoutManager = GridLayoutManager(this, 2)
        val adapter = CardAdapter(this, list)
        binding.main.recyclerView.adapter = adapter
        adapter.setOnClick {
            //CreateActivity.openCreatePage(this@MainActivity, it.name)
        }
    }
}