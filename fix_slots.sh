#!/bin/bash
sed -i -e '1238,1353c\
            }\
        } else {\
            Text("🎉 听写默写完成对照复盘", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)\
            Text("共默写 ${dictationItems.size} 项，请对照检查手写痕迹。", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)\
            Spacer(modifier = Modifier.height(8.dp))\
            LazyColumn(\
                modifier = Modifier.weight(1f).fillMaxWidth(),\
                verticalArrangement = Arrangement.spacedBy(12.dp)\
            ) {\
                items(dictationItems.size) { idx ->\
                    val item = dictationItems[idx]\
                    val cWord = com.example.util.CharacterCompoundDictionary.getCompoundWord(item.charOrWord)\
                    val sStrokes = allSavedStrokes.getOrNull(idx) ?: emptyList()\
                    Card(\
                        modifier = Modifier.fillMaxWidth(),\
                        shape = RoundedCornerShape(8.dp),\
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)\
                    ) {\
                        Column(\
                            modifier = Modifier.padding(12.dp).fillMaxWidth()\
                        ) {\
                            Text("${item.lessonTitle} · 第 ${idx + 1} 个", fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)\
                            Text(\
                                text = item.charOrWord,\
                                fontSize = 24.sp,\
                                fontWeight = FontWeight.Bold,\
                                color = MaterialTheme.colorScheme.primary\
                            )\
                            if (!cWord.isNullOrBlank()) {\
                                Text("组词: $cWord", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)\
                            }\
                            Spacer(modifier = Modifier.height(8.dp))\
                            \
                            androidx.compose.foundation.lazy.LazyRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {\
                                items(item.charOrWord.indices.toList()) { i ->\
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {\
                                        Box(modifier = Modifier.size(60.dp).background(Color.White).border(1.dp, Color.Gray).padding(2.dp)) {\
                                            if (i < sStrokes.size) {\
                                                val saved = sStrokes[i]\
                                                MiniCanvas(strokes = saved.strokes, originalWidth = saved.width, originalHeight = saved.height, cols = 1, rows = 1)\
                                            }\
                                        }\
                                    }\
                                }\
                            }\
                        }\
                    }\
                }\
            }\
            Spacer(modifier = Modifier.height(8.dp))\
            Row(\
                modifier = Modifier.fillMaxWidth(),\
                horizontalArrangement = Arrangement.spacedBy(16.dp)\
            ) {\
                OutlinedButton(\
                    onClick = {\
                        step = 0\
                        currentIndex = 0\
                        charIndex = 0\
                        wordStrokesList = mutableListOf()\
                        allSavedStrokes = mutableListOf()\
                    },\
                    modifier = Modifier.weight(1f)\
                ) { Text("重新听写") }\
\
                Button(\
                    onClick = {\
                        viewModel.insertDictationRecord(\
                            com.example.data.HolidayDictationRecord(\
                                materialId = material.materialId,\
                                materialTitle = "${material.title}${if (selectedLessonFilter != null) " - $selectedLessonFilter" else ""}",\
                                sentenceIndex = -1,\
                                standardText = dictationItems.joinToString(" ") { it.charOrWord },\
                                handwrittenStrokesJson = "[]"\
                            )\
                        )\
                        viewModel.updateMaterialDictationStatus(material.materialId, "PASSED", taskId)\
                        onComplete()\
                    },\
                    modifier = Modifier.weight(1f)\
                ) { Text("提交听写成绩") }\
            }\
        }\
    }\
}' app/src/main/java/com/example/ui/MaterialStudyScreen.kt
