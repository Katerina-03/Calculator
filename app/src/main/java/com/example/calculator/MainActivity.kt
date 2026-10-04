package com.example.calculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs

private const val NO_OP = ' '
private const val ERROR = "Error"

private val keys = listOf(
    listOf("C", "CE", "÷", "×"),
    listOf("7", "8", "9", "−"),
    listOf("4", "5", "6", "+"),
    listOf("1", "2", "3", "="),
    listOf("0", "."),
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                CalculatorScreen()
            }
        }
    }
}

@Composable
fun CalculatorScreen() {
    var input by rememberSaveable { mutableStateOf("0") }
    var acc by rememberSaveable { mutableDoubleStateOf(0.0) }
    var op by rememberSaveable { mutableStateOf(NO_OP) }
    var fresh by rememberSaveable { mutableStateOf(true) }

    fun clearAll() {
        input = "0"
        acc = 0.0
        op = NO_OP
        fresh = true
    }

    fun showResult(value: Double) {
        if (value.isNaN() || value.isInfinite()) {
            clearAll()
            input = ERROR
        } else {
            input = format(value)
            fresh = true
        }
    }

    fun onKey(key: String) {
        when (key) {
            "C" -> clearAll()
            "CE" -> {
                input = "0"
                fresh = true
            }

            "." -> {
                if (fresh) {
                    input = "0"
                    fresh = false
                }
                if ('.' !in input) input += "."
            }

            "+", "−", "×", "÷" -> {
                val current = input.toDoubleOrNull() ?: 0.0
                if (op != NO_OP && !fresh) {
                    val result = calculate(acc, op, current)
                    showResult(result)
                    acc = if (result.isNaN() || result.isInfinite()) 0.0 else result
                } else {
                    acc = current
                }
                if (input != ERROR) op = key[0]
                fresh = true
            }

            "=" -> {
                if (op != NO_OP) {
                    showResult(calculate(acc, op, input.toDoubleOrNull() ?: 0.0))
                    op = NO_OP
                }
            }

            else -> {
                if (fresh) {
                    input = ""
                    fresh = false
                }
                input = if (input == "0") key else input + key
            }
        }
    }

    Scaffold(
        modifier = Modifier.semantics { testTagsAsResourceId = true }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SelectionContainer(Modifier.weight(1.5f)) {
                Text(
                    text = input,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("result"),
                    fontSize = 40.sp,
                    textAlign = TextAlign.End,
                    maxLines = 1,
                )
            }
            keys.forEach { row ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    row.forEach { key ->
                        Button(
                            onClick = { onKey(key) },
                            modifier = Modifier
                                .weight(if (key == "0") 3f else 1f)
                                .fillMaxSize()
                                .testTag("key_$key"),
                        ) {
                            Text(
                                key,
                                fontSize = 22.sp,
                                modifier = Modifier.align(Alignment.CenterVertically)
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun calculate(a: Double, op: Char, b: Double): Double = when (op) {
    '+' -> a + b
    '−' -> a - b
    '×' -> a * b
    '÷' -> a / b
    else -> b
}

private fun format(value: Double): String =
    if (abs(value) < 1e15 && value == value.toLong().toDouble()) {
        value.toLong().toString()
    } else {
        value.toString()
    }
