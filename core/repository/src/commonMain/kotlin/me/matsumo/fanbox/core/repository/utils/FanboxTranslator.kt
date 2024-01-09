package me.matsumo.fanbox.core.repository.utils

import io.github.aakira.napier.Napier
import io.ktor.http.parseUrlEncodedParameters
import kotlinx.datetime.Instant
import me.matsumo.fanbox.core.model.FanboxTag
import me.matsumo.fanbox.core.model.PageCursorInfo
import me.matsumo.fanbox.core.model.PageNumberInfo
import me.matsumo.fanbox.core.model.PageOffsetInfo
import me.matsumo.fanbox.core.model.fanbox.FanboxBell
import me.matsumo.fanbox.core.model.fanbox.FanboxCover
import me.matsumo.fanbox.core.model.fanbox.FanboxCreator
import me.matsumo.fanbox.core.model.fanbox.FanboxCreatorDetail
import me.matsumo.fanbox.core.model.fanbox.FanboxCreatorPlan
import me.matsumo.fanbox.core.model.fanbox.FanboxCreatorPlanDetail
import me.matsumo.fanbox.core.model.fanbox.FanboxCreatorTag
import me.matsumo.fanbox.core.model.fanbox.FanboxCursor
import me.matsumo.fanbox.core.model.fanbox.FanboxMetaData
import me.matsumo.fanbox.core.model.fanbox.FanboxNewsLetter
import me.matsumo.fanbox.core.model.fanbox.FanboxPaidRecord
import me.matsumo.fanbox.core.model.fanbox.FanboxPost
import me.matsumo.fanbox.core.model.fanbox.FanboxPostDetail
import me.matsumo.fanbox.core.model.fanbox.FanboxUser
import me.matsumo.fanbox.core.model.fanbox.PaymentMethod
import me.matsumo.fanbox.core.model.fanbox.entity.FanboxBellItemsEntity
import me.matsumo.fanbox.core.model.fanbox.entity.FanboxCreatorEntity
import me.matsumo.fanbox.core.model.fanbox.entity.FanboxCreatorItemsEntity
import me.matsumo.fanbox.core.model.fanbox.entity.FanboxCreatorPlanEntity
import me.matsumo.fanbox.core.model.fanbox.entity.FanboxCreatorPlansEntity
import me.matsumo.fanbox.core.model.fanbox.entity.FanboxCreatorPostsPaginateEntity
import me.matsumo.fanbox.core.model.fanbox.entity.FanboxCreatorSearchEntity
import me.matsumo.fanbox.core.model.fanbox.entity.FanboxCreatorTagsEntity
import me.matsumo.fanbox.core.model.fanbox.entity.FanboxMetaDataEntity
import me.matsumo.fanbox.core.model.fanbox.entity.FanboxNewsLettersEntity
import me.matsumo.fanbox.core.model.fanbox.entity.FanboxPaidRecordEntity
import me.matsumo.fanbox.core.model.fanbox.entity.FanboxPostCommentItemsEntity
import me.matsumo.fanbox.core.model.fanbox.entity.FanboxPostDetailEntity
import me.matsumo.fanbox.core.model.fanbox.entity.FanboxPostItemsEntity
import me.matsumo.fanbox.core.model.fanbox.entity.FanboxPostSearchEntity
import me.matsumo.fanbox.core.model.fanbox.entity.FanboxTagsEntity
import me.matsumo.fanbox.core.model.fanbox.id.CommentId
import me.matsumo.fanbox.core.model.fanbox.id.CreatorId
import me.matsumo.fanbox.core.model.fanbox.id.NewsLetterId
import me.matsumo.fanbox.core.model.fanbox.id.PlanId
import me.matsumo.fanbox.core.model.fanbox.id.PostId

internal fun FanboxPostItemsEntity.translate(bookmarkedPosts: List<PostId>): PageCursorInfo<FanboxPost> {
    return PageCursorInfo(
        contents = body.items.map { it.translate(bookmarkedPosts) },
        cursor = body.nextUrl?.translateToCursor(),
    )
}

internal fun FanboxPostItemsEntity.Body.Item.translate(bookmarkedPosts: List<PostId>): FanboxPost {
    return FanboxPost(
        id = PostId(id),
        title = title,
        excerpt = excerpt,
        publishedDatetime = Instant.parse(publishedDatetime),
        updatedDatetime = Instant.parse(updatedDatetime),
        isLiked = isLiked,
        isBookmarked = bookmarkedPosts.contains(PostId(id)),
        likeCount = likeCount,
        commentCount = commentCount,
        feeRequired = feeRequired,
        isRestricted = isRestricted,
        hasAdultContent = hasAdultContent,
        tags = tags,
        cover = cover?.let { cover ->
            FanboxCover(
                type = cover.type,
                url = cover.url,
            )
        },
        user = FanboxUser(
            userId = user.userId,
            creatorId = CreatorId(creatorId),
            name = user.name,
            iconUrl = user.iconUrl,
        ),
    )
}

internal fun FanboxCreatorEntity.translate(): FanboxCreatorDetail {
    return body.translate()
}

internal fun FanboxCreatorItemsEntity.translate(): List<FanboxCreatorDetail> {
    return body.map { it.translate() }
}

internal fun FanboxCreatorEntity.Body.translate(): FanboxCreatorDetail {
    return FanboxCreatorDetail(
        creatorId = CreatorId(creatorId),
        coverImageUrl = coverImageUrl,
        description = description,
        hasAdultContent = hasAdultContent,
        hasBoothShop = hasBoothShop,
        isAcceptingRequest = isAcceptingRequest,
        isFollowed = isFollowed,
        isStopped = isStopped,
        isSupported = isSupported,
        profileItems = profileItems.map { profileItem ->
            FanboxCreatorDetail.ProfileItem(
                id = profileItem.id,
                imageUrl = profileItem.imageUrl,
                thumbnailUrl = profileItem.thumbnailUrl,
                type = profileItem.type,
            )
        },
        profileLinks = profileLinks.map { profileLink ->
            FanboxCreatorDetail.ProfileLink(
                url = profileLink,
                link = FanboxCreatorDetail.Platform.fromUrl(profileLink),
            )
        },
        user = FanboxUser(
            userId = user.userId,
            creatorId = CreatorId(creatorId),
            name = user.name,
            iconUrl = user.iconUrl,
        ),
    )
}

internal fun FanboxPostDetailEntity.translate(bookmarkedPosts: List<PostId>): FanboxPostDetail {
    var bodyBlock: FanboxPostDetail.Body = FanboxPostDetail.Body.Unknown

    if (!body.body?.blocks.isNullOrEmpty()) {
        body.body?.blocks?.let { blocks ->
            // 文字列や画像、ファイルなどのブロックが混在している場合

            val images = body.body?.imageMap.orEmpty()
            val files = body.body?.fileMap.orEmpty()
            val urls = body.body?.urlEmbedMap.orEmpty()

            bodyBlock = FanboxPostDetail.Body.Article(
                blocks = blocks.mapNotNull {
                    when {
                        it.text != null -> {
                            if (it.text!!.isEmpty()) null else FanboxPostDetail.Body.Article.Block.Text(it.text!!)
                        }

                        it.imageId != null -> {
                            images[it.imageId!!]?.let { image ->
                                FanboxPostDetail.Body.Article.Block.Image(
                                    FanboxPostDetail.ImageItem(
                                        id = image.id,
                                        postId = PostId(body.id),
                                        extension = image.extension,
                                        originalUrl = image.originalUrl,
                                        thumbnailUrl = image.thumbnailUrl,
                                        aspectRatio = image.width.toFloat() / image.height.toFloat(),
                                    ),
                                )
                            }
                        }

                        it.fileId != null -> {
                            files[it.fileId!!]?.let { file ->
                                FanboxPostDetail.Body.Article.Block.File(
                                    FanboxPostDetail.FileItem(
                                        id = file.id,
                                        postId = PostId(body.id),
                                        extension = file.extension,
                                        name = file.name,
                                        size = file.size,
                                        url = file.url,
                                    ),
                                )
                            }
                        }

                        it.urlEmbedId != null -> {
                            urls[it.urlEmbedId!!]?.let { url ->
                                FanboxPostDetail.Body.Article.Block.Link(
                                    html = url.html,
                                    post = url.postInfo?.translate(bookmarkedPosts),
                                )
                            }
                        }

                        else -> {
                            Napier.w { "FanboxPostDetailEntity translate error: Unknown block type. $it" }
                            null
                        }
                    }
                },
            )
        }
    }

    if (!body.body?.images.isNullOrEmpty()) {
        body.body?.images?.let { blocks ->
            // 画像のみのブロックの場合

            bodyBlock = FanboxPostDetail.Body.Image(
                text = body.body?.text.orEmpty(),
                images = blocks.map {
                    FanboxPostDetail.ImageItem(
                        id = it.id,
                        postId = PostId(body.id),
                        extension = it.extension,
                        originalUrl = it.originalUrl,
                        thumbnailUrl = it.thumbnailUrl,
                        aspectRatio = it.width.toFloat() / it.height.toFloat(),
                    )
                },
            )
        }
    }

    if (!body.body?.files.isNullOrEmpty()) {
        body.body?.files?.let { blocks ->
            // ファイルのみのブロックの場合

            bodyBlock = FanboxPostDetail.Body.File(
                text = body.body?.text.orEmpty(),
                files = blocks.map {
                    FanboxPostDetail.FileItem(
                        id = it.id,
                        postId = PostId(body.id),
                        name = it.name,
                        extension = it.extension,
                        size = it.size,
                        url = it.url,
                    )
                },
            )
        }
    }

    return FanboxPostDetail(
        id = PostId(body.id),
        title = body.title,
        publishedDatetime = Instant.parse(body.publishedDatetime),
        updatedDatetime = Instant.parse(body.updatedDatetime),
        isLiked = body.isLiked,
        isBookmarked = bookmarkedPosts.contains(PostId(body.id)),
        likeCount = body.likeCount,
        coverImageUrl = body.coverImageUrl,
        commentCount = body.commentCount,
        feeRequired = body.feeRequired,
        isRestricted = body.isRestricted,
        hasAdultContent = body.hasAdultContent,
        tags = body.tags,
        user = FanboxUser(
            userId = body.user.userId,
            creatorId = CreatorId(body.creatorId),
            name = body.user.name,
            iconUrl = body.user.iconUrl,
        ),
        body = bodyBlock,
        excerpt = body.excerpt,
        commentList = PageOffsetInfo(
            contents = body.commentList.items.map { it.translate() },
            offset = body.commentList.nextUrl?.let { it.parseUrlEncodedParameters()["offset"]?.toIntOrNull() },
        ),
        nextPost = body.nextPost?.let {
            FanboxPostDetail.OtherPost(
                id = PostId(it.id),
                title = it.title,
                publishedDatetime = Instant.parse(it.publishedDatetime),
            )
        },
        prevPost = body.prevPost?.let {
            FanboxPostDetail.OtherPost(
                id = PostId(it.id),
                title = it.title,
                publishedDatetime = Instant.parse(it.publishedDatetime),
            )
        },
        imageForShare = body.imageForShare,
    )
}

internal fun FanboxPostDetailEntity.Body.CommentList.Item.translate(): FanboxPostDetail.Comment.CommentItem {
    return FanboxPostDetail.Comment.CommentItem(
        body = body,
        createdDatetime = Instant.parse(createdDatetime),
        id = CommentId(id),
        isLiked = isLiked,
        isOwn = isOwn,
        likeCount = likeCount,
        parentCommentId = CommentId(parentCommentId),
        rootCommentId = CommentId(rootCommentId),
        replies = replies.map { it.translate() }.sortedBy { it.createdDatetime },
        user = FanboxUser(
            userId = user.userId,
            creatorId = CreatorId(""),
            name = user.name,
            iconUrl = user.iconUrl,
        ),
    )
}

internal fun FanboxCreatorTagsEntity.translate(): List<FanboxCreatorTag> {
    return body.map {
        FanboxCreatorTag(
            count = it.count,
            coverImageUrl = it.coverImageUrl,
            name = it.tag,
        )
    }
}

internal fun FanboxCreatorPlansEntity.translate(): List<FanboxCreatorPlan> {
    return body.map {
        FanboxCreatorPlan(
            coverImageUrl = it.coverImageUrl,
            description = it.description,
            fee = it.fee,
            hasAdultContent = it.hasAdultContent,
            id = PlanId(it.id),
            paymentMethod = PaymentMethod.fromString(it.paymentMethod),
            title = it.title,
            user = FanboxUser(
                userId = it.user.userId,
                creatorId = CreatorId(it.creatorId),
                name = it.user.name,
                iconUrl = it.user.iconUrl,
            ),
        )
    }
}

internal fun FanboxCreatorPlanEntity.translate(): FanboxCreatorPlanDetail {
    return FanboxCreatorPlanDetail(
        plan = FanboxCreatorPlan(
            id = PlanId(body.plan.id),
            title = body.plan.title,
            description = body.plan.description,
            fee = body.plan.fee,
            coverImageUrl = body.plan.coverImageUrl,
            hasAdultContent = body.plan.hasAdultContent,
            paymentMethod = PaymentMethod.fromString(body.plan.paymentMethod),
            user = FanboxUser(
                userId = body.plan.user.userId,
                creatorId = CreatorId(body.plan.creatorId),
                name = body.plan.user.name,
                iconUrl = body.plan.user.iconUrl,
            ),
        ),
        supportStartDatetime = body.supportStartDatetime,
        supportTransactions = body.supportTransactions.map {
            FanboxCreatorPlanDetail.SupportTransaction(
                id = it.id,
                paidAmount = it.paidAmount,
                transactionDatetime = Instant.parse(it.transactionDatetime),
                targetMonth = it.targetMonth,
                user = FanboxUser(
                    userId = it.supporter.userId,
                    creatorId = CreatorId(body.plan.creatorId),
                    name = it.supporter.name,
                    iconUrl = it.supporter.iconUrl,
                ),
            )
        },
        supporterCardImageUrl = body.supporterCardImageUrl,
    )
}

internal fun FanboxPaidRecordEntity.translate(): List<FanboxPaidRecord> {
    return body.map {
        FanboxPaidRecord(
            id = it.id,
            paidAmount = it.paidAmount,
            paymentDateTime = Instant.parse(it.paymentDatetime),
            paymentMethod = PaymentMethod.fromString(it.paymentMethod),
            creator = FanboxCreator(
                creatorId = it.creator.creatorId?.let { id -> CreatorId(id) },
                user = FanboxUser(
                    userId = it.creator.user.userId,
                    creatorId = CreatorId(it.creator.creatorId.orEmpty()),
                    name = it.creator.user.name,
                    iconUrl = it.creator.user.iconUrl,
                ),
            ),
        )
    }
}

internal fun FanboxNewsLettersEntity.translate(): List<FanboxNewsLetter> {
    return body.map {
        FanboxNewsLetter(
            body = it.body,
            createdAt = Instant.parse(it.createdAt),
            creator = FanboxCreator(
                creatorId = CreatorId(it.creator.creatorId),
                user = FanboxUser(
                    userId = it.creator.user.userId,
                    creatorId = CreatorId(it.creator.creatorId),
                    name = it.creator.user.name,
                    iconUrl = it.creator.user.iconUrl,
                ),
            ),
            id = NewsLetterId(it.id),
            isRead = it.isRead,
        )
    }
}

internal fun FanboxBellItemsEntity.translate(): PageNumberInfo<FanboxBell> {
    return PageNumberInfo(
        contents = body.items.mapNotNull {
            when (it.type) {
                "on_post_published" -> {
                    FanboxBell.PostPublished(
                        id = PostId(it.post!!.id),
                        notifiedDatetime = Instant.parse(it.notifiedDatetime),
                        post = it.post!!.translate(emptyList()),
                    )
                }

                "post_comment" -> {
                    FanboxBell.Comment(
                        id = CommentId(it.id),
                        notifiedDatetime = Instant.parse(it.notifiedDatetime),
                        comment = it.postCommentBody!!,
                        isRootComment = it.isRootComment!!,
                        creatorId = CreatorId(it.creatorId!!),
                        postId = PostId(it.postId!!),
                        postTitle = it.postTitle!!,
                        userName = it.userName!!,
                        userProfileIconUrl = it.userProfileImg!!,
                    )
                }

                "post_comment_like" -> {
                    FanboxBell.Like(
                        id = it.id,
                        notifiedDatetime = Instant.parse(it.notifiedDatetime),
                        comment = it.postCommentBody!!,
                        creatorId = CreatorId(it.creatorId!!),
                        postId = PostId(it.postId!!),
                        count = it.count!!,
                    )
                }

                else -> {
                    Napier.w { "FanboxBellItemsEntity translate error: Unknown bell type. $it" }
                    null
                }
            }
        },
        nextPage = body.nextUrl?.let { it.parseUrlEncodedParameters()["page"]?.toIntOrNull() },
    )
}

internal fun FanboxMetaDataEntity.translate(): FanboxMetaData {
    return FanboxMetaData(
        apiUrl = apiUrl,
        csrfToken = csrfToken,
        context = FanboxMetaData.Context(
            privacyPolicy = FanboxMetaData.Context.PrivacyPolicy(
                policyUrl = context.privacyPolicy.policyUrl,
                revisionHistoryUrl = context.privacyPolicy.revisionHistoryUrl,
                shouldShowNotice = context.privacyPolicy.shouldShowNotice,
                updateDate = context.privacyPolicy.updateDate,
            ),
            user = FanboxMetaData.Context.User(
                creatorId = context.user.creatorId?.let { id -> CreatorId(id) },
                fanboxUserStatus = context.user.fanboxUserStatus,
                hasAdultContent = context.user.hasAdultContent ?: false,
                hasUnpaidPayments = context.user.hasUnpaidPayments,
                iconUrl = context.user.iconUrl,
                isCreator = context.user.isCreator,
                isMailAddressOutdated = context.user.isMailAddressOutdated,
                isSupporter = context.user.isSupporter,
                lang = context.user.lang,
                name = context.user.name,
                planCount = context.user.planCount,
                showAdultContent = context.user.showAdultContent,
                userId = context.user.userId,
            ),
        ),
    )
}

internal fun FanboxCreatorSearchEntity.translate(): PageNumberInfo<FanboxCreatorDetail> {
    return PageNumberInfo(
        contents = body.creators.map { it.translate() },
        nextPage = body.nextPage,
    )
}

internal fun FanboxPostSearchEntity.translate(bookmarkedPosts: List<PostId>): PageNumberInfo<FanboxPost> {
    return PageNumberInfo(
        contents = body.items.map { it.translate(bookmarkedPosts) },
        nextPage = body.nextUrl?.let { it.parseUrlEncodedParameters()["page"]?.toIntOrNull() },
    )
}

internal fun FanboxPostCommentItemsEntity.translate(): PageOffsetInfo<FanboxPostDetail.Comment.CommentItem> {
    return PageOffsetInfo(
        contents = body.items.map { it.translate() },
        offset = body.nextUrl?.let { it.parseUrlEncodedParameters()["page"]?.toIntOrNull() },
    )
}

internal fun FanboxCreatorPostsPaginateEntity.translate(): List<FanboxCursor> {
    return body.map { it.translateToCursor() }
}

internal fun FanboxTagsEntity.translate(): List<FanboxTag> {
    return body.map {
        FanboxTag(
            name = it.value,
            count = it.count,
        )
    }
}

private fun String.translateToCursor(): FanboxCursor {
    val parameters = this.substringAfter("?")
        .split("&")
        .associate {
            val (key, value) = it.split("=")
            key to value
        }

    return FanboxCursor(
        maxPublishedDatetime = parameters["maxPublishedDatetime"]!!,
        maxId = parameters["maxId"]!!,
    )
}
