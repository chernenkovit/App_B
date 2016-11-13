package com.example.chernenkovit.b;

import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.chernenkovit.b.data.ImageContract;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static com.example.chernenkovit.b.data.ImageContract.IMAGES_CONTENT_URI;
import static com.example.chernenkovit.b.data.ImageContract.IMAGES_ID_COLUMN;
import static com.example.chernenkovit.b.data.ImageContract.IMAGES_LINK_COLUMN;

/**
 * Main screen and main activity.
 */
public class MainActivity extends AppCompatActivity {
    private static final String LOG_TAG = "AppB MainActivity";
    private Intent showImageIntent;
    private String imageLink;
    private int prevActivity;
    private ImageView imageView;
    private long timestamp;
    private int status = Const.STATUS_UNKNOWN;
    private int prevStatus;
    private ProgressDialog progressDialog;
    private long id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = (ImageView) findViewById(R.id.iv_image);

        //load data from intent
        showImageIntent = getIntent();
        imageLink = showImageIntent.getStringExtra("Link");
        prevActivity = showImageIntent.getIntExtra("Prev", Const.LAUNCHER);
        id = showImageIntent.getLongExtra("Id", 0);
        prevStatus = showImageIntent.getIntExtra("Status", Const.STATUS_UNKNOWN);

        //get current timestamp
        timestamp = System.currentTimeMillis();

        //open image according to previous Fragment or Launcher
        try {
            openImage(prevActivity);

        } catch (java.lang.IllegalArgumentException e) {
            Toast.makeText(this, "Empty link", Toast.LENGTH_SHORT).show();
        }
    }

    private void openImage(int prevFragment) {
        switch (prevFragment) {

            //if app was launch as a separate applicatiom
            case Const.LAUNCHER:
                closeApp();
                break;
            //if app was launched from test fragment
            case Const.TEST_FRAGMENT:
                Picasso.with(this).load(imageLink).into(imageView, new Callback() {
                    @Override
                    public void onSuccess() {
                        status = Const.STATUS_DOWNLOADED;
                        saveToDB(status, imageLink);
                    }

                    @Override
                    public void onError() {
                        status = Const.STATUS_ERROR;
                        Toast.makeText(getApplicationContext(), "Error downloading image!", Toast.LENGTH_SHORT).show();
                        saveToDB(status, imageLink);
                    }
                });
                break;

            //if app was launched from history fragment
            case Const.HISTORY_FRAGMENT:
                Picasso.with(this).load(imageLink).into(imageView, new Callback() {
                    @Override
                    public void onSuccess() {
                        //check what was the previous status and compare it with present status
                        if (prevStatus == Const.STATUS_DOWNLOADED) {
                            saveImage();
                            deleteImage(id);
                        } else {
                            status = Const.STATUS_DOWNLOADED;
                            updateDB(id, status);
                        }
                    }

                    @Override
                    public void onError() {
                        status = Const.STATUS_ERROR;
                        if (prevStatus != status) {
                            updateDB(id, status);
                            Toast.makeText(getApplicationContext(), "Error downloading image!", Toast.LENGTH_SHORT).show();
                        } else
                            Toast.makeText(getApplicationContext(), "Error downloading image!", Toast.LENGTH_SHORT).show();
                    }
                });
                break;
        }
    }

    private void saveToDB(int status, String imageLink) {

        //check if database already contains this imageLink
        Cursor cursor = getContentResolver().query(
                IMAGES_CONTENT_URI,
                null,
                IMAGES_LINK_COLUMN + "=?",
                new String[]{imageLink},
                null);

        //if contains - update only status and timestamp
        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndex(IMAGES_ID_COLUMN));
            ContentValues values = new ContentValues();
            values.put(ImageContract.IMAGES_STATUS_COLUMN, status);
            values.put(ImageContract.IMAGES_TIMESTAMP_COLUMN, timestamp);
            Uri uri = ContentUris.withAppendedId(IMAGES_CONTENT_URI, id);
            int cnt = getContentResolver().update(uri, values, null, null);
            Toast.makeText(getApplicationContext(), "Updated", Toast.LENGTH_SHORT).show();
            Log.d(LOG_TAG, "updated, result Uri : " + uri.toString());
            cursor.close();
        }

        //if does not contain - insert all data
        else {
            ContentValues values = new ContentValues();
            values.put(ImageContract.IMAGES_LINK_COLUMN, imageLink);
            values.put(ImageContract.IMAGES_STATUS_COLUMN, status);
            values.put(ImageContract.IMAGES_TIMESTAMP_COLUMN, timestamp);
            Uri uri = getContentResolver().insert(IMAGES_CONTENT_URI, values);
            Toast.makeText(getApplicationContext(), "Saved", Toast.LENGTH_SHORT).show();
            Log.d(LOG_TAG, "inserted, result Uri : " + uri.toString());
        }
    }

    //update database after clicking on the list item in App A
    private void updateDB(long id, int status) {
        ContentValues values = new ContentValues();
        values.put(ImageContract.IMAGES_STATUS_COLUMN, status);
        values.put(ImageContract.IMAGES_TIMESTAMP_COLUMN, timestamp);
        Uri uri = ContentUris.withAppendedId(IMAGES_CONTENT_URI, id);
        int cnt = getContentResolver().update(uri, values, null, null);
        Toast.makeText(getApplicationContext(), "Updated", Toast.LENGTH_SHORT).show();
        Log.d(LOG_TAG, "update, count = " + cnt);
    }

    //save image to SD
    private void saveImage() {
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            String filename = Uri.parse(imageLink).getLastPathSegment();

            Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
            File path = new File(Environment.getExternalStorageDirectory().getPath() + "/" + "BIGDIG/test/B");
            path.mkdirs();
            File sdFile = new File(path, filename);
            try {
                FileOutputStream ostream = new FileOutputStream(sdFile);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, ostream);
                ostream.close();

                if (sdFile.exists()) {
                    Toast.makeText(this, "Image saved in " + sdFile.toString(), Toast.LENGTH_LONG).show();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else
            Toast.makeText(this, "Error! No SD card mounted", Toast.LENGTH_LONG).show();
    }

    //delete image using service
    private void deleteImage(long id) {
        Intent startServiceIntent = new Intent(this, DeleteImageService.class);
        startServiceIntent.putExtra("Id", id);
        startService(startServiceIntent);
    }

    //close app after Launcher
    private void closeApp() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(R.string.Warning);
        new CountDownTimer(11000, 1000) {
            public void onTick(long millisUntilFinished) {
                if (!isFinishing()) {
                    progressDialog.setMessage("App B is not a stand-alone application and will be closed in: "
                            + millisUntilFinished / 1000 + " sec");
                    progressDialog.show();
                }
            }
            public void onFinish() {
                progressDialog.dismiss();
                finish();
            }
        }
                .start();

    }
}
