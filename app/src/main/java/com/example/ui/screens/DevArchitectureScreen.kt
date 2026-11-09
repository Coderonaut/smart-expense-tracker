package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DevArchitectureScreen(
    modifier: Modifier = Modifier
) {
    var selectedTopic by remember { mutableIntStateOf(0) }
    val topics = listOf(
        "1. MVVM Architecture",
        "2. Key Android Components",
        "3. Jetpack Compose Guide",
        "4. Gradle & Dependency Mgmt"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("dev_architecture_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title
        item {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = "Education",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "SYBSc CS Developer Guide",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Text(
                    text = "Core Android architecture, Jetpack Compose, Room & Gradle internals",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Topic Pills
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                topics.forEachIndexed { index, title ->
                    FilterChip(
                        selected = selectedTopic == index,
                        onClick = { selectedTopic = index },
                        label = { Text(title, fontWeight = if (selectedTopic == index) FontWeight.Bold else FontWeight.Normal) },
                        modifier = Modifier.testTag("topic_chip_$index")
                    )
                }
            }
        }

        when (selectedTopic) {
            0 -> {
                item {
                    ArchitectureDiagramCard()
                }
                item {
                    ConceptCard(
                        title = "What is MVVM (Model-View-ViewModel)?",
                        subtitle = "Clean Separation of Concerns",
                        icon = Icons.Default.AccountTree,
                        explanation = "In modern Android development, Google recommends the MVVM architecture with Unidirectional Data Flow (UDF):\n\n" +
                                "1. View (UI Layer): Built with Jetpack Compose. Observes UI State from the ViewModel and renders pixels. Dispatches user actions (clicks, inputs) as events.\n\n" +
                                "2. ViewModel: Survives configuration changes (e.g., screen rotation). Contains business logic, manages state using StateFlow, and communicates with Repositories.\n\n" +
                                "3. Repository: Acts as the Single Source of Truth. Mediates between local offline storage (Room SQLite database) and external APIs (Bank sync, network services)."
                    )
                }
            }
            1 -> {
                item {
                    ConceptCard(
                        title = "Android Core Components",
                        subtitle = "Building Blocks of an Android App",
                        icon = Icons.Default.Widgets,
                        explanation = "Key Android Building Blocks every CS student must master:\n\n" +
                                "• Activity (MainActivity.kt): The entry point window. Modern Compose apps use a Single-Activity Architecture where MainActivity hosts all Compose screens.\n\n" +
                                "• Coroutines & Flow: Kotlin's asynchronous concurrency model. 'suspend' functions prevent blocking the main thread (UI thread). 'Flow<T>' emits streams of data reactively.\n\n" +
                                "• Room Database: An Object-Relational Mapping (ORM) abstraction over SQLite. Uses @Entity for tables, @Dao for SQL queries, and @Database to manage instances.\n\n" +
                                "• WindowInsets & Edge-to-Edge: enableEdgeToEdge() draws content behind system status and navigation bars for modern immersive UX."
                    )
                }
            }
            2 -> {
                item {
                    ConceptCard(
                        title = "Jetpack Compose: Declarative UI",
                        subtitle = "How Compose Differs from Legacy XML",
                        icon = Icons.Default.Code,
                        explanation = "Legacy Android used imperative XML (findViewById / setText).\n\n" +
                                "Jetpack Compose is DECLARATIVE:\n" +
                                "• You describe WHAT the UI should look like for a given state.\n" +
                                "• When state changes (via remember { mutableStateOf() } or StateFlow.collectAsState()), Compose automatically triggers RECOMPOSITION to re-render only the affected UI nodes.\n\n" +
                                "Key Layout Composables:\n" +
                                "• Column: Vertical linear stack\n" +
                                "• Row: Horizontal linear stack\n" +
                                "• Box: Overlapping z-axis stack\n" +
                                "• LazyColumn: Recycler-like performant scrolling list (only renders visible items on screen!)\n" +
                                "• Modifier: Chained instructions for padding, sizing, background, click events, and accessibility test tags."
                    )
                }
            }
            3 -> {
                item {
                    ConceptCard(
                        title = "Gradle & Dependency Management",
                        subtitle = "The Android Studio Build Engine",
                        icon = Icons.Default.Build,
                        explanation = "Gradle compiles your Kotlin code, resources, and libraries into an APK/AAB:\n\n" +
                                "1. settings.gradle.kts: Defines repository sources (Google Maven, Maven Central) and includes project modules (:app).\n\n" +
                                "2. gradle/libs.versions.toml: Version Catalog! A centralized file where library versions and identifiers are defined to prevent dependency conflicts.\n\n" +
                                "3. app/build.gradle.kts: Module-level build script. Applies plugins (Android Application, Kotlin Compose, KSP, Google Services) and declares dependencies (Room, Coroutines, Material3).\n\n" +
                                "4. KSP (Kotlin Symbol Processing): A fast compiler plugin that generates boilerplate code for Room DAOs at compile time."
                    )
                }
            }
        }
    }
}

@Composable
fun ArchitectureDiagramCard() {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "Unidirectional Data Flow (UDF)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Diagram Blocks
            DiagramNode(
                title = "UI Layer (Jetpack Compose)",
                details = "Emits user events ➔ Renders UIState reactively",
                color = Color(0xFF3B82F6)
            )

            DiagramArrow(label = "User Events (clicks, inputs)")

            DiagramNode(
                title = "ViewModel Layer (ExpenseViewModel)",
                details = "Holds StateFlow<UIState> • Handles business logic & coroutines",
                color = Color(0xFF10B981)
            )

            DiagramArrow(label = "Repository Calls (suspend functions)")

            DiagramNode(
                title = "Data Layer (ExpenseRepository)",
                details = "Single Source of Truth • Coordinates Room & Bank APIs",
                color = Color(0xFFF59E0B)
            )

            DiagramArrow(label = "Flow<List<ExpenseEntity>> reactive streams")

            DiagramNode(
                title = "Local Storage (Room SQLite Database)",
                details = "AppDatabase • ExpenseDao • BankAccountDao",
                color = Color(0xFF8B5CF6)
            )
        }
    }
}

@Composable
fun DiagramNode(title: String, details: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.12f),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = details,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun DiagramArrow(label: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "↓ $label",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp
        )
    }
}

@Composable
fun ConceptCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    explanation: String
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = explanation,
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 22.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
