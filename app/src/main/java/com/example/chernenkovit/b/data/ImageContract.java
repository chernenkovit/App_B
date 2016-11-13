package com.example.chernenkovit.b.data;


import android.net.Uri;

/** Contract for using Content Provider. */
public class ImageContract {
    private static final String AUTHORITY = "com.example.chernenkovit.providers.media";
    private static final String IMAGE_PATH = "images";
    public static final Uri IMAGES_CONTENT_URI = Uri.parse("content://"
            + AUTHORITY + "/" + IMAGE_PATH);

    public static final String IMAGES_TABLE_NAME = "images";
    public static final String IMAGES_ID_COLUMN = "_id";
    public static final String IMAGES_LINK_COLUMN = "link";
    public static final String IMAGES_STATUS_COLUMN = "status";
    public static final String IMAGES_TIMESTAMP_COLUMN = "timestamp";
}