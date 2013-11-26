package com.nicocorp.nr1;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import java.util.HashSet;
import java.util.Set;

public class SettingsActivity extends PreferenceActivity
{
    SharedPreferences settings;
    PreferenceManager prefsMgr;

    public void ClickActivityFromMenu(MenuItem item)
    {
        Intent intent;
        prefsMgr = getPreferenceManager();
        switch (item.getItemId())
        {
            case R.id.btnShowContacts:
                intent = new Intent(this, AboutActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                startActivity(intent);
                break;
            case R.id.btnShowSettings:
                intent = new Intent(this, SettingsActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                startActivity(intent);
                break;
            case R.id.btnShowFiles:
                intent = new Intent(this, FilesActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                startActivity(intent);
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.barmenu, menu);
        MenuItem mi = menu.findItem(R.id.btnShowSettings);
        if (mi != null)
        {
            mi.setEnabled(false);
            mi.setIcon(R.drawable.active_settingsicon);
        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        getWindow().setWindowAnimations(android.R.anim.slide_in_left);

        //addPreferencesFromResource(R.layout.preferences);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new MyPreferenceFragment()).commit();


    }

    public static class MyPreferenceFragment extends PreferenceFragment
    {

        PreferenceManager prefsMgr;
        Preference.OnPreferenceChangeListener prefsChangeListener = new Preference.OnPreferenceChangeListener()
        {
            public boolean onPreferenceChange(Preference preference, Object newValue)
            {
                // Code goes here
                Log.d("NR1", "PROP CHANGED");
                updateprefs(preference, newValue);
                updateDays();
                updateHours();
                startWakeUpSetup(true);

                getFragmentManager().beginTransaction().replace(android.R.id.content, new MyPreferenceFragment()).commit();

                //TEST UPDATE


                return true;
            }
        };

        void updateprefs(Preference preference, Object newValue)
        {
            prefsMgr = getPreferenceManager();
            if (preference.getKey().equalsIgnoreCase(getString(R.string.cfgMaxFileDateDays)))
            {

                findPreference(
                        getResources().getString(R.string.cfgMaxFileDateDays))
                        .setSummary("Delete files after " + newValue.toString() + " days");

            }
            else if (preference.getKey().equalsIgnoreCase(getString(R.string.cfgDownloadTime)))
            {
                updateHours();
            }
            else if (preference.getKey()
                    .equalsIgnoreCase(getString(R.string.cfgAutorunningWeekdays)))
            {
                updateDays();
            }

        }

        @Override
        public void onCreate(final Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.layout.preferences);

            prefsMgr = getPreferenceManager();
            try
            {

                String maxDays = prefsMgr.getSharedPreferences().getString(
                        getResources().getString(R.string.cfgMaxFileDateDays), null
                ).toString();

                findPreference(
                        getResources().getString(R.string.cfgMaxFileDateDays))
                        .setSummary("Delete files after " + maxDays + " days");
            }
            catch (Exception e)
            {
                Log.e("NR1", e.toString());
            }


            ((EditTextPreference) findPreference(
                    getResources().getString(R.string.cfgMaxFileDateDays)))
                    .setOnPreferenceChangeListener(prefsChangeListener);


            ((EditTextPreference) findPreference(
                    getResources().getString(R.string.cfgDownloadTime)))
                    .setOnPreferenceChangeListener(prefsChangeListener);

            ((CheckBoxPreference) findPreference(
                    getResources().getString(R.string.cfgDownloadOnTimer)))
                    .setOnPreferenceChangeListener(prefsChangeListener);


            ((MultiSelectListPreference) findPreference(
                    getResources().getString(R.string.cfgAutorunningWeekdays)))
                    .setOnPreferenceChangeListener(prefsChangeListener);

            updateDays();
            updateHours();
            startWakeUpSetup();
        }

        void startWakeUpSetup()
        {
            startWakeUpSetup(false);
        }

        void startWakeUpSetup(boolean bDisplayMsg)
        {
            CommonFunctions cf = (CommonFunctions) (getActivity().getApplicationContext());
            cf.setAppliWakeUps(bDisplayMsg);

        }

        void updateHours()
        {
            prefsMgr = getPreferenceManager();
            String ddlTime = prefsMgr.getSharedPreferences().getString(
                    getResources().getString(R.string.cfgDownloadTime), ""
            ).toString();

            findPreference(
                    getResources().getString(R.string.cfgDownloadTime))
                    .setSummary("" + ddlTime.toString());

        }

        void updateDays()
        {
            updateDays(null);
        }

        void updateDays(Set<Integer> newValue)
        {
            android.util.Log.d("NR1", "updatedays start");
            MultiSelectListPreference mslpItem = ((MultiSelectListPreference) findPreference(
                    getResources().getString(R.string.cfgAutorunningWeekdays)));
            if (mslpItem == null)
            {
                android.util.Log.d("NR1", "updatedays NULL,stopping now");
                return;
            }
            prefsMgr = getPreferenceManager();

            Set<String> days = new HashSet<String>();

            if (newValue != null)
            {
                android.util.Log.d("NR1", "days = newValue");
                for (int i : newValue)
                {
                    //days=new .getClass(Set<String>());
                    days.add("" + i);
                }

            }
            else
            {
                android.util.Log.d("NR1", "newValue = null - taking existing dates");

                try
                {
                    String strName = getResources().getString(R.string.cfgAutorunningWeekdays);
                    days = prefsMgr.getSharedPreferences().getStringSet(
                            strName, new HashSet<String>());
                    if (days == null)
                    {
                        mslpItem.setSummary("existing days == null - exiting function");
                        Log.d("NR1", "existing days == null - exiting function");
                        return;
                    }
                }
                catch (Exception e)
                {
                    mslpItem.setSummary(e.getCause().toString());
                    return;
                }
            }

            android.util.Log.d("NR1", "updatedays ???? suite");
            String dayNames[] = getResources().getStringArray(R.array.tabWeekdays);
            String dayNamesInt[] = getResources().getStringArray(R.array.tabWeekdaysInt);

            String strResult = "";
            for (int i = 0; i <= dayNames.length - 1; i++)
            {

                String strDayName;
                boolean bFound = false;
                for (String s : days)
                {
                    if (s.equalsIgnoreCase("" + dayNamesInt[i]))
                    {
                        bFound = true;
                    }
                    else
                    {
                        //	android.util.Log.d("NR1", s + " <> " + dayNamesInt[i]);
                    }

                }
                if (!bFound)
                {
                    strDayName = "";//+dayNamesInt[i];
                    //android.util.Log.d("NR1", "dayNamesInt(" + i + ")");
                }
                else
                {

                    strDayName = (dayNames[i]);
                    android.util.Log.d("NR1", "dayNames(" + i + ")");

                }
                if (strDayName != "")
                {
                    strResult += (strResult != "" ? "," : "")
                            + strDayName;
                }

                //tabDaysID.put(dayNames[i], dayNamesInt[i]);
            }
            mslpItem.setSummary(strResult);
        }

    }
}

