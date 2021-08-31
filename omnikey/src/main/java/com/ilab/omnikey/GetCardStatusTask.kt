package com.ilab.omnikey

import android.smartcardio.Card
import android.smartcardio.CardTerminal
import android.smartcardio.CommandAPDU
import androidx.lifecycle.MutableLiveData
import com.ilab.omnikey.OmniCard.HAVE_NO_CARD
import kotlinx.coroutines.*

object GetCardStatusTask {
    private const val WILDCARD_PROTOCOL = "*"
    private const val READER_NAME = "OMNIKEY 5427 CK"
    private const val COMMAND_APDU = "ff680d0000"
    private const val CORRECT_APDU_END_RESPONSE = "9000"
    private const val SLEEP_MILLIS = 100L

    private lateinit var scope: CoroutineScope
    private lateinit var mutableCode: MutableLiveData<Pair<OmniCard.Status, String>>

    fun execute(
        terminal: CardTerminal,
        mutableCode: MutableLiveData<Pair<OmniCard.Status, String>>
    ) {
        stop()
        scope = CoroutineScope(Dispatchers.Default)
        this.mutableCode = mutableCode
        scope.launch {
            // 控制对外输出 无卡 一次
            var count = 0
            while (scope.isActive) {
                try {
                    if (!terminal.isCardPresent) {
                        // 卡不存在
                        if (count == 0) {
                            mutableCode.postValue(Pair(OmniCard.Status.MESSAGE, HAVE_NO_CARD))
                        }
                        count++
                    } else if (READER_NAME in terminal.name) {
                        count = 0
                        val card = terminal.connect(WILDCARD_PROTOCOL)
                        getCardAtr(card)
                        parseResp(requestApdu(card))
                        card.disconnect(true)
                        var isCardAbsent = false
                        while (scope.isActive && !isCardAbsent) {
                            isCardAbsent = terminal.waitForCardAbsent(SLEEP_MILLIS)
                            delay(SLEEP_MILLIS)
                        }
                        mutableCode.value?.apply {
                            if (second != HAVE_NO_CARD) {
                                mutableCode.postValue(Pair(OmniCard.Status.MESSAGE, HAVE_NO_CARD))
                            }
                        }
                    }
                    delay(SLEEP_MILLIS)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun stop() {
        try {
            if (this::scope.isInitialized && scope.isActive) {
                scope.cancel()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @Throws(Exception::class)
    private fun parseResp(resp: String) {
        if (resp.endsWith(CORRECT_APDU_END_RESPONSE)) {
            val bin = ConvertUtils.hexStringToAscii(resp.dropLast(4)).toLong(16).toString(2)
            if (bin.length == 26) {
                val pacs = ConvertUtils.convertBinaryToDecimal(bin.drop(9).dropLast(1).toLong())
                mutableCode.postValue(Pair(OmniCard.Status.READ, pacs.toString()))
            }
        }
    }

    @Throws(Exception::class)
    private fun getCardAtr(card: Card): String {
        var atr: String? = null
        val respATR = card.atr
        respATR?.let { atr = ConvertUtils.byteArrayToString(it.bytes) }
        return atr.orEmpty()
    }

    @Throws(Exception::class)
    private fun requestApdu(card: Card): String {
        val cmd = CommandAPDU(ConvertUtils.hexStringToByteArray(COMMAND_APDU))
        val bytes = card.basicChannel.transmit(cmd).bytes
        return bytes?.let { ConvertUtils.byteArrayToString(it) }.toString()
    }
}