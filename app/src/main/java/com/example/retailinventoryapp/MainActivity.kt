package com.example.retailinventoryapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavGraph
import androidx.navigation.compose.rememberNavController
import com.example.retailinventoryapp.ui.navigation.AppNavigation
import com.example.retailinventoryapp.ui.theme.RetailAppTheme
import com.example.retailinventoryapp.ui.theme.RetailColors
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RetailAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = RetailColors.Background
                ) {
                    val navController = rememberNavController()

                    // Remember it for later use
                    remember { navController }

                    // Launch the navigation graph
                    AppNavigation(navController = navController)
                }
            }
        }
    }
}