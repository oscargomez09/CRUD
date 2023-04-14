package uth.pmo1.crud.Adapter;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import uth.pmo1.crud.CreatePersonaActivity;
import uth.pmo1.crud.Model.Persona;
import uth.pmo1.crud.R;

public class PersonaAdapter extends FirestoreRecyclerAdapter<Persona, PersonaAdapter.ViewHolder> {

    private FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();
    Activity activity;
    /**
     * Create a new RecyclerView adapter that listens to a Firestore Query.  See {@link
     * FirestoreRecyclerOptions} for configuration options.
     *
     * @param options
     */
    public PersonaAdapter(@NonNull FirestoreRecyclerOptions<Persona> options, Activity activity) {
        super(options);
        this.activity = activity;
    }

    @Override
    protected void onBindViewHolder(@NonNull ViewHolder viewHolder, int i, @NonNull Persona persona) {

        DocumentSnapshot documentSnapshot = getSnapshots().getSnapshot(viewHolder.getAdapterPosition());
        final String id = documentSnapshot.getId();

        viewHolder.id_persona.setText(persona.getId_persona());
        viewHolder.nombre.setText(persona.getNombre());
        viewHolder.apellido.setText(persona.getApellido());
        viewHolder.fechaNacimiento.setText(persona.getFechaNacimiento());
        String foto = persona.getFoto();
        Picasso.get().load(foto).into(viewHolder.imagenL);

        viewHolder.boton_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(activity, CreatePersonaActivity.class);
                i.putExtra("id_persona", id);
                activity.startActivity(i);
            }
        });

        viewHolder.boton_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deletePersona(id);
            }
        });

    }

    private void deletePersona(String id) {

        mFirestore.collection("Persona").document(id).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(activity, "Eliminado correctamente", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(activity, "Error al eliminar", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_persona_lista, parent,false);
        return new ViewHolder(v);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView id_persona, nombre, apellido, fechaNacimiento;
        ImageView boton_delete, boton_edit, imagenL;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            id_persona = itemView.findViewById(R.id.txtIdPersonLista);
            nombre = itemView.findViewById(R.id.txtNombreLista);
            apellido = itemView.findViewById(R.id.txtApellidoLista);
            fechaNacimiento = itemView.findViewById(R.id.txtFechaLista);
            boton_delete = itemView.findViewById(R.id.btnEliminar);
            boton_edit = itemView.findViewById(R.id.btnEditar);
            imagenL = itemView.findViewById(R.id.imgFotoLista);

        }
    }
}
