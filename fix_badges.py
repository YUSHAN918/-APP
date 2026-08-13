import re

path = "app/src/main/java/com/example/ui/english/EnglishLessonScreen.kt"
with open(path, 'r') as f:
    content = f.read()

# Fix text wrapping
content = content.replace('text = if (isCompleted) "已通关" else "待挑战",\n', 'text = if (isCompleted) "已通关" else "待挑战",\n                                            maxLines = 1,\n                                            softWrap = false,\n')

# Convert FlowRow to normal Row with weight for title
old_flow = """                                FlowRow(
                                    verticalArrangement = Arrangement.Center,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = getLessonTitle(unit.unitId, lessonType),
                                        color = Color.White,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )"""

new_row = """                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = getLessonTitle(unit.unitId, lessonType),
                                        color = Color.White,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.weight(1f)
                                    )"""
content = content.replace(old_flow, new_row)

with open(path, 'w') as f:
    f.write(content)

