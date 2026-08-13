import re

def fix_file(path):
    with open(path, 'r') as f:
        content = f.read()

    # 1. Fix broken fun @OptIn... declarations
    content = content.replace("fun @OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class) androidx.compose.foundation.layout.FlowRow(", "fun FlowRowFallback_Unused(")
    
    # 2. Add import for FlowRow if missing
    if "import androidx.compose.foundation.layout.FlowRow" not in content:
        content = content.replace("import androidx.compose.foundation.layout.Arrangement", "import androidx.compose.foundation.layout.Arrangement\nimport androidx.compose.foundation.layout.FlowRow\nimport androidx.compose.foundation.layout.ExperimentalLayoutApi")
    
    # 3. Use FlowRow instead of FlowRowFallback
    content = content.replace("FlowRowFallback(", "FlowRow(")
    content = content.replace("FlowRowFallback_Unused(", "fun FlowRowFallback_Unused(")
    
    # 4. Remove inline @OptIn for FlowRow as we imported it
    content = content.replace("@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class) androidx.compose.foundation.layout.FlowRow(", "FlowRow(")

    # 5. Fix UnitChallengeScreen spacing params
    content = re.sub(r'horizontalSpacing\s*=\s*(.*?dp)', r'horizontalArrangement = Arrangement.spacedBy(\1)', content)
    content = re.sub(r'verticalSpacing\s*=\s*(.*?dp)', r'verticalArrangement = Arrangement.spacedBy(\1)', content)

    with open(path, 'w') as f:
        f.write(content)

fix_file("app/src/main/java/com/example/ui/english/EnglishLessonScreen.kt")
fix_file("app/src/main/java/com/example/ui/english/UnitChallengeScreen.kt")

# For AdventureMapScreen.kt, fix the '待挑战' layout
path_adv = "app/src/main/java/com/example/ui/AdventureMapScreen.kt"
with open(path_adv, 'r') as f:
    content_adv = f.read()

# Make sure "待挑战" doesn't wrap
content_adv = content_adv.replace('text = if (isCompleted) "已通关" else "待挑战",\n', 'text = if (isCompleted) "已通关" else "待挑战",\n                                            maxLines = 1,\n                                            softWrap = false,\n')

# Convert the FlowRow wrapping the badge into a normal Row with weight for the title
old_flow = """                                @OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
                                androidx.compose.foundation.layout.FlowRow(
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
content_adv = content_adv.replace(old_flow, new_row)

with open(path_adv, 'w') as f:
    f.write(content_adv)

