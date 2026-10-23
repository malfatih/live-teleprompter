package com.example.ui.editor

import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.WavingHand
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ScriptEntity
import com.example.ui.MainViewModel
import com.example.ui.theme.DarkOutline
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.LiveCrimson
import com.example.ui.theme.LiveCyan
import com.example.ui.theme.LiveGold
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.util.PermissionHelper
import com.example.util.ScriptStatsUtil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScriptEditorScreen(
    scriptId: Long,
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit,
    onLaunchFloatingDirectly: (ScriptEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scripts by viewModel.allScripts.collectAsState()

    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Live Jualan") }
    var isFavorite by remember { mutableStateOf(false) }
    var isInitialized by remember { mutableStateOf(false) }

    val categories = listOf("Live Jualan", "Affiliate", "Edukasi", "Webinar", "Promosi", "Lainnya")

    LaunchedEffect(scriptId, scripts) {
        if (!isInitialized && scriptId != 0L) {
            val script = scripts.find { it.id == scriptId }
            if (script != null) {
                title = script.title
                content = script.content
                category = script.category
                isFavorite = script.isFavorite
                isInitialized = true
            }
        }
    }

    val wordCount = ScriptStatsUtil.countWords(content)
    val estimatedDuration = ScriptStatsUtil.estimateDuration(wordCount)
    val lineCount = ScriptStatsUtil.countLines(content)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (scriptId == 0L) "Naskah Baru" else "Edit Naskah",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Kembali",
                            tint = TextPrimary
                        )
                    }
                },
                actions = {
                    // Quick Live Floating
                    IconButton(
                        onClick = {
                            if (content.isNotBlank()) {
                                viewModel.saveScript(
                                    id = scriptId,
                                    title = title.ifBlank { "Naskah Live" },
                                    content = content,
                                    category = category,
                                    isFavorite = isFavorite
                                ) { savedId ->
                                    val script = ScriptEntity(
                                        id = savedId,
                                        title = title.ifBlank { "Naskah Live" },
                                        content = content,
                                        category = category
                                    )
                                    if (PermissionHelper.canDrawOverlays(context)) {
                                        viewModel.startFloating(context, script)
                                        Toast.makeText(context, "Floating Teleprompter Aktif!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        val intent = PermissionHelper.getOverlayPermissionIntent(context)
                                        context.startActivity(intent)
                                    }
                                }
                            } else {
                                Toast.makeText(context, "Isi naskah terlebih dahulu", Toast.LENGTH_SHORT).show()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Mulai Floating",
                            tint = LiveCrimson
                        )
                    }

                    // Save Button
                    IconButton(
                        onClick = {
                            viewModel.saveScript(
                                id = scriptId,
                                title = title.ifBlank { "Naskah Live" },
                                content = content,
                                category = category,
                                isFavorite = isFavorite
                            ) {
                                Toast.makeText(context, "Naskah berhasil disimpan!", Toast.LENGTH_SHORT).show()
                                onNavigateBack()
                            }
                        },
                        modifier = Modifier.testTag("btn_save_script")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = "Simpan",
                            tint = LiveCyan
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)
            )
        },
        containerColor = Color(0xFF0B0E19)
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Title Input
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Judul Naskah", fontSize = 13.sp) },
                placeholder = { Text("Contoh: Promo Flash Sale 12.12 Jam 8 Malam", color = TextTertiary) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = LiveCyan,
                    unfocusedBorderColor = DarkOutline,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedContainerColor = DarkSurfaceVariant,
                    unfocusedContainerColor = DarkSurfaceVariant
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_script_title")
            )

            // Category Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(categories) { cat ->
                    val isSelected = category == cat
                    FilterChip(
                        selected = isSelected,
                        onClick = { category = cat },
                        label = { Text(cat, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = LiveCyan.copy(alpha = 0.2f),
                            selectedLabelColor = LiveCyan,
                            containerColor = DarkSurfaceVariant,
                            labelColor = TextSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) LiveCyan else DarkOutline
                        )
                    )
                }
            }

            // Real-time Statistics Bar
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .border(1.dp, DarkOutline, RoundedCornerShape(10.dp)),
                color = Color(0xFF13192B)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$wordCount Kata",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "⏱ Est. $estimatedDuration",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = LiveGold
                    )
                    Text(
                        text = "$lineCount Baris",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextSecondary
                    )
                }
            }

            // Quick Content Snippets Toolbar
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    SnippetButton(
                        icon = Icons.Default.WavingHand,
                        label = "Sapaan",
                        onClick = {
                            val greeting = "\nHalo semuanya! Selamat datang di live streaming kita hari ini! Boleh bantu tap-tap layar dulu ya teman-teman!\n"
                            content += greeting
                        }
                    )
                }
                item {
                    SnippetButton(
                        icon = Icons.Default.FlashOn,
                        label = "Flash Sale",
                        onClick = {
                            val hook = "\n🔥 PROMO SPESIAL FLASH SALE terbatas cuma 15 menit ke depan! Diskon hingga 50% plus gratis ongkir!\n"
                            content += hook
                        }
                    )
                }
                item {
                    SnippetButton(
                        icon = Icons.Default.ShoppingCart,
                        label = "Keranjang",
                        onClick = {
                            val cta = "\n🛒 Klik keranjang kuning di pojok kiri bawah sekarang juga sebelum kehabisan voucher!\n"
                            content += cta
                        }
                    )
                }
                item {
                    SnippetButton(
                        icon = Icons.Default.FormatListNumbered,
                        label = "Poin",
                        onClick = {
                            val bullet = "\n• Poin Penting: "
                            content += bullet
                        }
                    )
                }
                item {
                    SnippetButton(
                        icon = Icons.Default.ContentPaste,
                        label = "Tempel",
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clipData = clipboard.primaryClip
                            if (clipData != null && clipData.itemCount > 0) {
                                val pasted = clipData.getItemAt(0).text
                                if (!pasted.isNullOrBlank()) {
                                    content += "\n$pasted"
                                    Toast.makeText(context, "Teks ditempel!", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    )
                }
            }

            // Multiline Script Body
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                placeholder = {
                    Text(
                        "Ketik atau tempel teks naskah Anda di sini...\n" +
                                "Teks ini akan bergulir otomatis saat jendela mengambang aktif di atas TikTok / Instagram Live.",
                        color = TextTertiary,
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .testTag("input_script_content"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = LiveCyan,
                    unfocusedBorderColor = DarkOutline,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedContainerColor = DarkSurfaceVariant,
                    unfocusedContainerColor = DarkSurfaceVariant
                ),
                shape = RoundedCornerShape(12.dp)
            )

            // Bottom Action Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        if (content.isNotBlank()) {
                            viewModel.saveScript(
                                id = scriptId,
                                title = title.ifBlank { "Naskah Live" },
                                content = content,
                                category = category,
                                isFavorite = isFavorite
                            ) { savedId ->
                                val script = ScriptEntity(
                                    id = savedId,
                                    title = title.ifBlank { "Naskah Live" },
                                    content = content,
                                    category = category
                                )
                                onLaunchFloatingDirectly(script)
                            }
                        } else {
                            Toast.makeText(context, "Isi naskah terlebih dahulu", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LiveCrimson),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Mulai Floating Live", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                Button(
                    onClick = {
                        viewModel.saveScript(
                            id = scriptId,
                            title = title.ifBlank { "Naskah Live" },
                            content = content,
                            category = category,
                            isFavorite = isFavorite
                        ) {
                            Toast.makeText(context, "Naskah berhasil disimpan!", Toast.LENGTH_SHORT).show()
                            onNavigateBack()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LiveCyan),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, tint = Color(0xFF00363D), modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Simpan Naskah", color = Color(0xFF00363D), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
fun SnippetButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = DarkSurfaceVariant,
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkOutline)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = LiveCyan, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = label, fontSize = 11.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
        }
    }
}
