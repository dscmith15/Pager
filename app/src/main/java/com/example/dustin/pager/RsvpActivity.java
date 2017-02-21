package com.example.dustin.pager;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Vibrator;

import android.support.v7.app.AppCompatActivity;

import android.text.Html;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
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

import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.epub.EpubReader;

import static java.lang.Math.round;

public class RsvpActivity extends AppCompatActivity {

    private static final String LOG_TAG = "Log";
    Vibrator v;
    TextView firstTextView;

    //all of these should be added to all apps
    ImageButton slowdown;
    ImageButton speedup;

    ImageButton inctexsize;
    ImageButton dectexsize;
    ImageButton pausedInd;

    ImageButton player;
    ImageButton pauser;

    MenuItem hider;
    public int lastloc;

    public float proploc;




    //timer vars
    private long starttime;
    private long endTime;
    private boolean started = false;


    public String literature;
    public InputStream is = null;
    private int counter = 0;

    public int textsize;
    public int textchg = 5;
    public  String[] litSplit;
    public int wpm;
    public boolean threadSuspended = false;
    public boolean threadStarted = false;
    public boolean hidden = false;
    Thread RSVP;
    private Toast toast;
    private int pnum;
    private int mode;
    private boolean passageflag = false;
    private String passage;
    private String[][] orders;

    private int PassageCount=0;

    private long mDelay;
    public SharedPreferences prefs;
    public SharedPreferences.Editor editor;
    public static final String PREFS_NAME = "User_Data";
    public String ebook;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        textsize = prefs.getInt("fontsize", 32);
        lastloc =  prefs.getInt("chapter", 2);
        proploc = prefs.getFloat("prop_loc",0);
        counter = prefs.getInt("location", 0);
        wpm = prefs.getInt("rsvpSpeed", 250);
        ebook = prefs.getString("book",getString(R.string.book_title1));

        editor = prefs.edit();
        //rsvp = 1
        //pager = 2
        //scroll = 3
        // na = 4
        editor.putInt("read_mode", 1);
        editor.commit();



        setContentView(R.layout.activity_mrsvp);
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                //View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        slowdown = (ImageButton) findViewById(R.id.slower);
        speedup = (ImageButton) findViewById(R.id.faster);

        inctexsize = (ImageButton) findViewById(R.id.inctex);
        dectexsize = (ImageButton) findViewById(R.id.dectex);

        pausedInd = (ImageButton) findViewById(R.id.pause_ind);
        pausedInd.setVisibility(View.VISIBLE);

        hider = (MenuItem) findViewById(R.id.hide_button);

        pauser = (ImageButton) findViewById(R.id.imageButton4);
        pauser.setVisibility(View.INVISIBLE);
        player = (ImageButton) findViewById(R.id.imageButton3);


        toast = Toast.makeText(this, "", Toast.LENGTH_SHORT);

        mDelay= 60000/wpm;


        loadText(ebook,lastloc);
        counter = Math.round(proploc*litSplit.length);


        loadorders("Random_Orders_Spritz.csv");


        System.out.println(litSplit[1]);
        firstTextView = (TextView) findViewById(R.id.word_landing);
        firstTextView.setSelected(true);
        firstTextView.setTextSize(TypedValue.COMPLEX_UNIT_PT, textsize);

        RSVP = new Thread(new Runnable() {
            @Override
            public void run() {

                synchronized (this){
                    while(true)
                        try {

                            if(!threadSuspended) {

                                Thread.sleep(mDelay);

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {

                                        if (counter<litSplit.length) {

                                            firstTextView.setText(litSplit[counter]);
                                            counter++;
                                            mDelay=(long)((60000*delayValue()) / wpm);

                                        }
                                        if (counter==litSplit.length){
                                            lastloc++;
                                            counter = 0;
                                            loadText(ebook, lastloc);
                                            firstTextView.setText("Chapter Break");


                                        }

                                    }
                                });

                            }

                        } catch(InterruptedException e){
                            System.out.println("got interrupted!");
                        }

                }

            }
        });



    }

    protected void onDestroy() {
        super.onDestroy();
        if (!threadSuspended) {
            threadSuspended = true;
            pauser.setVisibility(View.INVISIBLE);
            player.setVisibility(View.VISIBLE);
            pausedInd.setVisibility(View.VISIBLE);
        }
        proploc = (float) counter/litSplit.length;
        editor = prefs.edit();
        editor.putInt("chapter", lastloc); // value to store
        editor.putInt("location", counter);
        editor.putFloat("prop_loc", proploc);
        editor.putInt("fontsize", textsize);
        editor.putInt("rsvpSpeed",wpm);
        editor.commit();
    }

    protected void onResume(){
        super.onResume();
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                //View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

    }
    protected void onStart(){
        super.onStart();
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                //View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

    }

    protected void onPause(){
        super.onPause();
        if (!threadSuspended) {
            threadSuspended = true;
            pauser.setVisibility(View.INVISIBLE);
            player.setVisibility(View.VISIBLE);
            pausedInd.setVisibility(View.VISIBLE);
        }
        proploc = (float) counter/litSplit.length;
        editor = prefs.edit();
        editor.putInt("chapter", lastloc); // value to store
        editor.putInt("location", counter);
        editor.putFloat("prop_loc", proploc);
        editor.putInt("fontsize", textsize);
        editor.putInt("rsvpSpeed",wpm);
        editor.commit();
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                //View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);



    }

    protected void onRestart(){
        super.onRestart();
        if (!threadSuspended) {
            threadSuspended = true;
            pauser.setVisibility(View.INVISIBLE);
            player.setVisibility(View.VISIBLE);
            pausedInd.setVisibility(View.VISIBLE);
        }
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                //View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

    }

    protected void onStop(){
        super.onStop();
        if (!threadSuspended) {
            threadSuspended = true;
            pauser.setVisibility(View.INVISIBLE);
            player.setVisibility(View.VISIBLE);
            pausedInd.setVisibility(View.VISIBLE);
        }
        proploc = (float) counter/litSplit.length;
        editor = prefs.edit();
        editor.putInt("chapter", lastloc); // value to store
        editor.putInt("location", counter);
        editor.putFloat("prop_loc", proploc);
        editor.putInt("fontsize", textsize);
        editor.putInt("rsvpSpeed",wpm);
        editor.commit();
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                //View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

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
    }




    public void onnextSelectValue(int id) {
        if (id == -2){

            if (PassageCount<1) {
                loadText("passage"+orders[pnum-1][PassageCount]+".txt",0);
                firstTextView.setText("X");
                PassageCount++;
            }
            else{
                displayText("You have completed all of the passages for this reading type");
            }


        }
    }

    public void NextRSVP(View view) {
        if(counter<litSplit.length){

            firstTextView.setText(litSplit[counter]);
            counter++;

            if (!threadSuspended) {
                threadSuspended = true;
                pauser.setVisibility(View.INVISIBLE);
                player.setVisibility(View.VISIBLE);
                pausedInd.setVisibility(View.VISIBLE);
            }
        }


    }

    public void PrevRSVP(View view) {

        if(counter>0){
            counter--;

            firstTextView.setText(litSplit[counter]);
            if (!threadSuspended) {
                threadSuspended = true;
                pauser.setVisibility(View.INVISIBLE);
                player.setVisibility(View.VISIBLE);
                pausedInd.setVisibility(View.VISIBLE);
            }

        } else {
            lastloc--;
            loadText(ebook, lastloc);
            counter = litSplit.length-1;
            firstTextView.setText("Previous Chapter");
        }


    }
    private void displayText(final String message) {

        toast.setText(message);
        toast.show();
    }
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
            literature = sb.toString();
            literature = literature.replace("„","\"");
            literature = literature.replace("“","\"");
            literature = literature.replace("\n\n","\n");
            literature = literature.replace("  @page { margin-bottom: 5.000000pt; margin-top: 5.000000pt; }","");
            literature = literature.replace("  p.sgc-1 {margin:0pt; border:0pt; height:1em}","");
            literature = literature.replace("  /*]]>*/","");
            literature = literature.replace("/*<![CDATA[*/","");
            literature = literature.replace("\n\n","\n");
            literature = literature.replace("-","- ");
            literature = literature.replace("\n\n","\n");
            literature = literature.replace("\n\n","\n");
            literature = literature.replace("\n\n","\n");
            literature = literature.replace("\n\n","\n");
            literature = literature.replace("\n\n","\n");
            literature = literature.replace("\n\n","\n");
            literature = literature.replace("\n\n","\n");

            litSplit = Html.fromHtml(literature).toString().split("\\s+");
            started = false;
            threadSuspended = true;
            pauser.setVisibility(View.INVISIBLE);
            player.setVisibility(View.VISIBLE);
            pausedInd.setVisibility(View.VISIBLE);



        } catch (IOException e) {

            e.printStackTrace();
        }

    }

    public void slowRSVP(View view) {
        if (wpm>10) {
            wpm -= 10;
        }
        // show end user new wpm

        displayText(Long.toString(wpm) + " wpm");

    }


    public void fastRSVP(View view) {
        wpm += 10;

        // show end user new wpm
        displayText(Long.toString(wpm) + " wpm");

    }

    public double delayValue(){
        double delayer =1;

        if (counter<litSplit.length) {
            String tempword = litSplit[counter];

            if (counter>0&counter<litSplit.length&(tempword.length())>0) {
                delayer = Math.pow(((tempword.length()) / 5f), .5);
            }} else{
            delayer = 1;
        }

        return delayer;
    }

    public void StartRSVP(View view) {
        if (!threadStarted){
            RSVP.start();
            threadStarted = true;
            player.setVisibility(View.INVISIBLE);
            pauser.setVisibility(View.VISIBLE);
            pausedInd.setVisibility(View.INVISIBLE);
        }
        if (!started){
            starttime=System.nanoTime();
            started = true;

        }

        if (threadSuspended&counter!=litSplit.length+1){
            threadSuspended = false;
            player.setVisibility(View.INVISIBLE);
            pauser.setVisibility(View.VISIBLE);
            pausedInd.setVisibility(View.INVISIBLE);

        }


    }
    public void pauseRSVP(View view) {


        if (!threadSuspended) {
            threadSuspended = true;
            pauser.setVisibility(View.INVISIBLE);
            player.setVisibility(View.VISIBLE);
            pausedInd.setVisibility(View.VISIBLE);
        }



    }

    public void incTextsize(View view){
        textsize+=textchg;
        firstTextView.setTextSize(TypedValue.COMPLEX_UNIT_PT, textsize);

        // show end user new font size
        displayText(Integer.toString(textsize) + "pt");



    }

    public void decTextsize(View view){
        textsize-=textchg;
        firstTextView.setTextSize(TypedValue.COMPLEX_UNIT_PT, textsize);


        // show end user new font size
        displayText(Integer.toString(textsize) + "pt");


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
                proploc = (float) counter/litSplit.length;
                editor = prefs.edit();
                editor.putInt("chapter", lastloc); // value to store
                editor.putInt("location", counter);
                editor.putFloat("prop_loc", proploc);
                editor.putInt("fontsize", textsize);
                editor.putInt("rsvpSpeed",wpm);
                editor.commit();
                Intent m_rsvp = new Intent(this, RsvpActivity.class);
                m_rsvp.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(m_rsvp);
                finish();
                return true;
            case R.id.pager_m:
                proploc = (float) counter/litSplit.length;
                editor = prefs.edit();
                editor.putInt("chapter", lastloc); // value to store
                editor.putInt("location", counter);
                editor.putFloat("prop_loc", proploc);
                editor.putInt("fontsize", textsize);
                editor.putInt("rsvpSpeed",wpm);
                editor.commit();
                Intent pagerm = new Intent(this, PagerActivity.class);
                pagerm.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(pagerm);
                finish();
                return true;
            case R.id.scroller:
                proploc = (float) counter/litSplit.length;
                editor = prefs.edit();
                editor.putInt("chapter", lastloc); // value to store
                editor.putInt("location", counter);
                editor.putFloat("prop_loc", proploc);
                editor.putInt("fontsize", textsize);
                editor.putInt("rsvpSpeed",wpm);
                editor.commit();
                Intent scrollm = new Intent(this, ScrollActivity.class);
                scrollm.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(scrollm);
                finish();
                return true;

            case R.id.books_1:

                editor = prefs.edit();
                editor.putString("book",getString(R.string.book_title1));
                editor.putInt("chapter", 2);
                editor.putInt("location", 0);
                editor.putFloat("prop_loc",0);
                editor.putInt("fontsize", textsize);
                editor.putInt("rsvpSpeed",wpm);
                editor.commit();
                m_rsvp = new Intent(this, RsvpActivity.class);
                m_rsvp.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(m_rsvp);
                finish();
                return true;

            case R.id.books_2:
                editor = prefs.edit();
                editor.putString("book",getString(R.string.book_title2));
                editor.putInt("chapter", 6);
                editor.putInt("location", 0);
                editor.putFloat("prop_loc",0);
                editor.putInt("fontsize", textsize);
                editor.putInt("rsvpSpeed",wpm);
                editor.commit();
                m_rsvp = new Intent(this, RsvpActivity.class);
                m_rsvp.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(m_rsvp);
                finish();
                return true;

            case R.id.books_3:
                editor = prefs.edit();
                editor.putString("book",getString(R.string.book_title3));
                editor.putInt("chapter", 5);
                editor.putInt("location", 0);
                editor.putFloat("prop_loc",0);
                editor.putInt("fontsize", textsize);
                editor.putInt("rsvpSpeed",wpm);
                editor.commit();
                m_rsvp = new Intent(this, RsvpActivity.class);
                m_rsvp.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(m_rsvp);
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

    private void loadorders(final String file) {
        try {
            orders = new String[30][2];


            InputStreamReader is = new InputStreamReader(getAssets().open(file));


            CSVReader reader = new CSVReader(new BufferedReader(is));
            String[] nextLine;


            int i = 0;

            while ((nextLine = reader.readNext()) != null){
                orders[i][0]=nextLine[0];
                orders[i][1]=nextLine[1];
                i++;
            }

        } catch (IOException e) {

            e.printStackTrace();
        }

    }



}