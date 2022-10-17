package com.zhushenwudi.libomnikey.dialog

import android.annotation.SuppressLint
import android.content.Context
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.zhushenwudi.libomnikey.DIALOG_TIMER
import com.zhushenwudi.libomnikey.R
import com.zhushenwudi.libomnikey.TIMER_UNIT

class OneButtonDialog constructor(
    context: Context,
    private val time: Long = DIALOG_TIMER * TIMER_UNIT * 1L,
    photo: Int = R.drawable.custom_error,
    photoVisible: Int = View.VISIBLE,
    title: String = TITLE,
    titleVisible: Int = View.VISIBLE,
    message: String = MESSAGE,
    messageVisible: Int = View.VISIBLE,
    private val tvConfirm: String = CONFIRM,
    private val onTimeout: () -> Unit,
    private val onConfirmListener: () -> Unit = onTimeout,
    private val dialogWidth: Int = DIALOG_WIDTH,
    private val dialogHeight: Int = DIALOG_HEIGHT,
    hideButton: Boolean = false
) : AlertDialog(context, R.style.MyDialogStyle) {
    private val view: View =
        LayoutInflater.from(context).inflate(R.layout.dialog_with_one_button, null)
    private val tvSecond: TextView = view.findViewById(R.id.tv_second)
    private val tvTitle: TextView = view.findViewById(R.id.tv_title)
    private val tvMessage: TextView = view.findViewById(R.id.tv_message)
    private val ivPhoto: ImageView = view.findViewById(R.id.iv_photo)
    private val btnNext: Button = view.findViewById(R.id.btn_next)

    init {
        ivPhoto.visibility = photoVisible
        tvTitle.visibility = titleVisible
        tvMessage.visibility = messageVisible

        ivPhoto.setImageResource(photo)
        tvTitle.text = title
        tvMessage.text = message
        if (hideButton) {
            btnNext.visibility = GONE
        }

        setCanceledOnTouchOutside(false)
        setCancelable(false)
        setView(view)

        btnNext.apply {
            text = tvConfirm
            setOnClickListener {
                onConfirmListener.invoke()
                dismiss()
            }
        }

        setWindow()
    }


    private val timer: CountDownTimer by lazy {
        object : CountDownTimer(time, 1L * TIMER_UNIT) {
            @SuppressLint("SetTextI18n")
            override fun onTick(millisUntilFinished: Long) {
                tvSecond.text = "${millisUntilFinished / TIMER_UNIT}s"
            }

            override fun onFinish() {
                dismiss()
                onTimeout.invoke()
            }
        }
    }

    override fun onStop() {
        super.onStop()
        timer.cancel()
        hideSoftInput()
    }

    override fun show() {
        super.show()
        timer.start()
    }

    private fun setWindow() {
        val lp = window?.attributes
        lp?.apply {
            width = dialogWidth
            height = dialogHeight
        }
        window?.attributes = lp
    }

    private fun hideSoftInput() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    companion object {
        const val TITLE = "标题"
        const val MESSAGE = "消息"
        const val CONFIRM = "我知道了"
        const val DIALOG_WIDTH = 628
        const val DIALOG_HEIGHT = 464
    }
}