package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.FileManagerScreen
import com.example.ui.FileManagerViewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      val viewModel: FileManagerViewModel = viewModel()
      val uiState by viewModel.uiState.collectAsStateWithLifecycle()
      MyApplicationTheme(darkTheme = uiState.isDarkTheme) {
        FileManagerScreen(viewModel = viewModel)
      }
    }
  }
}

