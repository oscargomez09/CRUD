package uth.pmo1.crud;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import uth.pmo1.crud.Adapter.PersonaAdapter;
import uth.pmo1.crud.Model.Persona;

public class MainActivity extends AppCompatActivity {

    Button boton_agregar;
    RecyclerView mRecycler;
    PersonaAdapter mAdapter;
    FirebaseFirestore mFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFirestore = FirebaseFirestore.getInstance();
        mRecycler = findViewById(R.id.recyclerViewPersonas);
        mRecycler.setLayoutManager(new LinearLayoutManager(this));
        Query query = mFirestore.collection("Persona");
        FirestoreRecyclerOptions<Persona> firestoreRecyclerOptions =
                new FirestoreRecyclerOptions.Builder<Persona>().setQuery(query, Persona.class).build();

        mAdapter = new PersonaAdapter(firestoreRecyclerOptions, this);
        mAdapter.notifyDataSetChanged();
        mRecycler.setAdapter(mAdapter);

        boton_agregar = findViewById(R.id.btnAgregar);

        boton_agregar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               Intent intent = new Intent(MainActivity.this, CreatePersonaActivity.class);
               startActivity(intent);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAdapter.stopListening();
    }
}