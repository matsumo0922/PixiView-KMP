package me.matsumo.fanbox.core.ui.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.calf.ui.progress.AdaptiveCircularProgressIndicator

@Composable
fun LoadingView(
    modifier: Modifier = Modifier,
) {
    Box(modifier) {
        AdaptiveCircularProgressIndicator(
            modifier = Modifier
                .padding(24.dp)
                .align(Alignment.Center),
            color = MaterialTheme.colorScheme.primary,
        )
    }
}
