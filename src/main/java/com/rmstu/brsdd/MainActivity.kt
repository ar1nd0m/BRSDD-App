package com.rmstu.brsdd

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.rmstu.brsdd.ui.AppRoot
import com.rmstu.brsdd.ui.theme.BRSDDTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BRSDDApp()
        }
    }
}

@Composable
fun BRSDDApp() {
    BRSDDTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            AppRoot()
        }
    }
}
