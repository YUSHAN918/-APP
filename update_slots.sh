#!/bin/bash
sed -i -e '1132,1204c\
            Spacer(modifier = Modifier.height(8.dp))\
            \
            Text("本题需要书写 ${charOrWord.length} 个字", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)\
            Text("当前第 ${charIndex + 1} / ${charOrWord.length} 个字", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)\
            Spacer(modifier = Modifier.height(8.dp))\
\
            // Slots display\
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 8.dp)) {\
                for (i in 0 until charOrWord.length) {\
                    val isCurrent = (i == charIndex)\
                    val saved = wordStrokesList.getOrNull(i)\
                    Box(\
                        modifier = Modifier\
                            .size(56.dp)\
                            .background(if (isCurrent) Color(0xFFFFF9C4) else Color(0xFFFDF6E3))\
                            .border(\
                                width = if (isCurrent) 2.dp else 1.dp,\
                                color = if (isCurrent) MaterialTheme.colorScheme.primary else Color.LightGray,\
                                shape = RoundedCornerShape(4.dp)\
                            )\
                    ) {\
                        if (saved != null) {\
                            MiniCanvas(saved.strokes, saved.width, saved.height, 1, 1)\
                        }\
                    }\
                }\
            }\
\
            Box(\
                modifier = Modifier\
                    .weight(1f)\
                    .fillMaxWidth()\
                    .padding(vertical = 4.dp)\
                    .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))\
                    .clip(RoundedCornerShape(12.dp))\
            ) {\
                androidx.compose.ui.viewinterop.AndroidView(\
                    factory = { ctx ->\
                        HandwritingView(ctx).apply { handwritingView = this }\
                    },\
                    update = { view ->\
                        view.setGrid(1, 1)\
                    },\
                    modifier = Modifier.fillMaxSize()\
                )\
            }\
            Spacer(modifier = Modifier.height(8.dp))\
            Row(\
                modifier = Modifier.fillMaxWidth(),\
                horizontalArrangement = Arrangement.spacedBy(4.dp)\
            ) {\
                OutlinedButton(\
                    onClick = { handwritingView?.clear() },\
                    modifier = Modifier.weight(1f),\
                    contentPadding = PaddingValues(0.dp)\
                ) {\
                    Text("清空", fontSize = 13.sp)\
                }\
                OutlinedButton(\
                    onClick = { handwritingView?.undo() },\
                    modifier = Modifier.weight(1f),\
                    contentPadding = PaddingValues(0.dp)\
                ) {\
                    Text("撤销", fontSize = 13.sp)\
                }\
                Button(\
                    onClick = {\
                        val currentStrokes = handwritingView?.getStrokes() ?: emptyList()\
                        val w = handwritingView?.width?.toFloat() ?: 1000f\
                        val h = handwritingView?.height?.toFloat() ?: 1000f\
                        val savedChar = DictationSavedChar(currentStrokes, w, h)\
                        val newList = wordStrokesList.toMutableList()\
                        if (charIndex < newList.size) {\
                            newList[charIndex] = savedChar\
                        } else {\
                            newList.add(savedChar)\
                        }\
                        wordStrokesList = newList\
                        handwritingView?.clear()\
                        if (charIndex < charOrWord.length - 1) {\
                            charIndex++\
                        } else {\
                            val newAllStrokes = allSavedStrokes.toMutableList()\
                            if (currentIndex < newAllStrokes.size) {\
                                newAllStrokes[currentIndex] = newList\
                            } else {\
                                newAllStrokes.add(newList)\
                            }\
                            allSavedStrokes = newAllStrokes\
                            if (currentIndex < dictationItems.size - 1) {\
                                currentIndex++\
                                charIndex = 0\
                                wordStrokesList = mutableListOf()\
                            } else {\
                                step = 1\
                            }\
                        }\
                    },\
                    modifier = Modifier.weight(2f),\
                    contentPadding = PaddingValues(0.dp)\
                ) {\
                    Text(\
                        text = if (charIndex < charOrWord.length - 1) "保存这个字" else "保存并下一题",\
                        fontSize = 13.sp,\
                        maxLines = 1\
                    )\
                }\
            }' app/src/main/java/com/example/ui/MaterialStudyScreen.kt
