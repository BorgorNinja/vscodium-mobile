package dev.vscodium.mobile.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import dev.vscodium.mobile.ui.theme.LocalSyntaxColors

/**
 * A scrollable code editor: a line-number gutter on the left and a
 * syntax-highlighted [BasicTextField] on the right, both sharing a single
 * vertical scroll state so they stay in sync.
 */
@Composable
fun CodeEditorField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    language: String,
    fontSize: Int,
    showLineNumbers: Boolean,
    wordWrap: Boolean,
    modifier: Modifier = Modifier,
) {
    val colors = LocalSyntaxColors.current
    val scrollState = rememberScrollState()
    val lineCount = value.text.count { it == '\n' } + 1
    val textStyle = TextStyle(
        color = colors.foreground,
        fontFamily = FontFamily.Monospace,
        fontSize = fontSize.sp,
        lineHeight = (fontSize * 1.5f).sp,
    )

    Row(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .verticalScroll(scrollState)
    ) {
        if (showLineNumbers) {
            Column(modifier = Modifier.width(48.dp).padding(top = 8.dp, end = 4.dp)) {
                for (line in 1..lineCount) {
                    Text(
                        text = line.toString(),
                        color = colors.gutterText,
                        fontFamily = FontFamily.Monospace,
                        fontSize = fontSize.sp,
                        lineHeight = (fontSize * 1.5f).sp,
                        modifier = Modifier.fillMaxSize(),
                        textAlign = androidx.compose.ui.text.style.TextAlign.End,
                    )
                }
            }
        }

        Box(modifier = Modifier.padding(top = 8.dp, start = 4.dp, end = 16.dp, bottom = 48.dp)) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = textStyle,
                cursorBrush = SolidColor(colors.foreground),
                visualTransformation = { text ->
                    TransformedText(
                        highlight(text.text, language, colors),
                        OffsetMapping.Identity
                    )
                },
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}
