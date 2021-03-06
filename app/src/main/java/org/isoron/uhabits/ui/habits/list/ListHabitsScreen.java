/*
 * Copyright (C) 2016 Álinson Santos Xavier <isoron@gmail.com>
 *
 * This file is part of Loop Habit Tracker.
 *
 * Loop Habit Tracker is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Loop Habit Tracker is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.isoron.uhabits.ui.habits.list;

import android.content.*;
import android.net.*;
import android.os.*;
import android.support.annotation.*;
import android.support.v7.app.*;

import com.android.colorpicker.*;

import org.isoron.uhabits.*;
import org.isoron.uhabits.models.*;
import org.isoron.uhabits.ui.*;
import org.isoron.uhabits.ui.about.*;
import org.isoron.uhabits.ui.habits.edit.*;
import org.isoron.uhabits.ui.habits.show.*;
import org.isoron.uhabits.ui.intro.*;
import org.isoron.uhabits.ui.settings.*;
import org.isoron.uhabits.utils.*;

import java.io.*;

public class ListHabitsScreen extends BaseScreen
{
    @Nullable
    ListHabitsController controller;

    public ListHabitsScreen(@NonNull BaseActivity activity,
                            ListHabitsRootView rootView)
    {
        super(activity);
        setRootView(rootView);
    }

    @Override
    public void onResult(int requestCode, int resultCode, Intent data)
    {
        if (controller == null) return;

        switch (resultCode)
        {
            case HabitsApplication.RESULT_IMPORT_DATA:
                showImportScreen();
                break;

            case HabitsApplication.RESULT_EXPORT_CSV:
                controller.onExportCSV();
                break;

            case HabitsApplication.RESULT_EXPORT_DB:
                controller.onExportDB();
                break;

            case HabitsApplication.RESULT_BUG_REPORT:
                controller.onSendBugReport();
                break;
        }
    }

    public void setController(@Nullable ListHabitsController controller)
    {
        this.controller = controller;
    }

    public void showAboutScreen()
    {
        Intent intent = new Intent(activity, AboutActivity.class);
        activity.startActivity(intent);
    }

    public void showColorPicker(Habit habit, OnColorSelectedListener callback)
    {
        int color = ColorUtils.getColor(activity, habit.getColor());

        ColorPickerDialog picker =
            ColorPickerDialog.newInstance(R.string.color_picker_default_title,
                ColorUtils.getPalette(activity), color, 4,
                ColorPickerDialog.SIZE_SMALL);

        picker.setOnColorSelectedListener(c -> {
            c = ColorUtils.colorToPaletteIndex(activity, c);
            callback.onColorSelected(c);
        });
        picker.show(activity.getSupportFragmentManager(), "picker");
    }

    public void showCreateHabitScreen()
    {
        showDialog(new CreateHabitDialogFragment(), "editHabit");
    }

    public void showDeleteConfirmationScreen(Callback callback)
    {
        new AlertDialog.Builder(activity)
            .setTitle(R.string.delete_habits)
            .setMessage(R.string.delete_habits_message)
            .setPositiveButton(android.R.string.yes,
                (dialog, which) -> callback.run())
            .setNegativeButton(android.R.string.no, null)
            .show();
    }

    public void showEditHabitScreen(Habit habit)
    {
        BaseDialogFragment frag =
            EditHabitDialogFragment.newInstance(habit.getId());
        frag.show(activity.getSupportFragmentManager(), "editHabit");
    }

    public void showFAQScreen()
    {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(activity.getString(R.string.helpURL)));
        activity.startActivity(intent);
    }

    public void showHabitScreen(@NonNull Habit habit)
    {
        Intent intent = new Intent(activity, ShowHabitActivity.class);
        intent.setData(
            Uri.parse("content://org.isoron.uhabits/habit/" + habit.getId()));
        activity.startActivity(intent);
    }

    public void showImportScreen()
    {
        if (controller == null) return;

        File dir = FileUtils.getFilesDir(null);
        if (dir == null)
        {
            showMessage(R.string.could_not_import);
            return;
        }

        FilePickerDialog picker = new FilePickerDialog(activity, dir);
        picker.setListener(file -> controller.onImportData(file));
        picker.show();
    }

    public void showIntroScreen()
    {
        Intent intent = new Intent(activity, IntroActivity.class);
        activity.startActivity(intent);
    }

    public void showSettingsScreen()
    {
        Intent intent = new Intent(activity, SettingsActivity.class);
        activity.startActivityForResult(intent, 0);
    }

    public void toggleNightMode()
    {
        if (InterfaceUtils.isNightMode())
            InterfaceUtils.setCurrentTheme(InterfaceUtils.THEME_LIGHT);
        else InterfaceUtils.setCurrentTheme(InterfaceUtils.THEME_DARK);

        refreshTheme();
    }

    private void refreshTheme()
    {
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(activity, MainActivity.class);

            activity.finish();
            activity.overridePendingTransition(android.R.anim.fade_in,
                android.R.anim.fade_out);
            activity.startActivity(intent);

        }, 500); // HACK: Let the menu disappear first
    }

    interface Callback
    {
        void run();
    }

    public interface OnColorSelectedListener
    {
        void onColorSelected(int color);
    }
}
