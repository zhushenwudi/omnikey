package com.zhushenwudi.libomnikey

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ilab.omnikey.OmniCard

class MainAvm: ViewModel() {
    // 读卡器读到的信息
    val omniMsg = MutableLiveData<Pair<OmniCard.Status, String>>()

    // 卡号
    val cardNum = MutableLiveData("**************")
}