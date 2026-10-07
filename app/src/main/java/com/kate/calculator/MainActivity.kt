package com.kate.calculator

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.math.BigDecimal
import java.math.MathContext

private const val ERROR = "Error"
private val OPERATORS = setOf("+", "−", "×", "÷")

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
            MaterialTheme { CalculatorScreen() }
        }
    }
}

@Composable
fun CalculatorScreen() {
    var input by rememberSaveable { mutableStateOf("0") }
    var acc by rememberSaveable { mutableDoubleStateOf(0.0) }
    var op by rememberSaveable { mutableStateOf("") }
    var fresh by rememberSaveable { mutableStateOf(true) }

    fun clearAll() {
        input = "0"
        acc = 0.0
        op = ""
        fresh = true
    }

    fun compute(): Boolean {
        val current = input.toDoubleOrNull() ?: 0.0
        val result = if (op.isEmpty()) current else calculate(acc, op, current)
        if (result.isNaN() || result.isInfinite()) {
            clearAll()
            input = ERROR
            return false
        }
        acc = result
        input = format(result)
        fresh = true
        return true
    }

    fun onKey(key: String) {
        if (input == ERROR && key != "C") return
        when (key) {
            "C" -> clearAll()

            "CE" -> {
                input = "0"
                fresh = true
            }

            "=" -> if (op.isNotEmpty() && compute()) op = ""

            in OPERATORS -> {
                if (key == "−" && op.isNotEmpty() && fresh) {
                    input = "-"
                    fresh = false
                    return
                }
                if (op.isNotEmpty() && !fresh && !compute()) return
                acc = input.toDoubleOrNull() ?: 0.0
                op = key
                fresh = true
            }

            "." -> {
                if (fresh) {
                    input = "0"
                    fresh = false
                }
                if (input == "-") input = "-0"
                if ('.' !in input) input += "."
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

    Scaffold(modifier = Modifier.semantics { testTagsAsResourceId = true }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
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
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    row.forEach { key ->
                        Button(
                            onClick = { onKey(key) },
                            modifier = Modifier
                                .weight(if (key == "0") 3f else 1f)
                                .fillMaxSize()
                                .testTag("key_$key"),
                        ) {
                            Text(key, fontSize = 22.sp)
                        }
                    }
                }
            }
        }
    }
}

private fun calculate(a: Double, op: String, b: Double): Double = when (op) {
    "+" -> a + b
    "−" -> a - b
    "×" -> a * b
    "÷" -> a / b
    else -> b
}

private fun format(value: Double): String =
    BigDecimal(value).round(MathContext(12)).stripTrailingZeros().toPlainString()