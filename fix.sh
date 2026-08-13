#!/bin/bash
sed -i 's/FlowRow(/@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class) androidx.compose.foundation.layout.FlowRow(/g' app/src/main/java/com/example/ui/english/EnglishLessonScreen.kt
