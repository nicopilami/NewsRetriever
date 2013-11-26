package com.nicocorp.nr1;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

/**
 * Created by Nico on 06/07/13.
 */
public class AboutActivity extends Activity {

    SharedPreferences settings;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.barmenu, menu);

        MenuItem mi = menu.findItem(R.id.btnShowContacts);
        if (mi != null) {
            mi.setEnabled(false);
            mi.setIcon(R.drawable.active_mailicon);
        }
        return true;
    }

    public void ClickActivityFromMenu(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
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

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about);
        getWindow().setWindowAnimations(android.R.anim.slide_out_right);

    }
}
