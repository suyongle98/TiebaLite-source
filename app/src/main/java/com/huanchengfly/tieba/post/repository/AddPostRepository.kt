package com.huanchengfly.tieba.post.repository

import com.huanchengfly.tieba.post.api.TiebaApi
import com.huanchengfly.tieba.post.api.models.protos.addPost.AddPostResponse
import com.huanchengfly.tieba.post.arch.GlobalEvent
import com.huanchengfly.tieba.post.arch.emitGlobalEvent
import com.huanchengfly.tieba.post.api.retrofit.exception.TiebaLocalException
import com.huanchengfly.tieba.post.utils.validateReplyTarget
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

object AddPostRepository {
    fun addPost(
        content: String,
        forumId: Long,
        forumName: String,
        threadId: Long,
        tbs: String? = null,
        nameShow: String? = null,
        postId: Long? = null,
        subPostId: Long? = null,
        replyUserId: Long? = null
    ): Flow<AddPostResponse> = flow {
        validateReplyTarget(forumId, forumName, threadId, postId, subPostId)
        emitAll(TiebaApi.getInstance()
            .addPostFlow(
                content,
                forumId.toString(),
                forumName,
                threadId.toString(),
                tbs,
                nameShow,
                postId?.toString(),
                subPostId?.toString(),
                replyUserId?.toString()
            ))
    }
            .onEach {
                val newPostId = it.data_?.pid?.toLongOrNull()?.takeIf { id -> id > 0 }
                    ?: throw TiebaLocalException(
                        -1,
                        it.data_?.msg?.takeIf { message -> message.isNotBlank() }
                            ?: "服务器未返回有效的回复 ID，请刷新确认是否已发送成功后再重试"
                    )
                GlobalScope.launch {
                    if (postId != null) {
                        emitGlobalEvent(
                            GlobalEvent.ReplySuccess(
                                threadId,
                                postId,
                                postId,
                                subPostId,
                                newPostId
                            )
                        )
                    } else {
                        emitGlobalEvent(GlobalEvent.ReplySuccess(threadId, newPostId))
                    }
                }
            }
}
