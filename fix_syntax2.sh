#!/bin/bash
sed -i -e '1235,1243c\
                ) {\
                    Text(\
                        text = if (charIndex < charOrWord.length - 1) "保存这个字" else "保存并下一题",\
                        fontSize = 13.sp,\
                        maxLines = 1\
                    )\
                }\
            }\
        } else {\
            Text("🎉 听写默写完成对照复盘", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)' app/src/main/java/com/example/ui/MaterialStudyScreen.kt
