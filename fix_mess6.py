import re

with open('app/src/main/java/com/example/ui/MaterialStudyScreen.kt', 'r') as f:
    content = f.read()

content = re.sub(
    r"maxLines = 1\s*}\s*}\s*\)\s*}\s*Spacer\(modifier = Modifier.width\(4.dp\)\)",
    "maxLines = 1\n                                        )\n                                    }\n                                    Spacer(modifier = Modifier.width(4.dp))",
    content
)

with open('app/src/main/java/com/example/ui/MaterialStudyScreen.kt', 'w') as f:
    f.write(content)
