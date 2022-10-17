package com.zhushenwudi.libomnikey

import android.Manifest
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.ilab.omnikey.OmniCard
import com.zhushenwudi.libomnikey.AppUtils.isNumeric
import com.zhushenwudi.libomnikey.databinding.ActivityMainBinding
import dev.utils.app.AppUtils
import dev.utils.app.PathUtils
import dev.utils.app.ResourceUtils
import dev.utils.app.toast.ToastUtils
import dev.utils.common.FileUtils
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean

class MainActivity: AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var mViewModel: MainAvm
    // 允许处理读卡器的消息
    private var isCanRead = AtomicBoolean(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mViewModel = ViewModelProvider(this)[MainAvm::class.java]
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.vm = mViewModel
        binding.lifecycleOwner = this

        binding.btnRead.setOnClickListener {
            permission()
        }

        mViewModel.omniMsg.observe(this) {
            if (it.second == OmniCard.READER_NOT_FOUND) {
                showNoButtonDialog(
                    title = "未找到刷卡硬件",
                    messageVisible = View.GONE,
                    photo = R.drawable.card_red
                )

                OmniCard.unbind()
                return@observe
            }
            if (it.first == OmniCard.Status.MESSAGE && isCanRead.get()) {
                ToastUtils.showLong("请将IC卡放置到您的读写设备上")
                showOneButtonDialog(
                    title = "温馨提示",
                    message = "请将IC卡放置到您的读写设备上",
                    photo = R.drawable.iccard_hand,
                    time = DIALOG_DELAY * TIMER_UNIT * 1L,
                    onTimeout = {
                        OmniCard.unbind()
                    },
                    onConfirm = {
                        OmniCard.unbind()
                    }
                )
                return@observe
            }
            if (isNumeric(it.second) && isCanRead.get()) {
                hideOneButtonDialog()
                isCanRead.set(false)
                OmniCard.unbind()
                mViewModel.cardNum.value = it.second
                Log.e("aaa", it.second)
            }
        }
    }

    private fun permission() {
        requestPermission(
            permissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
            onGrant = {
                if (AppUtils.isInstalledApp(OmniCard.PACKAGE_NAME)) {
                    requestCardPermission()
                } else {
                    installOmniCardApk()
                    ToastUtils.showLong("正在安装读卡驱动软件...")
                }
            },
            onRationale = {
                permission()
            }
        )
    }

    private fun installOmniCardApk() {
        val dest = PathUtils.getSDCard().sdCardPath + File.separator + OmniCard.APK_NAME
        try {
            FileUtils.copyFile(ResourceUtils.getAssets().open(OmniCard.APK_NAME), dest, true)
            AppUtils.installAppSilent(File(dest), "-r", true)
        } catch (e: Exception) {
            e.printStackTrace()
            ToastUtils.showLong("未找到要安装的 APK")
        }
    }

    private fun requestCardPermission(isInit: Boolean = false) {
        requestPermission(
            permissions = arrayOf(OmniCard.CARD_PERMISSION),
            onGrant = {
                isCanRead.set(true)
                //权限允许
                OmniCard.bind(application, mViewModel.omniMsg, isInit)
            },
            onRationale = {
                requestCardPermission(isInit)
            }
        )
    }

    override fun onStop() {
        OmniCard.release()
        super.onStop()
    }
}