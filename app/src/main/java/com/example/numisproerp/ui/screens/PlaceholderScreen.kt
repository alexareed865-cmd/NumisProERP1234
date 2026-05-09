package com.numisproerp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.numisproerp.ui.i18n.tr

@Composable
fun PlaceholderScreen(title: String, navController: NavHostController? = null) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = title, fontSize = 20.sp)
        if (navController != null) {
            TextButton(onClick = { navController.popBackStack() }) {
                Text(tr("Повернутися назад", "Go back"))
            }
        }
    }
}