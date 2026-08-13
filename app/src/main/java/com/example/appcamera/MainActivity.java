package com.example.appcamera;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.material.button.MaterialButton;
import androidx.cardview.widget.CardView;
import android.widget.ImageView;
import android.widget.VideoView;
import android.widget.FrameLayout;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    private static final int REQ_CAM = 100;
    private static final int REQ_WRITE = 101;
    private static final int REQ_CAPTURE_PHOTO = 1;
    private static final int REQ_CAPTURE_VIDEO = 2;

    private ImageView imageView;
    private VideoView videoView;
    private Uri photoUri;
    private Uri videoUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        imageView  = findViewById(R.id.imageView);
        videoView  = findViewById(R.id.videoView);
        MaterialButton btnPhoto = findViewById(R.id.btnCapturePhoto);
        MaterialButton btnVideo = findViewById(R.id.btnRecordVideo);

        btnPhoto.setOnClickListener(v -> checkPermissionsAndOpen(true));
        btnVideo.setOnClickListener(v -> checkPermissionsAndOpen(false));
    }

    private void checkPermissionsAndOpen(boolean isPhoto) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this, new String[]{Manifest.permission.CAMERA}, REQ_CAM);
            return;
        }
        if (!isPhoto && ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this, new String[]{Manifest.permission.RECORD_AUDIO}, REQ_CAM);
            return;
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQ_WRITE);
            return;
        }
        if (isPhoto) openCamera();
        else        openVideoRecorder();
    }

    private void openCamera() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentValues vals = new ContentValues();
            vals.put(MediaStore.Images.Media.DISPLAY_NAME,
                    System.currentTimeMillis() + ".jpg");
            vals.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
            vals.put(MediaStore.Images.Media.RELATIVE_PATH,
                    Environment.DIRECTORY_PICTURES + "/AppCamera");
            photoUri = getContentResolver()
                    .insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, vals);
        } else {
            File pics = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES);
            File dir = new File(pics, "AppCamera");
            if (!dir.exists()) dir.mkdirs();
            File file = new File(dir, System.currentTimeMillis() + ".jpg");
            photoUri = Uri.fromFile(file);
        }

        Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        i.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
        startActivityForResult(i, REQ_CAPTURE_PHOTO);
    }

    private void openVideoRecorder() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentValues vals = new ContentValues();
            vals.put(MediaStore.Video.Media.DISPLAY_NAME,
                    System.currentTimeMillis() + ".mp4");
            vals.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4");
            vals.put(MediaStore.Video.Media.RELATIVE_PATH,
                    Environment.DIRECTORY_MOVIES + "/AppCamera");
            videoUri = getContentResolver()
                    .insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, vals);
        } else {
            File mov = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_MOVIES);
            File dir = new File(mov, "AppCamera");
            if (!dir.exists()) dir.mkdirs();
            File file = new File(dir, System.currentTimeMillis() + ".mp4");
            videoUri = Uri.fromFile(file);
        }

        Intent i = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        i.putExtra(MediaStore.EXTRA_OUTPUT, videoUri);
        i.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
        i.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 60);
        startActivityForResult(i, REQ_CAPTURE_VIDEO);
    }

    @Override
    public void onRequestPermissionsResult(int code,
                                           @NonNull String[] perms,
                                           @NonNull int[] grants) {
        super.onRequestPermissionsResult(code, perms, grants);
        if ((code == REQ_CAM || code == REQ_WRITE)
                && grants.length > 0
                && grants[0] == PackageManager.PERMISSION_GRANTED) {
            openCamera();
        } else {
            Toast.makeText(this, "Permissão negada",
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int req, int res, @Nullable Intent d) {
        super.onActivityResult(req, res, d);
        if (res != RESULT_OK) return;

        if (req == REQ_CAPTURE_PHOTO) {
            videoView.setVisibility(VideoView.GONE);
            imageView.setVisibility(ImageView.VISIBLE);
            imageView.setImageURI(photoUri);
            Toast.makeText(this, "Foto salva!", Toast.LENGTH_SHORT).show();

        } else if (req == REQ_CAPTURE_VIDEO) {
            imageView.setVisibility(ImageView.GONE);
            videoView.setVisibility(VideoView.VISIBLE);
            videoView.setVideoURI(videoUri);
            videoView.start();
            Toast.makeText(this, "Vídeo salvo!", Toast.LENGTH_SHORT).show();
        }
    }
}
