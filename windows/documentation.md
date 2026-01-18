# Memento Mori - Windows Technical Documentation

This document outlines the technical implementation of the Memento Mori age tracker for Windows.

## Architecture

The application is built using **Python 3** and utilizes two main libraries for its user interface:
1.  **Tkinter**: Manages the always-on-top overlay window and the details popup.
2.  **Pystray**: Manages the system tray icon and its context menu.

### Core Logic (`MementoMori` class)
- **Birthdate Calculation**: Uses Python's `datetime` module. It calculates the exact age by comparing current time with the birthdate, handling leap years (Feb 29) by falling back to March 1st in non-leap years.
- **Persistence**: Configuration is stored in `config.json` in the same directory. The app reloads this file on demand when the user selects "Reload Config".

### User Interface

#### 1. Always-on-top Overlay (`Overlay` class)
- **Implementation**: Inherits from `tk.Tk`.
- **Styling**: Uses `overrideredirect(True)` to remove window decorations (title bar, borders) and `attributes("-topmost", True)` to ensure it stays above other windows.
- **Transparency**: Set via `attributes("-alpha", 0.9)` for a subtle look.
- **DPI Awareness**: Calls `windll.shcore.SetProcessDpiAwareness(1)` via `ctypes` to ensure sharp text on high-resolution displays.
- **Dragging**: Implemented by binding `<Button-1>` and `<B1-Motion>` to track mouse delta and update window geometry.
- **Ticking Clock**: Uses the `after(1000, ...)` method to schedule updates every second without blocking the main event loop.

#### 2. System Tray (`run_tray` function)
- **Thread Management**: Pystray's event loop (`icon.run()`) is blocking. It is launched in a separate `daemon` thread so it doesn't freeze the Tkinter main loop.
- **Communication**: Uses `overlay.after(0, ...)` to safely schedule UI actions (like showing the details window or closing the app) from the background tray thread to the main Tkinter thread.

#### 3. Interaction Design
- **Single Click + Drag**: Move the overlay.
- **Double Click**: Open the "Details" window.
- **Right Click**: Open a context menu (replicated on both the overlay and the tray icon).

## Standalone Executable (`.exe`)

The application has been built into a standalone executable for convenience.

- **Location**: `c:\Files\Hobby-Projects\memento-mori\windows\dist\MementoMori.exe`
- **Configuration**: Ensure `config.json` is kept in the same folder as `MementoMori.exe`.

### Building it Yourself
If you modify the code and want to rebuild the executable, run:
```bash
uv run pyinstaller --noconsole --onefile --name "MementoMori" memento_mori.py
```
The new `.exe` will appear in the `dist/` folder.

## Setup for Forever Running (Startup)

To make Memento Mori run automatically when Windows starts:

1.  Press `Win + R`, type `shell:startup`, and press **Enter**.
2.  Right-click `MementoMori.exe` (in the `dist` folder) and select **Show more options > Create shortcut**.
3.  Drag that shortcut into the **Startup** folder you opened in step 1.

*Since it's built with the `--noconsole` flag, it will run silently in the background with no terminal window appearing.*
