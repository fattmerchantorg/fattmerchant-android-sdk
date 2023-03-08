package com.staxpayments.sample.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

// Truffle Design Colors
// https://www.figma.com/file/3I0yooruw9JpjXSh9j3CRZ/Truffle-Design-System?node-id=0%3A1&t=rkjtLCUzwY00vxAn-0

val Purple50 = Color(0xFFF7E9FC)
val Purple100 = Color(0xFFEFD2F9)
val Purple400 = Color(0xFFC967EA)
val Purple500 = Color(0xFFB93BE4)
val Purple600 = Color(0xFF8C35B8)
val Purple700 = Color(0xFF602F8B)
val Purple800 = Color(0xFF33295F)

val Yellow50 = Color(0xFFFEFBE6)
val Yellow100 = Color(0xFFFDF6CE)
val Yellow200 = Color(0xFFFCEEA1)
val Yellow400 = Color(0xFFF8DC3D)
val Yellow500 = Color(0xFFD7B342)
val Yellow700 = Color(0xFF7F7F38)
val Yellow800 = Color(0xFF425135)

val Green50 = Color(0xFFEBF9F0)
val Green200 = Color(0xFFC4EDD3)
val Green300 = Color(0xFF9DE2B5)
val Green400 = Color(0xFF75D697)
val Green500 = Color(0xFF59A97E)
val Green600 = Color(0xFF3E7D65)
val Green800 = Color(0xFF22504C)

val Teal50 = Color(0xFFECF9F5)
val Teal200 = Color(0xFFBDEADC)
val Teal300 = Color(0xFF96DEC7)
val Teal400 = Color(0xFF6ED1B2)
val Teal600 = Color(0xFF49C59E)
val Teal700 = Color(0xFF25745B)
val Teal800 = Color(0xFF1C5946)

val Blue50 = Color(0xFFECF9F9)
val Blue100 = Color(0xFFD9F2F2)
val Blue300 = Color(0xFF8CD9D9)
val Blue400 = Color(0xFF66CCCC)
val Blue500 = Color(0xFF63A8AE)
val Blue700 = Color(0xFF367880)
val Blue800 = Color(0xFF1E4D59)

val Gray50 = Color(0xFFF2F2F2)
val Gray100 = Color(0xFFDDDFE4)
val Gray200 = Color(0xFFBDC9CC)
val Gray400 = Color(0xFF8D9799)
val Gray500 = Color(0xFF627684)
val Gray600 = Color(0xFF435E70)
val Gray700 = Color(0xFF294455)

val StaxBlack = Color(0xFF062333)

val NeutralBlue100 = Color(0xFFCEECFD)
val NeutralBlue500 = Color(0xFF009BF2)
val NeutralBlue800 = Color(0xFF004166)

val PositiveGreen200 = Color(0xFFDFFFE8)
val PositiveGreen500 = Color(0xFF28CB35)
val PositiveGreen800 = Color(0xFF21A446)

val WarningYellow200 = Color(0xFFFDF6CE)
val WarningYellow500 = Color(0xFFF8DC3D)
val WarningYellow700 = Color(0xFFD67300)

val AlertRed100 = Color(0xFFFF9999)
val AlertRed500 = Color(0xFFFF4646)
val AlertRed600 = Color(0xFFCC0000)

@Composable
@Preview(showBackground = true)
fun ColorPalettePreview() {
    Column {
        Row {
            Column {
                Box(modifier = Modifier.background(Purple50).width(60.dp).height(40.dp).padding(4.dp))
                Box(modifier = Modifier.background(Purple100).width(60.dp).height(40.dp).padding(4.dp))
                Box(modifier = Modifier.background(Purple400).width(60.dp).height(40.dp).padding(4.dp))
                Box(modifier = Modifier.background(Purple500).width(60.dp).height(40.dp).padding(4.dp))
                Box(modifier = Modifier.background(Purple600).width(60.dp).height(40.dp).padding(4.dp))
                Box(modifier = Modifier.background(Purple700).width(60.dp).height(40.dp).padding(4.dp))
                Box(modifier = Modifier.background(Purple800).width(60.dp).height(40.dp).padding(4.dp))
                Box(modifier = Modifier.background(StaxBlack).width(60.dp).height(40.dp).padding(4.dp))
            }
            Column {
                Box(modifier = Modifier.background(Yellow50).width(60.dp).height(40.dp).padding(4.dp))
                Box(modifier = Modifier.background(Yellow100).width(60.dp).height(40.dp).padding(4.dp))
                Box(modifier = Modifier.background(Yellow200).width(60.dp).height(40.dp).padding(4.dp))
                Box(modifier = Modifier.background(Yellow400).width(60.dp).height(40.dp).padding(4.dp))
                Box(modifier = Modifier.background(Yellow500).width(60.dp).height(40.dp).padding(4.dp))
                Box(modifier = Modifier.background(Yellow700).width(60.dp).height(40.dp).padding(4.dp))
                Box(modifier = Modifier.background(Yellow800).width(60.dp).height(40.dp).padding(4.dp))
                Box(modifier = Modifier.background(StaxBlack).width(60.dp).height(40.dp).padding(4.dp))
            }

            Column {
                Box(modifier = Modifier.background(Green50).width(60.dp).height(40.dp).padding(4.dp))
                Box(modifier = Modifier.background(Green200).width(60.dp).height(40.dp).padding(4.dp))
                Box(modifier = Modifier.background(Green300).width(60.dp).height(40.dp).padding(4.dp))
                Box(modifier = Modifier.background(Green400).width(60.dp).height(40.dp).padding(4.dp))
                Box(modifier = Modifier.background(Green500).width(60.dp).height(40.dp).padding(4.dp))
                Box(modifier = Modifier.background(Green600).width(60.dp).height(40.dp).padding(4.dp))
                Box(modifier = Modifier.background(Green800).width(60.dp).height(40.dp).padding(4.dp))
                Box(modifier = Modifier.background(StaxBlack).width(60.dp).height(40.dp).padding(4.dp))
            }

            Column {
                Box(modifier = Modifier.background(Teal50).width(60.dp).height(40.dp).padding(4.dp))
                Box(modifier = Modifier.background(Teal200).width(60.dp).height(40.dp).padding(4.dp))
                Box(modifier = Modifier.background(Teal300).width(60.dp).height(40.dp).padding(4.dp))
                Box(modifier = Modifier.background(Teal400).width(60.dp).height(40.dp).padding(4.dp))
                Box(modifier = Modifier.background(Teal600).width(60.dp).height(40.dp).padding(4.dp))
                Box(modifier = Modifier.background(Teal700).width(60.dp).height(40.dp).padding(4.dp))
                Box(modifier = Modifier.background(Teal800).width(60.dp).height(40.dp).padding(4.dp))
                Box(modifier = Modifier.background(StaxBlack).width(60.dp).height(40.dp).padding(4.dp))
            }

            Column {
                Box(modifier = Modifier.background(Blue50).width(60.dp).height(40.dp).padding(4.dp))
                Box(modifier = Modifier.background(Blue100).width(60.dp).height(40.dp).padding(4.dp))
                Box(modifier = Modifier.background(Blue300).width(60.dp).height(40.dp).padding(4.dp))
                Box(modifier = Modifier.background(Blue400).width(60.dp).height(40.dp).padding(4.dp))
                Box(modifier = Modifier.background(Blue500).width(60.dp).height(40.dp).padding(4.dp))
                Box(modifier = Modifier.background(Blue700).width(60.dp).height(40.dp).padding(4.dp))
                Box(modifier = Modifier.background(Blue800).width(60.dp).height(40.dp).padding(4.dp))
                Box(modifier = Modifier.background(StaxBlack).width(60.dp).height(40.dp).padding(4.dp))
            }

            Column {
                Box(modifier = Modifier.background(Gray50).width(60.dp).height(40.dp).padding(4.dp))
                Box(modifier = Modifier.background(Gray100).width(60.dp).height(40.dp).padding(4.dp))
                Box(modifier = Modifier.background(Gray200).width(60.dp).height(40.dp).padding(4.dp))
                Box(modifier = Modifier.background(Gray400).width(60.dp).height(40.dp).padding(4.dp))
                Box(modifier = Modifier.background(Gray500).width(60.dp).height(40.dp).padding(4.dp))
                Box(modifier = Modifier.background(Gray600).width(60.dp).height(40.dp).padding(4.dp))
                Box(modifier = Modifier.background(Gray700).width(60.dp).height(40.dp).padding(4.dp))
                Box(modifier = Modifier.background(StaxBlack).width(60.dp).height(40.dp).padding(4.dp))
            }
        }
        Row {
            Column {
                Box(modifier = Modifier.background(NeutralBlue100).width(90.dp).height(40.dp).padding(4.dp))
                Box(modifier = Modifier.background(NeutralBlue500).width(90.dp).height(40.dp).padding(4.dp))
                Box(modifier = Modifier.background(NeutralBlue800).width(90.dp).height(40.dp).padding(4.dp))
            }
            Column {
                Box(modifier = Modifier.background(PositiveGreen200).width(90.dp).height(40.dp).padding(4.dp))
                Box(modifier = Modifier.background(PositiveGreen500).width(90.dp).height(40.dp).padding(4.dp))
                Box(modifier = Modifier.background(PositiveGreen800).width(90.dp).height(40.dp).padding(4.dp))
            }
            Column {
                Box(modifier = Modifier.background(WarningYellow200).width(90.dp).height(40.dp).padding(4.dp))
                Box(modifier = Modifier.background(WarningYellow500).width(90.dp).height(40.dp).padding(4.dp))
                Box(modifier = Modifier.background(WarningYellow700).width(90.dp).height(40.dp).padding(4.dp))
            }
            Column {
                Box(modifier = Modifier.background(AlertRed100).width(89.dp).height(40.dp).padding(4.dp))
                Box(modifier = Modifier.background(AlertRed500).width(89.dp).height(40.dp).padding(4.dp))
                Box(modifier = Modifier.background(AlertRed600).width(89.dp).height(40.dp).padding(4.dp))
            }
        }
    }
}