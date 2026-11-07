package com.huanchengfly.tieba.post.components

import com.huanchengfly.tieba.post.api.models.UploadPictureResultBean
import com.huanchengfly.tieba.post.api.retrofit.body.MyMultipartBody
import com.huanchengfly.tieba.post.api.retrofit.exception.TiebaLocalException
import com.huanchengfly.tieba.post.api.retrofit.exception.getErrorCode
import com.huanchengfly.tieba.post.utils.MD5Util
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.RandomAccessFile

/** Intermediate chunk acknowledgements need not contain picId/picInfo. */
internal suspend fun uploadImageChunks(
    file: File,
    width: Int,
    height: Int,
    forumName: String,
    isOriginImage: Boolean,
    watermark: String,
    chunkSize: Int = 512_000,
    upload: suspend (MyMultipartBody) -> UploadPictureResultBean,
): UploadPictureResultBean {
    require(chunkSize > 0 && file.length() > 0 && width > 0 && height > 0) { "图片数据无效" }
    val length = file.length()
    val count = ((length + chunkSize - 1) / chunkSize).toInt()
    val resourceId = "${MD5Util.toMd5(file)}$chunkSize"
    var result: UploadPictureResultBean? = null
    for (index in 0 until count) {
        val offset = index.toLong() * chunkSize
        val bytes = ByteArray(minOf(chunkSize.toLong(), length - offset).toInt())
        withContext(Dispatchers.IO) {
            RandomAccessFile(file, "r").use {
                it.seek(offset)
                it.readFully(bytes)
            }
        }
        val body = MyMultipartBody.Builder("--------7da3d81520810*")
            .setType(MyMultipartBody.FORM).useKnownLength().apply {
                addFormDataPart("alt", "json")
                addFormDataPart("chunkNo", "${index + 1}")
                if (forumName.isNotBlank()) addFormDataPart("forum_name", forumName)
                addFormDataPart("groupId", "1")
                addFormDataPart("height", "$height")
                addFormDataPart("isFinish", if (index == count - 1) "1" else "0")
                addFormDataPart("is_bjh", "0")
                addFormDataPart("pic_water_type", watermark)
                addFormDataPart("resourceId", resourceId)
                addFormDataPart("saveOrigin", if (isOriginImage) "1" else "0")
                addFormDataPart("size", "$length")
                if (forumName.isNotBlank()) addFormDataPart("small_flow_fname", forumName)
                addFormDataPart("width", "$width")
                addFormDataPart("chunk", "file", bytes.toRequestBody("application/octet-stream".toMediaType()))
            }.build()
        result = try {
            upload(body).also {
                if (it.errorCode.isNotBlank() && it.errorCode != "0") {
                    throw TiebaLocalException(it.errorCode.toIntOrNull() ?: -1, it.errorMsg.ifBlank { "服务器拒绝图片上传" })
                }
            }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (failure: Exception) {
            throw TiebaLocalException(failure.getErrorCode(),
                "${if (isOriginImage) "原图" else "图片"}第 ${index + 1}/$count 块上传失败：${failure.message.orEmpty()}")
        }
    }
    return checkNotNull(result).also {
        if (it.picId.isBlank() || (it.picInfo.originPic.width.toIntOrNull() ?: 0) <= 0 ||
            (it.picInfo.originPic.height.toIntOrNull() ?: 0) <= 0) {
            throw TiebaLocalException(-1, "图片已传输，但服务器未返回完整的图片信息；尚未提交帖子")
        }
    }
}
