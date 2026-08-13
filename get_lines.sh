#!/bin/bash
cat app/src/main/java/com/example/ui/english/EnglishLessonScreen.kt | sed -n '1050,1150p' > lines.txt
