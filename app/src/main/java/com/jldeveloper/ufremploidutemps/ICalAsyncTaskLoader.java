package com.jldeveloper.ufremploidutemps;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.os.FileObserver;
import android.os.Handler;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Jeremy on 12/09/2017.
 */

public class ICalAsyncTaskLoader extends android.support.v4.content.AsyncTaskLoader<Calendar> {

    private Handler observerHandler;

    private Calendar vCalendar;

    public static final int RETURN_SAVED_INSTANCE = 0;

    public static final int START_LOADING = 1;

    public static final int DELIVER_RESULT = 2;


    public ICalAsyncTaskLoader(Context context) {
        super(context);
    }

    @Override
    protected void onStartLoading() {
        //System.out.println("CHARGEUR lancé");

        if (vCalendar != null) {


            deliverResult(vCalendar);
        }


        if (takeContentChanged() || vCalendar == null) {
            // Something has changed or we have no data,
            // so kick off loading it


            forceLoad();

        }
    }

    protected void setHandler(Handler handler) {
        this.observerHandler = handler;
    }

    @Override
    public Calendar loadInBackground() {


        //Notifier l'activité que l'action a demarrée
        if (observerHandler != null) {
            observerHandler.obtainMessage(START_LOADING).sendToTarget();
        } else {
            System.out.println("HANDLER NULL");
        }



        File savedIcs = new File(getContext().getFilesDir() + "/" + "ADECal.ics");
        Calendar c = null;
        try {
            c = loadCalendarFromIn(savedIcs);
            //System.out.println("J'ai chargé l'EDT");
            return c;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParserException e) {
            e.printStackTrace();
        }

        return c;
    }

    @Override
    public void deliverResult(Calendar data) {


        //Notifier l'activité que le resultat est dispo
        //Information donnée aussi dans le callback
        if (observerHandler != null) {
            observerHandler.obtainMessage(DELIVER_RESULT).sendToTarget();
        } else {
            System.out.println("HANDLER NULL");
        }


        vCalendar = data;
        super.deliverResult(data);
    }

    protected void onReset() {
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

}
