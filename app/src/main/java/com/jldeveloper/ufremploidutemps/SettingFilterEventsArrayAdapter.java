package com.jldeveloper.ufremploidutemps;

import android.content.Context;
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Jérémy on 18/11/2016.
 */

public class SettingFilterEventsArrayAdapter extends ArrayAdapter<String> {

    private ArrayList<String> excludedEvents;
    private Context context;

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if(convertView==null){
            LayoutInflater inflater=(LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            convertView=inflater.inflate(R.layout.filtering_event_value_listitem_layout,parent,false);
        }

        TextView filteringEventTextView=(TextView) convertView.findViewById(R.id.filtering_event_value_item_textview);
        ImageButton deleteButton=(ImageButton) convertView.findViewById(R.id.filtering_event_value_item_delete_image_button);

        final String excludedEvent=excludedEvents.get(position);

        filteringEventTextView.setText(excludedEvent);

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RemoveExcludedEvent(excludedEvent);
            }
        });

        return convertView;
    }


    public SettingFilterEventsArrayAdapter(Context context, ArrayList<String> objects) {
        super(context,R.layout.filtering_event_value_listitem_layout,objects);
        this.context=context;
        this.excludedEvents=objects;
    }

    private void RemoveExcludedEvent(String excludeEvent) {
        this.remove(excludeEvent);
    }

    public ArrayList<String> getAdapterContentValues(){
        return excludedEvents;
    }
}
