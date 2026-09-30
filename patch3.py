import re

with open('/Users/merchant/apps/prayer-times/app/src/main/java/com/valueappsolutions/prayertimes/ui/screens/QiblaCompassScreen.kt', 'r') as f:
    content = f.read()

content = content.replace("location!!.first", "location!!.latitude")
content = content.replace("location!!.second", "location!!.longitude")

with open('/Users/merchant/apps/prayer-times/app/src/main/java/com/valueappsolutions/prayertimes/ui/screens/QiblaCompassScreen.kt', 'w') as f:
    f.write(content)
