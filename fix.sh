sed -i '587,608c\
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {\
                            Button(\
                                onClick = { isRecordingMode = true },\
                                modifier = Modifier.weight(1f),\
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)\
                            ) {\
                                Icon(Icons.Default.Mic, contentDescription = null)\
                                Spacer(modifier = Modifier.width(8.dp))\
                                Text("开始背诵录音", fontSize = 16.sp, fontWeight = FontWeight.Bold)\
                            }\
                            Button(\
                                onClick = { isDictationMode = true },\
                                modifier = Modifier.weight(1f),\
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)\
                            ) {\
                                Icon(Icons.Default.EditNote, contentDescription = null)\
                                Spacer(modifier = Modifier.width(8.dp))\
                                Text("开始手写默写", fontSize = 16.sp, fontWeight = FontWeight.Bold)\
                            }\
                        }\
                    }\
                }\
            }\
        }\
    }\
}' app/src/main/java/com/example/ui/MaterialStudyScreen.kt
