package com.example.amendez.lab1myapplication;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Lab1MainActivity extends AppCompatActivity {
    List<User> model=new ArrayList<User>();
    UserAdapter adapter=null;
    private static final int IMAGE_CAPTURE = 101;
    private static final int REQUEST_CODE = 1234;
    Button startButton;
    Button photoButton;
    EditText name,email;
    TextView speechTextView;
    Dialog matchTextDialog;
    ListView textListView,list;
    ArrayList<String> matchesText;
    public  static String perfil = "";
    public  static Uri imageUri;
    RadioButton  takeOut;
    //Provee La instancia de la base de datos
    //En el constructor recibe un string indicando el recurso
    //Que queremos obtener para este ejemplo se usa el de Users
    FirebaseServicesProvider dataProvider;
    //Mediante el value listener nuestra aplicacion
    //puede detectar eventos en los cuales se actualizan los datos
    ValueEventListener DataListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_list_main);
        Button save=(Button) findViewById(R.id.save);
        list=(ListView)findViewById(R.id.restaurants);
        adapter=new UserAdapter();
        list.setAdapter(adapter);
        list.setOnItemClickListener(onListClick);

        photoButton = (Button) findViewById(R.id.photoButton);
        email=(EditText)findViewById(R.id.email);
        name=(EditText)findViewById(R.id.name);
        //Inicializacion de la referencia al la base de datos
        dataProvider=new FirebaseServicesProvider("Users");
        //Esta clase es reutilizable solo se debe indicar en el contructor
        //el nombre del elemento de la bd que queremos referenciar

        email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isConnected()){
                    Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                    startActivityForResult(intent, REQUEST_CODE);
                } else {
                    Toast.makeText(Lab1MainActivity.this, "", Toast.LENGTH_SHORT).show();
                }
            }
        });
        photoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startRecording(view);
            }
        });
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                User r=new User();

                r.setName(name.getText().toString());
                r.setEmail(email.getText().toString());

                dataProvider.writeNewUser(r);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        /*
        * Mediante el userListener nuestra aplicacion puede
        * detectar cambios en tiempo real y actualizarlos en la
        * interfaz
        *
        * */
        ValueEventListener userListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Un datasnapshot es como un pantallazo de los datos
                // es decir recibimos por parametro todos los registros
                // de la referencia a la que enlazamos nuestro userListener en
                //un determinado tiempo
                if (dataSnapshot.exists()) {


                    Log.e("SNAPSHOT", "onDataChange: Message data is updated: ");

                    //Limpiamos la lista de usuarios
                    model.clear();


                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        //Recorremos el snapshot y guardamos los datos en un objeto
                        User user = postSnapshot.getValue(User.class);

                        //adding artist to the list
                        model.add(user);
                    }

                    //creating adapter
                    UserAdapter userAdapter = new UserAdapter();
                    //attaching adapter to the listview
                    list.setAdapter(userAdapter);

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

                Log.e("onCancled", "onCancelled: Failed to read message");


            }


        };
        //Asignamos este listener a nuestra referencia de usuarios
        dataProvider.getReference().addValueEventListener(userListener);

        DataListener=userListener;
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (DataListener != null) {
            dataProvider.getReference().removeEventListener(DataListener);
        }
    }

    private AdapterView.OnItemClickListener onListClick = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            String url = "http://www.nacion.com";
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
        }
    };

    class UserAdapter extends ArrayAdapter<User> {

        UserAdapter() {
            super(Lab1MainActivity.this, R.layout.row, model);
        }
        public View getView(int position, View convertView, ViewGroup parent){
            View row=convertView;
            UserHolder holder=null;
            if(row==null){
                LayoutInflater inflater=getLayoutInflater();
                row=inflater.inflate(R.layout.row, parent,false);
                holder=new UserHolder(row);
                row.setTag(holder);
            }
            else{
                holder=(UserHolder) row.getTag();
            }
            holder.populateFrom(model.get(position));
            //Hay que modificar el model
            return (row);
        }
    }

    static class UserHolder{
        private TextView name=null;
        private TextView address=null;
        UserHolder(View row){
            name=(TextView)row.findViewById(R.id.title);
            address=(TextView)row.findViewById(R.id.address);

        }
        void populateFrom(User r){
            name.setText(String.format("Nombre:%s", r.getName()));
            address.setText(String.format("Email:%s",r.getEmail()));
            /*if(r.getType().equals("sit_down")){
                icon.setImageResource(R.drawable.ball_red);
            }
            else if(r.getType().equals("take_out")){
                //icon.setImageResource(R.drawable.ball_yellow);
                icon.setImageURI(imageUri);
            }
            else{
                icon.setImageResource(R.drawable.ball_green);
            }*/
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK){
            matchTextDialog = new Dialog(Lab1MainActivity.this); //Create a Dialog
            matchTextDialog.setContentView(R.layout.dialog_matches_frag); //Link the new Dialog with the dialog_matches frag
            matchTextDialog.setTitle("Select Matching Text"); //Add title to the Dialog
            textListView = (ListView) matchTextDialog.findViewById(R.id.listView1);
            matchesText = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS); //Get data of data
            perfil =  matchesText.get(0);
            email.setText(perfil);
        }



        if (requestCode == IMAGE_CAPTURE) {
            if (resultCode == RESULT_OK) {

                try {
                    dataProvider=new FirebaseServicesProvider("users");
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                    dataProvider.uploadPhotoToDeal(bitmap,name.getText().toString(),getApplicationContext());

                } catch (IOException e) {
                    e.printStackTrace();
                }
                Toast.makeText(this, "Video saved to:\n" +
                        imageUri, Toast.LENGTH_LONG).show();
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Video recording cancelled.",
                        Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Failed to record video",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     To Check if the net is available and conected
     * @return true if the net is available and conected
     * and false in other case
     */
    public boolean isConnected(){
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo net = cm.getActiveNetworkInfo();
        if (net!= null && net.isAvailable() && net.isConnected()){
            return true;
        }   else {
            return false;
        }
    }


    private static final int VIDEO_CAPTURE = 101;

    public void startRecording(View view)
    {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, IMAGE_CAPTURE);
    }


    private boolean hasCamera() {
        return (getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_CAMERA_ANY));
    }



}
