package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Currency
import com.example.data.model.ExpenseCategory
import com.example.data.repository.ParsedReceipt
import com.example.ui.components.CategoryBadge
import com.example.ui.viewmodel.ExpenseViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ReceiptScannerScreen(
    viewModel: ExpenseViewModel,
    onReceiptSaved: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var receiptInputText by remember { mutableStateOf("") }
    var isAnalyzing by remember { mutableStateOf(false) }
    var scanStatusMessage by remember { mutableStateOf("") }
    val parsedReceipt by viewModel.parsedReceipt.collectAsState()

    val sampleReceipts = listOf(
        "Whole Foods Market\n123 Market St, San Francisco\nOrganic Honeycrisp Apples $5.49\nAlmond Milk Unsweetened $3.99\nWild Sockeye Salmon Fillet $18.50\nSubtotal: $27.98\nSales Tax 8.5%: $2.38\nTotal: $30.36\nPayment: VISA **** 8912",
        "Starbucks Coffee #1042\n2x Iced Caramel Macchiato $11.50\n1x Butter Croissant $3.75\nSubtotal: $15.25\nTax: $1.30\nGrand Total: $16.55\nThank you for visiting!",
        "Shell Petroleum Fuel\nPump #04 Regular Gasoline\n12.4 Gallons @ $3.89/gal\nTotal: $48.24\nCard: Mastercard **** 3910",
        "Le Petit Bistro Paris\n15 Rue de Rivoli, Paris\n2x Plat du Jour €36.00\n1x Bouteille Eau €4.50\n1x Café Gourmand €7.50\nTVA 10%: €4.80\nTotal: €48.00\nMerci de votre visite",
        "Apple Store Fifth Avenue\nAirPods Pro 2nd Gen USB-C $249.00\nAppleCare+ 2yr Plan $29.00\nState Tax: $24.33\nTotal Amount: $302.33\nAuth: 893128"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("receipt_scanner_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Smart Receipt OCR",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Automated merchant, line item & category extraction",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.Default.DocumentScanner,
                contentDescription = "Scanner",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
        }

        // Quick Preset Sample Receipts
        Text(
            text = "Select a sample receipt or paste below:",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val labels = listOf("Whole Foods", "Starbucks", "Shell Gas", "Paris Bistro (€)", "Apple Store")
            sampleReceipts.forEachIndexed { index, receiptText ->
                OutlinedButton(
                    onClick = {
                        receiptInputText = receiptText
                        triggerAnalysis(receiptText, viewModel) { msg, analyzing ->
                            scanStatusMessage = msg
                            isAnalyzing = analyzing
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Receipt,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(labels[index], style = MaterialTheme.typography.labelSmall)
                }
            }
        }

        // Receipt Input Box / Scanner Viewfinder
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Receipt Text / OCR Stream",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    if (receiptInputText.isNotBlank()) {
                        TextButton(onClick = {
                            receiptInputText = ""
                            viewModel.clearParsedReceipt()
                        }) {
                            Text("Clear")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = receiptInputText,
                    onValueChange = { receiptInputText = it },
                    placeholder = {
                        Text(
                            "Paste or type receipt text here...\nExample:\nStarbucks Coffee\n1x Latte $4.50\nTotal: $4.50",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .testTag("receipt_ocr_text_field"),
                    textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        triggerAnalysis(receiptInputText, viewModel) { msg, analyzing ->
                            scanStatusMessage = msg
                            isAnalyzing = analyzing
                        }
                    },
                    enabled = receiptInputText.isNotBlank() && !isAnalyzing,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("analyze_receipt_button")
                ) {
                    if (isAnalyzing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Analyzing...")
                    } else {
                        Icon(imageVector = Icons.Default.AutoFixHigh, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Analyze & Categorize Receipt")
                    }
                }

                if (scanStatusMessage.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = scanStatusMessage,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Parsed Receipt Result Card
        AnimatedVisibility(visible = parsedReceipt != null) {
            parsedReceipt?.let { parsed ->
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("parsed_receipt_card")
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Success",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Smart Extraction Successful",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            CategoryBadge(category = parsed.category)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Extracted Details Grid
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Merchant",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = parsed.merchant,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Total Amount",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = Currency.formatAmount(parsed.totalAmount, parsed.currency),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Detected Tax / VAT",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = Currency.formatAmount(parsed.taxAmount, parsed.currency),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Auto Category",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = parsed.category.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = parsed.category.color
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Line Items: ${parsed.itemsSummary}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                viewModel.saveParsedReceipt(parsed)
                                onReceiptSaved()
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("save_parsed_receipt_button")
                        ) {
                            Icon(imageVector = Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Save to Local Room Cache")
                        }
                    }
                }
            }
        }
    }
}

private fun triggerAnalysis(
    text: String,
    viewModel: ExpenseViewModel,
    updateState: (String, Boolean) -> Unit
) {
    if (text.isBlank()) return
    updateState("Scanning text & recognizing tokens...", true)
    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
        updateState("Matching merchants and parsing numbers...", true)
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            viewModel.parseReceiptText(text)
            updateState("Extraction complete! Ready to save.", false)
        }, 300)
    }, 400)
}
