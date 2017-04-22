package com.jldeveloper.ufremploidutemps;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.content.res.ResourcesCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.fortuna.ical4j.model.component.VEvent;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


/**
 * Created by Jeremy on 03/11/2016.
 */

public class EventArrayAdapter extends ArrayAdapter<VEvent> {


    private ArrayList<net.fortuna.ical4j.model.component.VEvent> eventarray;
    private Context context;
    private int[] colors;
    private SimpleDateFormat df= new SimpleDateFormat("HH:mm");
    private boolean usePersonnalizedColoration;
    private int personnalizedColor;
    private LayoutInflater mInflater;

    /**
     * Selects an index in an array based on the hash integer passed.
     * @param hash the hash integer which sine is calculated from
     * @param array an array in which the index should choosen
     * @return
     */
    private static int getColorIndexFromHash(int hash,int[] array){
        int l=array.length;

        return (int) ((Math.sin(hash)+1)*l)/2;
    }

    /**
     * Trims everything that there is behind the letters CM TD TP and ( to allow to have the same hashcode from string
     * differing by only the letter behind CM TD TP...
     * @param s
     * @return
     */
    private static String trimStringFromTPTDCM(String s){
        String result=s;
        if(result.contains("CM")){
            result=result.substring(0,result.indexOf("CM"));
        }
        if(result.contains("TD")){
            result=result.substring(0,result.indexOf("TD"));
        }
        if(result.contains("TP")){
            result=result.substring(0,result.indexOf("TP"));
        }
        if(result.contains("(")){
            result=result.substring(0,result.indexOf("("));
        }

        return result;
    }



    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        //retreive the corresponding event
        net.fortuna.ical4j.model.component.VEvent event=eventarray.get(position);

        //If the view doesn't already exists create it. Else, reuse it
        if(convertView==null){

            convertView=mInflater.inflate(R.layout.event_item_layout_2,parent,false);
        }

        //Retreive views to fill them
        LinearLayout linearLayout=(LinearLayout) convertView.findViewById(R.id.event_layout_main);


        TextView textViewDTStart=(TextView) convertView.findViewById(R.id.start_hour_event);
        TextView textViewDTEnd=(TextView) convertView.findViewById(R.id.end_hour_event);
        TextView textViewLocation=(TextView) convertView.findViewById(R.id.location_event);
        TextView textViewDescription=(TextView) convertView.findViewById(R.id.title_event);

        //Retrieve the event values
        String summary=event.getSummary().getValue();
        String location=event.getLocation().getValue();

        //Update the view with event data
        textViewDescription.setText(summary);
        textViewLocation.setText(location);

        Date dateStart=event.getStartDate().getDate();
        Date dateEnd=event.getEndDate().getDate();

        textViewDTStart.setText(df.format(dateStart));
        textViewDTEnd.setText(df.format(dateEnd));

        //Background color settings
        if(usePersonnalizedColoration){
            linearLayout.setBackgroundColor(personnalizedColor);
        }
        else{
            linearLayout.setBackgroundColor(colors[getColorIndexFromHash(trimStringFromTPTDCM(summary).hashCode(),colors)]);
        }

        return convertView;
    }

    public EventArrayAdapter(Context context, ArrayList<net.fortuna.ical4j.model.component.VEvent> eventarray){
        super(context,R.layout.event_item_layout_2,eventarray);
        this.eventarray=eventarray;
        this.context=context;
        //tool to inflate views
        this.mInflater=(LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        //retreiving user personnalization settings
        SharedPreferences sharedPreferences= PreferenceManager.getDefaultSharedPreferences(context);
        usePersonnalizedColoration =sharedPreferences.getBoolean(PreferenceKeys.EVENTS_CUSTOM_COLOR_BOOLEAN,false);
        colors = context.getResources().getIntArray(R.array.event_colors);
        personnalizedColor=sharedPreferences.getInt(PreferenceKeys.EVENTS_COLOR_INT, ResourcesCompat.getColor(context.getResources(), R.color.colorEventBackground, null));
    }




}
