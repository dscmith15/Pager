package com.example.dustin.pager;

import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.os.Environment;
import android.os.Handler;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;


import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.opencsv.CSVReader;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.epub.EpubReader;

public class ScrollActivity extends AppCompatActivity {

    private static final String LOG_TAG = "Log";
    Vibrator vibrator;
    TextView firstTextView;

    //all of these should be added to all apps
    ImageButton slowdown;
    ImageButton speedup;

    ImageButton inctexsize;
    ImageButton dectexsize;

    ImageButton player;
    ImageButton pauser;

    MenuItem hider;

    //timer vars
    private long starttime;
    private long endTime;
    private boolean started;

    private boolean paused = true;


    protected String[] mWordArray;
    protected boolean mPlayingRequested;
    public String literature;
    public  String[] litSplit;
    public InputStream is = null;
    private int counter = 0;

    public int splitter = 13; // decides how long words can be
    public int textsize = 32;
    public int textchg = 5;
    ScrollTextView scrolltext;
    private Toast toast;
    public boolean hidden = false;

    private int pnum;
    private int mode = -1;
    private boolean passageflag = false;
    private String passage;
    private String[][] orders;
    private String wpm = "NAN";
    private boolean completed = false;
    private boolean passageswitch = true;
    public int lastloc = 2;
    Thread timeNano;


    //this should help with frame issue
    private static final int UPDATE_RATE = 30;



    private int PassageCount=0;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_scroll);
        firstTextView = (TextView) findViewById(R.id.word_landing);
        firstTextView.setSelected(true);

        slowdown = (ImageButton) findViewById(R.id.slower);
        speedup = (ImageButton) findViewById(R.id.faster);

        inctexsize = (ImageButton) findViewById(R.id.inctex);
        dectexsize = (ImageButton) findViewById(R.id.dectex);

        hider = (MenuItem) findViewById(R.id.hide_button);

        pauser = (ImageButton) findViewById(R.id.imageButton4);
        pauser.setVisibility(View.INVISIBLE);
        player = (ImageButton) findViewById(R.id.imageButton3);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        //DialogFragment modeFrag = new ModeDialogFragment();
        //modeFrag.show(getFragmentManager(),"Mode Frag");



        toast = Toast.makeText(this, "", Toast.LENGTH_SHORT);



        scrolltext=(ScrollTextView) findViewById(R.id.scrolltext);
        scrolltext.setSelected(true);
        loadText("AIWL.epub",lastloc);
        scrolltext.setText(literature);
        scrolltext.setTextColor(Color.BLACK);
        scrolltext.startScroll();
        started = false;
        scrolltext.pauseScroll();
        scrolltext.setVisibility(View.INVISIBLE);
        firstTextView.setText("X");






        //Timer thread stuff

        timeNano = new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (this) {
                    while (true)
                        try {

                            long beginTimeMillis, timeTakenMillis, timeLeftMillis;

                            // get the time before updates/draw
                            beginTimeMillis = System.currentTimeMillis();

                            // do the thread processing / draw
                            completed=scrolltext.isdone();
                            if (completed & mode == -2 & passageswitch) {
                                Thread.sleep(1);
                                endTime = System.nanoTime() - starttime;
                                vibrator.vibrate(1000);
                                passageswitch=false;
                                passageflag = false;

                                completed = false;
                                scrolltext.setDone(completed);
                                passageswitch=true;
                            }
                            if (completed & mode == -1){
                                paused=true;
                                vibrator.vibrate(1000);
                                lastloc++;

                                completed = false;
                                scrolltext.setDone(completed);
                                passageswitch=true;
                            }

                            // get the time after processing and calculate the difference
                            timeTakenMillis = System.currentTimeMillis() - beginTimeMillis;

                            // check how long there is until we reach the desired refresh rate
                            timeLeftMillis = (1000L / UPDATE_RATE) - timeTakenMillis;

                            // set some kind of minimum to prevent spinning
                            if (timeLeftMillis < 5) {
                                timeLeftMillis = 5; // Set a minimum
                            }

                            // sleep until the end of the current frame
                            try {
                                TimeUnit.MILLISECONDS.sleep(timeLeftMillis);
                            } catch (InterruptedException ie) {
                            }



                        } catch (InterruptedException e) {
                            System.out.println("got interrupted!");
                        }
                }
            }
        });
        timeNano.start();


    }

    private void displayText(final String message) {

        toast.setText(message);
        toast.show();
    }

    public void NextRSVP(View view) {
        scrolltext.pauseScroll();
        scrolltext.goForward();


        pauser.setVisibility(View.INVISIBLE);
        player.setVisibility(View.VISIBLE);


    }

    public void PrevRSVP(View view) {
        scrolltext.pauseScroll();
        scrolltext.goBack();

        pauser.setVisibility(View.INVISIBLE);
        player.setVisibility(View.VISIBLE);




    }
    public void slowRSVP(View view) {

        if(scrolltext.getmScrollSpeed()>0) {
            if (paused) {

                scrolltext.goSlower();

                displayText(Float.toString(scrolltext.getmScrollSpeed()));
            } else {
                scrolltext.pauseScroll();
                scrolltext.resumeScroll();
                pauser.setVisibility(View.INVISIBLE);
                player.setVisibility(View.VISIBLE);
                scrolltext.goSlower();
                displayText(Float.toString(scrolltext.getmScrollSpeed()));
            }
        }


    }
    public void fastRSVP(View view) {


        if (paused){
            scrolltext.goFaster();


            displayText(Float.toString(scrolltext.getmScrollSpeed()));
        } else {
            scrolltext.pauseScroll();
            scrolltext.resumeScroll();
            pauser.setVisibility(View.INVISIBLE);
            player.setVisibility(View.VISIBLE);
            scrolltext.goFaster();
            displayText(Float.toString(scrolltext.getmScrollSpeed()));
        }



    }
    public void StartScroll(View view) {
        completed = scrolltext.isdone();
        if(!started){
            firstTextView.setVisibility(View.INVISIBLE);
            scrolltext.setVisibility(View.VISIBLE);
            started = true;
            starttime = System.nanoTime();

        }

        if (!completed) {
            scrolltext.resumeScroll();
            paused=false;
            player.setVisibility(View.INVISIBLE);
            pauser.setVisibility(View.VISIBLE);
            firstTextView.setVisibility(View.INVISIBLE);
            scrolltext.setVisibility(View.VISIBLE);

        }
    }
    public void pauseScroll(View view) {
        scrolltext.pauseScroll();
        pauser.setVisibility(View.INVISIBLE);
        player.setVisibility(View.VISIBLE);
        paused=true;


    }

    public void incTextsize(View view){
        textsize+=textchg;
        scrolltext.setTextSize(TypedValue.COMPLEX_UNIT_PT, textsize);
        firstTextView.setTextSize(TypedValue.COMPLEX_UNIT_PT, textsize);
        displayText(Integer.toString(textsize) + "px");
        scrolltext.pauseScroll();
        pauser.setVisibility(View.INVISIBLE);
        player.setVisibility(View.VISIBLE);

    }

    public void decTextsize(View view){
        textsize-=textchg;
        scrolltext.setTextSize(TypedValue.COMPLEX_UNIT_PT, textsize);
        firstTextView.setTextSize(TypedValue.COMPLEX_UNIT_PT, textsize);
        displayText(Integer.toString(textsize) + "pt");
        scrolltext.pauseScroll();
        pauser.setVisibility(View.INVISIBLE);
        player.setVisibility(View.VISIBLE);

    }


    //Get that text in there given input
    private void loadText(final String file, final int loc) {
        try {
            AssetManager am = this.getAssets();
            InputStream is = am.open(file);
            // Load Book from inputStream
            Book book = (new EpubReader()).readEpub(is);
            InputStream was = book.getSpine().getSpineReferences().get(loc).getResource().getInputStream();

            passage=file;
            is.close();
            BufferedReader reader = new BufferedReader(new InputStreamReader(was));
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null)
            {
                sb.append(line +"\n");

            }
            was.close();
            literature = Html.fromHtml(sb.toString()).toString();
            counter = 0;
            litSplit = Html.fromHtml(literature).toString().split("\\s+");
            scrolltext.setDistance();
            started = false;


        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }





    //action bar stuff
    public boolean onCreateOptionsMenu(Menu menu){
        //make it appear
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    public boolean onOptionsItemSelected(MenuItem item){
        //Deals with the actual clicking
        switch (item.getItemId()) {

            case R.id.hide_button:

                if (hidden == false) {


                    inctexsize.setVisibility(View.INVISIBLE);
                    dectexsize.setVisibility(View.INVISIBLE);
                    hidden = true;

                } else {


                    inctexsize.setVisibility(View.VISIBLE);
                    dectexsize.setVisibility(View.VISIBLE);
                    hidden = false;

                }
                return true;

            case R.id.rsvp:
                Intent m_rsvp = new Intent(this, RsvpActivity.class);
                startActivity(m_rsvp);
                finish();
                return true;
            case R.id.pager_m:
                Intent pagerm = new Intent(this, PagerActivity.class);
                startActivity(pagerm);
                finish();
                return true;
            case R.id.scroller:
                Intent scrollm = new Intent(this, ScrollActivity.class);
                startActivity(scrollm);
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }


    }







    public File getDataStorageDir(String DataName) {
        // Get the directory for the user's public pictures directory.
        File file = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOCUMENTS), DataName);
        if (!file.mkdirs()) {
            Log.e(LOG_TAG, "Directory not created");
        }
        return file;
    }

    private void writeStringToTextFile(String s, String f){
        File sdCard = getDataStorageDir(f);
        File dir = new File (sdCard.getAbsolutePath());
        dir.mkdirs();
        File file = new File(dir, f);
        try{
            FileOutputStream f1 = new FileOutputStream(file,true); //True = Append to file, false = Overwrite
            PrintStream p = new PrintStream(f1);
            p.print(s);
            p.close();
            f1.close();
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        }   }

}