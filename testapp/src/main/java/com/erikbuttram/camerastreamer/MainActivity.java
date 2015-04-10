package com.erikbuttram.camerastreamer;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.erikbuttram.cameralib.activities.FullscreenCameraActivity;

import java.io.File;


public class MainActivity extends ActionBarActivity {

    ImageView preview;
    private File file;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button button = (Button)findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, FullscreenCameraActivity.class);
                file = new File(getExternalCacheDir(), "temp.jpg");
                intent.putExtra(FullscreenCameraActivity.MEDIA_OUT_KEY, Uri.fromFile(file));
                startActivityForResult(intent, 1);
            }
        });

        preview = (ImageView)findViewById(R.id.preview_image);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == Activity.RESULT_CANCELED) {
            return;
        }

        if (resultCode == FullscreenCameraActivity.RESULT_MEDIA_ACTION_SUCCESS) {
            Bitmap bitmap = BitmapFactory.decodeFile(file.getPath());
            preview.setImageBitmap(bitmap);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


}
