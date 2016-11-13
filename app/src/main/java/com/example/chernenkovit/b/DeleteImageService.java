package com.example.chernenkovit.b;

import android.app.Service;
import android.content.ContentUris;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.widget.Toast;

import java.util.concurrent.TimeUnit;

import static com.example.chernenkovit.b.data.ImageContract.IMAGES_CONTENT_URI;

/** Image delete service. */
public class DeleteImageService extends Service {
    private Handler handler;
    long id;

    public DeleteImageService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        handler = new Handler();
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        id = intent.getLongExtra("Id", 0);
        deleteImage(id);
        stopSelf();
        return Service.START_STICKY;
    }

    //delete downloaded image with 15 sec delay
    public void deleteImage(final long id) {
        new Thread(new Runnable() {
            public void run() {
                try {
                    TimeUnit.SECONDS.sleep(15);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Uri uri = ContentUris.withAppendedId(IMAGES_CONTENT_URI, id);
                getContentResolver().delete(uri, null, null);
                message();
            }
        }).start();
    }

    private void message() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), "Image was deleted", Toast.LENGTH_LONG).show();
            }
        });
    }
}


