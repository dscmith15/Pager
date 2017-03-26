package com.example.dustin.pager;



import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {
    private TextView mTextView;
    private TextView firstTextView;
    private static final String LOG_TAG = "Log";
    Vibrator vibrator;
    ImageButton inctexsize;
    ImageButton dectexsize;




    public int mCurrentIndex;
    public int textsize;

    public Toast toast;

    public boolean hidden = true;

    public int wpm;


    public int lastloc;







    public SharedPreferences prefs;
    public SharedPreferences.Editor editor;
    public static final String PREFS_NAME = "User_Data";

    public Boolean UserHistory;


    Button pickpager;
    Button pickscroll;
    Button pickrsvp;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);




        setContentView(R.layout.activity_main);
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                //View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        toast = Toast.makeText(this, "", Toast.LENGTH_SHORT);

        mTextView = (TextView) findViewById(R.id.tv);


        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();

        UserHistory = prefs.getBoolean("userhist",false);

        //rsvp = 1
        //pager = 2
        //scroll = 3
        // na = 4
        int reading_mode = prefs.getInt("read_mode", 4);

        if (!UserHistory) {
            editor.putBoolean("userhist", true);
            editor.putInt("read_mode", 4);
            editor.putInt("chapter",  getResources().getInteger(R.integer.book1_chap));
            editor.putFloat("prop_loc",0);
            editor.putInt("location", 0);
            editor.putInt("fontsize", 32);
            editor.putInt("offset",52);
            editor.putInt("rsvpSpeed", 250);
            //editor.putInt("scrolldist",0);
            editor.putFloat("scrollSpeed", 200);
            editor.putString("book", getString(R.string.book_title1));
            editor.commit();
        } else {
            if (reading_mode == 1){ // this is RSVP
                Intent m_rsvp = new Intent(this, RsvpActivity.class);
                m_rsvp.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(m_rsvp);
                finish();
            }
            if (reading_mode == 2){ // this is
                Intent pagerm = new Intent(this, PagerActivity.class);
                pagerm.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(pagerm);
                finish();

            }
            if (reading_mode == 3){ // This is Scroll
                Intent scrollm = new Intent(this, ScrollActivity.class);
                scrollm.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(scrollm);
                finish();

            }
        }




        pickpager = (Button) findViewById(R.id.pagerbutton);
        pickrsvp = (Button) findViewById(R.id.rsvpbutton);
        pickscroll = (Button) findViewById(R.id.scrollbutton);


    }


    public void pickPager(View view){
        Intent pagerm = new Intent(this, PagerActivity.class);
        pagerm.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(pagerm);
        finish();

    }
    public void pickScroll(View view){
        Intent scrollm = new Intent(this, ScrollActivity.class);
        scrollm.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(scrollm);
        finish();
    }
    public void pickRSVP(View view){
        Intent m_rsvp = new Intent(this, RsvpActivity.class);
        m_rsvp.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(m_rsvp);
        finish();

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
            
            case R.id.rsvp:
                Intent m_rsvp = new Intent(this, RsvpActivity.class);
                m_rsvp.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(m_rsvp);
                finish();
                return true;
            case R.id.pager_m:
                Intent pagerm = new Intent(this, PagerActivity.class);
                pagerm.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(pagerm);
                finish();
                return true;
            case R.id.scroller:
                Intent scrollm = new Intent(this, ScrollActivity.class);
                scrollm.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(scrollm);
                finish();
                return true;
            case R.id.books_1:
                editor.putString("book",getString(R.string.book_title1));
                editor.putInt("chapter", getResources().getInteger(R.integer.book1_chap));
                editor.putInt("location", 0);
                editor.commit();
                return true;

            case R.id.books_2:
                editor.putString("book",getString(R.string.book_title2));
                editor.putInt("chapter",  getResources().getInteger(R.integer.book2_chap));
                editor.putInt("location", 0);
                editor.commit();
                return true;

            case R.id.books_3:
                editor.putString("book",getString(R.string.book_title3));
                editor.putInt("chapter",  getResources().getInteger(R.integer.book3_chap));
                editor.putInt("location", 0);
                editor.commit();
                return true;




            default:
                return super.onOptionsItemSelected(item);
        }


    }







}
