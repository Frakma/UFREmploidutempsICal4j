package com.jldeveloper.ufremploidutemps;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import com.google.android.material.snackbar.Snackbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;


import android.view.MenuItem;
import android.widget.Toast;

import com.codetroopers.betterpickers.hmspicker.HmsPickerBuilder;
import com.codetroopers.betterpickers.hmspicker.HmsPickerDialogFragment;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class SettingsActivity extends AppCompatActivity {


    static final int PERMISSIONS_REQUEST_CAMERA = 8793;

    boolean hasCameraPermission = false;

    CoordinatorLayout coordinatorLayout;

    HmsPickerBuilder syncOnStartDelayPicker;
    HmsPickerDialogFragment.HmsPickerDialogHandlerV2 handlerDelayPicker;

    SharedPreferences sharedPreferences;
    Intent resultIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(R.string.title_activity_settings);

        setContentView(R.layout.activity_settings);

        resultIntent=new Intent();

        setResult(RESULT_OK,resultIntent);

        sharedPreferences=PreferenceManager.getDefaultSharedPreferences(this);

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.activity_settings_coordinator_layout);

        //Check si la permission de camera est autorisé

        hasCameraPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }


        handlerDelayPicker = new HmsPickerDialogFragment.HmsPickerDialogHandlerV2() {
            @Override
            public void onDialogHmsSet(int reference, boolean isNegative, int hours, int minutes, int seconds) {
                //Log.d("abc", hours + "|" + minutes + "|" + seconds);
                publishSyncOnStartDelay(hours,minutes,seconds);
            }
        };

        syncOnStartDelayPicker = new HmsPickerBuilder()
                .setFragmentManager(getSupportFragmentManager())
                .setStyleResId(R.style.BetterPickersDialogFragment_Light)
                .addHmsPickerDialogHandler(handlerDelayPicker);


//        Button btn=(Button) findViewById(R.id.buttonScan);
//        btn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                if(!hasCameraPermission){
//                    requestCameraPermission();
//                }else{
//                    scanQr();
//                }
//
//            }
//        });


    }


    private void requestCameraPermission() {
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

        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void scanQr() {

        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setBeepEnabled(false);
        //integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES );
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
        integrator.setPrompt(getResources().getString(R.string.qr_code_scan_prompt));
        integrator.initiateScan();
    }

    public void setSyncOnStartDelay() {
        fillSyncOnStartDelayPicker();
        syncOnStartDelayPicker.show();
    }

    public void fillSyncOnStartDelayPicker() {
        String time=sharedPreferences.getString(PreferenceKeys.SYNC_ON_START_DELAY_PREF,"0:10:0");
        String[] hms=time.split(":");
        syncOnStartDelayPicker.setTime(Integer.parseInt(hms[0]),Integer.parseInt(hms[1]),Integer.parseInt(hms[2]));
    }

    public void publishSyncOnStartDelay(int hours, int minutes, int seconds) {
        String time=String.format("%d:%d:%d",hours,minutes,seconds);

        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putString(PreferenceKeys.SYNC_ON_START_DELAY_PREF,time);
        editor.apply();
    }

    public static class AllSettings extends PreferenceFragment {


        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_all);

            EditTextPreference urlPref=(EditTextPreference) findPreference(PreferenceKeys.SYNC_URL_STRING);

            urlPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {

                    ((SettingsActivity)getActivity()).resultIntent.putExtra(PreferenceKeys.INTENT_SETTINGS_NEEDS_RESYNG,true);
                    return true;
                }
            });

            Preference scanQrPref = (Preference) findPreference(PreferenceKeys.SCAN_QR_CODE_PREF);

            scanQrPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {


                    if (!((SettingsActivity) getActivity()).hasCameraPermission) {
                        ((SettingsActivity) getActivity()).requestCameraPermission();
                    } else {
                        ((SettingsActivity) getActivity()).scanQr();
                    }

                    return true;
                }
            });

            Preference syncOnStartDelayPref = (Preference) findPreference(PreferenceKeys.SYNC_ON_START_DELAY_PREF);

            syncOnStartDelayPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {

                    ((SettingsActivity) getActivity()).setSyncOnStartDelay();
                    return true;
                }
            });
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        IntentResult intentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

        if (intentResult != null) {
            if (intentResult.getContents() != null) {

                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(PreferenceKeys.SYNC_URL_STRING, intentResult.getContents());
                editor.apply();
                snackBarMaker(R.string.success_qr_code_url_saving, Snackbar.LENGTH_LONG);
                //Now should reload time schedule with new URL
                resultIntent.putExtra(PreferenceKeys.INTENT_SETTINGS_NEEDS_RESYNG,true);

            } else {
                snackBarMaker(R.string.failed_qr_code_url_saving, Snackbar.LENGTH_LONG);
            }

        } else {
            snackBarMaker(R.string.failed_qr_code_url_saving, Snackbar.LENGTH_LONG);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CAMERA: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    hasCameraPermission = true;
                    scanQr();
                } else {
                    hasCameraPermission = false;
                    snackBarMaker(R.string.forbidden_camera_permission_hint, Snackbar.LENGTH_SHORT);
                }
                break;
            }


        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    void snackBarMaker(String s, int duration) {
        Snackbar.make(coordinatorLayout, s, duration).show();
    }

    void snackBarMaker(int id, int duration) {
        Snackbar.make(coordinatorLayout, getResources().getString(id), duration).show();
    }

    void toastMaker(String s, int duration) {
        Toast.makeText(this, s, duration).show();
    }

    public String getSyncURLFromPreferences() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        return sharedPreferences.getString(PreferenceKeys.SYNC_URL_STRING, "");
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
    }
}
