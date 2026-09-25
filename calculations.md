# Prayer Times Calculation Methods

This document outlines how the timings for all daily prayers (Salat) are calculated in the app. The primary prayer times are calculated entirely locally offline using the `com.batoulapps.adhan` library, while the supplementary prayers are calculated based on the primary timings.

## Primary Prayers (Batoul Apps Adhan Library)

The standard five daily prayers and sunrise are calculated using astronomical formulas based on the user's location (latitude/longitude), the chosen calculation method (e.g., Muslim World League, ISNA, Egyptian General Authority of Survey), and the chosen Madhab (for Asr).

- **Fajr**: Begins at true dawn. Calculated when the sun reaches a specific angle below the horizon (varies by calculation method, e.g., 18 degrees or 15 degrees).
- **Sunrise**: The time when the upper edge of the sun's disk appears above the eastern horizon.
- **Dhuhr**: Begins just after the sun passes its zenith (highest point in the sky) and begins to decline.
- **Asr**: Calculated using shadow lengths. 
  - *Standard (Shafi'i, Maliki, Hanbali)*: When the shadow of an object equals its length plus its shadow at solar noon.
  - *Hanafi*: When the shadow of an object is twice its length plus its shadow at solar noon.
- **Maghrib**: Begins at sunset, when the sun completely disappears below the western horizon. (Sunset and Maghrib are effectively the same time in standard calculations).
- **Isha**: Begins when twilight disappears. Calculated when the sun reaches a specific angle below the horizon (varies by calculation method, e.g., 18 degrees or 15 degrees).

## Supplementary Prayers (Locally Calculated)

The supplementary prayers (Ishraq, Chasht, and Tahajjud) are derived directly from the primary timings (Fajr, Sunrise, Maghrib).

- **Ishraq (Post-Sunrise Prayer)**: 
  - **Calculation**: `Sunrise + 15 minutes`
  - **Description**: The time for Ishraq starts shortly after sunrise, once the sun has completely risen and is not red anymore. In this app, it is standardized as exactly 15 minutes after the calculated Sunrise.

- **Chasht / Duha (Mid-Morning Prayer)**: 
  - **Calculation**: `Sunrise + 45 minutes`
  - **Description**: This time starts later in the morning when the sun has risen higher. While technically the period extends until just before Dhuhr (solar zenith), the app provides a standard starting time of 45 minutes after Sunrise.

- **Tahajjud (Night Vigil Prayer)**:
  The app supports multiple calculation methods for Tahajjud, which can be selected in the Settings screen.
  
  **Method 1: Last Third of the Night (Default)**
  - **Calculation**: `Maghrib + (Night Duration × 2/3)`
  - **Description**: The most preferred time for Tahajjud is the last third of the night. 
  - **Detailed Formula**: 
    1. Calculate the total duration of the night by calculating the time difference between **Maghrib** (start of the night) and **Fajr** (end of the night).
    2. Divide the total night duration by 3 to get the length of one third of the night.
    3. Multiply by 2 to find the elapsed time for the first two-thirds of the night.
    4. Add this elapsed time to the **Maghrib** time. The result is the start of the last third of the night.

  **Method 2: Midnight (Middle of the Night)**
  - **Calculation**: `Maghrib + (Night Duration / 2)`
  - **Description**: Another common approach where the preferred time for Tahajjud starts exactly halfway between Maghrib and Fajr.
  - **Detailed Formula**:
    1. Calculate the total duration of the night.
    2. Divide by 2 to get the elapsed time for half the night.
    3. Add this to the **Maghrib** time.

## Hijri Date Calculation (Locally Calculated via Moon Visibility)

The application calculates the Hijri (Islamic) date dynamically by blending the standard algorithmic calendar (Umm al-Qura) with actual astronomical moon visibility for maximum accuracy. It uses the local `commons-suncalc` library to run these physics and astronomical equations completely offline.

- **Calculation Strategy**:
  1. Calculate the base mathematical Hijri date for the current day.
  2. Compute the precise sunset time at the user's location for **yesterday**.
  3. Calculate the exact **Moon Illumination** (fraction) and **Moon Altitude** (position) at that exact sunset time.
  4. Compare these values against strict astronomical visibility thresholds:
     - **Illumination threshold**: Must be >= 1.5%
     - **Altitude threshold**: Must be >= 5.0 degrees
  5. Apply self-correcting logic:
     - If the standard algorithm predicts today is the **1st of the month**, but the moon was NOT visible at yesterday's sunset (criteria not met), the app overrides the algorithm and rolls the date back to the **30th of the previous month**.
     - If the standard algorithm predicts today is the **30th of the month**, but the moon WAS visible at yesterday's sunset (criteria met), the app overrides the algorithm and rolls the date forward to the **1st of the new month**.
  6. Finally, if the user has defined any manual Hijri offset in the app settings, that manual offset is applied on top of this astronomically corrected date.
