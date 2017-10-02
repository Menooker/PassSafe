package com.passsafe.passsafe;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

public class FaceActivity extends AppCompatActivity {
    boolean haspic;
    final int TAKE_PHOTO_REQUEST_CODE=10026;


    void takepic(File tempFile)
    {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra("android.intent.extras.CAMERA_FACING", 1);
        Uri imageUri = FileProvider.getUriForFile(this, "com.passsafe.passsafe.fileProvider", tempFile);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        /*File tempFile = new File(this.getFilesDir(), "owner.jpg");
        try {
            tempFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }*/
        //Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE, FileProvider.getUriForFile(this, "auth", tempFile));
        //intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        //startActivity(i);
        //Uri imageUri = Uri.fromFile(tempFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, TAKE_PHOTO_REQUEST_CODE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face);
        Button but=(Button)findViewById(R.id.but_face_enter);
        but.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(FaceActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        SharedPreferences pref = getSharedPreferences("Login",MODE_PRIVATE);
        haspic = pref.getBoolean("haspic",false);
        if(!haspic)
        {
            Toast.makeText(getApplicationContext(), "Take a new photo to let the app remember you",
                    Toast.LENGTH_LONG).show();
            File tempFile = new File(getFilesDir(), "owner.jpg");
            takepic(tempFile);
        }
        else
        {
            File tempFile = new File(getCacheDir(), "temp.jpg");
            takepic(tempFile);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case TAKE_PHOTO_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    if(haspic)
                    {
                        //verify the photo
                        // file 1 : new File(this.getFilesDir(), "owner.jpg").getPath()
                        // file 2 : new File(this.getCacheDir(), "temp.jpg".getPath()
                        Intent intent = new Intent(FaceActivity.this, MainActivity.class);
                        startActivity(intent);
                    }
                    else
                    {
                        SharedPreferences pref = getSharedPreferences("Login",MODE_PRIVATE);
                        pref.edit().putBoolean("haspic",true).commit();
                        Intent intent = new Intent(FaceActivity.this, MainActivity.class);
                        startActivity(intent);
                    }
                }
                break;
            default:
                finish();
        }
    }

}
