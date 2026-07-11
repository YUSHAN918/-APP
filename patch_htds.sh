sed -i 's/fun HolidayTaskDetailScreen(/fun HolidayTaskDetailScreen(\
    onNavigateToBattle: () -> Unit = {},/g' app/src/main/java/com/example/ui/HolidayTaskDetailScreen.kt
sed -i 's/MaterialStudyScreen(/MaterialStudyScreen(\
            onNavigateToBattle = onNavigateToBattle,/g' app/src/main/java/com/example/ui/HolidayTaskDetailScreen.kt
