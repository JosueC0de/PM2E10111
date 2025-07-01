package com.example.pm2e10111;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.pm2e10111.Configuracion.SQLiteConexion;
import com.example.pm2e10111.Configuracion.Transacciones;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    Spinner pais;
    EditText nombre, telefono, nota;
    Button salvar, contactos, foto;
    ImageView imageView;

    Map<String, String> codigos = new HashMap<>();
    String codigoActual = "";
    static final int PETICION_FOTO = 100;
    Bitmap fotoBitmap = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        pais = findViewById(R.id.pais);
        nombre = findViewById(R.id.nombre);
        telefono = findViewById(R.id.telefono);
        nota = findViewById(R.id.nota);
        salvar = findViewById(R.id.salvar);
        contactos = findViewById(R.id.contactossalvados);
        foto = findViewById(R.id.foto);
        imageView = findViewById(R.id.imageView3);


        codigos.put("Honduras", "+504");
        codigos.put("Guatemala", "+502");
        codigos.put("El Salvador", "+503");
        codigos.put("Nicaragua", "+505");

        ArrayList<String> paises = new ArrayList<>(codigos.keySet());
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, paises);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        pais.setAdapter(adapter);


        codigoActual = codigos.get(paises.get(0));
        setTelefonoConCodigo(codigoActual);

        pais.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String seleccionado = pais.getSelectedItem().toString();
                String nuevoCodigo = codigos.get(seleccionado);

                if (nuevoCodigo != null && !nuevoCodigo.equals(codigoActual)) {
                    codigoActual = nuevoCodigo;


                    if (telefono.getText().toString().isEmpty()) {
                        telefono.setText(codigoActual);
                    } else if (!telefono.getText().toString().startsWith(codigoActual)) {

                        telefono.setText(codigoActual + telefono.getText().toString().replaceAll("^[+0-9]*", ""));
                    }
                }


            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        foto.setOnClickListener(view -> {
            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, 1);
            } else {
                TomarFoto();
            }
        });

        salvar.setOnClickListener(view -> {
            if (ValidarCampos()) {

                if ("actualizar".equals(getIntent().getStringExtra("modo"))) {
                    ActualizarContacto(getIntent().getIntExtra("id", -1));
                } else {

                    GuardarContacto();
                }
            }
        });

        contactos.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, ActivityRegistros.class);
            startActivity(intent);
        });

        Button botonCerrar = findViewById(R.id.botonCerrar);

        botonCerrar.setOnClickListener(v -> {
            finish();
            System.exit(0);
        });


        Intent intent = getIntent();
        if (intent != null && "actualizar".equals(intent.getStringExtra("modo"))) {
            int id = intent.getIntExtra("id", -1);
            String paisRecibido = intent.getStringExtra("pais");
            String nombreRecibido = intent.getStringExtra("nombre");
            String telefonoRecibido = intent.getStringExtra("telefono");
            String notaRecibida = intent.getStringExtra("nota");
            byte[] imagenRecibida = intent.getByteArrayExtra("imagen");
            nombre.setText(nombreRecibido);
            telefono.setText(telefonoRecibido);
            nota.setText(notaRecibida);

            ArrayAdapter<String> adapterUpdate = (ArrayAdapter<String>) pais.getAdapter();
            int index = adapterUpdate.getPosition(paisRecibido);
            if (index >= 0) pais.setSelection(index);


            if (imagenRecibida != null) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(imagenRecibida, 0, imagenRecibida.length);
                imageView.setImageBitmap(bitmap);
                fotoBitmap = bitmap;
            }

            salvar.setText("Actualizar Contacto");
        }
    }

    private void setTelefonoConCodigo(String codigo) {
        telefono.setText(codigo);
    }

    private void TomarFoto() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, PETICION_FOTO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PETICION_FOTO && resultCode == RESULT_OK && data != null) {
            Bundle extras = data.getExtras();
            if (extras != null) {
                fotoBitmap = (Bitmap) extras.get("data");
                imageView.setImageBitmap(fotoBitmap);
            }
        }
    }

    private boolean ValidarCampos() {
        String nombretxt = nombre.getText().toString();
        String telefonotxt = telefono.getText().toString();

        if (nombretxt.isEmpty()) {
            Toast.makeText(this, "Nombre inválido", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (telefonotxt.isEmpty()) {
            Toast.makeText(this, "Teléfono inválido", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void GuardarContacto() {
        SQLiteConexion conexion = new SQLiteConexion(this, Transacciones.NameDatabase, null, 1);
        SQLiteDatabase db = conexion.getWritableDatabase();

        ContentValues valores = new ContentValues();
        valores.put(Transacciones.pais, pais.getSelectedItem().toString());
        valores.put(Transacciones.nombre, nombre.getText().toString());
        valores.put(Transacciones.telefono, telefono.getText().toString());
        valores.put(Transacciones.nota, nota.getText().toString());

        if (fotoBitmap != null) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            fotoBitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
            byte[] imageBytes = byteArrayOutputStream.toByteArray();
            valores.put(Transacciones.imagen, imageBytes);
        }

        Long resultado = db.insert(Transacciones.tablaContactos, null, valores);
        Toast.makeText(this, "Contacto guardado. ID: " + resultado, Toast.LENGTH_LONG).show();

        db.close();
        LimpiarCampos();
    }

    private void ActualizarContacto(int id) {
        if (id == -1) {
            Toast.makeText(this, "ID no válido", Toast.LENGTH_SHORT).show();
            return;
        }

        SQLiteConexion conexion = new SQLiteConexion(this, Transacciones.NameDatabase, null, 1);
        SQLiteDatabase db = conexion.getWritableDatabase();

        ContentValues valores = new ContentValues();
        valores.put(Transacciones.pais, pais.getSelectedItem().toString());
        valores.put(Transacciones.nombre, nombre.getText().toString());
        valores.put(Transacciones.telefono, telefono.getText().toString());
        valores.put(Transacciones.nota, nota.getText().toString());

        if (fotoBitmap != null) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            fotoBitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
            byte[] imageBytes = byteArrayOutputStream.toByteArray();
            valores.put(Transacciones.imagen, imageBytes);
        }

        int resultado = db.update(Transacciones.tablaContactos, valores, "id = ?", new String[]{String.valueOf(id)});

        if (resultado > 0) {
            Toast.makeText(this, "Contacto actualizado", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, ActivityRegistros.class); // Para mostrar la lista actualizada
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Error al actualizar", Toast.LENGTH_SHORT).show();
        }

        db.close();
    }

    private void LimpiarCampos() {
        nombre.setText("");
        telefono.setText("");
        nota.setText("");
        imageView.setImageResource(0);
        fotoBitmap = null;
        pais.setSelection(0);
    }
}
