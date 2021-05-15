package com.dmejlvang.snapapp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import com.dmejlvang.snapapp.adapter.SnapAdapter;
import com.dmejlvang.snapapp.repository.Repository;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

//IMPORTANT METHODS
//startListener() in Repository.java
//receive() in ShowSnap.java

public class MainActivity extends AppCompatActivity implements Updatable {
    List<String> snaps = new ArrayList<>();
    ListView listView;
    SnapAdapter snapAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupListView();

        //setup Firebase repository
        Repository.r().setup(this, snaps);
    }

    //setup listview and connect adapter
    //on list item click, new activity with snap picture is shown
    public void setupListView() {
        listView = findViewById(R.id.snapList);
        snapAdapter = new SnapAdapter(snaps, this);
        listView.setAdapter(snapAdapter);
        listView.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent(MainActivity.this, ShowSnap.class);
            intent.putExtra("SNAP_ID", snaps.get(position));
            startActivity(intent);
        });
    }

    //method lets user choose whether to snap picture from gallery or take new picture with camera
    public void snapPictureButtonPressed(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Snap picture from gallery or from camera?");
        String[] options = {"From gallery", "From camera"};
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, 0);
            } else {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(takePictureIntent, 1);
            }
        });
        builder.show();
    }

    //onActivityResult is called when above intents return data
    Bitmap bitmapToUpload;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //if user chooses to snap picture from gallery
        if (requestCode == 0 && resultCode == RESULT_OK) {
            try {
                final Uri imageUri = data.getData();
                final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                //save picture bitmap outside method to access it in subsequent methods
                bitmapToUpload = BitmapFactory.decodeStream(imageStream);
            } catch (FileNotFoundException e) {
                System.out.println("Imagepicker error: " + e.getLocalizedMessage());
            }

        //if user chooses to snap picture from camera
        } else if (requestCode == 1 && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            bitmapToUpload = (Bitmap) extras.get("data");
        }
        getTextForImage();
    }

    //method creates an Alert box with EditText input so user can specify text to be written onto picture bitmap
    public void getTextForImage() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Write text on image");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> drawTextToBitmap(input.getText().toString()));
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    //method draws text onto picture bitmap
    public void drawTextToBitmap(String gText) {
        Bitmap.Config bitmapConfig = bitmapToUpload.getConfig();
        // set default bitmap config if none
        if(bitmapConfig == null) {
            bitmapConfig = Bitmap.Config.ARGB_8888;
        }
        // resource bitmaps are immutable,
        // so we need to convert it to mutable one
        bitmapToUpload = bitmapToUpload.copy(bitmapConfig, true);
        Canvas canvas = new Canvas(bitmapToUpload);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);// new anti-aliased Paint
        paint.setColor(Color.rgb(79, 17, 24));
        paint.setTextSize((int) (20)); // text size in pixels
        paint.setShadowLayer(1f, 0f, 1f, Color.WHITE); // text shadow
        canvas.drawText(gText, 10, 100, paint);
        //bitmap is uploaded to Storage
        //the text is uploaded as well for easier identification of documents in Firestore
        Repository.r().uploadBitmap(bitmapToUpload, gText);
    }

    @Override
    public void update(Object o) {
        snapAdapter.notifyDataSetChanged();
    }
}