package com.jldeveloper.ufremploidutemps;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;

import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.TreeSet;


/**
 * Created by Jérémy on 14/11/2016.
 */

public class EventDetailsActivity extends Activity {

    public static final int RESULT_OK_EXCLUDED_EVENT =68935;
    public static final String EXTRA_EXCLUDED_SUMMARY_VALUE="valeur Summary exclue";

    SimpleDateFormat dateFormat=new SimpleDateFormat("HH:mm");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_event_details);
        Intent startIntent=getIntent();

        //We receive an intent to fill the activity
        if(startIntent!=null){
            //Retreive the event
            net.fortuna.ical4j.model.component.VEvent event=(net.fortuna.ical4j.model.component.VEvent) startIntent.getSerializableExtra(MainActivity.EXTRA_EVENT_DETAIL_VEVENT);

            //Retreive the background color
            int color=startIntent.getIntExtra(MainActivity.EXTRA_EVENT_DETAIL_COLORBG, Color.BLACK);

            //Retreive the views
            TextView textViewDtStart=(TextView) findViewById(R.id.textview_event_detail_dtstart);
            TextView textViewDtEnd=(TextView) findViewById(R.id.textview_event_detail_dtend);
            TextView textViewSummary=(TextView) findViewById(R.id.textview_event_detail_summary);
            TextView textViewDescription=(TextView) findViewById(R.id.textview_event_detail_description);
            TextView textViewLocation=(TextView) findViewById(R.id.textview_event_detail_location);
            TextView textViewDuration=(TextView) findViewById(R.id.textview_event_detail_duration);

            LinearLayout linearLayout=(LinearLayout) findViewById(R.id.event_detail_layout_main);
            //Set the background color
            linearLayout.setBackgroundColor(color);

            //estimate the duration of the event in minutes and hours
            Date dtStart=event.getStartDate().getDate();
            Date dtEnd=event.getEndDate().getDate();

            long diffMin = Math.abs(dtStart.getTime() - dtEnd.getTime()) / 60000l;
            long numberMinutes = diffMin%60l;
            long numberHours = diffMin/60l;

            //Store the event summary string to use later
            final String summaryValue=event.getSummary().getValue();


            //Fill the views
            textViewDtStart.setText(dateFormat.format(dtStart));

            textViewDtEnd.setText(dateFormat.format(dtEnd));

            textViewSummary.setText(summaryValue);

            textViewDescription.setMaxLines(10);

            textViewDescription.setText(event.getDescription().getValue().replace("\\n","\n").trim());

            textViewLocation.setText(event.getLocation().getValue());

            textViewDuration.setText(numberHours + "h" + (numberMinutes==0 ? "00":numberMinutes));

            setTitle(event.getSummary().getValue());

            //Retreive and Bind the action of the button which excludes this event from displaying
            Button excludeEventButton=(Button) findViewById(R.id.exclude_event_button);
            excludeEventButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addSummaryToExcludedEvent(summaryValue);
                    Intent data=new Intent();
                    data.putExtra(EXTRA_EXCLUDED_SUMMARY_VALUE,summaryValue);
                    setResult(RESULT_OK_EXCLUDED_EVENT,data);
                    finish();
                }
            });

        }

        super.onCreate(savedInstanceState);
    }

    /**
     * Add the string summary to the preference string set @PreferenceKeys.EXCLUDED_EVENTS_SUMMARY_SET
     * @param summary
     */
    private void addSummaryToExcludedEvent(String summary){
        if(summary.length()<=0){
            return;
        }

        SharedPreferences sharedPreferences= PreferenceManager.getDefaultSharedPreferences(this);

        TreeSet<String> excludedSummaryValues=new TreeSet<>(sharedPreferences.getStringSet(PreferenceKeys.EXCLUDED_EVENTS_SUMMARY_SET,new TreeSet<String>()));

        excludedSummaryValues.add(summary);

        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putStringSet(PreferenceKeys.EXCLUDED_EVENTS_SUMMARY_SET,excludedSummaryValues);
        editor.apply();


    }
}
