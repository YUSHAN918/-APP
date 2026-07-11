import os

with open('app/src/main/java/com/example/ui/MaterialStudyScreen.kt', 'r') as f:
    content = f.read()

bad1 = """maxLines = 1
                    )
            }
                    } else if (step == 1) {"""

good1 = """maxLines = 1
                    )
                }
            }
        } else if (step == 1) {"""

bad2 = """maxLines = 1
                    )
            }
        } else {"""

good2 = """maxLines = 1
                    )
                }
            }
        } else {"""


content = content.replace(bad1, good1)
content = content.replace(bad2, good2)

with open('app/src/main/java/com/example/ui/MaterialStudyScreen.kt', 'w') as f:
    f.write(content)
