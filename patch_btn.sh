sed -i '/Text("开始手写默写"/i \
                                onClick = { isDictationMode = true },\
                                modifier = Modifier.weight(1f),\
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)\
                            ) {\
                                Icon(Icons.Default.EditNote, contentDescription = null)\
                                Spacer(modifier = Modifier.width(8.dp))' app/src/main/java/com/example/ui/MaterialStudyScreen.kt
