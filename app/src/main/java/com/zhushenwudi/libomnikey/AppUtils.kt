package com.zhushenwudi.libomnikey

import java.math.BigDecimal
import java.util.regex.Pattern

object AppUtils {
    // 匹配是否为数字
    fun isNumeric(str: String): Boolean {
        // 该正则表达式可以匹配所有的数字 包括负数
        val pattern = Pattern.compile("-?[0-9]+(\\.[0-9]+)?")
        val bigStr = try {
            BigDecimal(str).toString()
        } catch (e: java.lang.Exception) {
            return false //异常 说明包含非数字。
        }
        val isNum = pattern.matcher(bigStr) // matcher是全匹配
        return isNum.matches()
    }
}