package com.zhushenwudi.libomnikey.dialog

import android.annotation.SuppressLint
import android.content.Context
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.zhushenwudi.libomnikey.R
import com.zhushenwudi.libomnikey.TIMER_UNIT

@SuppressLint("SetTextI18n", "InflateParams")
class TwoButtonDialog constructor(
    context: Context,
    private val time: Long = DEFAULT_TIME,
    photo: Int = R.drawable.custom_error,
    photoVisible: Int = View.VISIBLE,
    title: String = TITLE,
    titleVisible: Int = View.VISIBLE,
    message: String = MESSAGE,
    messageVisible: Int = View.VISIBLE,
    hint: String = HINT,
    hintVisible: Int = View.VISIBLE,
    private val tvBack: String = BACK,
    private val tvConfirm: String = CONFIRM,
    private val onBackListener: () -> Unit = {},
    private val onConfirmListener: () -> Unit = onBackListener,
    private val onTimeout: () -> Unit = onBackListener,
    private val dialogWidth: Int = DIALOG_WIDTH,
    private val dialogHeight: Int = DIALOG_HEIGHT
) : AlertDialog(context, R.style.MyDialogStyle) {

    private val view: View =
        LayoutInflater.from(context).inflate(R.layout.dialog_with_two_button, null)
    private val tvSecond: TextView = view.findViewById(R.id.tv_second)
    private val tvTitle: TextView = view.findViewById(R.id.tv_title)
    private val tvMessage: TextView = view.findViewById(R.id.tv_message)
    private val ivPhoto: ImageView = view.findViewById(R.id.iv_photo)
    private val tvHint: TextView = view.findViewById(R.id.tv_hint)
    private val btnBack: Button = view.findViewById(R.id.btn_back)
    private val btnNext: Button = view.findViewById(R.id.btn_next)

    init {
        ivPhoto.visibility = photoVisible
        tvTitle.visibility = titleVisible
        tvMessage.visibility = messageVisible
        tvHint.visibility = hintVisible

        ivPhoto.setImageResource(photo)
        tvTitle.text = title
        tvMessage.text = message
        tvHint.text = hint

        setCanceledOnTouchOutside(false)
        setView(view)
    }

    private val timer: CountDownTimer by lazy {
        object : CountDownTimer(time, TIMER_UNIT * 1L) {
            override fun onTick(millisUntilFinished: Long) {
                tvSecond.text = "${millisUntilFinished / TIMER_UNIT}s"
            }

            override fun onFinish() {
                dismiss()
                onTimeout.invoke()
            }
        }
    }

    override fun show() {
        super.show()
        btnBack.apply {
            text = tvBack
            setOnClickListener {
                onBackListener.invoke()
                dismiss()
            }
        }

        btnNext.apply {
            text = tvConfirm
            setOnClickListener {
                onConfirmListener.invoke()
                dismiss()
            }
        }

        timer.start()
        setWindow()
    }

    private fun setWindow() {
        val lp = window?.attributes
        lp?.apply {
            width = dialogWidth
            height = dialogHeight
        }
        window?.attributes = lp
    }

    override fun dismiss() {
        super.dismiss()
        timer.cancel()
        hideSoftInput()
    }

    private fun hideSoftInput() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    companion object {
        const val DEFAULT_TIME = 11000L
        const val TITLE = "标题"
        const val MESSAGE = "消息"
        const val HINT = "提示"
        const val CONFIRM = "我知道了"
        const val BACK = "返回首页"
        const val DIALOG_WIDTH = 628
        const val DIALOG_HEIGHT = 464
    }
}