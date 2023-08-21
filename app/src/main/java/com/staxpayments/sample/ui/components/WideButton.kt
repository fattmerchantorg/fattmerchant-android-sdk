package com.staxpayments.sample.ui.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.staxpayments.sample.ui.theme.Gray50
import com.staxpayments.sample.ui.theme.Purple500
import com.staxpayments.sample.ui.theme.Purple800
import com.staxpayments.sample.ui.theme.StaxAndroidSDKTheme

@Composable
fun WideButton(
    modifier: Modifier = Modifier,
    text: String = "",
    onClick: () -> Unit = {}
) {

    Row(
        modifier = modifier.fillMaxWidth()
    ) {
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { onClick() },
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if(isSystemInDarkTheme()) Purple800 else Purple500
            )
        ) {
            Text(
                text = text.uppercase(),
                color = Gray50,
            )
        }
    }
}

@Preview(device = Devices.NEXUS_5)
@Composable
private fun WideButtonPreview() {
    StaxAndroidSDKTheme {
        WideButton(
            modifier = Modifier.padding(horizontal = 40.dp),
            text = "Hello",
        )
    }
}