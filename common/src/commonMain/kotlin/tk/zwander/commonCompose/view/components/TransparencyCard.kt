package tk.zwander.commonCompose.view.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import tk.zwander.commonCompose.view.LocalUseMicaEffect

@Composable
fun TransparencyCard(
    modifier: Modifier = Modifier,
    shape: Shape = CardDefaults.shape,
    elevation: CardElevation = CardDefaults.cardElevation(),
    border: BorderStroke? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = modifier,
        shape = shape,
        elevation = elevation,
        border = border,
        content = content,
        colors = CardDefaults.cardColors(
            containerColor = if (LocalUseMicaEffect.current) MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
        ),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransparencyCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = CardDefaults.shape,
    border: BorderStroke? = null,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = modifier,
        shape = shape,
        elevation = CardDefaults.outlinedCardElevation(),
        border = border,
        content = content,
        colors = CardDefaults.cardColors(
            containerColor = if (LocalUseMicaEffect.current) MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
        ),
        enabled = enabled,
        interactionSource = interactionSource,
        onClick = onClick,
    )
}
