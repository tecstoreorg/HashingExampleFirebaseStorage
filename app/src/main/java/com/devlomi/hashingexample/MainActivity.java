package com.devlomi.hashingexample;


import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    FirebaseStorage firebaseStorage;
    StorageReference mainRef;
    FirebaseDatabase database;


    public static final int PICK_IMG_REQUEST = 6541;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        firebaseStorage = FirebaseStorage.getInstance();
        mainRef = firebaseStorage.getReference();
        database = FirebaseDatabase.getInstance();


        Button button = findViewById(R.id.upload_btn);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, PICK_IMG_REQUEST);
            }
        });
    }

    @Override
    protected void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);


        if (resultCode == RESULT_OK) {
            final Uri imageUri = data.getData();
            Log.d("3llomi", "IMAGE URI is " + imageUri);
            File imageFile = new File(getPath(imageUri));
            Log.d("3llomi", "image file path is " + imageFile.getPath());
            final String md5 = MD5Util.calculateMD5(imageFile);



            FireUtil.checkIfFileExists(md5, new FireUtil.OnFinish() {
                @Override
                public void onFound(String path) {
                    Toast.makeText(MainActivity.this, "image is already uploaded, here is the path.. "+path, Toast.LENGTH_LONG).show();
                }

                @Override
                public void onNotFound() {
                    //file not found ,upload it
                    upload(imageUri, md5);

                }
            });


        } else {
            Toast.makeText(this, "no image selected :/", Toast.LENGTH_SHORT).show();
        }
    }

    private void upload(Uri uri, final String md5) {
        final String imageName = UUID.randomUUID().toString() + ".jpg";

        mainRef.child(imageName).putFile(uri)
                .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@android.support.annotation.NonNull Task<UploadTask.TaskSnapshot> task) {

                        if (task.isSuccessful()) {
                            String path = task.getResult().getStorage().getPath();
                            database.getReference().child("hash").child(md5).setValue(path);
                            Toast.makeText(MainActivity.this, "Uplaod Succeed", Toast.LENGTH_SHORT).show();

                        } else {
                            Log.d("3llomi", "upload Failed " + task.getException().getLocalizedMessage());
                            Toast.makeText(MainActivity.this, "Uplaod Failed :( " + task.getException().getLocalizedMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    public String getPath(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor == null) return null;
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String s = cursor.getString(column_index);
        cursor.close();
        return s;
    }
}
