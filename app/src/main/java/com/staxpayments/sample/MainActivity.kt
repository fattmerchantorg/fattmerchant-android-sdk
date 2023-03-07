package com.staxpayments.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.staxpayments.sample.ui.screens.MainScreen
import com.staxpayments.sample.ui.theme.StaxAndroidSDKTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            StaxAndroidSDKTheme {
                MainScreen()
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
private fun MainActivityPreview() {
    StaxAndroidSDKTheme {
        MainScreen()
    }
}