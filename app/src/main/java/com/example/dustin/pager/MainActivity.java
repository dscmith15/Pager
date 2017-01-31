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


    Button pickpager;
    Button pickscroll;
    Button pickrsvp;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        setContentView(R.layout.activity_main);
        toast = Toast.makeText(this, "", Toast.LENGTH_SHORT);

        mTextView = (TextView) findViewById(R.id.tv);


        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();

        editor.putInt("chapter", 2);
        editor.putInt("location", 0);
        editor.putInt("fontsize", 32);
        editor.putInt("rsvpSpeed",250);
        editor.putInt("scrollSpeed",200);
        editor.putString("book","QueenVictoria.epub");

        editor.commit();



        pickpager = (Button) findViewById(R.id.pagerbutton);
        pickrsvp = (Button) findViewById(R.id.rsvpbutton);
        pickscroll = (Button) findViewById(R.id.scrollbutton);


    }


    public void pickPager(View view){
        Intent pagerm = new Intent(this, PagerActivity.class);
        startActivity(pagerm);
        finish();

    }
    public void pickScroll(View view){
        Intent scrollm = new Intent(this, ScrollActivity.class);
        startActivity(scrollm);
        finish();
    }
    public void pickRSVP(View view){
        Intent m_rsvp = new Intent(this, RsvpActivity.class);
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







}
