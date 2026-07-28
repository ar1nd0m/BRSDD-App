package com.rmstu.brsdd.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private data class MetricRow(val name: String, val value: String)

private val metrics = listOf(
    MetricRow("Precision", "98.88%"),
    MetricRow("Recall", "99.82%"),
    MetricRow("F1 score", "99.35%"),
    MetricRow("mAP@0.5", "99.49%"),
    MetricRow("mAP@0.5:0.95", "97.03%"),
)

@Composable
fun AboutScreen() {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            SectionCard(title = "BRSDD Road Sign Detector") {
                Text(
                    "An offline, on-device object detector for Bangladeshi road signs, " +
                        "built on a custom-trained YOLOv8n model (29 sign classes) exported to TensorFlow Lite.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }

        item {
            SectionCard(title = "Developer") {
                InfoLine("Arindam Datta")
                InfoLine("BSc in Computer Science & Engineering (3rd Year)")
                InfoLine("Rangamati Science and Technology University (RMSTU)")
            }
        }

        item {
            SectionCard(title = "Course Instructor") {
                InfoLine("G M Sakhawat Hossain")
                InfoLine("Assistant Professor")
                InfoLine("gmsakhawat@rmstu.ac.bd")
            }
        }

        item {
            SectionCard(title = "Dataset") {
                Text(
                    "BRSDD — Bangladeshi Road Sign Detection Dataset, 29 road sign classes.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }

        item {
            SectionCard(title = "Model Validation Metrics") {
                metrics.forEachIndexed { index, m ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(m.name, style = MaterialTheme.typography.bodyMedium)
                        Text(
                            m.value,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.secondary,
                        )
                    }
                    if (index != metrics.lastIndex) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    }
                }
                Text(
                    "Reported from training/validation; real-world accuracy will vary with lighting, " +
                        "distance, and camera quality.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }

        item {
            SectionCard(title = "Privacy") {
                Text(
                    "All detection runs fully on-device. No images or camera frames are uploaded " +
                        "or sent to any server.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable ColumnScopeAlias.() -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp),
            )
            content()
        }
    }
}

@Composable
private fun InfoLine(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.padding(vertical = 2.dp),
    )
}

private typealias ColumnScopeAlias = androidx.compose.foundation.layout.ColumnScope
