package com.zhushenwudi.libomnikey

import android.view.View
import androidx.fragment.app.Fragment
import com.ftd.livepermissions.LivePermissions
import com.ftd.livepermissions.PermissionResult

fun MainActivity.requestPermission(
    vararg permissions: String,
    onGrant: () -> Unit,
    onRationale: () -> Unit,
    onDeny: (() -> Unit)? = null
) {
    LivePermissions(this)
        .request(*permissions)
        .observe(this) {
            when (it) {
                is PermissionResult.Grant -> {
                    //权限允许
                    onGrant.invoke()
                }
                is PermissionResult.Rationale -> {
                    //权限拒绝
                    showTwoButtonDialog(
                        message = "该功能需开启全部权限",
                        tvBack = "拒绝",
                        tvConfirm = "重选",
                        titleVisible = View.GONE,
                        photo = R.drawable.custom_error,
                        onBackListener = {
                            showErrorDialog("请联系管理员重新授权")
                        },
                        onConfirm = {
                            onRationale.invoke()
                        }
                    )
                }
                is PermissionResult.Deny -> {
                    //权限拒绝，且勾选了不再询问
                    showErrorDialog("请联系管理员重新授权")
                    onDeny?.invoke()
                }
            }
        }
}