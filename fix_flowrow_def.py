import re

path = "app/src/main/java/com/example/ui/english/EnglishLessonScreen.kt"
with open(path, "r") as f:
    content = f.read()

# Fix the broken definition of FlowRow
content = content.replace("fun @OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class) androidx.compose.foundation.layout.FlowRow(", "fun FlowRowFallback(")
content = content.replace("Real@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class) androidx.compose.foundation.layout.FlowRow(", "FlowRowFallback(")


with open(path, "w") as f:
    f.write(content)
