package com.mehmetbozkurt.questlog.core.designsystem.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.mehmetbozkurt.questlog.core.designsystem.uppercaseLocalized
import com.mehmetbozkurt.questlog.core.designsystem.theme.Spacing

@Composable
fun SectionEyebrow(
    title: String,
    modifier: Modifier = Modifier,
    trailing: String? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title.uppercaseLocalized(),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.width(Spacing.md))
        SectionRule(Modifier.weight(1f))
        if (trailing != null) {
            Spacer(Modifier.width(Spacing.md))
            Text(
                text = trailing,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.End,
            )
        }
    }
}
