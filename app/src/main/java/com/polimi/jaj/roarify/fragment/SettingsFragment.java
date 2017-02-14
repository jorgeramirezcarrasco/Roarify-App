package com.polimi.jaj.roarify.fragment;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;

import com.polimi.jaj.roarify.R;

import static com.polimi.jaj.roarify.activity.HomeActivity.db;

public class SettingsFragment extends PreferenceFragmentCompat {

    private AlertDialog.Builder builderDeleteF;
    private AlertDialog alertDeleteF;
    private AlertDialog.Builder builderDeleteM;
    private AlertDialog alertDeleteM;

    @Override
    public void onCreatePreferences(final Bundle savedInstanceState, String rootKey) {

        setPreferencesFromResource(R.xml.preferences_settings, rootKey);

        Preference favoritesPref = (Preference) findPreference("delete_favorites_preference");
        favoritesPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {

                builderDeleteF = new AlertDialog.Builder(getActivity());
                builderDeleteF.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        db.deleteAll();
                    }
                });
                builderDeleteF.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

                builderDeleteF.setTitle("Delete favorite messages");
                builderDeleteF.setMessage("Are you sure that you want to delete all your favorite messages?").setCancelable(false);

                alertDeleteF= builderDeleteF.create();
                alertDeleteF.show();

                return true;
            }
        });

        Preference messagesPref = (Preference) findPreference("delete_messages_preference");
        messagesPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {

                builderDeleteM = new AlertDialog.Builder(getActivity());
                builderDeleteM.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // CALL SERVER METHOD TO DELETE ALL YOUR MESSAGES
                    }
                });
                builderDeleteM.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

                builderDeleteM.setTitle("Delete own messages");
                builderDeleteM.setMessage("Are you sure that you want to delete all your messages? All your messages will disappear and you won't be able to get them back.").setCancelable(false);

                alertDeleteM= builderDeleteM.create();
                alertDeleteM.show();

                return true;
            }
        });
    }
}
