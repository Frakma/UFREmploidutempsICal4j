package com.jldeveloper.ufremploidutemps;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.codetroopers.betterpickers.calendardatepicker.CalendarDatePickerDialogFragment;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.filter.Filter;
import net.fortuna.ical4j.filter.PeriodRule;
import net.fortuna.ical4j.filter.Rule;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Dur;
import net.fortuna.ical4j.model.Period;
import net.fortuna.ical4j.model.component.VEvent;


import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;
import java.util.Scanner;
import java.util.TreeSet;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<net.fortuna.ical4j.model.Calendar> {

    public static final String TAG ="MainActivity";

    public static final int REQUEST_SETTINGS = 100;
    public static final int REQUEST_EVENT_DETAIL_ACTIVITY = 200;
    public static final int SUCCESS_CALENDAR_URL_PARSING = 1234;
    public static final int FAILED_CALENDAR_URL_PARSING = 2345;
    public static final int FAILED_URL_MALFORMATION = 7856;
    public static final int FAILED_CALENDAR_PARSING = 7216;
    public static final int FAILED_IO_ERROR = 8895;
    public static final int FAILED_TIMEOUT = 6599;
    public static final int START_REFRESH = 3456;
    public static final int FINISHED_REFRESH = 4567;
    public static final int FAILED_URL_RESOLUTION = 89894;
    public static final String FRAG_TAG_CALENDAR_DATEPICKER = "date picker calendar";
    public static final String KEY_CALENDAR_DATE = "date_calendrier_courant";
    public static final String KEY_VCALENDAR_OBJECT = "vcalendar_courant";
    public static final String EXTRA_EVENT_DETAIL_COLORBG = "background_color_event";
    public static final String EXTRA_EVENT_DETAIL_VEVENT = "Objet_VEvent_detail";
    public static final int ICS_LOADER_ID = 0;

    public static final int NO_ANIMATION = 0;
    public static final int FIRST_LOADED_ANIMATION = 1;
    public static final int UPDATED_ANIMATION = 2;
    public static final int GOTO_TODAY_ANIMATION = 3;
    public static final int NEW_DATE_SELECTED = 4;


    public static final int TIMEOUT_CONNECT_DELAY = 10 * 1000; //Delai de connexion passé a 15 secondes
    public static final int TIMEOUT_READ_DELAY = 10 * 1000;


    /**
     * This comparator compares two events based on their start date
     */
    public static Comparator<net.fortuna.ical4j.model.component.VEvent> eventComparatorFromStartDate = new Comparator<net.fortuna.ical4j.model.component.VEvent>() {
        @Override
        public int compare(net.fortuna.ical4j.model.component.VEvent o1, net.fortuna.ical4j.model.component.VEvent o2) {

            Date date1 = o1.getStartDate().getDate();
            Date date2 = o2.getStartDate().getDate();

            return date1.getTime() < date2.getTime() ? -1 : 1;
        }
    };


    //------------------------- Debut de l'activité--------------

    //Database storing ics file infos
    net.fortuna.ical4j.model.Calendar vcalendar;

    //Calendar containings infos about simple and reliable time manipulation
    Calendar calendarNow = Calendar.getInstance();
    Calendar calendarSelected;

    //Views of the activity
    View datePicker2;
    ImageButton buttonNextDate;
    ImageButton buttonPreviousDate;
    TextView textViewDate;
    ImageView refresh;
    ViewFlipper viewFlipper;
    FloatingActionButton goTodayFab;
    ProgressBar loadingProgressBar;

    CoordinatorLayout coordinatorLayout;

    //Gesture handler to make actions on swipes
    GestureDetectorCompat mDetectorCompat;

    //Animation for the refresh button in the statusbar
    Animation rotation;

    Animation scaleInFab;

    Animation scaleOutFab;

    //Global boolean to toggle update
    boolean isUpdating = false;

    //Initialized based values for swipe calculation (are normalized to screen size later
    int screenWidth = 500;
    int screenHeight = 500;

    //Values to determine if a swipe is valide or not
    float MAX_SWIPE_OFF_PATH;
    float SWIPE_THRESHOLD;

    //FAB visibility Toggle
    boolean showFAB=true;

    File savedIcs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Determines the swipe threshold values
        determineFlingThreshold();

        //Initialize the refresh button animation
        rotation = AnimationUtils.loadAnimation(this, R.anim.rotate);

        scaleInFab = AnimationUtils.loadAnimation(this, R.anim.fade_in_scale_up);

        scaleOutFab = AnimationUtils.loadAnimation(this, R.anim.fade_out_scale_down);

        //Initialize coordinatorLayout for snackbar displaying
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.main_coordinator_layout);

        //Load if it exists the saved ics file from private files directory
        savedIcs = new File(this.getFilesDir().getPath() + "/" + "ADECal.ics");

        //Initialize the date picker with 2 buttons and 1 textview
        datePicker2 = findViewById(R.id.date_picker);

        //Initialize the viewFlipper which does the animation job
        viewFlipper = (ViewFlipper) findViewById(R.id.eventViewFlipper);

        //FAB to go to Today's date
        goTodayFab = (FloatingActionButton) findViewById(R.id.return_today_fab);


        //ProgressBar to show the loading of the ICS file
        loadingProgressBar = (ProgressBar) findViewById(R.id.loadingProgressBar);

        //Initialize viewflippers's child Listviews
        //Add the itemclicklistener to show events details
        //Add the onTouchListner to detect swipes
        for (int i = 0; i < viewFlipper.getChildCount(); i++) {
            ListView lv = (ListView) viewFlipper.getChildAt(i);
            lv.setOnItemClickListener(mEventListItemClickListener);
            lv.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return mDetectorCompat.onTouchEvent(event);
                }
            });
        }

        //Initialize other views
        buttonNextDate = (ImageButton) datePicker2.findViewById(R.id.button_next_date);
        buttonPreviousDate = (ImageButton) datePicker2.findViewById(R.id.button_previous_date);
        textViewDate = (TextView) datePicker2.findViewById(R.id.textview_date);

        //Bind the showDatePickerDialog to textView
        textViewDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });

        //Initialize the calendar which will be used as a time cursor
        calendarSelected = (Calendar) calendarNow.clone();
        calendarSelected.set(Calendar.MINUTE, 0);
        calendarSelected.set(Calendar.HOUR_OF_DAY, 0);
        calendarSelected.set(Calendar.SECOND, 0);

        //Bind actions to the next and previous date buttons
        buttonNextDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectNextDayAndUpdate();
            }
        });

        buttonPreviousDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectPreviousDayAndUpdate();
            }
        });

        goTodayFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calendarSelected.set(calendarNow.get(Calendar.YEAR), calendarNow.get(Calendar.MONTH), calendarNow.get(Calendar.DAY_OF_MONTH));
                updateViewsFromCalendar(calendarSelected, true, GOTO_TODAY_ANIMATION);
            }
        });

        mDetectorCompat = new GestureDetectorCompat(this, new GestureDetector.SimpleOnGestureListener() {

            @Override
            public boolean onDown(MotionEvent e) {
                return false;
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                float horizontalsensitivity = SWIPE_THRESHOLD;
                float MAX_VERTICAL_OFF_PATH = MAX_SWIPE_OFF_PATH;

                //if we swipe to the left, then it means that we want to see next day
                if ((e1.getX() - e2.getX()) > horizontalsensitivity && Math.abs((e1.getY() - e2.getY())) < MAX_VERTICAL_OFF_PATH) {

                    selectNextDayAndUpdate();

                    return true;

                    //if we swipe to the right, then it means that we want to see previous day
                } else if ((e2.getX() - e1.getX()) > horizontalsensitivity && Math.abs((e1.getY() - e2.getY())) < MAX_VERTICAL_OFF_PATH) {

                    selectPreviousDayAndUpdate();

                    return true;
                }

                return false;
            }
        });


        //If you restore from saved instance (like rotation), then retrieve data and initialize variable with it
        if (savedInstanceState != null) {
            //restore the selected date
            int[] savedDate = savedInstanceState.getIntArray(KEY_CALENDAR_DATE);
            calendarSelected.set(savedDate[0], savedDate[1], savedDate[2]);

        }

        //We have just started the app for the first time
        else {

            if(syncsOnStart()){

                if(needsSyncing()){
                    if (!isUpdating) {
                        syncCal(getSyncURLFromPreferences());
                    } else {
                        snackBarMaker(R.string.already_updating, Snackbar.LENGTH_SHORT);
                    }
                }

            }
        }


        showFAB= showsTodayFab();
        if(showFAB){
            setupTodayFabAlpha();
        }

        //updateViewsFromCalendar(calendarSelected,true);


        Loader icalLoader = getSupportLoaderManager().getLoader(ICS_LOADER_ID);
        //The loader already exists
        if (icalLoader != null) {
            //Set the handler of the this activity
            ((ICalAsyncTaskLoader) icalLoader).setHandler(loadingIcsHandler);
        }


        getSupportLoaderManager().initLoader(ICS_LOADER_ID, null, this);

    }


    @Override
    public Loader<net.fortuna.ical4j.model.Calendar> onCreateLoader(int id, Bundle args) {


        ICalAsyncTaskLoader loader = new ICalAsyncTaskLoader(MainActivity.this);
        loader.setHandler(loadingIcsHandler);
        return loader;

    }

    @Override
    public void onLoadFinished(Loader<net.fortuna.ical4j.model.Calendar> loader, net.fortuna.ical4j.model.Calendar data) {

        vcalendar = data;
        updateViewsFromCalendar(calendarSelected, true, NO_ANIMATION);
    }

    @Override
    public void onLoaderReset(Loader<net.fortuna.ical4j.model.Calendar> loader) {

    }

    private void instantiateVCalendar(boolean displayLoadedHint) {
        try {
            //else, load the saved ics file
            vcalendar = loadCalendarFromIn(savedIcs);

            if (displayLoadedHint)
                snackBarMaker(R.string.success_loaded_timetable_from_file, Snackbar.LENGTH_SHORT);

        } catch (IOException | ParserException e) {
            e.printStackTrace();
        }
    }

    private net.fortuna.ical4j.model.Calendar loadCalendarFromIn(InputStream in) throws IOException, ParserException {
        CalendarBuilder builder = new CalendarBuilder();
        return builder.build(in);
    }

    private net.fortuna.ical4j.model.Calendar loadCalendarFromIn(File filein) throws IOException, ParserException {

        FileInputStream in = new FileInputStream(filein);
        net.fortuna.ical4j.model.Calendar c = loadCalendarFromIn(in);
        in.close();
        return c;
    }

    private void determineFlingThreshold() {

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        //Utile pour le swipe pour determiner combien de pixel il faut pour activer le swipe
        screenWidth = metrics.widthPixels;
        screenHeight = metrics.heightPixels;

        SWIPE_THRESHOLD = Math.max(screenWidth, screenHeight) * 0.25f;

        MAX_SWIPE_OFF_PATH = Math.max(screenHeight, screenWidth) * 0.15f;

    }


    private void showDatePickerDialog() {

        CalendarDatePickerDialogFragment cdp = new CalendarDatePickerDialogFragment()
                .setOnDateSetListener(onCalendarDateSetListener)
                .setPreselectedDate(calendarSelected.get(Calendar.YEAR), calendarSelected.get(Calendar.MONTH), calendarSelected.get(Calendar.DAY_OF_MONTH));

        cdp.show(getSupportFragmentManager(), FRAG_TAG_CALENDAR_DATEPICKER);
    }


    public void updateDateTextView(Calendar calendar) {

        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, d MMMM", Locale.getDefault());
        textViewDate.setText(dateFormat.format(calendar.getTime()));

    }



    @SuppressLint("RestrictedApi") // also suppressed the warning
    public void updateGoToTodayFab() {

        //On ne montre pas le FAB, donc on passe
        if(!showFAB){
            if(goTodayFab.getVisibility() != View.GONE){
                goTodayFab.setVisibility(View.GONE);
                //goTodayFab.hide(); //Suppresses errors of library group
            }
            return;
        }

        if ((calendarSelected.get(Calendar.DAY_OF_MONTH) == calendarNow.get(Calendar.DAY_OF_MONTH)) && (calendarSelected.get(Calendar.MONTH) == calendarNow.get(Calendar.MONTH)) && (calendarNow.get(Calendar.YEAR) == calendarSelected.get(Calendar.YEAR))) {
            goTodayFab.startAnimation(scaleOutFab);
            goTodayFab.setVisibility(View.GONE);
            //goTodayFab.hide();
        } else {

            if (goTodayFab.getVisibility() != View.VISIBLE) {
                goTodayFab.startAnimation(scaleInFab);
                goTodayFab.setVisibility(View.VISIBLE);
                //goTodayFab.show();
            }

        }
    }


    public void updateViewsFromCalendar(Calendar calendar, boolean updateCurrentListView, int listviewAnimation) {

        updateDateTextView(calendar);

        updateGoToTodayFab();


        if (updateCurrentListView) {

            ArrayList<net.fortuna.ical4j.model.component.VEvent> evArray = getVEventArrayFromCalendar(vcalendar, calendarSelected);


            switch (listviewAnimation) {
                case NO_ANIMATION:
                    //Maj de la view visible
                    updateListAdapterWithEvents(evArray,(ListView)viewFlipper.getCurrentView());
                    //fin
                    return;

                case FIRST_LOADED_ANIMATION:
                case UPDATED_ANIMATION:
                    viewFlipper.setInAnimation(this, R.anim.slide_in_up);
                    viewFlipper.setOutAnimation(this, android.R.anim.fade_out);
                    break;

                case NEW_DATE_SELECTED:
                    viewFlipper.setInAnimation(this, R.anim.fade_in_from_north);
                    viewFlipper.setOutAnimation(this, R.anim.fade_out_to_south);
                    break;
                case GOTO_TODAY_ANIMATION:

                    viewFlipper.setInAnimation(this, R.anim.fade_in_from_south_east);
                    viewFlipper.setOutAnimation(this, R.anim.fade_out_to_north_west);

                    break;

            }

            //Animate the transition between the 2 listviews.
            //update the one which is not visible
            //Out the displayed one
            //In the hidden one



            int toOutView = viewFlipper.getDisplayedChild();

            int toInView = toOutView == 0 ? 1 : 0;


            updateListAdapterWithEvents(evArray, (ListView) viewFlipper.getChildAt(toInView));

            if (toOutView == 0) {
                viewFlipper.showNext();
            } else {
                viewFlipper.showPrevious();
            }

        }

    }

    /**
     * @param eventarray @nullable array
     */
    private void updateListAdapterWithEvents(ArrayList<net.fortuna.ical4j.model.component.VEvent> eventarray, ListView eventListView) {

        if (eventarray != null) {
            if (!eventarray.isEmpty()) {
                eventListView.setAdapter(new EventArrayAdapter(this, eventarray));


            } else {

                ArrayAdapter<String> noEventAdapter = new ArrayAdapter<>(this, R.layout.event_no_item_layout);

                noEventAdapter.add(getResources().getString(R.string.no_events));

                eventListView.setAdapter(noEventAdapter);

            }
        } else {
            ArrayAdapter<String> noEventAdapter = new ArrayAdapter<>(this, R.layout.event_no_item_layout);

            noEventAdapter.add(getResources().getString(R.string.no_loaded_events));

            eventListView.setAdapter(noEventAdapter);
        }

    }


    private void selectNextDayAndUpdate() {
        calendarSelected.add(Calendar.DAY_OF_MONTH, 1);
        updateViewsFromCalendar(calendarSelected, false, NO_ANIMATION);

        int toOutViewID = viewFlipper.getDisplayedChild();
        int toInViewID = (toOutViewID == 0 ? 1 : 0); //the other view of the view flipper

        viewFlipper.setOutAnimation(this, R.anim.slide_out_to_left);
        viewFlipper.setInAnimation(this, R.anim.slide_in_from_right);

        ListView toInListView = (ListView) viewFlipper.getChildAt(toInViewID);
        ArrayList<VEvent> eventsArray = getVEventArrayFromCalendar(vcalendar, calendarSelected);

        updateListAdapterWithEvents(eventsArray, toInListView);

        //Swipe to left and displayed view is 0 so switch to 1
        if (toOutViewID == 0) {
            //Normal animation

            viewFlipper.showNext();
        } else if (toOutViewID == 1) {

            viewFlipper.showPrevious();
        }
    }

    private void selectPreviousDayAndUpdate() {
        calendarSelected.add(Calendar.DAY_OF_MONTH, -1);
        updateViewsFromCalendar(calendarSelected, false, NO_ANIMATION);

        int toOutViewID = viewFlipper.getDisplayedChild();
        int toInViewID = (toOutViewID == 0 ? 1 : 0); //the other view of the view flipper

        viewFlipper.setOutAnimation(this, R.anim.slide_out_to_right);
        viewFlipper.setInAnimation(this, R.anim.slide_in_from_left);

        ListView toInListView = (ListView) viewFlipper.getChildAt(toInViewID);
        ArrayList<VEvent> eventsArray = getVEventArrayFromCalendar(vcalendar, calendarSelected);

        updateListAdapterWithEvents(eventsArray, toInListView);

        //Swipe to left and displayed view is 0 so switch to 1
        if (toOutViewID == 0) {

            //Normal animation
            //From 0 to 1
            viewFlipper.showNext();
        } else if (toOutViewID == 1) {

            //From 1 to 0
            viewFlipper.showPrevious();
        }
    }

    private ArrayList<net.fortuna.ical4j.model.component.VEvent> getVEventArrayFromCalendar(net.fortuna.ical4j.model.Calendar calendar, Calendar calendarDate) {

        //Le calendrier n'est pas un objet calendar
        if (calendar == null || calendarDate == null) {
            return null;
        }

        Calendar calendarTemp = (Calendar) calendarDate.clone();
        calendarTemp.set(Calendar.MINUTE, 0);
        calendarTemp.set(Calendar.HOUR_OF_DAY, 0);
        calendarTemp.set(Calendar.SECOND, 0);

        // create a period starting now with a duration of one (1) day..
        Period period = new Period(new DateTime(calendarTemp.getTime()), new Dur(1, 0, 0, 0));

        Filter filter;
        if (usesFilterEvent()) {
            Rule[] rules = new Rule[2];
            rules[0] = new PeriodRule(period);
            rules[1] = getExcludedEventsRule();
            filter = new Filter(rules, Filter.MATCH_ALL);
        } else {
            Rule[] rules = new Rule[1];
            rules[0] = new PeriodRule(period);
            filter = new Filter(rules, Filter.MATCH_ALL);
        }


        Collection eventsToday = filter.filter(calendar.getComponents(Component.VEVENT));

        ArrayList<net.fortuna.ical4j.model.component.VEvent> lst = new ArrayList<>(eventsToday);

        Collections.sort(lst, eventComparatorFromStartDate);

        return lst;
    }

/* NOT USED
    private net.fortuna.ical4j.model.Calendar createCalendarFromURL(URL url) throws IOException, ParserException, SocketTimeoutException {
        FileInputStream in = new FileInputStream(downloadICStoFile(url));
        CalendarBuilder builder = new CalendarBuilder();
        return builder.build(in);
    }

    private Scanner downloadICStoScanner(URL u) throws IOException {

        File f = downloadICStoFile(u);
        return new Scanner(f);

    }

*/

//DEPRECATED, Replaced by downloadICStoFileOKHTTP, less buggy
    /**
     * Telecharge le contenu des octets accessible par l'url @u dans le fichier ADECal.ics
     * dans le dossier privé de l'application.
     *
     * @param u : l'URL à partir de laquelle on telecharge les octets
     * @return
     * @throws IOException
     */
    private File downloadICStoFile(URL u) throws IOException {

        URLConnection con = u.openConnection();
        con.setConnectTimeout(TIMEOUT_CONNECT_DELAY);
        con.setReadTimeout(TIMEOUT_READ_DELAY);
        InputStream is = con.getInputStream();

        DataInputStream dis = new DataInputStream(is);

        byte[] buffer = new byte[1024];
        int length;

        File f = new File(this.getFilesDir().getPath() + "/" + "ADECal.ics");

        FileOutputStream fos = new FileOutputStream(f);
        while ((length = dis.read(buffer)) > 0) {
            fos.write(buffer, 0, length);
        }


        fos.flush();

        fos.close();
        dis.close();
        is.close();


        return f;

    }

    private void downloadICStoFileOKHTTP(URL u) throws IOException {

        File f = new File(this.getFilesDir().getPath() + "/" + "ADECal.ics");
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(u)
                .build();

        Response res = client.newCall(request).execute();


        if (res.isSuccessful()) {

            String icsFile = res.body().string();

            if (icsFile == null) {
                throw new IOException("ICS String downloaded is null");
            }

            PrintStream out = new PrintStream(f);
            out.println(icsFile);
            out.close();
        }

    }

    final Handler loadingIcsHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ICalAsyncTaskLoader.START_LOADING:


                    textViewDate.setText(R.string.loading);
                    loadingProgressBar.setVisibility(View.VISIBLE);

                    break;
                case ICalAsyncTaskLoader.RETURN_SAVED_INSTANCE:


                    break;
                case ICalAsyncTaskLoader.DELIVER_RESULT:


                    updateDateTextView(calendarSelected);
                    loadingProgressBar.setVisibility(View.GONE);

                    break;
                default:
            }
        }
    };

    //Handles message from the Download Thread and does action in the UI thread like updating views
    final Handler myHandler = new Handler() {

        public void handleMessage(Message msg) {
            int result = msg.arg1;

            switch (result) {
                case SUCCESS_CALENDAR_URL_PARSING: {
                    //updateViewsFromCalendar(calendarSelected,true);
                    //Relancer le chargement du calendrier depuis le ICS
                    getSupportLoaderManager().getLoader(ICS_LOADER_ID).onContentChanged();

                    updateSyncTime();
                    snackBarMaker(R.string.successfully_updated, Snackbar.LENGTH_SHORT);
                    stopRefreshAnimation();
                    isUpdating = false;
                    break;
                }

                case FAILED_CALENDAR_URL_PARSING: {
                    stopRefreshAnimation();
                    snackBarMaker(R.string.failed_update, Snackbar.LENGTH_SHORT);
                    isUpdating = false;
                    break;
                }

                case FAILED_CALENDAR_PARSING:
                    stopRefreshAnimation();
                    snackBarMaker(R.string.failed_timetable_parsing, Snackbar.LENGTH_SHORT);
                    isUpdating = false;
                    break;

                case FAILED_IO_ERROR:
                    stopRefreshAnimation();
                    snackBarMaker(R.string.failed_timetable_io_reading, Snackbar.LENGTH_SHORT);
                    isUpdating = false;
                    break;

                case FAILED_TIMEOUT:
                    stopRefreshAnimation();
                    snackBarMaker(R.string.failed_timetable_online_timeout, Snackbar.LENGTH_LONG);
                    isUpdating = false;
                    break;

                case FAILED_URL_MALFORMATION:
                    stopRefreshAnimation();
                    snackBarMaker(R.string.url_marformation, Snackbar.LENGTH_SHORT);
                    isUpdating = false;
                    break;

                case START_REFRESH:
                    startRefreshAnimation();
                    isUpdating = true;
                    break;

                case FINISHED_REFRESH:
                    stopRefreshAnimation();
                    break;
                case FAILED_URL_RESOLUTION:
                    stopRefreshAnimation();
                    snackBarMaker(R.string.failed_url_resolution, Snackbar.LENGTH_LONG);
                    isUpdating = false;

                default:
                    break;
            }

        }
    };


    /**
     * Create the menu
     *
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_activity_menu, menu);

        final MenuItem item = menu.findItem(R.id.sync_menu_button);

        item.setActionView(R.layout.action_view_refresh_layout);

        refresh = (ImageView) item.getActionView().findViewById(R.id.refreshButton);

        refresh.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                onOptionsItemSelected(item);
            }
        });

        return true;


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sync_menu_button:
                if (!isUpdating) {
                    syncCal(getSyncURLFromPreferences());
                } else {
                    snackBarMaker(R.string.already_updating, Snackbar.LENGTH_SHORT);
                }
                return true;
            case R.id.settings_menu_button:
                menuItemOpenSettings();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void syncCal(final String savedUrl) {

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                Message msgStart = new Message();
                msgStart.arg1 = START_REFRESH;
                myHandler.sendMessage(msgStart);

                try {
                    URL u = new URL(savedUrl);
                    //vcalendar=createCalendarFromURL(u);
                    downloadICStoFileOKHTTP(u);


                    Message msgSuccess = new Message();
                    msgSuccess.arg1 = SUCCESS_CALENDAR_URL_PARSING;

                    myHandler.sendMessage(msgSuccess);

                } catch (MalformedURLException e) {

                    Message msg = new Message();
                    msg.arg1 = FAILED_URL_MALFORMATION;
                    myHandler.sendMessage(msg);
                    e.printStackTrace();

                } catch (FileNotFoundException e) {

                    Message msg = new Message();
                    msg.arg1 = FAILED_IO_ERROR;
                    myHandler.sendMessage(msg);
                    e.printStackTrace();

                } catch (SocketTimeoutException e) {

                    Message msg = new Message();
                    msg.arg1 = FAILED_TIMEOUT;
                    myHandler.sendMessage(msg);
                    e.printStackTrace();

                } /*catch (ParserException e) {

                    Message msg=new Message();
                    msg.arg1= FAILED_CALENDAR_PARSING;
                    myHandler.sendMessage(msg);
                    e.printStackTrace();

                }*/ catch (UnknownHostException e) {

                    Message msg = new Message();
                    msg.arg1 = FAILED_URL_RESOLUTION;
                    myHandler.sendMessage(msg);
                    e.printStackTrace();

                } catch (IOException e) {

                    Message msg = new Message();
                    msg.arg1 = FAILED_IO_ERROR;
                    myHandler.sendMessage(msg);
                    e.printStackTrace();

                }

            }
        });

        t.start();
    }

    void menuItemOpenSettings() {
        startActivityForResult(new Intent(this, SettingsActivity.class), REQUEST_SETTINGS);
    }


    public void toastMaker(String s) {
        Toast.makeText(this, s, Toast.LENGTH_LONG).show();
    }


    public String getSyncURLFromPreferences() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        return sharedPreferences.getString(PreferenceKeys.SYNC_URL_STRING, "");
    }

    void snackBarMaker(String s, int duration) {
        Snackbar.make(coordinatorLayout, s, duration).show();
    }

    void snackBarMaker(int id, int duration) {
        Snackbar.make(coordinatorLayout, getResources().getText(id), duration).show();
    }

    void startRefreshAnimation() {
        if (refresh != null) {
            refresh.startAnimation(rotation);
        }
    }

    void stopRefreshAnimation() {
        if (refresh != null) {
            refresh.clearAnimation();
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {


        //Save the current selected date
        int YEAR = calendarSelected.get(Calendar.YEAR);
        int MONTHOFYEAR = calendarSelected.get(Calendar.MONTH);
        int DAYOFMONTH = calendarSelected.get(Calendar.DAY_OF_MONTH);

        int[] currentdate = {YEAR, MONTHOFYEAR, DAYOFMONTH};

        outState.putIntArray(KEY_CALENDAR_DATE, currentdate);


        //outState.putSerializable(KEY_VCALENDAR_OBJECT,vcalendar);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
            case REQUEST_SETTINGS:

                boolean needResync=false;
                if(resultCode==RESULT_OK && data !=null)
                {
                    needResync=data.getBooleanExtra(PreferenceKeys.INTENT_SETTINGS_NEEDS_RESYNG,false);
                }
                showFAB=showsTodayFab();
                //Si on veut affiche le FAB apres le passage aux parametre, alors on maj le alpha
                if(showFAB){
                    setupTodayFabAlpha();
                }

                updateViewsFromCalendar(calendarSelected, true, NO_ANIMATION);

                if(needResync)
                {
                    if (!isUpdating) {
                        syncCal(getSyncURLFromPreferences());
                    } else {
                        snackBarMaker(R.string.already_updating, Snackbar.LENGTH_SHORT);
                    }
                }


                break;
            case REQUEST_EVENT_DETAIL_ACTIVITY:
                if (resultCode == EventDetailsActivity.RESULT_OK_EXCLUDED_EVENT) {
                    String excludedSummary = data.getStringExtra(EventDetailsActivity.EXTRA_EXCLUDED_SUMMARY_VALUE);
                    updateViewsFromCalendar(calendarSelected, true, NO_ANIMATION);
                    snackBarMaker(excludedSummary + " " + getResources().getString(R.string.added_to_hidden_events_types), Snackbar.LENGTH_LONG);

                }
                break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private AdapterView.OnItemClickListener mEventListItemClickListener = new AdapterView.OnItemClickListener() {

        long timeClick = 0;
        long coolDownDuration = 500; //500 ms entre chaque ouverture de détails d'evenement pour eviter d'ouvrir 2 boites de dialogues si on clique 2 fois

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            long previousClick = timeClick;
            timeClick = System.currentTimeMillis();

            if ((timeClick - previousClick) >= coolDownDuration) {
                //Si on a affaire a une vue evenement
                if (view.getId() == R.id.event_layout_main) {

                    LinearLayout linearLayout = (LinearLayout) view;
                    Drawable background = linearLayout.getBackground();

                    int color = Color.BLACK;
                    if (background instanceof ColorDrawable)
                        color = ((ColorDrawable) background).getColor();

                    net.fortuna.ical4j.model.component.VEvent event = (net.fortuna.ical4j.model.component.VEvent) parent.getItemAtPosition(position);

                    Intent startEventDetailsActivity = new Intent(MainActivity.this, EventDetailsActivity.class);

                    startEventDetailsActivity.putExtra(EXTRA_EVENT_DETAIL_COLORBG, color);
                    startEventDetailsActivity.putExtra(EXTRA_EVENT_DETAIL_VEVENT, event);

                    startActivityForResult(startEventDetailsActivity, REQUEST_EVENT_DETAIL_ACTIVITY);

                }
            }


        }
    };


    private CalendarDatePickerDialogFragment.OnDateSetListener onCalendarDateSetListener = new CalendarDatePickerDialogFragment.OnDateSetListener() {
        @Override
        public void onDateSet(CalendarDatePickerDialogFragment dialog, int year, int monthOfYear, int dayOfMonth) {
            calendarSelected.set(year, monthOfYear, dayOfMonth);
            updateViewsFromCalendar(calendarSelected, true, NEW_DATE_SELECTED);
        }
    };

    private Rule getExcludedEventsRule() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        final TreeSet<String> excludedStringSet = new TreeSet<>(sharedPreferences.getStringSet(PreferenceKeys.EXCLUDED_EVENTS_SUMMARY_SET, new TreeSet<String>()));

        return new Rule() {
            @Override
            public boolean match(Object o) {
                VEvent event = (VEvent) o;
                for (String excludedSummaryValue : excludedStringSet) {
                    if (event.getSummary().getValue().contains(excludedSummaryValue)) {
                        return false;
                    }
                }
                return true;
            }
        };
    }

    private boolean usesFilterEvent() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        return sharedPreferences.getBoolean(PreferenceKeys.EVENTS_FILTERING_BOOLEAN, false);
    }

    private void setupTodayFabAlpha(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        goTodayFab.setAlpha(0.01f*sharedPreferences.getInt(PreferenceKeys.FAB_ALPHA, 100));

    }

    private boolean showsTodayFab(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        return sharedPreferences.getBoolean(PreferenceKeys.SHOW_FAB, true);
    }

    private boolean syncsOnStart(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        return sharedPreferences.getBoolean(PreferenceKeys.SYNC_ON_START, false);
    }

    private boolean needsSyncing(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        long lastTime=sharedPreferences.getLong(PreferenceKeys.LAST_SYNC_TIME_SECONDS,0);
        long currentTime=System.currentTimeMillis()/1000; //retrieve the current time in s

        String time=sharedPreferences.getString(PreferenceKeys.SYNC_ON_START_DELAY_PREF,"0:10:0");
        String[] hms=time.split(":");

        long delay=Integer.parseInt(hms[0])*3600+Integer.parseInt(hms[1])*60+Integer.parseInt(hms[2]);

        return (currentTime-lastTime) >= delay;
    }
    private void updateSyncTime(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        long currentTime=System.currentTimeMillis()/1000; //retrieve the current time in s
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putLong(PreferenceKeys.LAST_SYNC_TIME_SECONDS,currentTime);
        editor.apply();
    }
}
