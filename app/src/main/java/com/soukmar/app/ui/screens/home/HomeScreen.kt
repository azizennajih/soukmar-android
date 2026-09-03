@file:OptIn(ExperimentalMaterial3Api::class)

package com.soukmar.app.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.soukmar.app.ui.components.SoukMarLogo
import com.soukmar.app.ui.model.CATEGORIES
import com.soukmar.app.ui.theme.BorderColor
import com.soukmar.app.ui.theme.TextMuted
import com.soukmar.app.ui.theme.TextPrimary
import com.soukmar.app.ui.theme.WhiteColor

@Composable
fun HomeScreen(
    onLoggedOut: () -> Unit,
    onOpenCategory: (String) -> Unit,
    onOpenSearch: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { SoukMarLogo() },
                actions = {
                    IconButton(onClick = { viewModel.logout(onLoggedOut) }) {
                        Icon(Icons.Filled.Logout, contentDescription = "Se déconnecter")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            viewModel.user?.let { u ->
                Text("Bonjour, ${u.name} 👋", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                Spacer(Modifier.height(12.dp))
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onOpenSearch)
                    .background(WhiteColor, RoundedCornerShape(999.dp))
                    .border(1.dp, BorderColor, RoundedCornerShape(999.dp))
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.Search, contentDescription = null, tint = TextMuted)
                Spacer(Modifier.width(8.dp))
                Text("Rechercher sur SoukMar…", color = TextMuted)
            }

            Spacer(Modifier.height(20.dp))
            Text("Catégories", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = TextPrimary)
            Spacer(Modifier.height(10.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(CATEGORIES, key = { it.value }) { cat ->
                    Column(
                        modifier = Modifier
                            .clickable { onOpenCategory(cat.value) }
                            .background(cat.bg, RoundedCornerShape(14.dp))
                            .padding(vertical = 16.dp, horizontal = 8.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(cat.emoji, fontSize = 26.sp)
                        Spacer(Modifier.height(6.dp))
                        Text(cat.label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = cat.fg, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    }
                }
            }
        }
    }
}
