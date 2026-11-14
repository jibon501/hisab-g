package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import com.example.R
import com.example.data.model.DebtKind
import com.example.data.model.HistoryItem
import com.example.data.model.fmtDate
import com.example.data.model.tk
import com.example.data.model.toBnDigits
import com.example.ui.theme.Blue600
import com.example.ui.theme.Emerald600
import com.example.ui.theme.Red600
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate50
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import kotlinx.coroutines.delay
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import kotlin.math.max

@Composable
fun HistoryShareModal(
    person: String,
    kind: DebtKind,
    items: List<HistoryItem>,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    var copied by remember { mutableStateOf(false) }

    val kindLabel = when (kind) {
        DebtKind.LENT -> "পাওনার হিসাব (ধার দেওয়া)"
        DebtKind.LOAN -> "দেনার হিসাব (লোন গ্রহণ)"
        DebtKind.SHOP -> "দোকান বাকি হিসাব"
    }

    val remaining = items.sumOf { if (it.payment) -it.amount else it.amount }

    val receiptBitmap = remember(person, kind, items) {
        renderReceiptBitmap(context, person, kind, kindLabel, items, remaining)
    }

    LaunchedEffect(copied) {
        if (copied) {
            delay(2000)
            copied = false
        }
    }

    val saveLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("image/png")
    ) { uri ->
        if (uri != null) {
            context.contentResolver.openOutputStream(uri)?.use { out ->
                receiptBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
        }
    }

    fun copySlipText() {
        val sb = StringBuilder()
        sb.append("$person • $kindLabel\n")
        sb.append("তারিখ: ${fmtDate(LocalDate.now().toString())}\n")
        sb.append("------------------------------------\n")
        items.forEach { h ->
            sb.append("${fmtDate(h.date)}  ${h.label}  ${if (h.payment) "পরিশোধ:" else ""} ${tk(h.amount)}\n")
        }
        sb.append("------------------------------------\n")
        sb.append("মোট বাকি: ${tk(remaining)}\n")

        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("hisab_slip", sb.toString()))
        copied = true
    }

    fun shareSlipImage() {
        try {
            val file = File(context.cacheDir, "hisab-slip-${System.currentTimeMillis()}.png")
            FileOutputStream(file).use { out ->
                receiptBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "$person - হিসাব বিবরণী")
                putExtra(Intent.EXTRA_TEXT, "$person-এর হিসাব বিবরণী। বাকি: ${tk(remaining)}")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "হিসাবের স্লিপ শেয়ার করুন"))
        } catch (_: Exception) {
            copySlipText()
        }
    }

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            shadowElevation = 12.dp,
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .heightIn(max = 660.dp)
        ) {
            Column {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Slate50)
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = person,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate800
                        )
                        Text(
                            text = "$kindLabel বিবরণী",
                            fontSize = 12.sp,
                            color = Slate500
                        )
                    }
                    IconButton(onClick = onClose, modifier = Modifier.testTag("close_slip_modal")) {
                        Icon(Icons.Default.Close, contentDescription = "বন্ধ করুন", tint = Slate600)
                    }
                }
                HorizontalDivider(color = Slate200)

                // Content Preview
                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .background(Slate100.copy(alpha = 0.7f))
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, Slate200),
                        shadowElevation = 1.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Image(
                            bitmap = receiptBitmap.asImageBitmap(),
                            contentDescription = "$person-এর হিসাবের স্লিপ",
                            contentScale = ContentScale.FillWidth,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, Slate200),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("মোট লেনদেন সংখ্যা:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Slate700)
                                Text("${toBnDigits(items.size.toString())} টি", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Slate700)
                            }
                            HorizontalDivider(color = Slate100)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("অবশিষ্ট বাকি:", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Slate900)
                                Text(
                                    text = tk(remaining),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (remaining > 0) Red600 else Emerald600
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(color = Slate200)

                // Footer Action Buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            saveLauncher.launch("হিসাব-$person-${LocalDate.now()}.png")
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Emerald600),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp)
                            .testTag("save_slip_image_button")
                    ) {
                        Icon(Icons.Outlined.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("ছবি সেভ", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }

                    Button(
                        onClick = { shareSlipImage() },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Blue600),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp)
                            .testTag("share_slip_button")
                    ) {
                        Icon(Icons.Outlined.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("শেয়ার", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }

                    Button(
                        onClick = { copySlipText() },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Slate100,
                            contentColor = Slate700
                        ),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp)
                            .testTag("copy_slip_text_button")
                    ) {
                        Icon(
                            imageVector = if (copied) Icons.Default.Check else Icons.Outlined.ContentCopy,
                            contentDescription = null,
                            tint = if (copied) Emerald600 else Slate700,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(if (copied) "কপি হয়েছে" else "টেক্সট কপি", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

private fun renderReceiptBitmap(
    context: Context,
    person: String,
    kind: DebtKind,
    kindLabel: String,
    items: List<HistoryItem>,
    remaining: Double
): Bitmap {
    val width = 720
    val pad = 40f
    val rowH = 68f
    val headH = 160f
    val footH = 120f
    val height = max(340, (headH + rowH * items.size + footH).toInt())

    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    val regularTypeface = ResourcesCompat.getFont(context, R.font.hind_siliguri) ?: Typeface.DEFAULT
    val boldTypeface = ResourcesCompat.getFont(context, R.font.hind_siliguri_bold) ?: Typeface.DEFAULT_BOLD

    val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    // White Background
    paint.color = android.graphics.Color.WHITE
    canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

    // Decorative Top Bar
    paint.color = when (kind) {
        DebtKind.LENT -> 0xFF16A34A.toInt()
        DebtKind.LOAN -> 0xFFDC2626.toInt()
        DebtKind.SHOP -> 0xFF9333EA.toInt()
    }
    canvas.drawRect(0f, 0f, width.toFloat(), 10f, paint)

    // Person Title
    paint.typeface = boldTypeface
    paint.color = 0xFF0F172A.toInt()
    paint.textSize = 36f
    canvas.drawText(person, pad, 64f, paint)

    // Kind label
    paint.typeface = regularTypeface
    paint.color = 0xFF64748B.toInt()
    paint.textSize = 20f
    canvas.drawText("$kindLabel • হিসাবের স্লিপ", pad, 102f, paint)

    // Print date
    paint.color = 0xFF94A3B8.toInt()
    paint.textSize = 16f
    canvas.drawText("মুদ্রণ তারিখ: ${fmtDate(LocalDate.now().toString())}", pad, 130f, paint)

    // Header divider
    paint.color = 0xFFE2E8F0.toInt()
    paint.strokeWidth = 2f
    canvas.drawLine(pad, headH - 10f, width - pad, headH - 10f, paint)

    // Rows
    var y = headH + 34f
    items.forEach { h ->
        paint.typeface = regularTypeface
        paint.color = 0xFF64748B.toInt()
        paint.textSize = 18f
        canvas.drawText(fmtDate(h.date), pad, y, paint)

        paint.color = 0xFF1E293B.toInt()
        paint.textSize = 20f
        canvas.drawText(h.label, pad + 150f, y, paint)

        val amtStr = (if (h.payment) "- " else "+ ") + tk(h.amount)
        paint.typeface = boldTypeface
        paint.color = if (h.payment) 0xFF16A34A.toInt() else 0xFFB91C1C.toInt()
        paint.textSize = 22f
        val textWidth = paint.measureText(amtStr)
        canvas.drawText(amtStr, width - pad - textWidth, y, paint)

        y += rowH
    }

    // Footer divider
    paint.color = 0xFFCBD5E1.toInt()
    paint.strokeWidth = 2f
    canvas.drawLine(pad, y - rowH + 30f, width - pad, y - rowH + 30f, paint)

    // Net remaining
    val restText = "মোট বাকি: ${tk(remaining)}"
    paint.typeface = boldTypeface
    paint.color = if (remaining > 0) 0xFFB91C1C.toInt() else 0xFF16A34A.toInt()
    paint.textSize = 30f
    val restWidth = paint.measureText(restText)
    canvas.drawText(restText, width - pad - restWidth, y + 26f, paint)

    // Watermark
    paint.typeface = regularTypeface
    paint.color = 0xFF94A3B8.toInt()
    paint.textSize = 16f
    canvas.drawText("হিসাব (HISAB) অ্যাপ দ্বারা প্রস্তুতকৃত", pad, y + 26f, paint)

    return bitmap
}
