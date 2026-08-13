import re

path = "app/src/main/java/com/example/ui/english/EnglishLessonScreen.kt"
with open(path, "r") as f:
    content = f.read()

# Make sure we add ExperimentalLayoutApi import
if "import androidx.compose.foundation.layout.ExperimentalLayoutApi" not in content:
    content = content.replace("import androidx.compose.foundation.layout.Arrangement", "import androidx.compose.foundation.layout.Arrangement\nimport androidx.compose.foundation.layout.ExperimentalLayoutApi\nimport androidx.compose.foundation.layout.FlowRow\n")

with open(path, "w") as f:
    f.write(content)
