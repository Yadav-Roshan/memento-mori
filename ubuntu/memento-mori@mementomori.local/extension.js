/* Memento Mori - GNOME Shell Extension
 * Display your age (time on Earth) in the top panel
 */

import GLib from 'gi://GLib';
import GObject from 'gi://GObject';
import Clutter from 'gi://Clutter';
import St from 'gi://St';
import Gio from 'gi://Gio';
import * as Main from 'resource:///org/gnome/shell/ui/main.js';
import * as PanelMenu from 'resource:///org/gnome/shell/ui/panelMenu.js';
import * as PopupMenu from 'resource:///org/gnome/shell/ui/popupMenu.js';
import { Extension } from 'resource:///org/gnome/shell/extensions/extension.js';

let mementoMoriIndicator = null;

class MementoMoriIndicator extends PanelMenu.Button {
    static {
        GObject.registerClass(this);
    }

    constructor(extension) {
        super(0.0, 'Memento Mori', false);

        this._extension = extension;
        this._settings = extension.getSettings();

        // Create the label for the panel
        this._label = new St.Label({
            text: '⌛ 💀 Loading...',
            y_align: Clutter.ActorAlign.CENTER,
            style_class: 'memento-mori-label'
        });

        this.add_child(this._label);

        // Load birthdate from settings
        this._birthdate = this._settings.get_int64('birthdate');

        // Create popup menu
        this._createMenu();

        // Start updating
        this._startUpdating();

        // Connect to settings changes
        this._settingsChangedId = this._settings.connect('changed::birthdate', () => {
            this._birthdate = this._settings.get_int64('birthdate');
        });
    }

    _createMenu() {
        // Show age details
        let detailsItem = new PopupMenu.PopupMenuItem('Show Age Details');
        detailsItem.connect('activate', () => {
            this._showAgeDetails();
        });
        this.menu.addMenuItem(detailsItem);

        this.menu.addMenuItem(new PopupMenu.PopupSeparatorMenuItem());

        // Settings info
        let settingsItem = new PopupMenu.PopupMenuItem('Open Extension Settings');
        settingsItem.connect('activate', () => {
            this._extension.openPreferences();
        });
        this.menu.addMenuItem(settingsItem);
    }

    _calculateAge() {
        if (this._birthdate <= 0) {
            return {
                years: 0,
                days: 0,
                hours: 0,
                minutes: 0,
                seconds: 0,
                valid: false
            };
        }

        const now = Date.now();
        const birthdate = new Date(this._birthdate);
        const nowDate = new Date(now);

        // Calculate years
        let years = nowDate.getFullYear() - birthdate.getFullYear();

        // Check if birthday hasn't occurred yet this year
        const birthMonth = birthdate.getMonth();
        const birthDay = birthdate.getDate();
        const nowMonth = nowDate.getMonth();
        const nowDay = nowDate.getDate();

        if (nowMonth < birthMonth || (nowMonth === birthMonth && nowDay < birthDay)) {
            years--;
        }

        // Calculate last birthday
        const lastBirthday = new Date(birthdate);
        lastBirthday.setFullYear(birthdate.getFullYear() + years);

        // Days since last birthday
        const daysSinceBirthday = Math.floor((now - lastBirthday.getTime()) / (1000 * 60 * 60 * 24));

        // Time components from age
        const ageMs = now - this._birthdate;
        const totalSeconds = Math.floor(ageMs / 1000);
        const secondsInDay = totalSeconds % 86400;
        const hours = Math.floor(secondsInDay / 3600);
        const minutes = Math.floor((secondsInDay % 3600) / 60);
        const seconds = secondsInDay % 60;

        return {
            years: years,
            days: daysSinceBirthday,
            hours: hours,
            minutes: minutes,
            seconds: seconds,
            valid: true
        };
    }

    _formatAge() {
        const age = this._calculateAge();

        if (!age.valid) {
            return '⌛ 💀 Set birthdate';
        }

        const h = String(age.hours).padStart(2, '0');
        const m = String(age.minutes).padStart(2, '0');
        const s = String(age.seconds).padStart(2, '0');

        return `⌛ 💀 ${age.years}y ${age.days}d ${h}:${m}:${s}`;
    }

    _showAgeDetails() {
        const age = this._calculateAge();

        if (!age.valid) {
            Main.notify('Memento Mori', 'Please set your birthdate in extension settings');
            return;
        }

        const totalDays = Math.floor((Date.now() - this._birthdate) / (1000 * 60 * 60 * 24));
        const totalHours = Math.floor((Date.now() - this._birthdate) / (1000 * 60 * 60));

        const message = `${this._formatAge()}\n\nTotal Days: ${totalDays.toLocaleString()}\nTotal Hours: ${totalHours.toLocaleString()}\n\nMemento Mori - Remember, you will die.`;

        Main.notify('Memento Mori', message);
    }

    _startUpdating() {
        this._update();
        this._timeout = GLib.timeout_add_seconds(GLib.PRIORITY_DEFAULT, 1, () => {
            this._update();
            return GLib.SOURCE_CONTINUE;
        });
    }

    _update() {
        this._label.set_text(this._formatAge());
        return true;
    }

    destroy() {
        if (this._timeout) {
            GLib.source_remove(this._timeout);
            this._timeout = null;
        }

        if (this._settingsChangedId) {
            this._settings.disconnect(this._settingsChangedId);
            this._settingsChangedId = null;
        }

        super.destroy();
    }
}

export default class MementoMoriExtension extends Extension {
    enable() {
        mementoMoriIndicator = new MementoMoriIndicator(this);
        Main.panel.addToStatusArea('memento-mori', mementoMoriIndicator, 0, 'right');
    }

    disable() {
        if (mementoMoriIndicator) {
            mementoMoriIndicator.destroy();
            mementoMoriIndicator = null;
        }
    }
}
