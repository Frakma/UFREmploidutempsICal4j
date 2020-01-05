package com.jldeveloper.ufremploidutemps;

import android.content.res.TypedArray;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;

import com.bumptech.glide.Glide;


public class HelpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        setTitle(R.string.how_to_use);

        if(getSupportActionBar()!=null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        ViewPager slideShow=(ViewPager) findViewById(R.id.help_viewpager);


        //Recuperation des ID des images a utiliser.
        TypedArray imageResIdsTyped=getResources().obtainTypedArray(R.array.tutorial_image_set);

        //Tableau contenant tout les ids d'images a utiliser.
        int[] imageResIds=new int[imageResIdsTyped.length()];

        for(int i=0;i<imageResIdsTyped.length();i++){

            imageResIds[i]=imageResIdsTyped.getResourceId(i,0);

        }


        imageResIdsTyped.recycle();


        //Création de l'adapteur pour afficher ces images
        slideShow.setAdapter(new HelpViewPagerAdapter(this,imageResIds));

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return true;
    }
}
