"""
Memento Mori - Windows Age Tracker
Displays your exact age (time on Earth) in a persistent taskbar overlay and system tray.
"""

import json
import os
import sys
import threading
import subprocess
from datetime import datetime
from pathlib import Path
import tkinter as tk

import pystray
from PIL import Image, ImageDraw

class MementoMori:
    def __init__(self):
        # Handle path correctly when frozen (PyInstaller)
        if hasattr(sys, '_MEIPASS'):
            # Running as a .exe, config should be next to the .exe
            self.current_dir = Path(sys.executable).parent
        else:
            self.current_dir = Path(__file__).parent
            
        self.config_path = self.current_dir / "config.json"
        self.birthdate = self.load_birthdate()
        self.running = True
        
    def load_birthdate(self) -> datetime:
        try:
            with open(self.config_path, 'r') as f:
                config = json.load(f)
                return datetime.strptime(config['birthdate'], '%Y-%m-%d %H:%M:%S')
        except (FileNotFoundError, KeyError, ValueError) as e:
            return datetime(2000, 1, 1, 0, 0, 0)
    
    def calculate_age(self) -> tuple:
        now = datetime.now()
        years = now.year - self.birthdate.year
        try:
            this_year_bday = self.birthdate.replace(year=now.year)
        except ValueError: # Leap year
            this_year_bday = self.birthdate.replace(year=now.year, month=3, day=1)
            
        if now < this_year_bday:
            years -= 1
            
        try:
            last_birthday = self.birthdate.replace(year=self.birthdate.year + years)
        except ValueError:
            last_birthday = self.birthdate.replace(year=self.birthdate.year + years, month=3, day=1)
            
        days_delta = now - last_birthday
        days = days_delta.days
        total_seconds = int(days_delta.total_seconds())
        remaining = total_seconds % 86400
        hours = remaining // 3600
        minutes = (remaining % 3600) // 60
        seconds = remaining % 60
        
        return years, days, hours, minutes, seconds
    
    def format_age(self) -> str:
        y, d, h, m, s = self.calculate_age()
        return f"💀 {y}y {d}d {h:02d}:{m:02d}:{s:02d}"

class Overlay(tk.Tk):
    def __init__(self, memento_mori):
        super().__init__()
        self.memento_mori = memento_mori
        
        # Overlay settings
        self.overrideredirect(True)
        self.attributes("-topmost", True)
        self.attributes("-alpha", 0.9)
        self.configure(bg='#1a1a1a')
        
        self.label = tk.Label(
            self, text=self.memento_mori.format_age(),
            font=("Consolas", 14, "bold"), fg="#ffffff", bg="#1a1a1a",
            padx=15, pady=8, cursor="fleur"
        )
        self.label.pack()
        
        self.label.bind("<Button-1>", self.start_move)
        self.label.bind("<B1-Motion>", self.do_move)
        self.label.bind("<Double-Button-1>", lambda e: self.show_details())
        self.label.bind("<Button-3>", self.post_menu)
        
        # Right-click menu
        self.menu = tk.Menu(self, tearoff=0, bg="#2a2a2a", fg="white", activebackground="#444")
        self.menu.add_command(label="Show Details", command=self.show_details)
        self.menu.add_command(label="Edit Birthdate", command=self.open_editor)
        self.menu.add_command(label="Reload Config", command=self.reload_config)
        self.menu.add_separator()
        self.menu.add_command(label="Hide Overlay", command=self.withdraw)
        self.menu.add_command(label="Quit Application", command=self.quit_app)

        # Initial positioning (bottom right)
        self.update_idletasks()
        sw, sh = self.winfo_screenwidth(), self.winfo_screenheight()
        w, h = self.winfo_width(), self.winfo_height()
        self.geometry(f"+{sw - w - 20}+{sh - h - 60}")
        
        self._drag_x = 0
        self._drag_y = 0
        self.update_clock()

    def start_move(self, event):
        self._drag_x = event.x
        self._drag_y = event.y

    def do_move(self, event):
        x = self.winfo_x() + (event.x - self._drag_x)
        y = self.winfo_y() + (event.y - self._drag_y)
        self.geometry(f"+{x}+{y}")

    def post_menu(self, event):
        self.menu.post(event.x_root, event.y_root)

    def update_clock(self):
        if self.memento_mori.running:
            age_str = self.memento_mori.format_age()
            self.label.config(text=age_str)
            if hasattr(self, 'tray_icon'):
                self.tray_icon.title = age_str
            self.after(1000, self.update_clock)

    def open_editor(self):
        try:
            subprocess.Popen(['notepad.exe', str(self.memento_mori.config_path)])
        except:
            os.startfile(self.memento_mori.config_path)

    def reload_config(self):
        self.memento_mori.birthdate = self.memento_mori.load_birthdate()

    def show_details(self):
        y, d, h, m, s = self.memento_mori.calculate_age()
        total_days = (datetime.now() - self.memento_mori.birthdate).days
        total_hours = int((datetime.now() - self.memento_mori.birthdate).total_seconds() // 3600)
        
        msg = f"Your Time on Earth:\n\n" \
              f"Age: {y}y {d}d\n" \
              f"Time: {h:02d}:{m:02d}:{s:02d}\n\n" \
              f"Total Days: {total_days:,}\n" \
              f"Total Hours: {total_hours:,}\n\n" \
              f"Birthdate: {self.memento_mori.birthdate.strftime('%Y-%m-%d %H:%M:%S')}\n\n" \
              f"Remember, you will die."

        details = tk.Toplevel(self)
        details.title("Memento Mori - Details")
        details.configure(bg="#1a1a1a")
        details.attributes("-topmost", True)
        details.resizable(False, False)
        
        tk.Label(
            details, text=msg, fg="white", bg="#1a1a1a", 
            padx=30, pady=30, justify="left", font=("Segoe UI", 10)
        ).pack()
        
        tk.Button(
            details, text="Close", command=details.destroy, 
            bg="#333", fg="white", padx=20, pady=5, borderwidth=0
        ).pack(pady=(0, 20))

    def quit_app(self):
        self.memento_mori.running = False
        if hasattr(self, 'tray_icon'):
            self.tray_icon.stop()
        self.destroy()
        sys.exit(0)

def run_tray(memento_mori, overlay):
    def on_quit(icon, item):
        overlay.after(0, overlay.quit_app)

    def on_show(icon, item):
        overlay.after(0, overlay.deiconify)
        overlay.after(0, lambda: overlay.attributes("-topmost", True))

    def on_details(icon, item):
        overlay.after(0, overlay.show_details)

    # Tray icon image
    size = 64
    image = Image.new('RGBA', (size, size), (0, 0, 0, 0))
    draw = ImageDraw.Draw(image)
    draw.ellipse([8, 4, 56, 48], fill='white', outline='black', width=2)
    draw.ellipse([16, 18, 28, 32], fill='black')
    draw.ellipse([36, 18, 48, 32], fill='black')
    draw.polygon([(32, 34), (28, 42), (36, 42)], fill='black')
    for i in range(4):
        x = 20 + i * 8
        draw.rectangle([x, 48, x + 6, 58], fill='white', outline='black')
    
    menu = pystray.Menu(
        pystray.MenuItem("Show Overlay", on_show),
        pystray.MenuItem("Show Details", on_details),
        pystray.MenuItem("Edit Birthdate", lambda i, t: overlay.after(0, overlay.open_editor)),
        pystray.MenuItem("Reload Config", lambda i, t: overlay.after(0, overlay.reload_config)),
        pystray.Menu.SEPARATOR,
        pystray.MenuItem("Quit", on_quit)
    )
    
    icon = pystray.Icon("memento_mori", image, "Memento Mori", menu)
    overlay.tray_icon = icon
    icon.run()

def main():
    try:
        from ctypes import windll
        windll.shcore.SetProcessDpiAwareness(1)
    except:
        pass

    m_mori = MementoMori()
    overlay = Overlay(m_mori)
    
    # Run tray in daemon thread
    threading.Thread(target=run_tray, args=(m_mori, overlay), daemon=True).start()
    
    overlay.mainloop()

if __name__ == "__main__":
    main()