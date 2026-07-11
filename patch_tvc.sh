sed -i 's/onStartDictation = { lessonTitle ->/onStartDictation = { lessonTitle ->\
                            val result = mutableListOf<com.example.data.WordItem>()\
                            val lessonLines = material.fullText.split("\\n").filter { it.isNotBlank() }\
                            for (line in lessonLines) {\
                                val parts = line.split("：")\
                                val title = parts.getOrNull(0)?.trim() ?: ""\
                                if (lessonTitle != null \&\& title != lessonTitle) continue\
                                val itemsInLesson = parts.getOrNull(1)?.split(" ")?.filter { it.isNotBlank() } ?: emptyList()\
                                for (item in itemsInLesson) {\
                                    result.add(com.example.data.WordItem(id = 0, word = item, pinyin = "", explanation = "", tags = ""))\
                                }\
                            }\
                            viewModel.startCustomBattle(lessonTitle ?: material.title, result)\
                            onNavigateToBattle()\
                        }/g' app/src/main/java/com/example/ui/MaterialStudyScreen.kt
