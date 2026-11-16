package com.soukmar.app.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.soukmar.app.ui.i18n.t
import com.soukmar.app.ui.theme.BorderColor
import com.soukmar.app.ui.theme.IconSize
import com.soukmar.app.ui.theme.Primary
import com.soukmar.app.ui.theme.TextMuted
import com.soukmar.app.ui.theme.TextPrimary
import com.soukmar.app.ui.theme.WhiteColor

/** The four tab destinations of the bottom bar; "post a listing" in the middle is an action, not a tab. */
val MAIN_TAB_ROUTES = setOf(Routes.HOME, Routes.CATEGORIES, Routes.CHAT_LIST, Routes.ACCOUNT)

/**
 * Bottom bar of the main screens: home, categories, post a listing (the red round button in the middle, the most visible
 * element), messages, my account. Same icon size (24 dp) and label style everywhere; outline icon when idle, filled and red
 * when selected. Sits above the system navigation bar (gesture or three-button) on every display size.
 */
@Composable
fun MainBottomBar(currentRoute: String?, onSelectTab: (String) -> Unit, onPost: () -> Unit) {
    Column(Modifier.fillMaxWidth().background(WhiteColor)) {
        HorizontalDivider(color = BorderColor)
        Row(
            modifier = Modifier.fillMaxWidth().windowInsetsPadding(WindowInsets.navigationBars).height(64.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TabItem(Icons.Outlined.Home, Icons.Filled.Home, t("nav.home"), currentRoute == Routes.HOME) { onSelectTab(Routes.HOME) }
            TabItem(Icons.Outlined.GridView, Icons.Filled.GridView, t("nav.categories"), currentRoute == Routes.CATEGORIES) { onSelectTab(Routes.CATEGORIES) }
            PostItem(label = t("nav.post_btn"), description = t("nav.post_full"), onClick = onPost)
            TabItem(Icons.Outlined.ChatBubbleOutline, Icons.Filled.ChatBubble, t("nav.messages"), currentRoute == Routes.CHAT_LIST) { onSelectTab(Routes.CHAT_LIST) }
            TabItem(Icons.Outlined.PersonOutline, Icons.Filled.Person, t("nav.account"), currentRoute == Routes.ACCOUNT) { onSelectTab(Routes.ACCOUNT) }
        }
    }
}

@Composable
private fun RowScope.TabItem(icon: ImageVector, selectedIcon: ImageVector, label: String, selected: Boolean, onClick: () -> Unit) {
    val tint = if (selected) Primary else TextMuted
    Column(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .semantics { this.selected = selected }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                role = Role.Tab,
                onClick = onClick
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(if (selected) selectedIcon else icon, contentDescription = null, tint = tint, modifier = Modifier.size(IconSize.standard))
        Spacer(Modifier.height(4.dp))
        Text(label, color = tint, style = MaterialTheme.typography.labelSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun RowScope.PostItem(label: String, description: String, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                role = Role.Button,
                onClickLabel = description,
                onClick = onClick
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(Modifier.size(40.dp).background(Primary, CircleShape), contentAlignment = Alignment.Center) {
            Icon(Icons.Outlined.Add, contentDescription = null, tint = WhiteColor, modifier = Modifier.size(IconSize.standard))
        }
        Spacer(Modifier.height(2.dp))
        Text(label, color = TextPrimary, style = MaterialTheme.typography.labelSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}
