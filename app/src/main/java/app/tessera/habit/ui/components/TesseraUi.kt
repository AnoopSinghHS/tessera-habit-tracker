package app.tessera.habit.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.tessera.habit.ui.theme.TesseraTheme
import app.tessera.habit.ui.theme.TesseraType

/**
 * Shared layout constants. Use these instead of ad-hoc dp values so every screen
 * sits on the same grid.
 */
object TesseraDimens {
    /** Spacing steps: 4, 8, 12, 16, 24, 32. */
    val SpaceXs = 4.dp
    val SpaceS = 8.dp
    val SpaceM = 12.dp
    val SpaceL = 16.dp
    val SpaceXl = 24.dp
    val SpaceXxl = 32.dp

    /** Horizontal page margin. */
    val ScreenPadding = 16.dp

    /** Corner radii: cards, controls (buttons, fields, inner panels), heatmap cells. */
    val RadiusCard = 20.dp
    val RadiusControl = 14.dp
    val RadiusCell = 3.dp

    /** Minimum height and width of anything tappable. */
    val TouchTarget = 48.dp
}

/**
 * The standard surface: a filled, borderless rounded card. Do not nest a bordered
 * box inside it; separate groups inside a card with [TesseraDivider] or spacing.
 */
@Composable
fun TesseraCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    contentPadding: Dp = TesseraDimens.SpaceL,
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = TesseraTheme.colors
    val shape = RoundedCornerShape(TesseraDimens.RadiusCard)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.surfaceCard)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(contentPadding),
        content = content
    )
}

/** Sentence-case section heading placed above a card or group. */
@Composable
fun SectionHeading(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        style = TesseraType.Label,
        color = TesseraTheme.colors.textSecondary,
        modifier = modifier.padding(horizontal = TesseraDimens.SpaceXs)
    )
}

/** Hairline between rows inside a card. */
@Composable
fun TesseraDivider(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(TesseraTheme.colors.borderHairline)
    )
}

/** A "label ... value" row, as used in stats lists. */
@Composable
fun StatRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    val colors = TesseraTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = TesseraDimens.SpaceM),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = TesseraType.Body,
            color = colors.textSecondary,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(TesseraDimens.SpaceM))
        Text(
            text = value,
            style = TesseraType.BodyStrong,
            color = colors.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/** The main action on a screen or sheet. Filled with the accent. One per view. */
@Composable
fun TesseraPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null
) {
    val colors = TesseraTheme.colors
    val container = if (enabled) colors.accent else colors.surfaceElevated
    val content = if (enabled) colors.onAccent else colors.textSecondary
    TesseraButtonLayout(
        text = text,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        icon = icon,
        containerColor = container,
        contentColor = content,
        height = 52.dp
    )
}

/** Any other button. Quiet fill; set [danger] for destructive actions. */
@Composable
fun TesseraSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    danger: Boolean = false,
    icon: ImageVector? = null
) {
    val colors = TesseraTheme.colors
    val content = when {
        !enabled -> colors.textSecondary.copy(alpha = 0.6f)
        danger -> colors.statusDanger
        else -> colors.textPrimary
    }
    TesseraButtonLayout(
        text = text,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        icon = icon,
        containerColor = colors.surfaceElevated,
        contentColor = content,
        height = TesseraDimens.TouchTarget
    )
}

@Composable
private fun TesseraButtonLayout(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier,
    enabled: Boolean,
    icon: ImageVector?,
    containerColor: Color,
    contentColor: Color,
    height: Dp
) {
    val shape = RoundedCornerShape(50)
    Row(
        modifier = modifier
            .defaultMinSize(minHeight = height)
            .clip(shape)
            .background(containerColor)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = TesseraDimens.SpaceXl),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(TesseraDimens.SpaceS))
        }
        Text(
            text = text,
            style = TesseraType.Label,
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/** A selectable pill: category filters, option pickers. */
@Composable
fun TesseraChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = TesseraTheme.colors
    val shape = RoundedCornerShape(50)
    Box(
        modifier = modifier
            .defaultMinSize(minHeight = 40.dp)
            .clip(shape)
            .background(if (selected) colors.accent else colors.surfaceCard)
            .then(
                if (selected) Modifier else Modifier.border(1.dp, colors.borderHairline, shape)
            )
            .clickable(role = Role.Tab, onClick = onClick)
            .padding(horizontal = TesseraDimens.SpaceL),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = TesseraType.Label,
            color = if (selected) colors.onAccent else colors.textPrimary,
            maxLines = 1
        )
    }
}

/** A round 48dp icon button on a quiet fill. */
@Composable
fun TesseraIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    filled: Boolean = true,
    tint: Color = TesseraTheme.colors.textPrimary
) {
    val colors = TesseraTheme.colors
    Box(
        modifier = modifier
            .size(TesseraDimens.TouchTarget)
            .clip(CircleShape)
            .background(if (filled) colors.surfaceCard else Color.Transparent)
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(22.dp)
        )
    }
}

/** Colours for every OutlinedTextField in the app. */
@Composable
fun tesseraTextFieldColors(): TextFieldColors {
    val colors = TesseraTheme.colors
    return OutlinedTextFieldDefaults.colors(
        focusedTextColor = colors.textPrimary,
        unfocusedTextColor = colors.textPrimary,
        focusedContainerColor = colors.surfaceCard,
        unfocusedContainerColor = colors.surfaceCard,
        cursorColor = colors.accent,
        focusedBorderColor = colors.accent,
        unfocusedBorderColor = colors.borderSpecular,
        focusedLabelColor = colors.accent,
        unfocusedLabelColor = colors.textSecondary,
        focusedPlaceholderColor = colors.textSecondary,
        unfocusedPlaceholderColor = colors.textSecondary,
        focusedSupportingTextColor = colors.textSecondary,
        unfocusedSupportingTextColor = colors.textSecondary
    )
}
