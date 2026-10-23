package com.huanchengfly.tieba.post.utils

import org.junit.Assert.*
import org.junit.Test

class PostActionUtilTest {
    @Test fun notificationPlaceholderUsesLoadedPostId() {
        assertEquals(123L, resolvePositiveId(123L, 0L))
        assertEquals(456L, resolvePositiveId(456L, 123L))
        assertEquals(123L, resolvePositiveId(0L, 123L))
        assertEquals(0L, resolvePositiveId(null, 0L))
    }

    @Test fun ownReplyIsRecognizedWhenTopLevelAuthorIdIsMissing() {
        assertTrue(isOwnPost(0L, 42L, 42L))
        assertTrue(isOwnPost(42L, null, 42L))
        assertFalse(isOwnPost(43L, 42L, 42L))
        assertFalse(isOwnPost(0L, 0L, 0L))
        assertFalse(isOwnPost(null, null, null))
        assertFalse(isOwnPost(43L, 43L, 42L))
    }

    @Test fun mainPostAndBothNestedReplyTargetsAreAccepted() {
        validateReplyTarget(1, "test", 2, null, null)
        validateReplyTarget(1, "test", 2, 3, null)
        validateReplyTarget(1, "test", 2, 3, 4)
    }

    @Test(expected = IllegalArgumentException::class)
    fun zeroFloorIdMustNotBeSentAsANewTopLevelReply() {
        validateReplyTarget(1, "test", 2, 0, null)
    }

    @Test(expected = IllegalArgumentException::class)
    fun nestedReplyRequiresItsParentFloor() {
        validateReplyTarget(1, "test", 2, null, 4)
    }

    @Test(expected = IllegalArgumentException::class)
    fun unloadedForumCannotBeSubmitted() {
        validateReplyTarget(0, "", 2, 3, null)
    }
}
