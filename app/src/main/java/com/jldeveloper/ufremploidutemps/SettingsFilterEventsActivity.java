package com.jldeveloper.ufremploidutemps;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.PersistableBundle;
import android.preference.PreferenceManager;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.TreeSet;

public class SettingsFilterEventsActivity extends AppCompatActivity {

    static final String BUNDLE_INSTANCE_LISTVIEW_STRINGARRAYLIST ="event list view content key";
    static final String BUNDLE_INSTANCE_ADAPTER_COUNT="boolean value if list modified";

    ListView filterListView;
    CoordinatorLayout coordinatorLayout;

    int currentAdapterCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_filter_events);

        setTitle(R.string.title_activity_hidden_events);

        if(getSupportActionBar()!=null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        filterListView=(ListView) findViewById(R.id.filter_event_settings_listview);

        if(savedInstanceState!=null){
            ArrayList<String> lastValues=savedInstanceState.getStringArrayList(BUNDLE_INSTANCE_LISTVIEW_STRINGARRAYLIST);
            currentAdapterCount=savedInstanceState.getInt(BUNDLE_INSTANCE_ADAPTER_COUNT);
            filterListView.setAdapter(new SettingFilterEventsArrayAdapter(this,lastValues));

        }else{
            filterListView.setAdapter(new SettingFilterEventsArrayAdapter(this,getFilterSummaryValues()));
            currentAdapterCount=filterListView.getAdapter().getCount();
        }


        coordinatorLayout=(CoordinatorLayout)findViewById(R.id.event_filter_settings_coordinator_layout);

    }

    ArrayList<String> getFilterSummaryValues(){
        SharedPreferences sharedPreferences= PreferenceManager.getDefaultSharedPreferences(this);
        return new ArrayList<String>(sharedPreferences.getStringSet(PreferenceKeys.EXCLUDED_EVENTS_SUMMARY_SET,new TreeSet<String>()));
    }

    /**
     * writes the listview content to a preference stringset to save everything.
     * return true if changes have been saved (if there is something to save). else return false if no changes have been made.
     * @return
     */
    boolean publishNewFilteredValues(){
        TreeSet<String> resultSet=new TreeSet<>();
        ListAdapter adapter=filterListView.getAdapter();
        //Si il n'y a pas eut des modifications, on passe la réécriture des preferences elle est inutile.
        if(currentAdapterCount==adapter.getCount()){
            return false;
        }
        currentAdapterCount=adapter.getCount();
        for(int i=0;i<currentAdapterCount;i++){
            resultSet.add((String)adapter.getItem(i));
        }

        SharedPreferences sharedPreferences=PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putStringSet(PreferenceKeys.EXCLUDED_EVENTS_SUMMARY_SET,resultSet);
        editor.apply();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.filter_event_settings_menu_save:
                if(publishNewFilteredValues()){
                    snackBarMaker(R.string.modifications_saved,Snackbar.LENGTH_SHORT);
                }
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater menuInflater=getMenuInflater();

        menuInflater.inflate(R.menu.filter_event_activity_menu,menu);

        return super.onCreateOptionsMenu(menu);
    }

    void showConfirmSaveBeforeFinishDialog(){
        AlertDialog.Builder builder=new AlertDialog.Builder(this,R.style.DialogStyle);
        builder.setTitle(R.string.title_dialog_save_before_quitting);
        builder.setMessage(R.string.dialog_promp_save_modifications_before_quitting);
        builder.setPositiveButton(R.string.YES, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                publishNewFilteredValues();
                finish();
            }
        });
        builder.setNegativeButton(R.string.NO, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        builder.show();
    }

    void snackBarMaker(String s,int duration){
        Snackbar.make(coordinatorLayout,s,duration).show();
    }

    void snackBarMaker(int id,int duration){
        Snackbar.make(coordinatorLayout,getResources().getString(id),duration).show();
    }

    @Override
    public void onBackPressed() {
        if(hasBeenModified()){
            showConfirmSaveBeforeFinishDialog();
        }else{
            finish();
        }
    }

    private boolean hasBeenModified(){
        return currentAdapterCount !=filterListView.getAdapter().getCount();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        ArrayList<String> currentListValues=((SettingFilterEventsArrayAdapter)filterListView.getAdapter()).getAdapterContentValues();
        outState.putStringArrayList(BUNDLE_INSTANCE_LISTVIEW_STRINGARRAYLIST,currentListValues);
        outState.putInt(BUNDLE_INSTANCE_ADAPTER_COUNT,currentAdapterCount);

        super.onSaveInstanceState(outState);
    }
}
