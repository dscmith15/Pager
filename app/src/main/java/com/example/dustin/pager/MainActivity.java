package com.example.dustin.pager;


import android.annotation.TargetApi;
import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.res.AssetManager;

import android.os.Build;

import android.os.Bundle;

import android.os.Environment;
import android.os.Vibrator;
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
    public int mCurrentIndex = 0;
    public int textsize = 32;
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
    public int lastloc = 1;


    private int PassageCount=0;

    private boolean begin = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toast = Toast.makeText(this, "", Toast.LENGTH_SHORT);

        mTextView = (TextView) findViewById(R.id.tv);


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
                if (begin == false) {
                    mTextView.setVisibility(View.VISIBLE);
                    firstTextView.setVisibility(View.INVISIBLE);
                    starttime = System.nanoTime();
                    begin = true;
                } else {
                    mCurrentIndex = (mCurrentIndex < mPagination.size() - 1) ? mCurrentIndex + 1 : mPagination.size() - 1;

                    //commenting below for a while to see if needed
                    /*if (passageflag & mode == -2) {
                        endTime = System.nanoTime() - starttime;

                        vibrator.vibrate(1000);
                        begin = false;
                        mTextView.setVisibility(View.INVISIBLE);
                        firstTextView.setVisibility(View.VISIBLE);

                        passageflag = false;
                        String pdata = "data.txt";
                        writeStringToTextFile(Integer.toString(pnum) + "\t" + "Pager" + "\t" + passage + "\t" + Long.toString(endTime) + "\t" + wpm + "\t" + Integer.toString(textsize) + "\t" + Long.toString(litSplit.length) + "\n", pdata);
                        mCurrentIndex++;
                        DialogFragment nextPasFrag = new NextPassageDialog();
                        nextPasFrag.show(getFragmentManager(), "nextPasFrag");
                    }*/

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
            case R.id.demo_1:
                if (item.isChecked()) item.setChecked(false);
                else item.setChecked(true);
                loadText("practice4.txt",lastloc);
                prepView();
                displayText("Demo Text Initiated");
                prepView();
                return true;
            case R.id.passage_1:
                if (item.isChecked()) item.setChecked(false);
                else item.setChecked(true);
                loadText("passage1.txt",lastloc);
                displayText("Passage 1 Initiated");
                prepView();
                return true;
            case R.id.passage_2:
                if (item.isChecked()) item.setChecked(false);
                else item.setChecked(true);
                loadText("passage2.txt",lastloc);
                displayText("Passage 2 Initiated");
                prepView();
                return true;
            case R.id.passage_3:
                if (item.isChecked()) item.setChecked(false);
                else item.setChecked(true);
                loadText("passage3.txt",lastloc);
                displayText("Passage 3 Initiated");
                prepView();
                return true;
            case R.id.passage_4:
                if (item.isChecked()) item.setChecked(false);
                else item.setChecked(true);
                loadText("passage4.txt",lastloc);
                displayText("Passage 4 Initiated");
                prepView();
                return true;
            case R.id.passage_5:
                if (item.isChecked()) item.setChecked(false);
                else item.setChecked(true);
                loadText("passage5.txt",lastloc);
                displayText("Passage 5 Initiated");
                prepView();
                return true;
            case R.id.passage_6:
                if (item.isChecked()) item.setChecked(false);
                else item.setChecked(true);
                loadText("passage6.txt",lastloc);
                displayText("Passage 6 Initiated");
                prepView();
                return true;
            case R.id.passage_7:
                if (item.isChecked()) item.setChecked(false);
                else item.setChecked(true);
                loadText("passage7.txt",lastloc);
                displayText("Passage 7 Initiated");
                prepView();
                return true;
            case R.id.passage_8:
                if (item.isChecked()) item.setChecked(false);
                else item.setChecked(true);
                loadText("passage8.txt",lastloc);
                displayText("Passage 8 Initiated");
                prepView();
                return true;
            case R.id.passage_9:
                if (item.isChecked()) item.setChecked(false);
                else item.setChecked(true);
                loadText("passage9.txt",lastloc);
                displayText("Passage 9 Initiated");
                prepView();
                return true;
            case R.id.passage_10:
                if (item.isChecked()) item.setChecked(false);
                else item.setChecked(true);
                loadText("passage10.txt",lastloc);
                displayText("Passage 10 Initiated");
                prepView();
                return true;
            case R.id.exit_the_app:
                finish();
                return true;

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
