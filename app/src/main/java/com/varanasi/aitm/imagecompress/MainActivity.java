package com.varanasi.aitm.imagecompress;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements PickImage.PickImageListener {
    private static final int CAMERA_REQUEST = 100;
    private static final int GALLERY_REQUEST = 101;
    private static final String TAG = "DIVYPRAKASH";
    Button selectimage;
    Bitmap photo;
    boolean mGranted;
    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        selectimage = findViewById(R.id.selectImage);
        imageView = findViewById(R.id.imageView);
        selectimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickImage();
            }
        });
    }

    public void pickImage() {
        PickImage pickImage = new PickImage();
        pickImage.show(getSupportFragmentManager(), "Pick Image");
    }

    @Override
    public void pickImage(String selectfrom) {
        if (selectfrom.equals("Camera")) {
            camera();
        } else if (selectfrom.equals("Gallery")) {
            gallery();
        } else {
            Toast.makeText(MainActivity.this, "Try Again", Toast.LENGTH_SHORT).show();
        }
    }

    private void gallery() {
        checkStoragePermission();
/*        Intent intent = new Intent();
        intent.setType("image/*");
        // used to filter an image
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select an image"), GALLERY_REQUEST);*/
        Intent i = new Intent(
                Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, GALLERY_REQUEST);
    }

    private void camera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, CAMERA_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            photo = (Bitmap) data.getExtras().get("data");
            Bitmap converetdImage = getResizedBitmap(photo, 500);
            imageView.setImageBitmap(converetdImage);
            File pictureFile = getOutputMediaFile();
            if (pictureFile == null) {
                Log.d(TAG,
                        "Error creating media file, check storage permissions: ");// e.getMessage());
                return;
            }
            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                  converetdImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
                fos.close();
            } catch (FileNotFoundException e) {
                Log.d(TAG, "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d(TAG, "Error accessing file: " + e.getMessage());
            }
            Toast.makeText(this, pictureFile.getAbsolutePath().toString(), Toast.LENGTH_SHORT).show();
        } else if (requestCode == GALLERY_REQUEST && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};
            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();
            photo = BitmapFactory.decodeFile(picturePath);
            Bitmap converetdImage = getResizedBitmap(photo, 500);
            imageView.setImageBitmap(converetdImage);
            File pictureFile = getOutputMediaFile();
            if (pictureFile == null) {
                Log.d(TAG,
                        "Error creating media file, check storage permissions: ");// e.getMessage());
                return;
            }
            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                 converetdImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
                fos.close();
            } catch (FileNotFoundException e) {
                Log.d(TAG, "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d(TAG, "Error accessing file: " + e.getMessage());
            }
            Toast.makeText(this, pictureFile.getAbsolutePath().toString(), Toast.LENGTH_SHORT).show();
        }

    }

    /**
     * Create a File for saving an image or video
     */
    private File getOutputMediaFile() {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory()
                + "/Android/Image/"
                + "/Files");

        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }
        // Create a media file name
        String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmm").format(new Date());
        File mediaFile;
        String mImageName = "MI_" + timeStamp + ".jpg";
        mediaFile = new File(mediaStorageDir.getPath() + File.separator + mImageName);
        return mediaFile;
    }


    public Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float) width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }


    public void checkStoragePermission() {

        String externalReadPermission = Manifest.permission.READ_EXTERNAL_STORAGE.toString();
        String externalWritePermission = Manifest.permission.WRITE_EXTERNAL_STORAGE.toString();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (!mGranted) {
                if (ActivityCompat.checkSelfPermission(this, externalReadPermission) != PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(this, externalWritePermission) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{externalReadPermission, externalWritePermission}, 200);
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 200 && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
                mGranted = true;
            } else {
                Toast.makeText(this, "Please allow the permission to read data", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
