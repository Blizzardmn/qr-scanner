package com.tools.easy.scanner.ui.new

import android.app.Activity
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.tools.easy.scanner.basic.BasicActivity
import com.tools.easy.scanner.databinding.ActivityServersBinding
import com.tools.easy.scanner.datas.DataStorage
import com.tools.easy.scanner.datas.entity.ServerEntity
import java.util.*
import kotlin.Comparator
import kotlin.collections.ArrayList

/**
 *  description :
 */
class ServersActivity: BasicActivity<ActivityServersBinding>() {

    override fun vBinding(): ActivityServersBinding {
        return ActivityServersBinding.inflate(layoutInflater)
    }

    private val selectListener = object : ServersAdapter.OnClickConnListener {
        override fun onClicked(connBean: ServerEntity) {
            HomeActivity.connectedServer = connBean
            setResult(Activity.RESULT_OK)
            finish()
        }
    }

    private val serverAdapter = ServersAdapter(this, selectListener)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.imgBack.setOnClickListener {
            onBackPressed()
        }
        binding.rv.layoutManager = LinearLayoutManager(this)
        binding.rv.adapter = serverAdapter
        //AdsLoader.preload(App.ins, AdPos.adTools)

        showLoading(true)
        DataStorage.getServers({
            if (it.isNullOrEmpty()) return@getServers
            Collections.sort(it, object :Comparator<ServerEntity>{
                override fun compare(p0: ServerEntity?, p1: ServerEntity?): Int {
                    if (p0 == null || p1 == null) return 0
                    if (p0.code == p1.code)
                        return p0.name.compareTo(p1.name)
                    return p0.code.compareTo(p1.code)
                }
            })

            val list = arrayListOf<ServerEntity>()
            list.addAll(it)
            list.add(0, ServerEntity(isFaster = true))
            onConfigLoaded(list)
        }, 5500L)
    }

    private fun onConfigLoaded(list: ArrayList<ServerEntity>) {
        showLoading(false)
        serverAdapter.setDataResource(list)
    }

    private fun showLoading(show: Boolean) {
        if (show) {
            binding.layoutLoading.visibility = View.VISIBLE
            binding.progressBar.show()
        } else {
            binding.layoutLoading.visibility = View.GONE
        }
    }

    override fun onBackPressed() {
        HomeActivity.atomicBackAd.set(true)
        super.onBackPressed()
    }
}