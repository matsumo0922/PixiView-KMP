package me.matsumo.fanbox.core.repository.mapper

import me.matsumo.fanbox.core.model.fanbox.CommentId
import me.matsumo.fanbox.core.model.fanbox.CreatorId
import me.matsumo.fanbox.core.model.fanbox.NewsLetterId
import me.matsumo.fanbox.core.model.fanbox.PlanId
import me.matsumo.fanbox.core.model.fanbox.PostId
import me.matsumo.fanbox.core.model.fanbox.PostItemId
import me.matsumo.fanbox.core.model.fanbox.UserId
import me.matsumo.fankt.fanbox.domain.model.id.FanboxCommentId
import me.matsumo.fankt.fanbox.domain.model.id.FanboxCreatorId
import me.matsumo.fankt.fanbox.domain.model.id.FanboxNewsLetterId
import me.matsumo.fankt.fanbox.domain.model.id.FanboxPlanId
import me.matsumo.fankt.fanbox.domain.model.id.FanboxPostId
import me.matsumo.fankt.fanbox.domain.model.id.FanboxPostItemId
import me.matsumo.fankt.fanbox.domain.model.id.FanboxUserId

fun FanboxPostId.toPostId() = PostId(value)
fun PostId.toFanboxPostId() = FanboxPostId(value)

fun FanboxPostItemId.toPostItemId() = PostItemId(value)

fun FanboxCreatorId.toCreatorId() = CreatorId(value)
fun CreatorId.toFanboxCreatorId() = FanboxCreatorId(value)

fun FanboxUserId.toUserId() = UserId(value)
fun UserId.toFanboxUserId() = FanboxUserId(value)

fun FanboxCommentId.toCommentId() = CommentId(value)
fun CommentId.toFanboxCommentId() = FanboxCommentId(value)

fun FanboxPlanId.toPlanId() = PlanId(value)

fun FanboxNewsLetterId.toNewsLetterId() = NewsLetterId(value)
