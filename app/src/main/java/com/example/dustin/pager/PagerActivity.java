package com.example.dustin.pager;


import android.annotation.TargetApi;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;

import android.os.Build;

import android.os.Bundle;


import android.os.Vibrator;

import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;

import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import com.opencsv.CSVReader;


import java.io.BufferedReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


// this is the section of imports for epub reading
import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.epub.EpubReader;

import static java.lang.Math.round;

public class PagerActivity extends AppCompatActivity {
    private TextView mTextView;
    private TextView firstTextView;
    private static final String LOG_TAG = "Log";
    Vibrator vibrator;
    ImageButton inctexsize;
    ImageButton dectexsize;

    MenuItem hider;

    //timer vars
    private long starttime;
    private long endTime;
    private boolean started = false;
    public String ebook;


    public Pagination mPagination;
    public CharSequence mText;
    public int mCurrentIndex;
    public int textsize;
    public int textchg = 5;
    public Toast toast;
    public String literature;
    public  String[] litSplit;
    public boolean hidden = true;
    public Spannable spanString;
    public int wpm;

    private int pnum;
    private int mode;
    private boolean passageflag = false;
    private String passage;
    private String[][] orders;
    public int lastloc;

    public Boolean settopen = false;


    private int PassageCount=0;

    private boolean begin = false;
    public SharedPreferences prefs;
    public SharedPreferences.Editor editor;
    public static final String PREFS_NAME = "User_Data";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        textsize = prefs.getInt("fontsize", 32);
        lastloc =  prefs.getInt("chapter", 2);
        mCurrentIndex = prefs.getInt("location", 0);
        ebook = prefs.getString("book",getString(R.string.book_title1));

        editor = prefs.edit();
        //rsvp = 1
        //pager = 2
        //scroll = 3
        // na = 4
        editor.putInt("read_mode", 2);
        editor.commit();


        setContentView(R.layout.activity_pager);
        toast = Toast.makeText(this, "", Toast.LENGTH_SHORT);

        mTextView = (TextView) findViewById(R.id.tv);

        inctexsize = (ImageButton) findViewById(R.id.inctex);
        dectexsize = (ImageButton) findViewById(R.id.dectex);

        hider = (MenuItem) findViewById(R.id.hide_button);

        inctexsize.setVisibility(View.INVISIBLE);
        dectexsize.setVisibility(View.INVISIBLE);

        loadText(ebook, lastloc);

        firstTextView = (TextView) findViewById(R.id.word_landing);
        firstTextView.setSelected(true);
        spanString = new SpannableString(Html.fromHtml(literature));
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        mText = spanString;

        mTextView.setVisibility(View.INVISIBLE);
        mTextView.setTextSize(TypedValue.COMPLEX_UNIT_PT, textsize);
        firstTextView.setTextSize(TypedValue.COMPLEX_UNIT_PT, textsize);
        mTextView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onGlobalLayout() {
                // Removing layout listener to avoid multiple calls
                mTextView.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                mPagination = new Pagination(mText,
                        mTextView.getWidth(),
                        mTextView.getHeight(),
                        mTextView.getPaint(),
                        mTextView.getLineSpacingMultiplier(),
                        mTextView.getLineSpacingExtra(),
                        mTextView.getIncludeFontPadding());
                update();
            }
        });



        findViewById(R.id.btn_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mCurrentIndex = (mCurrentIndex > 0) ? mCurrentIndex - 1 : 0;
                update();

            }
        });
        findViewById(R.id.btn_forward).setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {



                if (begin == false) {
                    mTextView.setVisibility(View.VISIBLE);
                    firstTextView.setVisibility(View.INVISIBLE);
                    starttime = System.nanoTime();
                    begin = true;
                } else {
                    mCurrentIndex = (mCurrentIndex < mPagination.size() - 1) ? mCurrentIndex + 1 : mPagination.size() - 1;
                    if (mCurrentIndex == mPagination.size() - 1) {
                        //passageflag is used to know if the passage is over
                        passageflag = true;
                        lastloc++;

                        loadText(ebook, lastloc);
                        mCurrentIndex = 0;

                        prepView();
                        passageflag = false;

                    }

                }




                update();
            }

        });
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
            litSplit = literature.split("\\s+");



        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void prepView(){

        spanString = new SpannableString(Html.fromHtml(literature));
        mText = spanString;


        mPagination = new Pagination(mText,
                mTextView.getWidth(),
                mTextView.getHeight(),
                mTextView.getPaint(),
                mTextView.getLineSpacingMultiplier(),
                mTextView.getLineSpacingExtra(),
                mTextView.getIncludeFontPadding());
        update();
    }

    private void update() {
        final CharSequence text = mPagination.get(mCurrentIndex);
        if(text != null) mTextView.setText(text);
    }
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void incTextsize(View view){
        textsize+=textchg;

        mTextView.setTextSize(TypedValue.COMPLEX_UNIT_PT, textsize);

        mPagination = new Pagination(mText,
                mTextView.getWidth(),
                mTextView.getHeight(),
                mTextView.getPaint(),
                mTextView.getLineSpacingMultiplier(),
                mTextView.getLineSpacingExtra(),
                mTextView.getIncludeFontPadding());
        update();


        // show end user new font size
        displayText(Integer.toString(textsize) + "pt");

    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void decTextsize(View view){
        textsize-=textchg;

        mTextView.setTextSize(TypedValue.COMPLEX_UNIT_PT, textsize);

        mPagination = new Pagination(mText,
                mTextView.getWidth(),
                mTextView.getHeight(),
                mTextView.getPaint(),
                mTextView.getLineSpacingMultiplier(),
                mTextView.getLineSpacingExtra(),
                mTextView.getIncludeFontPadding());
        update();


        // show end user new font size
        displayText(Integer.toString(textsize) + "pt");

    }
    private void displayText(final String message) {

        toast.setText(message);
        toast.show();
    }


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
                mCurrentIndex = mCurrentIndex/mPagination.size();
                editor = prefs.edit();
                editor.putInt("chapter", lastloc); // value to store
                editor.putInt("location", mCurrentIndex);
                editor.putInt("fontsize", textsize);
                editor.commit();
                Intent m_rsvp = new Intent(this, RsvpActivity.class);
                m_rsvp.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(m_rsvp);
                finish();
                return true;
            case R.id.pager_m:
                mCurrentIndex = mCurrentIndex/mPagination.size();
                editor = prefs.edit();
                editor.putInt("chapter", lastloc); // value to store
                editor.putInt("location", mCurrentIndex);
                editor.putInt("fontsize", textsize);
                editor.commit();
                Intent pagerm = new Intent(this, PagerActivity.class);
                pagerm.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(pagerm);
                finish();
                return true;
            case R.id.scroller:
                mCurrentIndex = mCurrentIndex/mPagination.size();
                editor = prefs.edit();
                editor.putInt("chapter", lastloc); // value to store
                editor.putInt("location", mCurrentIndex);
                editor.putInt("fontsize", textsize);
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
                editor.putInt("fontsize", textsize);

                editor.commit();
                pagerm = new Intent(this, PagerActivity.class);
                pagerm.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(pagerm);
                finish();
                return true;

            case R.id.books_2:
                editor = prefs.edit();
                editor.putString("book",getString(R.string.book_title2));
                editor.putInt("chapter", 1);
                editor.putInt("location", 0);
                editor.putInt("fontsize", textsize);

                editor.commit();
                pagerm = new Intent(this, PagerActivity.class);
                pagerm.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(pagerm);
                finish();
                return true;

            case R.id.books_3:
                editor = prefs.edit();
                editor.putString("book",getString(R.string.book_title3));
                editor.putInt("chapter", 2);
                editor.putInt("location", 0);
                editor.putInt("fontsize", textsize);

                editor.commit();
                pagerm = new Intent(this, PagerActivity.class);
                pagerm.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(pagerm);
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }


    }

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



    public void onnextSelectValue(int id) {
        if (id == -2){

            if (PassageCount<1
                    ) {
                loadText("passage"+orders[pnum-1][PassageCount]+".txt", lastloc);
                mTextView.setText("X");
                prepView();
                PassageCount++;
            }
            else{
                displayText("You have completed all of the passages for this reading type");
            }


        }
    }







}