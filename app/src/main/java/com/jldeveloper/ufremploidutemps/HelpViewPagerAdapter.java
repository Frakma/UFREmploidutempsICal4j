package com.jldeveloper.ufremploidutemps;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;


import java.io.File;
import java.io.InputStream;
import java.util.Locale;

/**
 * Created by Jérémy on 11/02/2017.
 */

public class HelpViewPagerAdapter extends PagerAdapter {

    int[] imageResIds;
    Context mContext;

    public HelpViewPagerAdapter(Context context, int[] imageresIDs) {
        this.imageResIds=imageresIDs;
        mContext=context;
    }




    @Override
    public Object instantiateItem(ViewGroup container, int position) {

        int resId=imageResIds[position];

        LayoutInflater inflater=LayoutInflater.from(mContext);

        ViewGroup layout= (ViewGroup) inflater.inflate(R.layout.help_item_view_layout,container,false);

        ImageView imageView=(ImageView)layout.findViewById(R.id.help_imageview);

        TextView cursorTextview=(TextView) layout.findViewById(R.id.help_cursor_textview);

        cursorTextview.setText(String.format(Locale.getDefault(),"%1$d / %2$d",position+1,getCount()));


        Glide.with(mContext).load(resId).into(imageView);


        container.addView(layout);

        return layout;
    }

    @Override
    public int getCount() {
        return imageResIds.length;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view==object;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View)object);

    }
}
