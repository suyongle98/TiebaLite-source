package com.huanchengfly.tieba.post.utils

/** Prefer IDs returned by the server; notification routes can contain zero placeholders. */
internal fun resolvePositiveId(loadedId: Long?, routeId: Long?): Long =
    loadedId?.takeIf { it > 0 } ?: routeId?.takeIf { it > 0 } ?: 0L

internal fun isOwnPost(authorId: Long?, nestedAuthorId: Long?, accountId: Long?): Boolean =
    accountId != null && accountId > 0 &&
        resolvePositiveId(authorId, nestedAuthorId) == accountId

internal fun validateReplyTarget(
    forumId: Long,
    forumName: String,
    threadId: Long,
    postId: Long?,
    subPostId: Long?,
) {
    require(forumId > 0 && forumName.isNotBlank()) { "贴吧信息未加载完整，请返回刷新后重试" }
    require(threadId > 0) { "帖子 ID 无效，请重新打开帖子" }
    require(postId == null || postId > 0) { "楼层 ID 无效，请重新打开楼中楼后重试" }
    require(subPostId == null || (subPostId > 0 && postId != null)) {
        "楼中楼 ID 或所属楼层信息缺失，请重新打开楼中楼后重试"
    }
}

internal fun validateNewThread(forumId: Long, forumName: String, title: String, content: String) {
    require(forumId > 0 && forumName.isNotBlank()) { "贴吧信息未加载完整，请返回刷新后重试" }
    require(title.isNotBlank()) { "请填写帖子标题" }
    require(title.trim().codePointCount(0, title.trim().length) <= 30) { "帖子标题最多 30 个字" }
    require(content.isNotBlank()) { "请填写正文或添加图片" }
}
