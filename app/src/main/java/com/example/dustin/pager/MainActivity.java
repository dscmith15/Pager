package com.example.dustin.pager;


import android.annotation.TargetApi;
import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;

import android.os.Build;

import android.os.Bundle;

import android.os.Environment;
import android.os.Vibrator;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
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
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.prefs.Preferences;

// this is the section of imports for epub reading
import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.epub.EpubReader;

public class MainActivity extends AppCompatActivity {
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

    private int pnum;
    private int mode;
    private boolean passageflag = false;
    private String passage;
    private String[][] orders;
    private String wpm = "NAN";
    public int lastloc;

    public Boolean settopen = false;


    private int PassageCount=0;

    private boolean begin = false;
    public SharedPreferences prefs;
    public SharedPreferences.Editor editor;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        setContentView(R.layout.activity_main);
        toast = Toast.makeText(this, "", Toast.LENGTH_SHORT);

        mTextView = (TextView) findViewById(R.id.tv);


        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        textsize = prefs.getInt("textsize_pref",32);
        lastloc =  prefs.getInt("chap_pref",1);
        mCurrentIndex = prefs.getInt("location_pref",0);
        final SharedPreferences.Editor editor = prefs.edit();

        inctexsize = (ImageButton) findViewById(R.id.inctex);
        dectexsize = (ImageButton) findViewById(R.id.dectex);

        hider = (MenuItem) findViewById(R.id.hide_button);

        inctexsize.setVisibility(View.INVISIBLE);
        dectexsize.setVisibility(View.INVISIBLE);

        loadText("AIWL.epub", lastloc);

        firstTextView = (TextView) findViewById(R.id.word_landing);
        firstTextView.setSelected(true);

        //DialogFragment modeFrag = new ModeDialogFragment();
        //modeFrag.show(getFragmentManager(),"Mode Frag");
        loadorders("Random_Orders_Pager.csv");
        spanString = new SpannableString(Html.fromHtml(literature));
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        mText = spanString;

        mTextView.setVisibility(View.INVISIBLE);
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
                lastloc =  prefs.getInt("chap_pref",lastloc);
                mCurrentIndex = prefs.getInt("location_pref",mCurrentIndex);

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
                        displayText("next chapter");
                        loadText("AIWL.epub", lastloc);

                        prepView();
                        passageflag = false;

                    }

                }
                editor.putInt("chap_pref", lastloc); // value to store
                editor.putInt("location_pref", mCurrentIndex);
                editor.commit();


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
            litSplit = literature.split("\\s+");
            mCurrentIndex = 0;


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
        final SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("textsize_pref", textsize);
        editor.commit();

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
        final SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("textsize_pref", textsize);
        editor.commit();

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

            case R.id.sett:
                //Id like to put my settings here please
                Intent settings = new Intent(this, SettingsActivity.class);
                startActivity(settings);
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

    public void onPnumSelectValue(int which) {
        pnum = which+1;
        displayText("Participant " + Integer.toString(pnum)+ " Selected");
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

    public void onModeSelectValue(int id) {
        mode = id;
        if (mode==-2){
            DialogFragment pnumFrag = new PnumDialogFragment();
            pnumFrag.show(getFragmentManager(), "PnumFrag");
        }
    }




}
