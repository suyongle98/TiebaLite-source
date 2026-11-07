package com.huanchengfly.tieba.post.ui.page.reply

import kotlinx.collections.immutable.persistentListOf
import org.junit.Assert.*
import org.junit.Test

class ReplyStateTest {
    @Test fun removingAnImageIgnoresStaleIndexes() {
        val old = ReplyUiState(selectedImageList = persistentListOf("content://image/1"))
        assertEquals(old, ReplyPartialChange.RemoveImage(-1).reduce(old))
        assertEquals(old, ReplyPartialChange.RemoveImage(1).reduce(old))
        val removed = ReplyPartialChange.RemoveImage(0).reduce(old)
        assertTrue(removed.selectedImageList.isEmpty())
        assertEquals(removed, ReplyPartialChange.RemoveImage(0).reduce(removed))
    }

    @Test fun removingAnImageOnlyRemovesTheSelectedOccurrence() {
        val old = ReplyUiState(selectedImageList = persistentListOf(
            "content://image/1", "content://image/2", "content://image/1"))
        val updated = ReplyPartialChange.RemoveImage(2).reduce(old)
        assertEquals(listOf("content://image/1", "content://image/2"), updated.selectedImageList)
        assertEquals(3, old.selectedImageList.size)
    }

    @Test fun choosingPicturesReturnsToEditorAndKeepsEarlierSelection() {
        val old = ReplyUiState(replyPanelType = ReplyPanelType.IMAGE,
            selectedImageList = persistentListOf("content://image/1"))
        val updated = ReplyPartialChange.AddImage(listOf("content://image/2")).reduce(old)
        assertEquals(ReplyPanelType.NONE, updated.replyPanelType)
        assertEquals(listOf("content://image/1", "content://image/2"), updated.selectedImageList)
        assertEquals(old, ReplyPartialChange.AddImage(emptyList()).reduce(old))
    }

    @Test fun serverFailureKeepsSelectedImagesAndVisibleError() {
        val old = ReplyUiState(isSending = true, selectedImageList = persistentListOf("content://image/1"))
        val failed = ReplyPartialChange.Send.Failure(123, "invalid post id").reduce(old)
        assertFalse(failed.isSending)
        assertFalse(failed.replySuccess)
        assertEquals(123, failed.errorCode)
        assertEquals("invalid post id", failed.errorMessage)
        assertEquals(old.selectedImageList, failed.selectedImageList)
    }

    @Test fun reopeningEditorClearsPreviousSuccessAndImages() {
        val old = ReplyUiState(replySuccess = true, selectedImageList = persistentListOf("content://image/1"))
        assertEquals(ReplyUiState(), ReplyPartialChange.BeginEditing.reduce(old))
    }

    @Test fun reopeningEditorCannotClearAnInFlightSend() {
        val old = ReplyUiState(isSending = true)
        assertEquals(old, ReplyPartialChange.BeginEditing.reduce(old))
    }
}
