package com.jldeveloper.ufremploidutemps;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class SettingsActivity extends AppCompatActivity {



    static final int PERMISSIONS_REQUEST_CAMERA=8793;

    boolean hasCameraPermission=false;
    CoordinatorLayout coordinatorLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(R.string.title_activity_settings);

        setContentView(R.layout.activity_settings);

        coordinatorLayout=(CoordinatorLayout) findViewById(R.id.activity_settings_coordinator_layout);

        //Check si la permission de camera est autorisé

        hasCameraPermission= ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;

        if(getSupportActionBar()!=null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }



        Button btn=(Button) findViewById(R.id.buttonScan);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!hasCameraPermission){
                    requestCameraPermission();
                }else{
                    scanQr();
                }

            }
        });



    }

    

    private void requestCameraPermission(){
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    PERMISSIONS_REQUEST_CAMERA);

        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void scanQr(){

        IntentIntegrator integrator=new IntentIntegrator(this);
        integrator.setBeepEnabled(false);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
        integrator.setPrompt(getResources().getString(R.string.qr_code_scan_prompt));
        integrator.initiateScan();
    }


    public static class AllSettings extends PreferenceFragment{


        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_all);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        IntentResult intentResult=IntentIntegrator.parseActivityResult(requestCode,resultCode,data);

        if(intentResult!=null){
            if(intentResult.getContents() !=null){

                SharedPreferences sharedPreferences= PreferenceManager.getDefaultSharedPreferences(this);
                SharedPreferences.Editor editor= sharedPreferences.edit();
                editor.putString(PreferenceKeys.SYNC_URL_STRING,intentResult.getContents());
                editor.apply();
                snackBarMaker(R.string.success_qr_code_url_saving,Snackbar.LENGTH_LONG);


            }
            else{
                snackBarMaker(R.string.failed_qr_code_url_saving,Snackbar.LENGTH_LONG);
            }

        }
        else{
           snackBarMaker(R.string.failed_qr_code_url_saving,Snackbar.LENGTH_LONG);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case PERMISSIONS_REQUEST_CAMERA:{
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    hasCameraPermission=true;
                    scanQr();
                }
                else{
                    hasCameraPermission=false;
                    snackBarMaker(R.string.forbidden_camera_permission_hint,Snackbar.LENGTH_SHORT);
                }
                break;
            }


        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    void snackBarMaker(String s,int duration){
        Snackbar.make(coordinatorLayout,s,duration).show();
    }

    void snackBarMaker(int id,int duration){
        Snackbar.make(coordinatorLayout,getResources().getString(id),duration).show();
    }

    void toastMaker(String s,int duration){
        Toast.makeText(this,s,duration).show();
    }
}
