package com.huanchengfly.tieba.post.repository

import com.huanchengfly.tieba.post.api.TiebaApi
import com.huanchengfly.tieba.post.api.retrofit.RetrofitTiebaApi
import com.huanchengfly.tieba.post.utils.validateNewThread
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow

object AddThreadRepository {
    fun addThread(title: String, content: String, forumId: Long, forumName: String): Flow<Long> = flow {
        validateNewThread(forumId, forumName, title, content)
        // Refresh the token before submitting; never automatically repeat a write request.
        val tbs = TiebaApi.getInstance().loginFlow().first().anti.tbs
        require(tbs.isNotBlank()) { "未取得发帖验证信息，请重新登录后重试" }
        val result = RetrofitTiebaApi.OFFICIAL_TIEBA_WRITE_API.addThreadFlow(
            title = title.trim(), content = content, forumId = forumId.toString(),
            forumName = forumName, tbs = tbs,
        ).first()
        emit(result.requireThreadId())
    }
}
