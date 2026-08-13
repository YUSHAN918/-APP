path = "app/src/main/java/com/example/ui/english/EnglishLessonScreen.kt"
with open(path, 'r') as f:
    content = f.read()

old_display = """        // Display panel
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .background(Color(0xFF0F172A), RoundedCornerShape(12.dp))
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {"""

new_display = """        // Display panel
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 60.dp)
                .background(Color(0xFF0F172A), RoundedCornerShape(12.dp))
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {"""

content = content.replace(old_display, new_display)

with open(path, 'w') as f:
    f.write(content)

