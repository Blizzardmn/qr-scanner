package com.tools.easy.scanner.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.recyclerview.widget.GridLayoutManager
import com.tools.easy.scanner.R
import com.tools.easy.scanner.basic.BasicActivity
import com.tools.easy.scanner.databinding.ActivityMainBinding
import com.tools.easy.scanner.support.GpSupport
import com.tools.easy.scanner.support.Supports
import com.tools.easy.scanner.ui.adapter.CardAdapter
import com.tools.easy.scanner.ui.other.HtmlActivity

class MainActivity : BasicActivity<ActivityMainBinding>(), View.OnClickListener {

    override fun vBinding(): ActivityMainBinding {
        return ActivityMainBinding.inflate(layoutInflater)
    }

    data class TypeItem(@DrawableRes val icon: Int, val nameImg: Int, val name: String)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initList()

        binding.main.apply {
            imgScan.setOnClickListener(this@MainActivity)
            imgSettings.setOnClickListener(this@MainActivity)
        }
        binding.navView.apply {
            findViewById<TextView>(R.id.tv_rate).setOnClickListener(this@MainActivity)
            findViewById<TextView>(R.id.tv_privacy).setOnClickListener(this@MainActivity)
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

            R.id.tv_rate -> {
                GpSupport.skip2Market(packageName)
            }

            R.id.tv_privacy -> {
                HtmlActivity.openPrivacy(this)
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
            TypeItem(R.mipmap.ic_home_instagram, R.mipmap.hm_instagram, Supports.catInstagram),
            TypeItem(R.mipmap.ic_home_facebook, R.mipmap.hm_facebook, Supports.catFacebook),
            TypeItem(R.mipmap.ic_home_twitter, R.mipmap.hm_twitter, Supports.catTwitter),
            TypeItem(R.mipmap.ic_home_youtube, R.mipmap.hm_youtube, Supports.catYoutube),
            TypeItem(R.mipmap.ic_home_whatsapp, R.mipmap.hm_whatsapp, Supports.catWhatsapp),
            TypeItem(R.mipmap.ic_home_tiktok, R.mipmap.hm_tiktok, Supports.catTiktok),
            TypeItem(R.mipmap.ic_home_clipboard, R.mipmap.hm_clipboard, Supports.catClipboard),
            TypeItem(R.mipmap.ic_home_email, R.mipmap.hm_email, Supports.catEmail),
            TypeItem(R.mipmap.ic_home_website, R.mipmap.hm_websites, Supports.catWebsite),
            TypeItem(R.mipmap.ic_home_text, R.mipmap.hm_text, Supports.catText),
            /*TypeItem(R.mipmap.ic_home_contacts, R.mipmap.hm_contacts, Supports.catContacts),
            TypeItem(R.mipmap.ic_home_wifi, R.mipmap.hm_wifi, Supports.catWifi)*/
        )
        binding.main.recyclerView.layoutManager = GridLayoutManager(this, 2)
        val adapter = CardAdapter(this, list)
        binding.main.recyclerView.adapter = adapter
        adapter.setOnClick {
            CreateActivity.openCreatePage(this@MainActivity, it.name)
        }
    }
}