package com.polimi.jaj.roarify.fragment;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.widget.Toast;

import com.facebook.Profile;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.polimi.jaj.roarify.R;
import com.polimi.jaj.roarify.model.Message;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static com.polimi.jaj.roarify.activity.HomeActivity.db;

public class SettingsFragment extends PreferenceFragmentCompat {

    private AlertDialog.Builder builderDeleteF;
    private AlertDialog alertDeleteF;
    private AlertDialog.Builder builderDeleteM;
    private AlertDialog alertDeleteM;
    String idMessage;
    List<String> messageIDs = new ArrayList<String>();

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
                        new GetUserMessages().execute();
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



    private class DeleteMessage extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {

            HttpPost post = new HttpPost("https://1-dot-roarify-server.appspot.com/deleteMessage");
            List<NameValuePair> pairs = new ArrayList<NameValuePair>();

            pairs.add(new BasicNameValuePair("id", "" + idMessage));


            try {
                post.setEntity(new UrlEncodedFormEntity(pairs));
            } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            try {
                HttpClient client = new DefaultHttpClient();
                HttpResponse response = client.execute(post);
                if (response.getStatusLine().getStatusCode() == 200) {
                    return true;
                } else {
                    return false;
                }
            } catch (ClientProtocolException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return false;
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return false;
            }

        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onPostExecute(Boolean result) {

        }

    }


    private class GetUserMessages extends AsyncTask<Void, Message, Boolean> {

        ProgressDialog asyncDialog = new ProgressDialog(getActivity());

        @Override
        protected Boolean doInBackground(Void... params) {
            /*while (!isCancelled()) {*/
            List<NameValuePair> pairs = new ArrayList<NameValuePair>();


            pairs.add(new BasicNameValuePair("userId", "" + Profile.getCurrentProfile().getId().toString()));

            String paramsString = URLEncodedUtils.format(pairs, "UTF-8");
            HttpGet get = new HttpGet("http://1-dot-roarify-server.appspot.com/getUserMessages" + "?" + paramsString);

            try {
                HttpClient client = new DefaultHttpClient();
                HttpResponse response = client.execute(get);
                HttpEntity entity = response.getEntity();
                BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent(), "iso-8859-1"), 8);

                String jsonResponse = reader.readLine();

                Gson gson = new Gson();
                TypeToken<List<Message>> token = new TypeToken<List<Message>>() {
                };
                List<Message> messagesList = gson.fromJson(jsonResponse, token.getType());
                if (messagesList != null) {
                    publishProgress(null);
                    for (Message a : messagesList) {
                        publishProgress(a);
                    }

                }

            } catch (ClientProtocolException e) {
                e.printStackTrace();
                showToastedWarning();
            } catch (IOException e) {
                e.printStackTrace();
                showToastedWarning();
            }

            return null;
        }

        private void showToastedWarning() {
            try {
                (getActivity()).runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        Toast.makeText((getActivity()), R.string.load_messages_fail, Toast.LENGTH_SHORT).show();
                    }
                });
                Thread.sleep(300);
            } catch (InterruptedException e2) {
                e2.printStackTrace();
            }
        }



        @Override
        protected void onPreExecute() {
            asyncDialog.setMessage("Deleting your messages");
            asyncDialog.show();
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Boolean result) {
            for (int i=0; i<messageIDs.size();i++) {
                idMessage = messageIDs.get(i);
                try {
                    new DeleteMessage().execute().get(); //Wait until the message is deleted, to delete the following one
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
            asyncDialog.dismiss();
            super.onPostExecute(result);
        }

        @Override
        protected void onProgressUpdate(Message... values) {
            // TODO Auto-generated method stub
            if (values == null) {

            } else {
                messageIDs.add(values[0].getMessageId());
            }
        }

    }


}
