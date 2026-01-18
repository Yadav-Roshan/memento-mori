# Memento Mori - Ubuntu GNOME Shell Extension

A GNOME Shell extension that displays your exact age (time on Earth) in the top panel with live-updating seconds.

## Requirements

- GNOME Shell 45, 46, or 47 (Ubuntu 24.04+)
- `glib-compile-schemas` (usually pre-installed)

## Installation

```bash
cd ubuntu
chmod +x install.sh
./install.sh
```

## After Installation

1. **Restart GNOME Shell**:
   - On X11: Press `Alt+F2`, type `r`, press Enter
   - On Wayland: Log out and log back in

2. **Enable the extension**:
   ```bash
   gnome-extensions enable memento-mori@mementomori.local
   ```

3. **Set your birthdate**:
   ```bash
   gnome-extensions prefs memento-mori@mementomori.local
   ```
   Or use GNOME Extensions app to access settings.

## Features

- **Panel Display**: Age shown in top panel (right side)
- **Real-time Updates**: Seconds tick every second
- **Click Menu**: Shows age details and settings access
- **Settings Dialog**: Easy birthdate configuration

## Display Format

```
💀 28y 156d 14:23:47
```

- `28y` = Years lived
- `156d` = Days into current year of life  
- `14:23:47` = Hours:Minutes:Seconds (ticking in real-time)

## Uninstallation

```bash
gnome-extensions disable memento-mori@mementomori.local
rm -rf ~/.local/share/gnome-shell/extensions/memento-mori@mementomori.local
```

Then restart GNOME Shell.
