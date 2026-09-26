package com.example

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.fragment.app.FragmentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.AdminMetadataDialog
import com.example.ui.components.DecryptedRecordDialog
import com.example.ui.components.UploadRecordDialog
import com.example.ui.screens.PatientVaultScreen
import com.example.ui.theme.HealthVaultTheme
import com.example.viewmodel.HealthVaultViewModel

class MainActivity : FragmentActivity() {

    private val viewModel: HealthVaultViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            HealthVaultTheme {
                val showUploadModal by viewModel.showUploadModal.collectAsStateWithLifecycle()
                val selectedDecryptedRecord by viewModel.selectedDecryptedRecord.collectAsStateWithLifecycle()
                val adminInspectedRecord by viewModel.adminInspectedRecord.collectAsStateWithLifecycle()

                Scaffold(
                    modifier = Modifier.statusBarsPadding(),
                    containerColor = androidx.compose.material3.MaterialTheme.colorScheme.background
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                    ) {
                        PatientVaultScreen(viewModel = viewModel)

                        // Active Dialogs & Modals
                        if (showUploadModal) {
                            UploadRecordDialog(
                                onDismiss = { viewModel.showUploadModal(false) },
                                onUpload = { name, category, content ->
                                    viewModel.uploadRecord(name, category, content)
                                }
                            )
                        }

                        selectedDecryptedRecord?.let { viewRecord ->
                            DecryptedRecordDialog(
                                decryptedData = viewRecord,
                                onDismiss = { viewModel.closeDecryptedRecord() }
                            )
                        }

                        adminInspectedRecord?.let { record ->
                            AdminMetadataDialog(
                                record = record,
                                onDismiss = { viewModel.closeAdminInspection() }
                            )
                        }
                    }
                }
            }
        }
    }
}

