package com.huanchengfly.tieba.post.ui.page.thread

import com.huanchengfly.tieba.post.api.models.protos.Post
import com.huanchengfly.tieba.post.arch.wrapImmutable
import kotlinx.collections.immutable.persistentListOf
import org.junit.Assert.*
import org.junit.Test

class ThreadDeletionStateTest {
    private fun post(id: Long) = PostItemData(
        post = Post(id = id).wrapImmutable(),
        blocked = false,
        contentRenders = persistentListOf(),
        subPosts = persistentListOf(),
    )

    @Test fun deletingReplyInLatestPostsDoesNotCrashOrRemoveAnotherFloor() {
        val old = ThreadUiState(data = persistentListOf(post(3)), latestPosts = persistentListOf(post(4)))
        val updated = ThreadPartialChange.DeletePost.Success(4).reduce(old)
        assertEquals(listOf(3L), updated.data.map { it.post.get { id } })
        assertTrue(updated.latestPosts.isEmpty())
    }

    @Test fun alreadyRemovedReplyIsSafeToHandleAgain() {
        val old = ThreadUiState(data = persistentListOf(post(3)))
        assertEquals(old, ThreadPartialChange.DeletePost.Success(99).reduce(old))
    }
}
