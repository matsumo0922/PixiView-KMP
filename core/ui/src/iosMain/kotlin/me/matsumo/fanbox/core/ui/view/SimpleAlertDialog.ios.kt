package me.matsumo.fanbox.core.ui.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.window.Dialog
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCAction
import platform.UIKit.UIAlertAction
import platform.UIKit.UIAlertActionStyleDefault
import platform.UIKit.UIAlertController
import platform.UIKit.UIAlertControllerStyleAlert
import platform.UIKit.UIApplication
import platform.objc.sel_registerName

@Composable
actual fun SimpleAlertDialog(
    title: String,
    description: String,
    positiveText: String?,
    negativeText: String?,
    onClickPositive: () -> Unit,
    onClickNegative: () -> Unit,
    isCaution: Boolean,
) {
    Dialog(
        onDismissRequest = { onClickNegative.invoke() },
    ) {
        val alertDialogManager = remember {
            AlertDialogManager(
                title = title,
                description = description,
                positiveText = positiveText,
                negativeText = negativeText,
                onClickPositive = onClickPositive,
                onClickNegative = onClickNegative,
                isCaution = isCaution,
            )
        }

        LaunchedEffect(title, description, positiveText, negativeText, onClickPositive, onClickNegative, isCaution) {
            alertDialogManager.title = title
            alertDialogManager.description = description
            alertDialogManager.positiveText = positiveText
            alertDialogManager.negativeText = negativeText
            alertDialogManager.onClickPositive = onClickPositive
            alertDialogManager.onClickNegative = onClickNegative
            alertDialogManager.isCaution = isCaution
        }

        LaunchedEffect(true) {
            alertDialogManager.show()
        }
    }
}

private class AlertDialogManager(
    var title: String,
    var description: String,
    var positiveText: String?,
    var negativeText: String?,
    var onClickPositive: () -> Unit,
    var onClickNegative: () -> Unit,
    var isCaution: Boolean,
) {
    private var isPresented = false
    private var isAnimating = false

    @OptIn(ExperimentalForeignApi::class)
    private val dismissPointer = sel_registerName("dismiss")

    private val onDismissLambda = {
        UIApplication.sharedApplication.keyWindow?.rootViewController?.dismissViewControllerAnimated(
            flag = true,
            completion = {
                isPresented = false
                isAnimating = false
                onClickNegative.invoke()
            },
        )
    }

    @OptIn(BetaInteropApi::class)
    @ObjCAction
    fun dismiss() {
        if (!isPresented || isAnimating) return

        isAnimating = true
        onDismissLambda.invoke()
    }

    @OptIn(ExperimentalForeignApi::class)
    fun show() {
        if (isPresented || isAnimating) return

        isAnimating = true

        val alertController = UIAlertController.alertControllerWithTitle(
            title = title,
            message = description,
            preferredStyle = UIAlertControllerStyleAlert,
        )

        if (positiveText != null) {
            alertController.addAction(
                UIAlertAction.actionWithTitle(
                    title = positiveText,
                    style = UIAlertActionStyleDefault,
                    handler = { onClickPositive.invoke() },
                ),
            )
        }

        if (negativeText != null) {
            alertController.addAction(
                UIAlertAction.actionWithTitle(
                    title = negativeText,
                    style = UIAlertActionStyleDefault,
                    handler = { onClickNegative.invoke() },
                ),
            )
        }

        UIApplication.sharedApplication.keyWindow?.rootViewController?.presentViewController(
            viewControllerToPresent = alertController,
            animated = true,
            completion = {
                isPresented = true
                isAnimating = false
            },
        )
    }
}
