import subprocess
try:
    with open("app/src/main/java/com/example/ui/english/EnglishLessonScreen.kt", "r") as f:
        lines = f.readlines()
    for i in range(1250, 1280):
        print(f"{i+1}: {lines[i].strip()}")
except Exception as e:
    print(e)
