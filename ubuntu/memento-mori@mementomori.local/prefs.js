/* Memento Mori - Preferences Dialog */

import Adw from 'gi://Adw';
import Gtk from 'gi://Gtk';
import GLib from 'gi://GLib';
import Gio from 'gi://Gio';
import { ExtensionPreferences } from 'resource:///org/gnome/Shell/Extensions/js/extensions/prefs.js';

export default class MementoMoriPreferences extends ExtensionPreferences {
    fillPreferencesWindow(window) {
        const settings = this.getSettings();

        // Create a preferences page
        const page = new Adw.PreferencesPage({
            title: 'General',
            icon_name: 'dialog-information-symbolic',
        });
        window.add(page);

        // Create a preferences group
        const group = new Adw.PreferencesGroup({
            title: 'Birthdate Settings',
            description: 'Set your birthdate to calculate your age',
        });
        page.add(group);

        // Current birthdate display
        const currentBirthdate = settings.get_int64('birthdate');
        let birthdateText = 'Not set';
        if (currentBirthdate > 0) {
            const date = new Date(currentBirthdate);
            birthdateText = date.toLocaleString();
        }

        const currentRow = new Adw.ActionRow({
            title: 'Current Birthdate',
            subtitle: birthdateText,
        });
        group.add(currentRow);

        // Year entry
        const yearRow = new Adw.ActionRow({
            title: 'Year',
        });
        const yearEntry = new Gtk.SpinButton({
            adjustment: new Gtk.Adjustment({
                lower: 1900,
                upper: 2100,
                step_increment: 1,
            }),
            valign: Gtk.Align.CENTER,
        });
        yearEntry.set_value(2000);
        yearRow.add_suffix(yearEntry);
        group.add(yearRow);

        // Month entry
        const monthRow = new Adw.ActionRow({
            title: 'Month',
        });
        const monthEntry = new Gtk.SpinButton({
            adjustment: new Gtk.Adjustment({
                lower: 1,
                upper: 12,
                step_increment: 1,
            }),
            valign: Gtk.Align.CENTER,
        });
        monthEntry.set_value(1);
        monthRow.add_suffix(monthEntry);
        group.add(monthRow);

        // Day entry
        const dayRow = new Adw.ActionRow({
            title: 'Day',
        });
        const dayEntry = new Gtk.SpinButton({
            adjustment: new Gtk.Adjustment({
                lower: 1,
                upper: 31,
                step_increment: 1,
            }),
            valign: Gtk.Align.CENTER,
        });
        dayEntry.set_value(1);
        dayRow.add_suffix(dayEntry);
        group.add(dayRow);

        // Hour entry
        const hourRow = new Adw.ActionRow({
            title: 'Hour (0-23)',
        });
        const hourEntry = new Gtk.SpinButton({
            adjustment: new Gtk.Adjustment({
                lower: 0,
                upper: 23,
                step_increment: 1,
            }),
            valign: Gtk.Align.CENTER,
        });
        hourEntry.set_value(0);
        hourRow.add_suffix(hourEntry);
        group.add(hourRow);

        // Minute entry
        const minuteRow = new Adw.ActionRow({
            title: 'Minute (0-59)',
        });
        const minuteEntry = new Gtk.SpinButton({
            adjustment: new Gtk.Adjustment({
                lower: 0,
                upper: 59,
                step_increment: 1,
            }),
            valign: Gtk.Align.CENTER,
        });
        minuteEntry.set_value(0);
        minuteRow.add_suffix(minuteEntry);
        group.add(minuteRow);

        // Load current values if set
        if (currentBirthdate > 0) {
            const date = new Date(currentBirthdate);
            yearEntry.set_value(date.getFullYear());
            monthEntry.set_value(date.getMonth() + 1);
            dayEntry.set_value(date.getDate());
            hourEntry.set_value(date.getHours());
            minuteEntry.set_value(date.getMinutes());
        }

        // Save button
        const saveButton = new Gtk.Button({
            label: 'Save Birthdate',
            css_classes: ['suggested-action'],
            valign: Gtk.Align.CENTER,
            halign: Gtk.Align.CENTER,
            margin_top: 20,
        });

        saveButton.connect('clicked', () => {
            const date = new Date(
                yearEntry.get_value(),
                monthEntry.get_value() - 1,
                dayEntry.get_value(),
                hourEntry.get_value(),
                minuteEntry.get_value(),
                0
            );

            settings.set_int64('birthdate', date.getTime());
            currentRow.set_subtitle(date.toLocaleString());
        });

        const buttonGroup = new Adw.PreferencesGroup();
        buttonGroup.add(saveButton);
        page.add(buttonGroup);

        // Info group
        const infoGroup = new Adw.PreferencesGroup({
            title: 'About',
            description: 'Memento Mori - Remember, you will die.\n\nThis extension displays your age in the top panel as a constant reminder to live intentionally.',
        });
        page.add(infoGroup);
    }
}
