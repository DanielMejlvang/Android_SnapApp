package com.dmejlvang.snapapp;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;

import com.dmejlvang.snapapp.repository.Repository;

public class ShowSnap extends AppCompatActivity implements TaskListener {
    ImageView imageView;
    String imageId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_snap);

        imageId = getIntent().getStringExtra("SNAP_ID");

        //download bitmap of snap with current snap ID
        Repository.r().downloadBitmap(imageId, this);
    }

    //IMPORTANT METHOD
    //when downloadBitmap has finished successfully, this method is called
    //method creates bitmap from received bytes and puts into imageview
    //uses TaskListener interface to receive byte array
    @Override
    public void receive(byte[] bytes) {
        imageView = findViewById(R.id.snapImage);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inMutable = true;
        Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
        imageView.setImageBitmap(bmp);
    }

    //when user tabs to go back, the snap is deleted and the activity is finished
    @Override
    public void onBackPressed() {
        Repository.r().deleteSnap(imageId);
        finish();
    }
}