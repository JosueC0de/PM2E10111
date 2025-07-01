package com.example.pm2e10111;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.pm2e10111.Configuracion.ActivityVerImagen;
import com.example.pm2e10111.Configuracion.Contactos;
import com.example.pm2e10111.Configuracion.SQLiteConexion;
import com.example.pm2e10111.Configuracion.Transacciones;

import java.util.ArrayList;

public class ActivityRegistros extends AppCompatActivity {

    Button atras, compartir, eliminar, actualizar, llamar, verImagen;
    ListView listacontactos;
    SearchView buscar;

    ArrayList<Contactos> listaContactos = new ArrayList<>();
    ArrayList<String> listaString = new ArrayList<>();

    int selectedIndex = -1;
    Contactos contactoSeleccionado = null;
    AdaptadorLista adaptador;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registros);

        atras = findViewById(R.id.atras);
        compartir = findViewById(R.id.compartir);
        eliminar = findViewById(R.id.eliminar);
        actualizar = findViewById(R.id.actualizar);
        listacontactos = findViewById(R.id.contactos);
        buscar = findViewById(R.id.buscar);
        llamar = findViewById(R.id.llamar);
        verImagen = findViewById(R.id.verImagen);

        llamar.setEnabled(false);
        ObtenerContactos();

        adaptador = new AdaptadorLista();
        listacontactos.setAdapter(adaptador);

        

        buscar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) { return false; }

            @Override
            public boolean onQueryTextChange(String newText) {
                adaptador.getFilter().filter(newText);
                return false;
            }
        });


        listacontactos.setOnItemClickListener((adapterView, view, i, l) -> {
            selectedIndex = i;
            contactoSeleccionado = listaContactos.get(i);
            Toast.makeText(getApplicationContext(), "Seleccionado: " + contactoSeleccionado.getNombre(), Toast.LENGTH_SHORT).show();
            llamar.setEnabled(true);
            adaptador.notifyDataSetChanged();
        });





        compartir.setOnClickListener(view -> {
            if (contactoSeleccionado == null) {
                Toast.makeText(this, "Seleccione un contacto primero", Toast.LENGTH_SHORT).show();
                return;
            }
            String textoCompartir = "Nombre: " + contactoSeleccionado.getNombre() + "\nTeléfono: " + contactoSeleccionado.getTelefono();
            Intent intentCompartir = new Intent(Intent.ACTION_SEND);
            intentCompartir.setType("text/plain");
            intentCompartir.putExtra(Intent.EXTRA_TEXT, textoCompartir);
            startActivity(Intent.createChooser(intentCompartir, "Compartir contacto via"));
        });

        eliminar.setOnClickListener(view -> {
            if (contactoSeleccionado == null) {
                Toast.makeText(this, "Seleccione un contacto primero", Toast.LENGTH_SHORT).show();
                return;
            }
            SQLiteDatabase db = new SQLiteConexion(this, Transacciones.NameDatabase, null, 1).getWritableDatabase();
            int res = db.delete(Transacciones.tablaContactos, Transacciones.id + "=?", new String[]{String.valueOf(contactoSeleccionado.getId())});
            db.close();

            if (res > 0) {
                Toast.makeText(this, "Contacto eliminado", Toast.LENGTH_SHORT).show();
                ObtenerContactos();
                adaptador = new AdaptadorLista();
                listacontactos.setAdapter(adaptador);
                contactoSeleccionado = null;
                selectedIndex = -1;
                llamar.setEnabled(false);
            } else {
                Toast.makeText(this, "Error al eliminar", Toast.LENGTH_SHORT).show();
            }
        });

        actualizar.setOnClickListener(view -> {
            if (contactoSeleccionado == null) {
                Toast.makeText(this, "Seleccione un contacto primero", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.putExtra("modo", "actualizar");
            intent.putExtra("id", contactoSeleccionado.getId());
            intent.putExtra("pais", contactoSeleccionado.getPais());
            intent.putExtra("nombre", contactoSeleccionado.getNombre());
            intent.putExtra("telefono", contactoSeleccionado.getTelefono());
            intent.putExtra("nota", contactoSeleccionado.getNota());
            intent.putExtra("imagen", contactoSeleccionado.getImagen());
            startActivity(intent);
            finish();
        });

        atras.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        });

        llamar.setOnClickListener(view -> {
            if (contactoSeleccionado == null) {
                Toast.makeText(this, "Seleccione un contacto", Toast.LENGTH_SHORT).show();
                return;
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, 1);
            } else {
                LlamarContacto(contactoSeleccionado.getTelefono());
            }
        });

        verImagen.setOnClickListener(view -> {
            if (contactoSeleccionado != null) {
                byte[] imageBytes = contactoSeleccionado.getImagen();

                Intent intent = new Intent(ActivityRegistros.this, ActivityVerImagen.class);
                intent.putExtra("imagen", imageBytes);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Seleccione un contacto primero", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void ObtenerContactos() {
        SQLiteConexion conexion = new SQLiteConexion(this, Transacciones.NameDatabase, null, 1);
        SQLiteDatabase db = conexion.getReadableDatabase();

        listaContactos.clear();
        listaString.clear();

        Cursor cursor = db.rawQuery(Transacciones.SelectTableContactos, null);

        while (cursor.moveToNext()) {
            Contactos contacto = new Contactos();
            contacto.setId(cursor.getInt(0));
            contacto.setPais(cursor.getString(1));
            contacto.setNombre(cursor.getString(2));
            contacto.setTelefono(cursor.getString(3));
            contacto.setNota(cursor.getString(4));


            byte[] imageBytes = cursor.getBlob(cursor.getColumnIndex(Transacciones.imagen));
            contacto.setImagen(imageBytes);

            listaContactos.add(contacto);
            listaString.add(contacto.getNombre() + " | " + contacto.getTelefono());
        }

        cursor.close();
        db.close();
    }

    class AdaptadorLista extends ArrayAdapter<String> {
        public AdaptadorLista() {
            super(ActivityRegistros.this, android.R.layout.simple_list_item_1, listaString);
        }

        @Override
        public View getView(int position, View convertView, android.view.ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
            if (position == selectedIndex) {
                view.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_light));
            } else {
                view.setBackgroundColor(getResources().getColor(android.R.color.transparent));
            }
            return view;
        }
    }

    private void LlamarContacto(String numero) {
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:" + numero));
        startActivity(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (contactoSeleccionado != null) {
                LlamarContacto(contactoSeleccionado.getTelefono());
            }
        } else {
            Toast.makeText(this, "Permiso de llamada denegado", Toast.LENGTH_SHORT).show();
        }
    }
}

