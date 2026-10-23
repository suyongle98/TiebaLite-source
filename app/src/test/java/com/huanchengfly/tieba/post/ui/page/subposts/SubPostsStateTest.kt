package com.huanchengfly.tieba.post.ui.page.subposts

import com.huanchengfly.tieba.post.api.models.protos.Anti
import com.huanchengfly.tieba.post.api.models.protos.Post
import com.huanchengfly.tieba.post.api.models.protos.SimpleForum
import com.huanchengfly.tieba.post.api.models.protos.SubPostList
import com.huanchengfly.tieba.post.api.models.protos.ThreadInfo
import com.huanchengfly.tieba.post.arch.wrapImmutable
import kotlinx.collections.immutable.persistentListOf
import org.junit.Assert.*
import org.junit.Test

class SubPostsStateTest {
    @Test fun loadedVerificationTokenIsRetained() {
        val loaded = SubPostsPartialChange.Load.Success(
            anti = Anti(tbs = "fresh-token").wrapImmutable(),
            forum = SimpleForum(id = 1).wrapImmutable(),
            thread = ThreadInfo(id = 2).wrapImmutable(),
            post = Post(id = 3).wrapImmutable(),
            postContentRenders = persistentListOf(),
            subPosts = persistentListOf(),
            hasMore = false,
            currentPage = 1,
            totalPage = 1,
            totalCount = 0,
        ).reduce(SubPostsUiState())
        assertEquals("fresh-token", loaded.anti?.get { tbs })
        assertEquals(3L, loaded.post?.get { id })
    }

    private fun reply(id: Long) = SubPostItemData(
        SubPostList(id = id).wrapImmutable(), persistentListOf(), blocked = false
    )

    @Test fun deletingNestedReplyKeepsSiblingsAndUpdatesCount() {
        val old = SubPostsUiState(subPosts = persistentListOf(reply(4), reply(5)), totalCount = 2)
        val updated = SubPostsPartialChange.DeletePost.Success(3, 4).reduce(old)
        assertEquals(listOf(5L), updated.subPosts.map { it.id })
        assertEquals(1, updated.totalCount)
    }

    @Test fun failedDeletionKeepsReplyVisible() {
        val old = SubPostsUiState(subPosts = persistentListOf(reply(4)), totalCount = 1)
        val updated = SubPostsPartialChange.DeletePost.Failure(1, "denied").reduce(old)
        assertEquals(old, updated)
    }

    @Test fun deletingParentFloorClearsThePage() {
        val old = SubPostsUiState(post = Post(id = 3).wrapImmutable(), subPosts = persistentListOf(reply(4)), totalCount = 1)
        val updated = SubPostsPartialChange.DeletePost.Success(3).reduce(old)
        assertNull(updated.post)
        assertTrue(updated.subPosts.isEmpty())
        assertEquals(0, updated.totalCount)
        assertFalse(updated.hasMore)
    }
}
