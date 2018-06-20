package com.github.n1try.quiznerd.service;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class FirestoreService {
    public static final String COLL_MATCHES = "matches";

    private static final String TAG = "FirestoreService";
    private static final FirestoreService ourInstance = new FirestoreService();

    private FirebaseFirestore mFirestore;

    public static FirestoreService getInstance() {
        return ourInstance;
    }

    private FirestoreService() {
        mFirestore = FirebaseFirestore.getInstance();
    }

    public void fetchMatches() {
        mFirestore.collection(COLL_MATCHES).get().addOnCompleteListener(new OnMatchesFetchedListener());
    }


    private class OnMatchesFetchedListener implements OnCompleteListener<QuerySnapshot> {
        @Override
        public void onComplete(@NonNull Task<QuerySnapshot> task) {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot doc : task.getResult()) {
                    System.out.println(doc.getId());
                }
            } else {
                Log.w(TAG, task.getException());
            }
        }
    }
}
