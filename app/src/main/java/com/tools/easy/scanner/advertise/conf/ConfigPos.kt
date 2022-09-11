package com.tools.easy.scanner.advertise.conf

import com.tools.easy.scanner.advertise.base.AdPos

/**
 *  description :
 */
class ConfigPos(val pos: AdPos, val ids: ArrayList<ConfigId>) {

    fun isEmpty(): Boolean {
        return ids.isNullOrEmpty()
    }
}