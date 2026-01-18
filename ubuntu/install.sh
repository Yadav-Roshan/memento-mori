#!/bin/bash

# Memento Mori - GNOME Shell Extension Installer

EXTENSION_UUID="memento-mori@mementomori.local"
EXTENSION_DIR="$HOME/.local/share/gnome-shell/extensions/$EXTENSION_UUID"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SOURCE_DIR="$SCRIPT_DIR/$EXTENSION_UUID"

echo "💀 Memento Mori - Installation Script"
echo "======================================"
echo ""

# Check if source exists
if [ ! -d "$SOURCE_DIR" ]; then
    echo "❌ Error: Extension source not found at $SOURCE_DIR"
    exit 1
fi

# Create extensions directory if it doesn't exist
mkdir -p "$HOME/.local/share/gnome-shell/extensions"

# Remove existing installation
if [ -d "$EXTENSION_DIR" ]; then
    echo "🔄 Removing existing installation..."
    rm -rf "$EXTENSION_DIR"
fi

# Copy extension files
echo "📁 Copying extension files..."
cp -r "$SOURCE_DIR" "$EXTENSION_DIR"

# Compile schemas
echo "⚙️  Compiling GSettings schemas..."
cd "$EXTENSION_DIR/schemas"
glib-compile-schemas .

echo ""
echo "✅ Installation complete!"
echo ""
echo "📋 Next steps:"
echo "   1. Restart GNOME Shell:"
echo "      - On X11: Press Alt+F2, type 'r', press Enter"
echo "      - On Wayland: Log out and log back in"
echo ""
echo "   2. Enable the extension:"
echo "      gnome-extensions enable $EXTENSION_UUID"
echo ""
echo "   3. Configure your birthdate:"
echo "      gnome-extensions prefs $EXTENSION_UUID"
echo ""
echo "💀 Remember, you will die. Live intentionally."
