package com.csun.spotr;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.ListView;
import android.widget.Toast;

import com.csun.spotr.adapter.CheckInUserItemAdapter;
import com.csun.spotr.adapter.EventAdapter;
import com.csun.spotr.core.Event;
import com.csun.spotr.skeleton.IActivityProgressUpdate;
import com.csun.spotr.skeleton.IAsyncTask;
import com.csun.spotr.util.JsonHelper;

/**
 * NOTE: Refactoring by Chan Nguyen: 03/06/2012
 **/

public class CheckInActivity extends BasicSpotrActivity implements
        IActivityProgressUpdate<String> {
    private static final String TAG = "(CheckInActivity)";
    private static final String DO_CHECK_IN_URL = "http://107.22.209.62/android/do_check_in.php";
    private static final String GET_CHECKIN_USERS_URL = "http://107.22.209.62/android/get_checkin_users.php";
    private static final String GET_EVENTS = "http://107.22.209.62/android/get_events.php";
    private String usersId;
    private String spotsId;
    private String challengesId;
    private CheckInTask task;
    private List<String> userImageList;
    private List<Event> eventList;
    private Gallery gallery;
    private ListView listview;
    private CheckInUserItemAdapter adapter;
    private EventAdapter eventAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.check_in);
        setupTitleBar();
        initUserDataFromBundle();
        setupCheckInUserGallery();
        setupEventListView();
        setupCheckInButton();
        new GetCheckInUsersTask(this, spotsId).execute();
        new GetEventTask(this, spotsId).execute();
    }

    protected void setupTitleBar() {
        super.setupTitleBar();
    }

    private void setupCheckInButton() {
        Button checkin = (Button) findViewById(R.id.check_in_xml_button_checkin);
        checkin.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                task = new CheckInTask(CheckInActivity.this, usersId, spotsId,
                        challengesId);
                task.execute();
            }
        });
    }

    private void initUserDataFromBundle() {
        Bundle extras = getIntent().getExtras();
        usersId = extras.getString("users_id");
        spotsId = extras.getString("spots_id");
        challengesId = extras.getString("challenges_id");
    }

    private void setupCheckInUserGallery() {
        userImageList = new ArrayList<String>();
        gallery = (Gallery) findViewById(R.id.check_in_xml_gallery_checkin_people);
        adapter = new CheckInUserItemAdapter(this, userImageList);
        gallery.setAdapter(adapter);
    }

    private void setupEventListView() {
        eventList = new ArrayList<Event>();
        listview = (ListView) findViewById(R.id.check_in_xml_listview_events);
        eventAdapter = new EventAdapter(this, eventList);
        listview.setAdapter(eventAdapter);
        listview.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
                Intent intent = new Intent(getApplicationContext(),
                        EventActivity.class);
                intent.putExtra("event_link", eventList.get(position).getUrl());
                startActivity(intent);
            }
        });
    }

    private static class CheckInTask extends AsyncTask<Void, Void, String>
            implements IAsyncTask<CheckInActivity> {
        private static final String TAG = "[AsyncTask].CheckInTask";
        private WeakReference<CheckInActivity> ref;
        private String usersId;
        private String spotsId;
        private String challengesId;
        private String badgeProgress = "incomplete";

        public CheckInTask(CheckInActivity a, String usersId, String spotsId,
                String challengesId) {
            attach(a);
            this.usersId = usersId;
            this.spotsId = spotsId;
            this.challengesId = challengesId;
        }

        private List<NameValuePair> prepareUploadData() {
            List<NameValuePair> data = new ArrayList<NameValuePair>();
            data.add(new BasicNameValuePair("users_id", usersId));
            data.add(new BasicNameValuePair("spots_id", spotsId));
            data.add(new BasicNameValuePair("challenges_id", challengesId));
            return data;
        }

        @Override
        protected String doInBackground(Void... voids) {
            /*
             * 1. Retrieve data from [activity] table where $users_id and
             * $places_id 2. Check the result of this query: a. If the result is
             * null, then user hasn't visited this place yet which also implies
             * that he has not done any challenges. Thus we can update the
             * current user: i. Update [activity] table with $users_id,
             * $places_id, $challenges_id ii. Update [users] table with +
             * $challenges_done = $challenges_done + 1 + $points +=
             * challenges.points + $places_visited = $places_visited + 1 b. If
             * the result is not null, update [activity] table with $users_id,
             * $places_id, $challenges_id with CURRENT_TIMESTAMP, but don't run
             * the statement: + $places_visited = $places_visited + 1 3. All
             * these complexity is done at server side, i.e. php script, so we
             * only need to post to the server three parameters: a. users_id b.
             * places_id c. challenges_id 4. The return of this query is the
             * number points is added the points added to the user account.
             */
            List<NameValuePair> data = prepareUploadData();
            JSONObject json = JsonHelper.getJsonObjectFromUrlWithData(
                    DO_CHECK_IN_URL, data);
            String result = "";
            try {
                result = json.getString("result");
                badgeProgress = json.getString("badge");
            } catch (JSONException e) {
                Log.e(TAG + ".doInBackGround(Void ...voids) : ",
                        "JSON error parsing data", e);
            }
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            if (result.equals("success")) {
                Button checkin = (Button) ref.get().findViewById(
                        R.id.check_in_xml_button_checkin);
                checkin.setBackgroundColor(Color.RED);
                if(badgeProgress.equalsIgnoreCase("incomplete")){ // If user has not earned a badge, do nothing.
                   
                }
                else {
                    Toast.makeText(ref.get().getApplicationContext(), "You earned the badge: " + badgeProgress, Toast.LENGTH_LONG).show();
                }
                Intent intent = new Intent();
                intent.setData(Uri.parse("done"));
                ref.get().setResult(RESULT_OK, intent);
                ref.get().finish();
            } else {
                ref.get().showDialog(0);
            }
            detach();
        }

        public void attach(CheckInActivity a) {
            ref = new WeakReference<CheckInActivity>(a);
        }

        public void detach() {
            ref.clear();
        }
    }

    private static class GetCheckInUsersTask extends
            AsyncTask<Integer, String, Boolean> implements
            IAsyncTask<CheckInActivity> {
        private WeakReference<CheckInActivity> ref;
        private String spotsId;

        public GetCheckInUsersTask(CheckInActivity a, String spotsId) {
            attach(a);
            this.spotsId = spotsId;
        }

        @Override
        protected void onProgressUpdate(String... s) {
            ref.get().updateAsyncTaskProgress(s[0]);
        }

        @Override
        protected Boolean doInBackground(Integer... offsets) {
            List<NameValuePair> data = new ArrayList<NameValuePair>();
            data.add(new BasicNameValuePair("place_id", spotsId));
            JSONArray array = JsonHelper.getJsonArrayFromUrlWithData(
                    GET_CHECKIN_USERS_URL, data);
            if (array != null) {
                try {
                    for (int i = 0; i < array.length(); ++i) {
                        publishProgress(array.getJSONObject(i).getString(
                                "users_tbl_user_image_url"));
                    }
                } catch (JSONException e) {
                    Log.e(TAG
                            + "GetCheckInUsersTask.doInBackGround(Integer... offsets) : ",
                            "JSON error parsing data", e);
                }
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            detach();
        }

        public void attach(CheckInActivity a) {
            ref = new WeakReference<CheckInActivity>(a);
        }

        public void detach() {
            ref.clear();
        }
    }

    private static class GetEventTask extends
            AsyncTask<Integer, Event, Boolean> implements
            IAsyncTask<CheckInActivity> {

        private WeakReference<CheckInActivity> ref;
        private String spotsId;

        public GetEventTask(CheckInActivity a, String spotsId) {
            attach(a);
            this.spotsId = spotsId;
        }

        @Override
        protected void onProgressUpdate(Event... e) {
            ref.get().eventList.add(e[0]);
            ref.get().eventAdapter.notifyDataSetChanged();
        }

        @Override
        protected Boolean doInBackground(Integer... offsets) {
            List<NameValuePair> data = new ArrayList<NameValuePair>();
            data.add(new BasicNameValuePair("place_id", spotsId));
            JSONArray array = JsonHelper.getJsonArrayFromUrlWithData(
                    GET_EVENTS, data);
            if (array != null) {
                try {
                    for (int i = 0; i < array.length(); ++i) {
                        publishProgress(new Event(array.getJSONObject(i)
                                .getInt("event_tbl_id"), array.getJSONObject(i)
                                .getString("event_tbl_name"), array
                                .getJSONObject(i)
                                .getString("event_tbl_context"), array
                                .getJSONObject(i).getString("event_tbl_url"),
                                array.getJSONObject(i).getString(
                                        "event_tbl_time")));
                    }
                } catch (JSONException e) {
                    Log.e(TAG
                            + "GetEventTask.doInBackGround(Integer... offsets) : ",
                            "JSON error parsing data", e);
                }
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            detach();
        }

        public void attach(CheckInActivity a) {
            ref = new WeakReference<CheckInActivity>(a);
        }

        public void detach() {
            ref.clear();
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
        case 0:
            return new AlertDialog.Builder(this)
                    .setIcon(R.drawable.error_circle)
                    .setTitle("Warning!")
                    .setMessage(
                            "You checked in recently. You can only check in once every 24 hours. :(!")
                    .setPositiveButton("OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                        int whichButton) {
                                }
                            }).create();
        case 1:
            return new AlertDialog.Builder(this)
                    .setIcon(R.drawable.error_circle)
                    .setTitle("Error Message")
                    .setMessage("<undefined>")
                    .setPositiveButton("OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                        int whichButton) {
                                }
                            }).create();
        }
        return null;
    }

    public void updateAsyncTaskProgress(String u) {
        userImageList.add(u);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroy() {
        Log.v(TAG, "I'm destroyed!");
        super.onDestroy();
    }

    @Override
    public void onRestart() {
        Log.v(TAG, "I'm restarted!");
        super.onRestart();
    }

    @Override
    public void onStop() {
        Log.v(TAG, "I'm stopped!");
        super.onStop();
    }

    @Override
    public void onPause() {
        Log.v(TAG, "I'm paused!");
        super.onPause();
    }

    @Override
    public void onResume() {
        Log.v(TAG, "I'm resumed");
        super.onResume();
    }
}