package com.zhushenwudi.libomnikey

import android.app.Dialog
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import com.zhushenwudi.libomnikey.dialog.TwoButtonDialog
import com.zhushenwudi.libomnikey.dialog.OneButtonDialog
import java.util.concurrent.atomic.AtomicInteger

// -1: 401, 0: 无弹窗, 1: 有弹窗
val isHasDialog = AtomicInteger(0)
var oneButtonDialog: Dialog? = null
var twoButtonDialog: Dialog? = null

const val TITLE = "标题"
const val MESSAGE = "消息"
const val HINT = "提示"
const val BACK = "返回首页"
const val CONFIRM = "我知道了"
const val DIALOG_WIDTH = 628
const val DIALOG_HEIGHT = 464
const val DIALOG_DELAY = 30
const val DIALOG_TIMER = 10
const val TIMER_UNIT = 1000

/**
 * 打开两个按钮对话框
 */
fun AppCompatActivity.showTwoButtonDialog(
    time: Long = DIALOG_TIMER * TIMER_UNIT * 1L,
    photo: Int = R.drawable.custom_error,
    photoVisible: Int = View.VISIBLE,
    title: String = TITLE,
    titleVisible: Int = View.VISIBLE,
    message: String = MESSAGE,
    messageVisible: Int = View.VISIBLE,
    hint: String = HINT,
    hintVisible: Int = View.VISIBLE,
    tvBack: String = BACK,
    tvConfirm: String = CONFIRM,
    onBackListener: (() -> Unit) = {},
    onConfirm: (() -> Unit) = onBackListener,
    onTimeout: (() -> Unit) = onBackListener,
    dialogWidth: Int = DIALOG_WIDTH,
    dialogHeight: Int = DIALOG_HEIGHT
) {
    if (!this.isFinishing) {
        if (isHasDialog.get() == 0) {
            isHasDialog.set(1)
            twoButtonDialog = null
            twoButtonDialog = TwoButtonDialog(
                this, time, photo, photoVisible, title,
                titleVisible, message, messageVisible, hint, hintVisible, tvBack, tvConfirm,
                onBackListener = {
                    isHasDialog.set(0)
                    onBackListener.invoke()
                },
                onConfirmListener = {
                    isHasDialog.set(0)
                    onConfirm.invoke()
                },
                onTimeout = {
                    isHasDialog.set(0)
                    onTimeout.invoke()
                }, dialogWidth, dialogHeight
            ).lifecycleOwner(this)
            twoButtonDialog?.show()
        }
    }
}

/**
 * 打开错误对话框
 */
fun AppCompatActivity.showErrorDialog(msg: String) {
    if (!this.isFinishing) {
        if (isHasDialog.get() == 0) {
            isHasDialog.set(1)
            oneButtonDialog = null
            oneButtonDialog = OneButtonDialog(
                context = this,
                photo = R.drawable.custom_error,
                messageVisible = View.GONE,
                title = if (msg.length > 20) msg.substring(0, 19) else msg,
                onTimeout = {
                    isHasDialog.set(0)
                }
            )
            oneButtonDialog?.lifecycleOwner(this)
            oneButtonDialog?.show()
        }
    }
}

/**
 * 打开一个按钮对话框
 */
fun AppCompatActivity.showOneButtonDialog(
    time: Long = DIALOG_TIMER * TIMER_UNIT * 1L,
    photo: Int = R.drawable.custom_error,
    photoVisible: Int = View.VISIBLE,
    title: String = TITLE,
    titleVisible: Int = View.VISIBLE,
    message: String = MESSAGE,
    messageVisible: Int = View.VISIBLE,
    tvConfirm: String = CONFIRM,
    onConfirm: (() -> Unit) = {},
    onTimeout: (() -> Unit) = {},
    dialogWidth: Int = DIALOG_WIDTH,
    dialogHeight: Int = DIALOG_HEIGHT
) {
    if (!this.isFinishing) {
        if (isHasDialog.get() == 0) {
            isHasDialog.set(1)
            oneButtonDialog = null
            oneButtonDialog = OneButtonDialog(
                context = this,
                time = time,
                photo = photo,
                photoVisible = photoVisible,
                title = title,
                titleVisible = titleVisible,
                message = message,
                messageVisible = messageVisible,
                tvConfirm = tvConfirm,
                onTimeout = {
                    isHasDialog.set(0)
                    onTimeout.invoke()
                },
                onConfirmListener = {
                    isHasDialog.set(0)
                    onConfirm.invoke()
                },
                dialogWidth = dialogWidth,
                dialogHeight = dialogHeight
            ).lifecycleOwner(this)
            oneButtonDialog?.show()
        }
    }
}

fun MainActivity.showNoButtonDialog(
    title: String = TITLE,
    message: String = MESSAGE,
    messageVisible: Int = View.VISIBLE,
    photo: Int = R.drawable.custom_error,
    time: Long = DIALOG_TIMER * TIMER_UNIT * 1L,
    onConfirm: () -> Unit = {},
    onTimeout: () -> Unit = {}
) {
    if (!isFinishing) {
        if (isHasDialog.get() == 0) {
            isHasDialog.set(1)
            oneButtonDialog = OneButtonDialog(
                context = this,
                title = title,
                message = message,
                messageVisible = messageVisible,
                photo = photo,
                time = time,
                onConfirmListener = {
                    isHasDialog.set(0)
                    onConfirm.invoke()
                },
                onTimeout = {
                    isHasDialog.set(0)
                    onTimeout.invoke()
                },
                hideButton = true
            ).lifecycleOwner(this)
            oneButtonDialog?.show()
        }
    }
}

fun MainActivity.hideOneButtonDialog() {
    if (isHasDialog.get() == 1) {
        oneButtonDialog?.hide()
        oneButtonDialog = null
        isHasDialog.set(0)
    }
}

fun <T : Dialog> T.lifecycleOwner(owner: LifecycleOwner? = null): T {
    val observer = DialogLifecycleObserver(::dismiss)
    val lifecycleOwner = owner ?: (context as? LifecycleOwner
        ?: throw IllegalStateException(
            "$context is not a LifecycleOwner."
        ))
    lifecycleOwner.lifecycle.addObserver(observer)
    return this
}

internal class DialogLifecycleObserver(private val dismiss: () -> Unit) : LifecycleObserver {
    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() = run {
        dismiss()
        isHasDialog.set(0)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun onPause() = run {
        dismiss()
        isHasDialog.set(0)
    }
}