package com.huanchengfly.tieba.post.utils

import com.google.gson.Gson
import com.huanchengfly.tieba.post.api.models.AddThreadBean
import com.huanchengfly.tieba.post.api.retrofit.exception.TiebaLocalException
import org.junit.Assert.*
import org.junit.Test

class NewThreadTest {
    private fun response(json: String) = Gson().fromJson(json, AddThreadBean::class.java)

    @Test fun acceptsNumericAndStringThreadIds() {
        assertEquals(123456789012L, response("""{"error_code":"0","tid":123456789012}""").requireThreadId())
        assertEquals(123L, response("""{"error_code":0,"tid":"123"}""").requireThreadId())
        assertEquals(123L, response("""{"error_code":0,"tid":"123","info":[]}""").requireThreadId())
    }

    @Test fun neverTreatsAnEmptyOrMalformedResponseAsPublished() {
        listOf("{}", """{"error_code":"0"}""", """{"error_code":"0","tid":"0"}""",
            """{"error_code":"0","tid":"-1"}""", """{"error_code":"0","tid":"oops"}""")
            .forEach { json ->
                assertThrows(TiebaLocalException::class.java) { response(json).requireThreadId() }
            }
    }

    @Test fun preservesServerErrorInsteadOfOpeningAThread() {
        val failure = assertThrows(TiebaLocalException::class.java) {
            response("""{"error_code":"280041","error_msg":"需要验证","tid":"123"}""").requireThreadId()
        }
        assertEquals(280041, failure.code)
        assertEquals("需要验证", failure.message)
    }

    @Test fun verificationChallengeIsNotSuccessEvenWithAThreadId() {
        assertThrows(TiebaLocalException::class.java) {
            response("""{"error_code":"0","tid":"123","info":{"need_vcode":"1"}}""").requireThreadId()
        }
    }

    @Test fun validatesForumAndRequiredFields() {
        listOf<() -> Unit>(
            { validateNewThread(0, "测试", "标题", "正文") },
            { validateNewThread(1, " ", "标题", "正文") },
            { validateNewThread(1, "测试", " ", "正文") },
            { validateNewThread(1, "测试", "标题", " \n ") },
            { validateNewThread(1, "测试", "字".repeat(31), "正文") },
        ).forEach { assertThrows(IllegalArgumentException::class.java) { it() } }
    }

    @Test fun acceptsThirtyUnicodeCharactersAndImageOnlyBody() {
        validateNewThread(1, "测试", "😀".repeat(30), "#(pic,image-id,100,100)")
    }
}
