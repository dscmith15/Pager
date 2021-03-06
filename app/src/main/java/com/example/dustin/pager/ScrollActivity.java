package com.example.dustin.pager;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.os.Environment;

import android.os.Message;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.util.TypedValue;

import android.view.View;
import android.widget.ImageButton;
import android.widget.ScrollView;
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
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.logging.Handler;
import java.util.logging.LogRecord;


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
    public int offsetchg = 4;
    public int offsettx = 52;
    ScrollTextView scrolltext;

    public float proploc;

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
    public Integer breaker = 2;
    public String[] litsplit_sent;
    public String buffedString;
    public Float scrollerspeed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        textsize = prefs.getInt("fontsize", 32);
        lastloc =  prefs.getInt("chapter", 2);
        counter = prefs.getInt("location", 0);
        proploc = prefs.getFloat("prop_loc",0);
        scrollerspeed = prefs.getFloat("scrollspeed",200);

        offsettx = prefs.getInt("offset",52);
        ebook = prefs.getString("book",getString(R.string.book_title1));


        editor = prefs.edit();
        //rsvp = 1
        //pager = 2
        //scroll = 3
        // na = 4
        editor.putInt("read_mode", 3);
        editor.commit();

        setContentView(R.layout.activity_scroll);
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                 View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);


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
        scrolltext.setmScrollSpeed(scrollerspeed);

        loadText(ebook,lastloc);
        BranchCount = Math.round(proploc*trimmer(litsplit_sent));
        loadMiniText(litsplit_sent,breaker,BranchCount);

        buffedString = stringGrow(offsettx);
        scrolltext.setText(buffedString+litBranch);
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
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        // do the thread processing / draw
                        completed = scrolltext.isdone();

                        if (completed & !BranchFlag) {
                            paused = true;
                            counter = 0;
                            completed = false;
                            scrolltext.setDone(completed);
                            BranchCount++;
                            loadMiniText(litsplit_sent, breaker, BranchCount);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    scrolltext.pauseScroll();
                                    player.setVisibility(View.INVISIBLE);
                                    pauser.setVisibility(View.VISIBLE);
                                    pauser.setVisibility(View.INVISIBLE);
                                    player.setVisibility(View.VISIBLE);
                                }
                            });
                            while(paused){
                                paused = scrolltext.isPaused();
                            }
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    scrolltext.pauseScroll();
                                    scrolltext.setDistance();
                                    buffedString = stringGrow(offsettx);
                                    scrolltext.setText(buffedString+litBranch);
                                    player.setVisibility(View.INVISIBLE);
                                    pauser.setVisibility(View.VISIBLE);
                                    pauser.setVisibility(View.INVISIBLE);
                                    player.setVisibility(View.VISIBLE);
                                    scrolltext.setVisibility(View.INVISIBLE);
                                    completed = false;
                                    scrolltext.setDone(completed);
                                    scrolltext.resumeScroll();


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
                            loadMiniText(litsplit_sent, breaker, BranchCount);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    scrolltext.pauseScroll();
                                    player.setVisibility(View.INVISIBLE);
                                    pauser.setVisibility(View.VISIBLE);
                                    pauser.setVisibility(View.INVISIBLE);
                                    player.setVisibility(View.VISIBLE);
                                }
                            });
                            while(paused){
                                paused = scrolltext.isPaused();
                            }
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    completed = false;
                                    scrolltext.setDone(completed);
                                    displayText("Next Chapter");
                                    scrolltext.setDistance();
                                    buffedString = stringGrow(offsettx);
                                    scrolltext.setText(buffedString+litBranch);
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

    protected void onDestroy() {
        super.onDestroy();
        proploc = (float) BranchCount/(litsplit_sent.length/breaker);
        editor = prefs.edit();
        editor.putInt("chapter", lastloc); // value to store
        editor.putInt("location", counter);
        editor.putFloat("prop_loc", proploc);
        editor.putFloat("scrollspeed",scrolltext.getmScrollSpeed());
        editor.putInt("fontsize", textsize);
        editor.putInt("offset",offsettx);
        editor.commit();
    }
    protected void onResume(){
        super.onResume();
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        scrolltext.scrollTo(scrolltext.getDistance(),0);



    }
    protected void onStart(){
        super.onStart();
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        scrolltext.scrollTo(scrolltext.getDistance(),0);


    }



    protected void onPause(){
        super.onPause();
        proploc = (float) BranchCount/(litsplit_sent.length/breaker);
        editor = prefs.edit();
        editor.putInt("chapter", lastloc); // value to store
        editor.putInt("location", counter);
        editor.putFloat("prop_loc", proploc);
        editor.putFloat("scrollspeed",scrolltext.getmScrollSpeed());
        editor.putInt("fontsize", textsize);
        editor.putInt("offset",offsettx);
        editor.commit();
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        scrolltext.scrollTo(scrolltext.getDistance(),0);


    }

    protected void onRestart(){
        super.onRestart();




    }

    protected void onStop(){
        super.onStop();
        proploc = (float) BranchCount/(litsplit_sent.length/breaker);
        editor = prefs.edit();
        editor.putInt("chapter", lastloc); // value to store
        editor.putInt("location", counter);
        editor.putFloat("prop_loc", proploc);
        editor.putFloat("scrollspeed",scrolltext.getmScrollSpeed());
        editor.putInt("fontsize", textsize);
        editor.putInt("offset",offsettx);
        editor.commit();


    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        scrolltext.scrollTo(scrolltext.getDistance(),0);

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
        if (scrolltext.getDistance()> 0) {
            scrolltext.pauseScroll();
            scrolltext.goBack();

            pauser.setVisibility(View.INVISIBLE);
            player.setVisibility(View.VISIBLE);
        } else if (scrolltext.getDistance()<1 & BranchCount > 0){
            displayText("Back");
            scrolltext.pauseScroll();
            pauser.setVisibility(View.INVISIBLE);
            player.setVisibility(View.VISIBLE);
            BranchCount--;
            loadMiniText(litsplit_sent, breaker, BranchCount);
            scrolltext.setDistance();
            buffedString = stringGrow(offsettx);
            scrolltext.setText(buffedString+litBranch);
            player.setVisibility(View.INVISIBLE);
            pauser.setVisibility(View.VISIBLE);
            pauser.setVisibility(View.INVISIBLE);
            player.setVisibility(View.VISIBLE);
            scrolltext.setVisibility(View.INVISIBLE);
            completed = false;
            scrolltext.setDone(completed);
        } else if (scrolltext.getDistance()<1 & BranchCount == 0){
            displayText("Back");
            scrolltext.pauseScroll();
            lastloc--;
            BranchCount = 0;
            counter = 0;
            completed = false;
            scrolltext.setDone(completed);
            loadText(ebook,lastloc);
            loadMiniText(litsplit_sent, breaker, BranchCount);
            pauser.setVisibility(View.INVISIBLE);
            player.setVisibility(View.VISIBLE);
            displayText("Next Chapter");
            scrolltext.setDistance();
            buffedString = stringGrow(offsettx);
            scrolltext.setText(buffedString+litBranch);
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

    public String stringGrow(Integer leng) {
        String result=" ";
        for (int i = 0; i<leng; i++) {
            result = result + " ";
        }
        return result;
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
        scrolltext.pauseScroll();
        textsize+=textchg;
        offsettx-=offsetchg;
        scrolltext.pauseScroll();
        Integer tempspot = scrolltext.getDistance();
        scrolltext.setTextSize(TypedValue.COMPLEX_UNIT_PT, textsize);
        firstTextView.setTextSize(TypedValue.COMPLEX_UNIT_PT, textsize);
        buffedString = stringGrow(offsettx);
        scrolltext.setText(buffedString+litBranch);
        scrolltext.scrollTo(tempspot,0);
        scrolltext.resumeScroll();
        scrolltext.pauseScroll();
        displayText(Integer.toString(textsize) + "px");
        pauser.setVisibility(View.INVISIBLE);
        player.setVisibility(View.VISIBLE);

    }

    public void decTextsize(View view){
        scrolltext.pauseScroll();
        textsize-=textchg;
        offsettx+=offsetchg;
        Integer tempspot = scrolltext.getDistance();
        scrolltext.setTextSize(TypedValue.COMPLEX_UNIT_PT, textsize);
        firstTextView.setTextSize(TypedValue.COMPLEX_UNIT_PT, textsize);
        buffedString = stringGrow(offsettx);
        scrolltext.setText(buffedString+litBranch);
        scrolltext.scrollTo(tempspot,0);
        scrolltext.resumeScroll();
        scrolltext.pauseScroll();
        displayText(Integer.toString(textsize) + "pt");
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
            literature = sb.toString();
            literature = literature.replace("„","\"");
            literature = literature.replace("“","\"");
            literature = literature.replace("\n\n","\n");
            literature = literature.replace("  @page { margin-bottom: 5.000000pt; margin-top: 5.000000pt; }","");
            literature = literature.replace("  p.sgc-1 {margin:0pt; border:0pt; height:1em}","");
            literature = literature.replace("  /*]]>*/","");
            literature = literature.replace("/*<![CDATA[*/","");
            literature = literature.replace("\n\n","\n");
            literature = literature.replace("\n\n","\n");
            literature = literature.replace("\n\n","\n");
            literature = literature.replace("\n\n","\n");
            literature = literature.replace("\n\n","\n");
            literature = literature.replace("\n\n","\n");
            literature = literature.replace("\n\n","\n");
            literature = literature.replace("\n\n","\n");

            //litSplit = sb.toString().replace("Copyright © 1956 by Street and Smith Publications, Inc. ","").split("\\r\\n|\\n|\\r");
            litSplit = literature.split("\\r\\n|\\n|\\r");

            litsplit_sent = literature.split("\\r\\n|\\n|\\r");


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

    private void loadMiniText(final String[] lit, int loc, int iter) {

        Integer div = trimmer(lit);
        Integer mod = stems(lit);

        if (loc > div) {
            loc = div;
            BranchFlag = true;
        }


        Boolean tempmodflag;
        if (mod > 0){
            tempmodflag = true;
        } else {
            tempmodflag = false;
        }
        if (iter < div-1) {
            litBranch = Html.fromHtml(TextUtils.join(" ", Arrays.asList(lit).subList(loc * iter, loc * iter + loc))).toString();

        } else if (!tempmodflag && iter == div-1) {
            litBranch = Html.fromHtml(TextUtils.join(" ", Arrays.asList(lit).subList(loc * iter, loc * iter + loc))).toString();
            BranchFlag = true;

        } else if (tempmodflag && iter == div-1){
            litBranch = Html.fromHtml(TextUtils.join(" ", Arrays.asList(lit).subList(loc * iter, loc * iter + loc))).toString();

        } else if (tempmodflag && iter == (div)){
            litBranch = Html.fromHtml(TextUtils.join(" ", Arrays.asList(lit).subList(loc * iter, loc * iter + mod))).toString();

            BranchFlag = true;
        }
        started = false;

    }




    public Integer trimmer(final String[] lit){

        return lit.length/breaker;

    }

    public Integer stems(final String[] lit){

        return lit.length%breaker;

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
                    slowdown.setVisibility(View.INVISIBLE);
                    speedup.setVisibility(View.INVISIBLE);
                    inctexsize.setVisibility(View.INVISIBLE);
                    dectexsize.setVisibility(View.INVISIBLE);
                    hidden = true;

                } else {
                    slowdown.setVisibility(View.VISIBLE);
                    speedup.setVisibility(View.VISIBLE);
                    inctexsize.setVisibility(View.VISIBLE);
                    dectexsize.setVisibility(View.VISIBLE);
                    hidden = false;

                }
                return true;

            case R.id.rsvp:
                proploc = (float) BranchCount/(litsplit_sent.length/breaker);
                editor = prefs.edit();
                editor.putInt("chapter", lastloc); // value to store
                editor.putInt("location", counter);
                editor.putFloat("prop_loc", proploc);
                editor.putFloat("scrollspeed",scrolltext.getmScrollSpeed());
                editor.putInt("fontsize", textsize);
                editor.putInt("offset",offsettx);
                editor.commit();
                Intent m_rsvp = new Intent(this, RsvpActivity.class);
                m_rsvp.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(m_rsvp);
                finish();
                return true;
            case R.id.pager_m:
                proploc = (float) BranchCount/(litsplit_sent.length/breaker);
                editor = prefs.edit();
                editor.putInt("chapter", lastloc); // value to store
                editor.putInt("location", counter);
                editor.putFloat("prop_loc", proploc);
                editor.putFloat("scrollspeed",scrolltext.getmScrollSpeed());
                editor.putInt("fontsize", textsize);
                editor.putInt("offset",offsettx);
                editor.commit();
                Intent pagerm = new Intent(this, PagerActivity.class);
                pagerm.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(pagerm);
                finish();
                return true;
            case R.id.scroller:
                proploc = (float) BranchCount/(litsplit_sent.length/breaker);
                editor = prefs.edit();
                editor.putInt("chapter", lastloc); // value to store
                editor.putInt("location", counter);
                editor.putFloat("prop_loc", proploc);
                editor.putFloat("scrollspeed",scrolltext.getmScrollSpeed());
                editor.putInt("fontsize", textsize);
                editor.putInt("offset",offsettx);
                editor.commit();
                Intent scrollm = new Intent(this, ScrollActivity.class);
                scrollm.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(scrollm);
                finish();
                return true;


            case R.id.books_1:
                editor = prefs.edit();
                editor.putString("book",getString(R.string.book_title1));
                editor.putInt("chapter", getResources().getInteger(R.integer.book1_chap));
                editor.putInt("location", 0);
                editor.putFloat("prop_loc",0);
                editor.putInt("fontsize", textsize);
                editor.putInt("offset",offsettx);
                editor.commit();
                lastloc=getResources().getInteger(R.integer.book1_chap);
                counter=0;
                scrollm = new Intent(this, ScrollActivity.class);
                scrollm.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(scrollm);
                finish();
                return true;

            case R.id.books_2:
                editor = prefs.edit();
                editor.putString("book",getString(R.string.book_title2));
                editor.putInt("chapter",  getResources().getInteger(R.integer.book2_chap));
                editor.putInt("location", 0);
                editor.putFloat("prop_loc",0);
                editor.putInt("fontsize", textsize);
                editor.putInt("offset",offsettx);
                editor.commit();
                lastloc=getResources().getInteger(R.integer.book2_chap);
                counter=0;
                scrollm = new Intent(this, ScrollActivity.class);
                scrollm.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(scrollm);
                finish();
                return true;

            case R.id.books_3:
                editor = prefs.edit();
                editor.putString("book",getString(R.string.book_title3));
                editor.putInt("chapter",  getResources().getInteger(R.integer.book3_chap));
                editor.putInt("location", 0);
                editor.putFloat("prop_loc",0);
                editor.putInt("fontsize", textsize);
                editor.putInt("offset",offsettx);
                editor.commit();
                lastloc=getResources().getInteger(R.integer.book3_chap);
                counter=0;
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