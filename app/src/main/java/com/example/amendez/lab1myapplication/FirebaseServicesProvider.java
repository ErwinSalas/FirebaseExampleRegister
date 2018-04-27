package com.example.amendez.lab1myapplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

/**
 * Created by root on 4/12/18.
 */

public class FirebaseServicesProvider {

    private DatabaseReference Database;
    private DatabaseReference Reference;
    private StorageReference StorageReference;


    public FirebaseServicesProvider(String resourse) {
        //Recibe como parametro un string con el nombre del recurso que
        //y lo añade a la referencia de la bd
        this.StorageReference = FirebaseStorage.getInstance().getReference();
        this.Database = FirebaseDatabase.getInstance().getReference();
        this.Reference =  FirebaseDatabase.getInstance().getReference(resourse);
    }
    public void writeNewUser(User user) {
        /*
        * Para añadir un nuevo usuario debemos generar
         * un id para agregarlo al schema de usuarios
        *
        * */
        String id  = Reference.push().getKey();
        //Añade un objeto con los atributos del
        //usuario
        Reference.child(id).setValue(user);
    }

    public StorageReference getStorageReference() {
        return StorageReference;
    }

    public void setStorageReference(StorageReference storageReference) {
        StorageReference = storageReference;
    }

    public DatabaseReference getDatabase() {
        return Database;
    }

    public void setDatabase(DatabaseReference database) {
        Database = database;
    }

    public DatabaseReference getReference() {
        return Reference;
    }

    public void setReference(DatabaseReference reference) {
        Reference = reference;
    }


    public void uploadPhotoToDeal(Bitmap photo, String dealId, final Context ctx) {

        final String idImage = dealId.concat(".jpg");

        final StorageReference imgRef = this.StorageReference.child("image/" + idImage);

        final byte[] data = getImageCompressed(photo);

        UploadTask uploadTask = imgRef.putBytes(data);

        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                HashMap<String, Object> uriChildren = new HashMap<String, Object>();
                uriChildren.put("photo", idImage);

                CharSequence text = "Carga Exitosa";
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(ctx, text, duration);
                toast.show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }

    private byte[] getImageCompressed(Bitmap photo) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        photo.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        return baos.toByteArray();
    }

}
