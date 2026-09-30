import re

# Patch LocationsListScreen.kt
with open('/Users/merchant/apps/prayer-times/app/src/main/java/com/valueappsolutions/prayertimes/ui/screens/LocationsListScreen.kt', 'r') as f:
    content = f.read()
    
content = content.replace("currentLoc.first", "currentLoc.latitude")
content = content.replace("currentLoc.second", "currentLoc.longitude")

with open('/Users/merchant/apps/prayer-times/app/src/main/java/com/valueappsolutions/prayertimes/ui/screens/LocationsListScreen.kt', 'w') as f:
    f.write(content)
    
# Patch QiblaCompassScreen.kt
with open('/Users/merchant/apps/prayer-times/app/src/main/java/com/valueappsolutions/prayertimes/ui/screens/QiblaCompassScreen.kt', 'r') as f:
    content = f.read()

content = content.replace("location.first", "location.latitude")
content = content.replace("location.second", "location.longitude")

with open('/Users/merchant/apps/prayer-times/app/src/main/java/com/valueappsolutions/prayertimes/ui/screens/QiblaCompassScreen.kt', 'w') as f:
    f.write(content)
