package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Apartment
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material.icons.outlined.Smartphone
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.data.getLogoResForAccount
import com.example.ui.theme.Slate200

enum class AvatarSize(
    val boxSize: Dp,
    val cornerRadius: Dp,
    val iconSize: Dp,
    val imagePad: Dp
) {
    SM(32.dp, 8.dp, 16.dp, 3.dp),
    MD(44.dp, 12.dp, 20.dp, 4.dp),
    LG(56.dp, 16.dp, 26.dp, 6.dp)
}

data class AccountIconStyle(
    val icon: ImageVector,
    val color: Color,
    val bg: Color
)

private val ACC_ICON_CONFIG: Map<String, AccountIconStyle> = mapOf(
    "cash" to AccountIconStyle(Icons.Outlined.Payments, Color(0xFF16A34A), Color(0xFFDCFCE7)),
    "phone" to AccountIconStyle(Icons.Outlined.Smartphone, Color(0xFFDB2777), Color(0xFFFCE7F3)),
    "phone2" to AccountIconStyle(Icons.Outlined.Smartphone, Color(0xFFEA580C), Color(0xFFFFEDD5)),
    "bank" to AccountIconStyle(Icons.Outlined.AccountBalance, Color(0xFF2563EB), Color(0xFFDBEAFE)),
    "bank2" to AccountIconStyle(Icons.Outlined.AccountBalance, Color(0xFF0891B2), Color(0xFFCFFAFE)),
    "card" to AccountIconStyle(Icons.Outlined.CreditCard, Color(0xFF9333EA), Color(0xFFF3E8FF)),
    "wallet" to AccountIconStyle(Icons.Outlined.AccountBalanceWallet, Color(0xFF475569), Color(0xFFF1F5F9)),
    "savings" to AccountIconStyle(Icons.Outlined.Savings, Color(0xFF4F46E5), Color(0xFFE0E7FF)),
    "savings2" to AccountIconStyle(Icons.Outlined.Savings, Color(0xFF0D9488), Color(0xFFCCFBF1)),
    "land" to AccountIconStyle(Icons.Outlined.Apartment, Color(0xFF854D0E), Color(0xFFFEF9C3)),
    "home" to AccountIconStyle(Icons.Outlined.Home, Color(0xFFCA8A04), Color(0xFFFEF08A))
)

fun defaultIconKey(type: String): String = when (type) {
    "cash" -> "cash"
    "mobile" -> "phone"
    "bank" -> "bank"
    "dps" -> "savings"
    "asset" -> "land"
    else -> "wallet"
}

@Composable
fun AccountAvatar(
    accountName: String,
    accountType: String,
    iconKey: String = "",
    size: AvatarSize = AvatarSize.MD,
    modifier: Modifier = Modifier
) {
    val logoRes = getLogoResForAccount(accountName)
    val resolvedKey = iconKey.ifBlank { defaultIconKey(accountType) }
    val style = ACC_ICON_CONFIG[resolvedKey] ?: ACC_ICON_CONFIG.getValue("wallet")
    val shape = RoundedCornerShape(size.cornerRadius)

    if (logoRes != null) {
        Surface(
            modifier = modifier.size(size.boxSize),
            shape = shape,
            color = Color.White,
            border = BorderStroke(1.dp, Slate200),
            shadowElevation = 1.dp
        ) {
            Image(
                painter = painterResource(id = logoRes),
                contentDescription = accountName,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(size.imagePad)
            )
        }
    } else {
        Box(
            modifier = modifier
                .size(size.boxSize)
                .clip(shape)
                .background(style.bg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = style.icon,
                contentDescription = accountName,
                tint = style.color,
                modifier = Modifier.size(size.iconSize)
            )
        }
    }
}
