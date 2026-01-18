# Memento Mori - Android App

A persistent notification app that displays your exact age (time on Earth) with live-updating seconds.

## Building

### Option 1: Android Studio
1. Open `android/` folder in Android Studio
2. Let Gradle sync
3. Click Run or Build → Build APK

### Option 2: Command Line
```bash
cd android
./gradlew assembleDebug
```

APK will be at: `app/build/outputs/apk/debug/app-debug.apk`

## Installation

1. Transfer the APK to your phone
2. Enable "Install from unknown sources" if prompted
3. Install the APK

## Usage

1. Open the app
2. Set your birthdate using the date and time pickers
3. Tap "Save Birthdate"
4. Tap "Start" to begin the notification
5. Grant notification permission when prompted

## Features

- **Persistent Notification**: Always visible in status bar
- **Real-time Updates**: Seconds tick every second
- **Auto-start on Boot**: Starts automatically when phone restarts
- **Low Battery Impact**: Optimized for minimal power usage

## Display Format

```
💀 28y 156d 14:23:47
```

- `28y` = Years lived
- `156d` = Days into current year of life  
- `14:23:47` = Hours:Minutes:Seconds (ticking in real-time)

## Permissions Required

- **Notifications**: To show the persistent age notification
- **Boot Completed**: To auto-start on device boot
- **Foreground Service**: To keep running in background
