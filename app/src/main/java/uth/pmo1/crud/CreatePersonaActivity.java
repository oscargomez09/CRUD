package uth.pmo1.crud;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class CreatePersonaActivity extends AppCompatActivity {
    Button boton_registrar, boton_cancelar, boton_tomarFoto;
    ImageView imagenPersona;
    EditText idpersona, nombre, apellido, fechaNacimiento;
    StorageReference storageReference;
    private FirebaseFirestore mfirestore;
    private FirebaseAuth mAuth;
    private static final int COD_SEL_STORAGE = 200;
    private static final int COD_SEL_IMAGE = 300;
    private Uri image_url;
    String foto = "foto";
    String idd;
    String storage_path = "Persona/*";
    LinearLayout linearLayout_image_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_persona);
        this.setTitle("Datos de Personas");

        String id = getIntent().getStringExtra("id_persona");
        mfirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        boton_registrar = findViewById(R.id.btnRegistrar);
        boton_cancelar = findViewById(R.id.btnCancelar);
        boton_tomarFoto = findViewById(R.id.btntomarFoto);
        imagenPersona = findViewById(R.id.imgFoto);

        idpersona = findViewById(R.id.txtIdPersona);
        nombre = findViewById(R.id.txtNombre);
        apellido = findViewById(R.id.txtApellido);
        fechaNacimiento = findViewById(R.id.txtFechaNacimiento);

        linearLayout_image_btn = findViewById(R.id.imagenes_btn);

        boton_tomarFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cargarPhoto();
            }
        });


        if (id==null || id==""){
            linearLayout_image_btn.setVisibility(View.GONE);

            boton_registrar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    String nombrePerson = nombre.getText().toString().trim();
                    String apellidoPerson = apellido.getText().toString().trim();
                    String fechaNacimientoPerson = fechaNacimiento.getText().toString().trim();

                    if (nombrePerson.isEmpty() && apellidoPerson.isEmpty() && fechaNacimientoPerson.isEmpty()){
                        Toast.makeText(CreatePersonaActivity.this, "Debe ingresar los datos!", Toast.LENGTH_SHORT).show();
                    }else {
                        postPersona(nombrePerson, apellidoPerson, fechaNacimientoPerson);
                    }
                }
            });
        }else{
            idd = id;
            boton_registrar.setText("Actualizar");
            getPersona(id);

            boton_registrar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    String nombrePerson = nombre.getText().toString().trim();
                    String apellidoPerson = apellido.getText().toString().trim();
                    String fechaNacimientoPerson = fechaNacimiento.getText().toString().trim();

                    if (nombrePerson.isEmpty() && apellidoPerson.isEmpty() && fechaNacimientoPerson.isEmpty()){
                        Toast.makeText(CreatePersonaActivity.this, "Debe ingresar los datos!", Toast.LENGTH_SHORT).show();
                    }else {
                        updatePersona(nombrePerson, apellidoPerson, fechaNacimientoPerson, id);
                    }
                }
            });
        }

        boton_cancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nombre.setText("");
                apellido.setText("");
                fechaNacimiento.setText("");

                Intent intent = new Intent(CreatePersonaActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }

    private void cargarPhoto() {
        Intent i = new Intent(Intent.ACTION_PICK);
        i.setType("image/*");
        startActivityForResult(i, COD_SEL_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK){
            if (requestCode == COD_SEL_IMAGE){
                image_url = data.getData();
                subirPhoto(image_url);
                //Toast.makeText(CreatePersonaActivity.this, "Cargando foto!", Toast.LENGTH_SHORT).show();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void subirPhoto(Uri image_url) {
        String rute_storage_photo = storage_path + "" + foto + "" + mAuth.getUid() +""+ idd;
        StorageReference reference = storageReference.child(rute_storage_photo);
        reference.putFile(image_url).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                while (!uriTask.isSuccessful());
                if (uriTask.isSuccessful()){
                    uriTask.addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String download_uri = uri.toString();
                            HashMap<String, Object> map = new HashMap<>();
                            map.put("foto", download_uri);
                            Picasso.get().load(download_uri).into(imagenPersona);
                            mfirestore.collection("Persona").document(idd).update(map);
                        }
                    });
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(CreatePersonaActivity.this, "Error al cargar foto", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updatePersona(String nombrePerson, String apellidoPerson, String fechaNacimientoPerson, String id) {

        Map<String, Object> map = new HashMap<>();
        map.put("nombre", nombrePerson);
        map.put("apellido", apellidoPerson);
        map.put("fechaNacimiento", fechaNacimientoPerson);

        mfirestore.collection("Persona").document(id).update(map).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(CreatePersonaActivity.this, "Actualizado exitosamente!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(CreatePersonaActivity.this, MainActivity.class));
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(CreatePersonaActivity.this, "Error al actualizar!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void postPersona(String nombrePerson, String apellidoPerson, String fechaNacimientoPerson) {

        //String idUser = mAuth.getCurrentUser().getUid();
        DocumentReference id = mfirestore.collection("Persona").document();

        Map<String, Object> map = new HashMap<>();
        //map.put("id_usuario", idUser);
        map.put("id_persona", id.getId());
        map.put("nombre", nombrePerson);
        map.put("apellido", apellidoPerson);
        map.put("fechaNacimiento", fechaNacimientoPerson);

        mfirestore.collection("Persona").document(id.getId()).set(map).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(CreatePersonaActivity.this, "Creado exitosamente!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(CreatePersonaActivity.this, MainActivity.class));
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(CreatePersonaActivity.this, "Error al ingresar los datos!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getPersona(String id){

        mfirestore.collection("Persona").document(id).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                String idP = documentSnapshot.getString("id_persona");
                String nombreP = documentSnapshot.getString("nombre");
                String apellidoP = documentSnapshot.getString("apellido");
                String fechaNacimientoP = documentSnapshot.getString("fechaNacimiento");
                String fotoP = documentSnapshot.getString("foto");

                idpersona.setText(idP);
                nombre.setText(nombreP);
                apellido.setText(apellidoP);
                fechaNacimiento.setText(fechaNacimientoP);

                Picasso.get().load(fotoP).resize(150, 150).into(imagenPersona);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(CreatePersonaActivity.this, "Error al obtener los datos!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return false;
    }

    public void abrirCalendario(View view) {

        Calendar calendar = Calendar.getInstance();
        int anio = calendar.get(Calendar.YEAR);
        int mes = calendar.get(Calendar.MONTH);
        int dia = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(CreatePersonaActivity.this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayofMonth) {
                String fecha = dayofMonth + "/" + month + "/" + year;
                fechaNacimiento.setText(fecha);
            }
        }, anio, mes, dia);
        datePickerDialog.show();
    }
}