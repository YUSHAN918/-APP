import re

with open('app/src/main/java/com/example/ui/MaterialStudyScreen.kt', 'r') as f:
    content = f.read()

content = re.sub(
    r"maxLines = 1\s*\)\s*}\s*}\s*else if \(step == 1\) {",
    "maxLines = 1\n                    )\n                }\n            }\n        } else if (step == 1) {",
    content
)

content = re.sub(
    r"maxLines = 1\s*\)\s*}\s*}\s*else {",
    "maxLines = 1\n                    )\n                }\n            }\n        } else {",
    content
)

with open('app/src/main/java/com/example/ui/MaterialStudyScreen.kt', 'w') as f:
    f.write(content)
