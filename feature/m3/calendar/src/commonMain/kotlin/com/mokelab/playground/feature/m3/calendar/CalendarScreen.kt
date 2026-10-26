package com.mokelab.playground.feature.m3.calendar

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mokelab.playground.core.ui.FeatureScaffold
import kotlinx.datetime.LocalDate
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

private const val MILLIS_PER_DAY = 24 * 60 * 60 * 1000L

@OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class)
@Composable
internal fun CalendarScreen(
    back: () -> Unit,
) {
    var showDialog by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf<Long?>(null) }
    val now = remember { Clock.System.now().toEpochMilliseconds() }
    val pickerState = rememberDatePickerState(
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                return utcTimeMillis >= now
            }
        },
    )

    FeatureScaffold(title = "M3 Calendar", back = back) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Button(onClick = {
                showDialog = true
            }) {
                Text("Show Dialog")
            }

            selected?.let { millis ->
                // DatePicker の値は UTC 0 時の epoch millis
                Text("Selected: ${LocalDate.fromEpochDays((millis / MILLIS_PER_DAY).toInt())}")
            }

            if (showDialog) {
                DatePickerDialog(
                    onDismissRequest = {
                        showDialog = false
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            showDialog = false
                            selected = pickerState.selectedDateMillis
                        }) {
                            Text("OK")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            showDialog = false
                        }) {
                            Text("Cancel")
                        }
                    },
                ) {
                    DatePicker(
                        state = pickerState,
                    )
                }
            }
        }
    }
}
