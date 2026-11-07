package com.huanchengfly.tieba.post.components

import com.huanchengfly.tieba.post.api.models.PicInfo
import com.huanchengfly.tieba.post.api.models.PicInfoItem
import com.huanchengfly.tieba.post.api.models.UploadPictureResultBean
import com.huanchengfly.tieba.post.api.newBuilder
import com.huanchengfly.tieba.post.api.retrofit.body.MyMultipartBody
import com.huanchengfly.tieba.post.api.retrofit.exception.TiebaLocalException
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okio.Buffer
import org.junit.Assert.*
import org.junit.Test
import java.io.File

class ImageUploadProtocolTest {
    private fun success() = UploadPictureResultBean(picId = "image-id",
        picInfo = PicInfo(originPic = PicInfoItem(width = "1200", height = "900")))

    private fun field(body: MyMultipartBody, name: String): ByteArray = body.parts
        .first { it.headers?.get("Content-Disposition")?.contains("name=\"$name\"") == true }
        .body.let { part -> Buffer().also { part.writeTo(it) }.readByteArray() }

    @Test fun originalSixHundredKbIsSentIntactInTwoOrderedChunks() = runBlocking {
        val bytes = ByteArray(630_123) { (it % 251).toByte() }
        val file = File.createTempFile("original", ".jpg").apply { writeBytes(bytes) }
        val bodies = mutableListOf<MyMultipartBody>()
        try {
            val result = uploadImageChunks(file, 1200, 900, "测试", true, "2") { body ->
                bodies.add(body)
                if (bodies.size == 1) Json.decodeFromString<UploadPictureResultBean>(
                    """{"error_code":"0","resourceId":"accepted","chunkNo":"1"}""") else success()
            }
            assertEquals("image-id", result.picId)
            assertEquals(listOf("1", "2"), bodies.map { field(it, "chunkNo").decodeToString() })
            assertEquals(listOf("0", "1"), bodies.map { field(it, "isFinish").decodeToString() })
            assertEquals(listOf(512_000, 118_123), bodies.map { field(it, "chunk").size })
            assertArrayEquals(bytes, field(bodies[0], "chunk") + field(bodies[1], "chunk"))
            assertArrayEquals(bytes, file.readBytes())
            assertEquals(1, bodies.map { field(it, "resourceId").decodeToString() }.distinct().size)
            bodies.forEach { body ->
                assertEquals("1", field(body, "saveOrigin").decodeToString())
                val actual = Buffer().also { body.writeTo(it) }.size
                assertEquals(actual, body.contentLength())
                assertTrue(body.contentLength() > 0)
                val rebuilt = body.newBuilder().apply { body.parts.forEach { addPart(it) } }
                    .addFormDataPart("_client_version", "test").build()
                assertEquals(Buffer().also { rebuilt.writeTo(it) }.size, rebuilt.contentLength())
            }
        } finally { file.delete() }
    }

    @Test fun exactChunkBoundaryDoesNotSendAnEmptyFinalChunk() = runBlocking {
        val file = File.createTempFile("boundary", ".jpg").apply { writeBytes(ByteArray(512_000)) }
        var requests = 0
        try {
            uploadImageChunks(file, 1, 1, "test", false, "0") { body ->
                requests++
                assertEquals("1", field(body, "isFinish").decodeToString())
                assertEquals("0", field(body, "saveOrigin").decodeToString())
                assertEquals(512_000, field(body, "chunk").size)
                success()
            }
            assertEquals(1, requests)
        } finally { file.delete() }
    }

    @Test fun serverRejectionKeepsItsCodeAndIdentifiesTheFailedChunk() = runBlocking {
        val file = File.createTempFile("failure", ".jpg").apply { writeBytes(ByteArray(630_123)) }
        var requests = 0
        try {
            try {
                uploadImageChunks(file, 1, 1, "test", true, "0") {
                    requests++
                    if (requests == 2) throw TiebaLocalException(2230203, "上传失败")
                    UploadPictureResultBean()
                }
                fail("Expected server rejection")
            } catch (failure: TiebaLocalException) {
                assertEquals(2230203, failure.code)
                assertTrue(failure.message.orEmpty().contains("2/2"))
                assertTrue(failure.message.orEmpty().contains("上传失败"))
            }
            assertEquals(2, requests)
        } finally { file.delete() }
    }

    @Test fun finalAcknowledgementWithoutImageIdIsNotSuccess() = runBlocking {
        val file = File.createTempFile("empty-result", ".jpg").apply { writeBytes(ByteArray(100)) }
        try {
            try {
                uploadImageChunks(file, 1, 1, "test", true, "0") { UploadPictureResultBean() }
                fail("Missing final picture ID must be rejected")
            } catch (failure: TiebaLocalException) {
                assertTrue(failure.message.orEmpty().contains("尚未提交帖子"))
            }
        } finally { file.delete() }
    }
}
