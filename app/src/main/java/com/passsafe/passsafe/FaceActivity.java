package com.passsafe.passsafe;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.util.Log;

import android.graphics.Bitmap;
import android.os.AsyncTask;

import java.io.File;
import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import com.microsoft.projectoxford.face.FaceServiceClient;
import com.microsoft.projectoxford.face.FaceServiceRestClient;
import com.microsoft.projectoxford.face.contract.Face;
import com.microsoft.projectoxford.face.contract.VerifyResult;
import com.passsafe.passsafe.helper.ImageHelper;

public class FaceActivity extends AppCompatActivity {

    // Background task for face verification.
    private class VerificationTask extends AsyncTask<Void, String, VerifyResult> {
        // The IDs of two face to verify.
        private UUID mFaceId0;
        private UUID mFaceId1;

        VerificationTask (UUID faceId0, UUID faceId1) {
            mFaceId0 = faceId0;
            mFaceId1 = faceId1;
        }

        @Override
        protected VerifyResult doInBackground(Void... params) {
            // Get an instance of face service client to detect faces in image.
            FaceServiceClient faceServiceClient = new FaceServiceRestClient(getString(R.string.endpoint), getString(R.string.subscription_key));
            try{
                publishProgress("Verifying...");

                // Start verification.
                return faceServiceClient.verify(
                        mFaceId0,      /* The first face ID to verify */
                        mFaceId1);     /* The second face ID to verify */
            }  catch (Exception e) {
                publishProgress(e.getMessage());
                //addLog(e.getMessage());
                return null;
            }
        }

        @Override
        protected void onPreExecute() {
            progressDialog.show();
        }

        @Override
        protected void onProgressUpdate(String... progress) {
            progressDialog.setMessage(progress[0]);
        }

        @Override
        protected void onPostExecute(VerifyResult result) {
            // Deal things when verification is done.
            dealAfterVerification(result);
        }
    }

    // Background task of face detection.
    private class DetectionTask extends AsyncTask<InputStream, String, Face[]> {
        // Index indicates detecting in which of the two images.
        private int mIndex;
        private boolean mSucceed = true;

        DetectionTask(int index) {
            mIndex = index;
        }

        @Override
        protected Face[] doInBackground(InputStream... params) {
            // Get an instance of face service client to detect faces in image.
            FaceServiceClient faceServiceClient = new FaceServiceRestClient(getString(R.string.endpoint), getString(R.string.subscription_key));

            try{
                publishProgress("Detecting...");

                // Start detection.
                return faceServiceClient.detect(
                        params[0],  /* Input stream of image to detect */
                        true,       /* Whether to return face ID */
                        false,       /* Whether to return face landmarks */
                        /* Which face attributes to analyze, currently we support:
                           age,gender,headPose,smile,facialHair */
                        null);
            }  catch (Exception e) {
                mSucceed = false;
                publishProgress(e.getMessage());
                Log.v("error = ", e.getMessage());
                return null;
            }
        }

        @Override
        protected void onPreExecute() {
            progressDialog.show();
        }

        @Override
        protected void onProgressUpdate(String... progress) {
            progressDialog.setMessage(progress[0]);
        }

        @Override
        protected void onPostExecute(Face[] result) {
            // Deal things when detection is done.
            dealAfterDetection(result, mIndex, mSucceed);
        }
    }

    boolean hasPhoto;
    final int TAKE_PHOTO_REQUEST_CODE=10026;

    // The URI of photo taken with camera
    private Uri mUriPhotoTaken;

    // The IDs of the two faces to be verified.
    private UUID mFaceId0;
    private UUID mFaceId1;

    // Progress dialog popped up when communicating with server.
    ProgressDialog progressDialog;

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

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(getString(R.string.progress_dialog_title));

        UUID faceUUID = getFaceid();

        if (faceUUID == null) {
            hasPhoto = false;
        }
        else {
            hasPhoto = true;
            mFaceId0 = faceUUID;
        }

        if(!hasPhoto)
        {
            Toast.makeText(getApplicationContext(), "Take a new photo to let the app remember you",
                    Toast.LENGTH_LONG).show();
            takePhoto();
        }
        else
        {
            Toast.makeText(getApplicationContext(), "Take a photo to login",
                    Toast.LENGTH_LONG).show();
            takePhoto();
        }
    }

    // Deal things after verification
    private void dealAfterVerification(VerifyResult result) {
        progressDialog.dismiss();

        // Verification result.
        if (result != null) {
            DecimalFormat formatter = new DecimalFormat("#0.00");
            String verificationResult = (result.isIdentical ? "The same person": "Different persons")
                    + ". The confidence is " + formatter.format(result.confidence);
            Log.v("verify result = ", verificationResult);

            if (result.isIdentical) {
                Toast.makeText(getApplicationContext(), "Verify successfully",
                        Toast.LENGTH_LONG).show();

                Intent intent = new Intent(FaceActivity.this, MainActivity.class);
                startActivity(intent);
            }
            else {
                Toast.makeText(getApplicationContext(), "Different persons. Try again.",
                        Toast.LENGTH_LONG).show();
                takePhoto();
            }
        }
    }

    // Deal things after detection
    private void dealAfterDetection(Face[] result, int index, boolean succeed) {
        progressDialog.dismiss();

        if (succeed) {
            if (result != null) {
                Log.v("result", result.length + " face" + (result.length != 1 ? "s": "")  + " detected");

                List<Face> faces;
                faces = Arrays.asList(result);

                // Set the default face ID to the ID of first face, if one or more faces are detected.
                if (faces.size() != 0) {
                    if (index == 0) {
                        mFaceId0 = faces.get(0).faceId;

                        saveFaceid(mFaceId0);

                        Toast.makeText(getApplicationContext(), "Face detected and saved.",
                                Toast.LENGTH_LONG).show();

                        Intent intent = new Intent(FaceActivity.this, MainActivity.class);
                        startActivity(intent);
                    }
                    else {
                        mFaceId1 = faces.get(0).faceId;

                        // Verify faces
                        new VerificationTask(mFaceId0, mFaceId1).execute();
                    }
                }
            }
        }

        if (result != null && result.length == 0) {
            Log.v("result", "No face detected!");
            Toast.makeText(getApplicationContext(), "No Face detected. Try again.",
                    Toast.LENGTH_LONG).show();
            takePhoto();
        }
    }

    // Start detecting in image
    private void detect(Bitmap bitmap, int index) {
        // Put the image into an input stream for detection.
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(output.toByteArray());

        // Detect faces in the image.
        new DetectionTask(index).execute(inputStream);
    }

    // Take photo
    private void takePhoto()
    {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra("android.intent.extras.CAMERA_FACING", 1);
        if(intent.resolveActivity(getPackageManager()) != null) {
            // Save the photo taken to a temporary file.
            File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            try {
                File file = File.createTempFile("IMG_", ".jpg", storageDir);
                mUriPhotoTaken = Uri.fromFile(file);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, mUriPhotoTaken);
                startActivityForResult(intent,TAKE_PHOTO_REQUEST_CODE);
            } catch (IOException e) {
                Log.v("error = ", e.getMessage());
            }
        }
    }

    // Save the activity state when it's going to stop.
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("ImageUri", mUriPhotoTaken);
    }

    // Recover the saved state when the activity is recreated.
    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mUriPhotoTaken = savedInstanceState.getParcelable("ImageUri");
    }

    // After taking photo
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case TAKE_PHOTO_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    Uri imageUri;
                    if (data == null || data.getData() == null) {
                        imageUri = mUriPhotoTaken;
                    } else {
                        imageUri = data.getData();
                    }

                    // If image is selected successfully, set the image URI and bitmap.
                    Bitmap bitmap = ImageHelper.loadSizeLimitedBitmapFromUri(imageUri, getContentResolver());
                    if (bitmap != null) {
                        // Start detecting in image.
                        detect(bitmap, hasPhoto ? 1 : 0);
                    }
                }
                break;
            default:
                finish();
        }
    }

    // Save faceid
    private void saveFaceid(UUID faceid){
        SharedPreferences mSharedPreferences = getSharedPreferences("Login", MODE_PRIVATE);
        SharedPreferences.Editor mEditor = mSharedPreferences.edit();
        mEditor.putString("faceid", faceid.toString());
        mEditor.commit();
    }

    // Get faceid
    private UUID getFaceid() {
        SharedPreferences mSharedPreferences = getSharedPreferences("Login", MODE_PRIVATE);
        SharedPreferences.Editor mEditor = mSharedPreferences.edit();
        String faceid = mSharedPreferences.getString("faceid", "0");

        if (faceid.equals("0")) {
            return null;
        }

        UUID faceUUID = UUID.fromString(faceid);

        return faceUUID;
    }
}