package com.devlomi.hashingexample;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class FireUtil {

    public static void checkIfFileExists(String md5, final OnFinish onFinish) {
        if (md5 == null && onFinish != null) onFinish.onNotFound();

        FirebaseDatabase.getInstance().getReference().child("hash").child(md5).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //file not exists
                if (dataSnapshot.getValue() == null) {
                    if (onFinish != null) {
                        onFinish.onNotFound();
                    }
                } else {
                    //file exists
                    if (onFinish != null) {
                        //file path on Firebase Storage
                        String path = dataSnapshot.getValue(String.class);
                        onFinish.onFound(path);
                    }
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public interface OnFinish {
        void onFound(String path);

        void onNotFound();
    }
}
