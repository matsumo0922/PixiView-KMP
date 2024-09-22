package me.matsumo.fanbox.core.ui.view

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape

@OptIn(ExperimentalMaterial3Api::class)
@Composable
actual fun SimpleBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier,
    skipPartiallyExpanded: Boolean,
    content: @Composable ColumnScope.() -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded)

    ModalBottomSheet(
        modifier = Modifier.fillMaxWidth(),
        sheetState = sheetState,
        shape = RectangleShape,
        dragHandle = null,
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) },
        onDismissRequest = onDismissRequest,
    ) {
        content.invoke(this)
    }
}
