import os

with open('app/src/main/java/com/example/ui/MaterialStudyScreen.kt', 'r') as f:
    content = f.read()

bad_str1 = """maxLines = 1
                    }
                }
                    )"""

bad_str2 = """maxLines = 1
                    )
                }"""

good_str = """maxLines = 1
                    )"""

content = content.replace(bad_str1, good_str).replace(bad_str2, good_str)

with open('app/src/main/java/com/example/ui/MaterialStudyScreen.kt', 'w') as f:
    f.write(content)
