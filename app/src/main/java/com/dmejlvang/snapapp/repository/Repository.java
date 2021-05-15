package com.dmejlvang.snapapp.repository;

import android.graphics.Bitmap;

import com.dmejlvang.snapapp.TaskListener;
import com.dmejlvang.snapapp.Updatable;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Repository {
    private Updatable activity;

    private static final Repository repository = new Repository();
    private final String COLLECTION_PATH = "snaps";
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseStorage storage = FirebaseStorage.getInstance();

    public static Repository r() {
        return repository;
    }

    public List<String> snaps = new ArrayList<>();

    public void setup(Updatable a, List<String> snaps) {
        activity = a;
        this.snaps = snaps;
        startListener();
    }

    //IMPORTANT METHOD
    //get updated data from database and handle each document data as a whole separately
    //method listens to changes in Firestore collection
    public void startListener() {
        db.collection(COLLECTION_PATH).addSnapshotListener(((value, error) -> {
            snaps.clear();
            for (DocumentSnapshot snap : value.getDocuments()) {
                snaps.add(snap.getId());
            }
            activity.update(null);
        }));
    }

    //upload bitmap to Firebase
    public void uploadBitmap(Bitmap bitmap, String imageText) {
        //a document in Firestore is created to keep track of ID for bitmap
        DocumentReference doc = db.collection(COLLECTION_PATH).document();
        Map<String, String> map = new HashMap<>();
        map.put("text", imageText);
        doc.set(map);

        //bitmap is uploaded to Storage in folder "snaps" with name = document ID from Firestore
        StorageReference ref = storage.getReference(COLLECTION_PATH + "/" + doc.getId());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        ref.putBytes(baos.toByteArray()).addOnCompleteListener(snap -> {
            System.out.println("OK to upload: " + snap);
        }).addOnFailureListener(exception -> {
            System.out.println("Failure to upload: " + exception);
        });
    }

    //download bitmap from Storage and call Tasklistener receive() with downloaded bytes
    public void downloadBitmap(String id, TaskListener taskListener){
        StorageReference ref = storage.getReference(COLLECTION_PATH + "/" + id);
        int max = 1024 * 1024;
        ref.getBytes(max).addOnSuccessListener(taskListener::receive)
                         .addOnFailureListener(ex -> System.out.println("Error in download " + ex));
    }

    //delete document in Firestore and picture from Storage
    public void deleteSnap(String id){
        db.collection(COLLECTION_PATH).document(id).delete();
        storage.getReference(COLLECTION_PATH + "/" + id).delete();
    }
}
