# Memento Mori 💀

**"Remember, you will die."**

A cross-platform age tracker that displays your exact time on Earth as a constant reminder to live intentionally.

## Platforms

| Platform | Location | Display |
|----------|----------|---------|
| Windows  | System Tray | Hover tooltip |
| Android  | Notification Bar | Persistent notification |
| Ubuntu   | Top Panel | Panel indicator |

## Display Format

All platforms show your age as a **live ticking counter**:

```
💀 28y 156d 14:23:47
```

- **28y** = Years lived
- **156d** = Days into current year of life
- **14:23:47** = Hours:Minutes:Seconds (updating every second)

## Quick Start

### Windows
```bash
cd windows
uv venv
uv pip install -r requirements.txt
# Edit config.json with your birthdate
uv run python memento_mori.py
```

### Android
1. Build APK with Android Studio or `./gradlew assembleDebug`
2. Install on device
3. Set birthdate and tap Start

### Ubuntu
```bash
cd ubuntu
./install.sh
gnome-extensions enable memento-mori@mementomori.local
```

## See individual platform READMEs for detailed instructions.
