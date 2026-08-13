import re

path = "app/src/main/java/com/example/ui/english/EnglishLessonScreen.kt"
with open(path, "r") as f:
    content = f.read()

# Fix the broken parameters in FlowRowFallback at 1811
content = content.replace("FlowRowFallback(\n                horizontalSpacing = 8.dp,\n                verticalSpacing = 8.dp,", "FlowRowFallback(\n                horizontalArrangement = Arrangement.spacedBy(8.dp),\n                verticalArrangement = Arrangement.spacedBy(8.dp),")

content = content.replace("FlowRowFallback(\n            horizontalSpacing = 8.dp,\n            verticalSpacing = 8.dp,", "FlowRowFallback(\n            horizontalArrangement = Arrangement.spacedBy(8.dp),\n            verticalArrangement = Arrangement.spacedBy(8.dp),")


with open(path, "w") as f:
    f.write(content)
