sed -i 's/onNavigateToDictation: ((Int) -> Unit)? = null/onNavigateToDictation: ((Int) -> Unit)? = null,\
    onNavigateToBattle: () -> Unit = {}/g' app/src/main/java/com/example/ui/HolidayHomeworkCenterScreen.kt
sed -i 's/HolidayTaskDetailScreen(/HolidayTaskDetailScreen(\
                    onNavigateToBattle = onNavigateToBattle,/g' app/src/main/java/com/example/ui/HolidayHomeworkCenterScreen.kt
