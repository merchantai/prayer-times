#!/bin/bash
cd /Users/merchant/apps/prayer-times/app/src/main/java/com/valueappsolutions/prayertimes/domain
# add imports
sed -i '' '/import kotlinx.coroutines.withContext/a\
import kotlinx.coroutines.async\
import kotlinx.coroutines.withTimeoutOrNull
' PrayerRepository.kt

# fix async and withTimeoutOrNull
sed -i '' 's/kotlinx.coroutines.async(Dispatchers.IO)/async/' PrayerRepository.kt
sed -i '' 's/kotlinx.coroutines.withTimeoutOrNull/withTimeoutOrNull/' PrayerRepository.kt
