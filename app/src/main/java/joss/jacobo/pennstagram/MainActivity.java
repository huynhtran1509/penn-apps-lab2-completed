package joss.jacobo.pennstagram;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
    private static final String STATE_PICTURE_TAKEN_BITMAP = "state_picture_taken_bitmap";
    private static final String STATE_PICTURE_URI = "state_picture_uri";
    private static final String IMAGE_NAME = "pennstagram_captured_image";

    private Uri pictureFileUri;
    private Bitmap pictureTakenBitmap;

    private TextView pictureText;
    private ImageView platyImageView;
    private FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        pictureText = (TextView) findViewById(R.id.text);
        platyImageView = (ImageView) findViewById(R.id.platy);

        // Set FAB's onClickListener
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (pictureTakenBitmap == null) {
                    startImageCapture();
                } else {
                    shareImage();
                }
            }
        });

        // Set ImageView's onClickListener
        platyImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startImageCapture();
            }
        });

        // If the savedInstanceState bundle is not null, it means we saved data in it.
        // Grab the data out and restore the state (i.e. Text, Image, and Button Icon)
        if (savedInstanceState != null) {
            pictureTakenBitmap = savedInstanceState.getParcelable(STATE_PICTURE_TAKEN_BITMAP);
            pictureFileUri = savedInstanceState.getParcelable(STATE_PICTURE_URI);
            setContent();
        }

    }

    /**
     * Set the screen's content depending on whether the user has already taken a picture or not.
     */
    private void setContent() {
        if (pictureTakenBitmap != null) {
            pictureText.setText(R.string.retake_picture);
            platyImageView.setImageBitmap(pictureTakenBitmap);
            fab.setImageResource(android.R.drawable.ic_menu_share);
        } else {
            pictureText.setText(R.string.take_picture);
            platyImageView.setImageResource(R.drawable.platy);
            fab.setImageResource(android.R.drawable.ic_menu_camera);
        }

    }

    private void startImageCapture() {
        // create Intent to take a picture and return control to the calling application
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        pictureFileUri = getOutputMediaFileUri(this, IMAGE_NAME); // create a file to save the image
        if (pictureFileUri != null) {
            intent.putExtra(MediaStore.EXTRA_OUTPUT, pictureFileUri); // set the image file name

            // start the image capture Intent
            startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
        } else {
            Toast.makeText(this, "There was an error creating the file to save the image to", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // Image captured and saved to pictureFileUri specified in the Intent
                File image = new File(pictureFileUri.getPath());
                if (image.exists()) {
                    pictureTakenBitmap = BitmapFactory.decodeFile(image.getAbsolutePath());
                    setContent();
                }
            } else if (resultCode == RESULT_CANCELED) {
                // User cancelled the image capture
                // Image captured and saved to pictureFileUri specified in the Intent
            } else {
                // Image capture failed, advise user
            }
        }
    }

    private void shareImage() {
        // Create Share Intent
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("image/jpeg");
        shareIntent.putExtra(Intent.EXTRA_STREAM, pictureFileUri);
        startActivity(shareIntent);
    }

    @Nullable
    private Uri getOutputMediaFileUri(Context context, String name) {
        // Get external storage directory
        File filesDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        // Get or Create image folder
        File imagesDir = new File(filesDir, "images");
        if (imagesDir.mkdir() || imagesDir.isDirectory()) {
            // If images folder exists create an image file to save our picture taken
            // and return it's Uri.
            return Uri.fromFile(new File(imagesDir, name.replace("/", "_") + ".jpeg"));
        }
        return null;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save image bitmap and it's uri when activity gets destroyed
        // (i.e. on screen rotation)
        outState.putParcelable(STATE_PICTURE_TAKEN_BITMAP, pictureTakenBitmap);
        outState.putParcelable(STATE_PICTURE_URI, pictureFileUri);
    }
}
