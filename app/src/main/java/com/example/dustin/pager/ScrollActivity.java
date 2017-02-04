package com.example.dustin.pager;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.os.Environment;

import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;

import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;


import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;




import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Arrays;


import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.epub.EpubReader;

import static java.lang.Math.round;

public class ScrollActivity extends AppCompatActivity {

    private static final String LOG_TAG = "Log";
    Vibrator vibrator;
    TextView firstTextView;

    public Integer twigs;
    //all of these should be added to all apps
    ImageButton slowdown;
    ImageButton speedup;

    ImageButton inctexsize;
    ImageButton dectexsize;

    ImageButton player;
    ImageButton pauser;

    MenuItem hider;

    //timer vars

    private boolean started;

    private boolean paused = true;

    public String litBranch;

    public String literature;
    public  String[] litSplit;
    public InputStream is = null;
    private int counter = 0;

    public int textsize = 32;
    public int textchg = 5;
    ScrollTextView scrolltext;
    private Toast toast;
    public boolean hidden = false;

    private int mode = -1;

    private boolean completed = false;

    public int lastloc = 2;
    Thread timeNano;
    public String ebook;

    public Boolean BranchFlag = false;
    public Integer BranchCount = 0;

    public SharedPreferences prefs;
    public SharedPreferences.Editor editor;
    public static final String PREFS_NAME = "User_Data";
    public Integer breaker = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        textsize = prefs.getInt("fontsize", 32);
        lastloc =  prefs.getInt("chapter", 2);
        counter = prefs.getInt("location", 0);
        ebook = prefs.getString("book","AIWL.epub");

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

        loadText(ebook,lastloc);
        loadMiniText(literature,breaker,BranchCount);

        twigs = stems(literature);

        scrolltext.setText("                           "+litBranch);
        scrolltext.setTextColor(Color.BLACK);
        scrolltext.setTextSize(TypedValue.COMPLEX_UNIT_PT, textsize);
        firstTextView.setTextSize(TypedValue.COMPLEX_UNIT_PT, textsize);
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
                    while (true) {

                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        // do the thread processing / draw
                        completed = scrolltext.isdone();

                        if (completed & mode == -1 & !BranchFlag) {
                            paused = true;
                            counter = 0;
                            completed = false;
                            scrolltext.setDone(completed);
                            BranchCount++;
                            loadMiniText(literature, breaker, BranchCount);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    scrolltext.setDistance();
                                    scrolltext.setText("                           "+litBranch);
                                    scrolltext.resumeScroll();
                                    player.setVisibility(View.INVISIBLE);
                                    pauser.setVisibility(View.VISIBLE);
                                    scrolltext.pauseScroll();
                                    pauser.setVisibility(View.INVISIBLE);
                                    player.setVisibility(View.VISIBLE);
                                    scrolltext.setVisibility(View.INVISIBLE);
                                    StartScroll(scrolltext);


                                }
                            });

                        } else if (completed & mode == -1 & BranchFlag) {
                            paused = true;
                            lastloc++;
                            BranchCount = 0;
                            counter = 0;
                            completed = false;
                            scrolltext.setDone(completed);
                            loadText(ebook,lastloc);
                            loadMiniText(literature, breaker, BranchCount);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    displayText("Next Chapter");
                                    scrolltext.setDistance();
                                    scrolltext.setText("                           "+litBranch);
                                    scrolltext.resumeScroll();
                                    player.setVisibility(View.INVISIBLE);
                                    pauser.setVisibility(View.VISIBLE);
                                    scrolltext.pauseScroll();
                                    pauser.setVisibility(View.INVISIBLE);
                                    player.setVisibility(View.VISIBLE);
                                    scrolltext.setVisibility(View.INVISIBLE);
                                    firstTextView.setVisibility(View.VISIBLE);
                                    firstTextView.setText("Chapter Break");


                                }
                            });


                        }
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
        displayText(Integer.toString(stems(literature)));


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
            BranchFlag = false;
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

            litSplit = Html.fromHtml(literature).toString().split("\\s+");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    scrolltext.setDistance();

                }
            });

            started = false;

            //literature = TextUtils.join(" ", Arrays.asList(litSplit).subList(0,20));



        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    private void loadMiniText(final String lit, int loc, int iter) {

        Integer div = trimmer(lit);
        Integer mod = stems(lit);

        if (loc > div){
            loc = div;
        }
        displayText(Integer.toString(loc));
        Boolean tempmodflag;
        if (mod > 0){
            tempmodflag = true;
        } else {
            tempmodflag = false;
        }
        if (iter < div) {
            litBranch = TextUtils.join(" ", Arrays.asList(litSplit).subList(loc * iter, loc * iter + loc));

        } else if (!tempmodflag && iter == div) {
            litBranch = TextUtils.join(" ", Arrays.asList(litSplit).subList(loc * iter, loc * iter + loc));
            BranchFlag = true;

        } else if (tempmodflag && iter == div){
            litBranch = TextUtils.join(" ", Arrays.asList(litSplit).subList(loc * iter, loc * iter + loc));

        } else if (tempmodflag && iter == (div + 1)){
            litBranch = TextUtils.join(" ", Arrays.asList(litSplit).subList(loc * iter, loc * iter + mod));

            BranchFlag = true;
        }
        started = false;

    }



    public Integer trimmer(final String lit){

        return Math.round(((float) lit.split("\\s+").length)/20);

    }

    public Integer stems(final String lit){

        return lit.split("\\s+").length%20;

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

                editor = prefs.edit();
                editor.putInt("chapter", lastloc); // value to store
                editor.putInt("location", counter);
                editor.putInt("fontsize", textsize);
                editor.commit();
                Intent m_rsvp = new Intent(this, RsvpActivity.class);
                m_rsvp.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(m_rsvp);
                finish();
                return true;
            case R.id.pager_m:

                editor = prefs.edit();
                editor.putInt("chapter", lastloc); // value to store
                editor.putInt("location", counter);
                editor.putInt("fontsize", textsize);
                editor.commit();
                Intent pagerm = new Intent(this, PagerActivity.class);
                pagerm.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(pagerm);
                finish();
                return true;
            case R.id.scroller:

                editor = prefs.edit();
                editor.putInt("chapter", lastloc); // value to store
                editor.putInt("location", counter);
                editor.putInt("fontsize", textsize);
                editor.commit();
                Intent scrollm = new Intent(this, ScrollActivity.class);
                scrollm.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(scrollm);
                finish();
                return true;


            case R.id.books_1:
                editor = prefs.edit();
                editor.putString("book","Ambush_at_Corellia_by_Macbride_Roger_Allen.epub");
                editor.putInt("chapter", 2);
                editor.putInt("location", 0);
                editor.putInt("fontsize", textsize);

                editor.commit();
                scrollm = new Intent(this, ScrollActivity.class);
                scrollm.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(scrollm);
                finish();
                return true;

            case R.id.books_2:
                editor = prefs.edit();
                editor.putString("book","Assault_at_Selonia_by_Roger_Allen_MacBride.epub");
                editor.putInt("chapter", 2);
                editor.putInt("location", 0);
                editor.putInt("fontsize", textsize);

                editor.commit();
                scrollm = new Intent(this, ScrollActivity.class);
                scrollm.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(scrollm);
                finish();
                return true;

            case R.id.books_3:
                editor = prefs.edit();
                editor.putString("book","Before_the_Storm_by_P_Michael_Kube-McDowell.epub");
                editor.putInt("chapter", 1);
                editor.putInt("location", 0);
                editor.putInt("fontsize", textsize);

                editor.commit();
                scrollm = new Intent(this, ScrollActivity.class);
                scrollm.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(scrollm);
                finish();
                return true;

            case R.id.books_4:
                editor = prefs.edit();
                editor.putString("book","False_Colors_(Masterpiece_in_Murder)_by_Richard_Powell.epub");
                editor.putInt("chapter", 2);
                editor.putInt("location", 0);
                editor.putInt("fontsize", textsize);

                editor.commit();
                scrollm = new Intent(this, ScrollActivity.class);
                scrollm.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(scrollm);
                finish();
                return true;

            case R.id.books_5:
                editor = prefs.edit();
                editor.putString("book","Richard_Powell_-_Pioneer_Go_Home.epub");
                editor.putInt("chapter", 2);
                editor.putInt("location", 0);
                editor.putInt("fontsize", textsize);

                editor.commit();
                scrollm = new Intent(this, ScrollActivity.class);
                scrollm.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(scrollm);
                finish();
                return true;

            case R.id.books_6:
                editor = prefs.edit();
                editor.putString("book","Shield_of_Lies_by_P_Michael_Kube-McDowell.epub");
                editor.putInt("chapter", 2);
                editor.putInt("location", 0);
                editor.putInt("fontsize", textsize);

                editor.commit();
                scrollm = new Intent(this, ScrollActivity.class);
                scrollm.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(scrollm);
                finish();
                return true;

            case R.id.books_7:
                editor = prefs.edit();
                editor.putString("book","Showdown_at_Centerpoint_by_Macbride_Roger_Allen.epub");
                editor.putInt("chapter", 2);
                editor.putInt("location", 0);
                editor.putInt("fontsize", textsize);

                editor.commit();
                scrollm = new Intent(this, ScrollActivity.class);
                scrollm.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(scrollm);
                finish();
                return true;
            case R.id.books_8:
                editor = prefs.edit();
                editor.putString("book","Tyrant's_Test_by_P_Michael_Kube-McDowell.epub");
                editor.putInt("chapter", 2);
                editor.putInt("location", 0);
                editor.putInt("fontsize", textsize);

                editor.commit();
                scrollm = new Intent(this, ScrollActivity.class);
                scrollm.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
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