package com.jldeveloper.ufremploidutemps;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.view.MenuItem;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getSupportActionBar()!=null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        setContentView(R.layout.activity_about);
        setTitle(R.string.about_app);


        TextView textView=(TextView) findViewById(R.id.textView_about);
        textView.setText(fromHTML(readTxt(R.raw.privacy_policy)+readTxt(R.raw.about)));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Converts the HTML source code to Spanned
     * Spanned allow textViews to display HTML style like h1, bold or italic
     * @param source
     * @return
     */
    @SuppressWarnings("deprecation")
    private Spanned fromHTML(String source){
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            return Html.fromHtml(source,Html.FROM_HTML_MODE_LEGACY);
        }
        else {
            return Html.fromHtml(source);
        }
    }

    /**
     * Reads the text from the hmtl file associated to resId
     * @param resId
     * @return
     */
    private String readTxt(int resId){
        InputStream inputStream = getResources().openRawResource(resId);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        int i;
        try {
            i = inputStream.read();
            while (i != -1)
            {
                byteArrayOutputStream.write(i);
                i = inputStream.read();
            }
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return byteArrayOutputStream.toString();
    }
}

