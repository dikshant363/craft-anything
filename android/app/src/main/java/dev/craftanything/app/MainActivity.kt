package dev.craftanything.app

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.craftanything.app.core.NativeBridge
import dev.craftanything.app.saf.DocumentMetadata
import dev.craftanything.app.saf.StorageManager
import dev.craftanything.app.ui.theme.CraftAnythingTheme

class MainActivity : ComponentActivity() {

    private lateinit var storageManager: StorageManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        storageManager = StorageManager(this)

        setContent {
            CraftAnythingTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CraftMainScreen(storageManager = storageManager)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CraftMainScreen(storageManager: StorageManager) {
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var documents by remember { mutableStateOf<List<DocumentMetadata>>(emptyList()) }
    var engineStatus by remember { mutableStateOf("Initializing...") }

    LaunchedEffect(Unit) {
        val version = NativeBridge.safeGetVersion()
        engineStatus = "Craft Core v$version"
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        if (uri != null) {
            val persisted = storageManager.persistTreePermission(uri)
            if (persisted) {
                selectedUri = uri
                documents = storageManager.listFolder(uri)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Craft Anything") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Engine Status",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = engineStatus,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Button(
                onClick = { launcher.launch(null) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Select Workspace Directory (SAF)")
            }

            selectedUri?.let { uri ->
                Text(
                    text = "Active Workspace: ${uri.lastPathSegment ?: uri.toString()}",
                    style = MaterialTheme.typography.labelMedium
                )
            }

            if (documents.isNotEmpty()) {
                Text(
                    text = "Documents (${documents.size}):",
                    style = MaterialTheme.typography.titleSmall
                )
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(documents) { doc ->
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(12.dp)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = doc.name,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = if (doc.isDirectory) "Directory" else "${doc.size} bytes",
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }
                    }
                }
            } else if (selectedUri != null) {
                Text(
                    text = "No files found in selected directory.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
