package com.soukmar.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.soukmar.app.ui.i18n.t
import com.soukmar.app.ui.theme.Primary

/** Follow/unfollow toggle for a seller or buyer profile — mirrors the web's
 * app-follow-button (reused on both SellerProfileScreen and
 * MesAbonnementsScreen). No confirmation dialog: following isn't
 * destructive, unlike blocking a user in chat. */
@Composable
fun FollowButton(following: Boolean, submitting: Boolean, onToggle: () -> Unit) {
    if (following) {
        OutlinedButton(
            onClick = onToggle,
            enabled = !submitting,
            border = BorderStroke(1.dp, Primary),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Primary)
        ) {
            Icon(Icons.Filled.PersonRemove, contentDescription = null, modifier = androidx.compose.ui.Modifier.padding(end = 6.dp))
            Text(t("seller.unfollow"))
        }
    } else {
        Button(onClick = onToggle, enabled = !submitting, colors = ButtonDefaults.buttonColors(containerColor = Primary)) {
            Icon(Icons.Filled.PersonAdd, contentDescription = null, modifier = androidx.compose.ui.Modifier.padding(end = 6.dp))
            Text(t("seller.follow"))
        }
    }
}
