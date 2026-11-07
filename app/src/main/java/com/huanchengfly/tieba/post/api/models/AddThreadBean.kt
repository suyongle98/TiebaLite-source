package com.huanchengfly.tieba.post.api.models

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.google.gson.JsonElement
import com.huanchengfly.tieba.post.api.retrofit.exception.TiebaLocalException

@Keep
data class AddThreadBean(
    @SerializedName("error_code") val errorCode: String? = null,
    @SerializedName("error_msg") val errorMessage: String? = null,
    val msg: String? = null,
    val tid: String? = null,
    val pid: String? = null,
    val info: JsonElement? = null,
) {
    fun requireThreadId(): Long {
        if (errorCode != "0") {
            throw TiebaLocalException(errorCode?.toIntOrNull() ?: -1,
                errorMessage?.takeIf { it.isNotBlank() } ?: msg?.takeIf { it.isNotBlank() }
                ?: "发帖结果无法确认，请先刷新贴吧检查是否已发布")
        }
        val needVcode = info?.takeIf { it.isJsonObject }?.asJsonObject
            ?.get("need_vcode")?.takeIf { it.isJsonPrimitive }?.asString
        if (needVcode == "1") {
            throw TiebaLocalException(-1, "发帖需要验证码，请在贴吧官方客户端完成验证后重试")
        }
        return tid?.toLongOrNull()?.takeIf { it > 0 }
            ?: throw TiebaLocalException(-1,
                "服务器未返回有效的帖子 ID，请先刷新贴吧确认是否已发布后再重试")
    }
}
