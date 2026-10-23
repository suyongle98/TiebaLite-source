package com.huanchengfly.tieba.post.ui.page.reply

import kotlinx.collections.immutable.persistentListOf
import org.junit.Assert.*
import org.junit.Test

class ReplyStateTest {
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
