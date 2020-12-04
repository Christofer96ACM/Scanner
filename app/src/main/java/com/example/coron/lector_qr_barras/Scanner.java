package com.example.coron.lector_qr_barras;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.ArraySet;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.util.HashSet;
import java.util.Set;


public class Scanner extends AppCompatActivity {

    SharedPreferences preference;
    private Button btnScanner;
    private Button btnEnvioLote;
    private TextView tvBarCode;
    private TextView tvCodigo;
    DatabaseReference mDatabaseReference = FirebaseDatabase.getInstance().getReference();
    DatabaseReference mRootChild = mDatabaseReference.child("Numero_Lote").child("Numero");
    private ListView listScannedQRs;
    private Set<String> scannedQRs;
    String CodigoLeido;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);
        btnScanner = findViewById(R.id.btnScanner);
        btnEnvioLote = findViewById(R.id.btnEnvioLote);
        tvBarCode = findViewById(R.id.tvBarCode);
        tvCodigo = findViewById(R.id.tvCodigo);
        btnScanner.setOnClickListener(mOnClickListener);
        btnEnvioLote.setOnClickListener(mOnClickListener);
        listScannedQRs = findViewById(R.id.lIstScannedQR);
        Update_List();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mRootChild.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //String Num_Lote = dataSnapshot.getValue().toString();
                CodigoLeido = dataSnapshot.getValue().toString();
                tvBarCode.setText("Lote Numero: "+ CodigoLeido);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null)
            if(result.getContents() != null){

                String escaneado = result.getContents();
                Update_List(escaneado);
                Insert_OnebyOne(escaneado);

                tvCodigo.setText("El Código es: " + result.getContents());
            }
            else{
                tvCodigo.setText("Error al Escanear");
            }
    }

    private View.OnClickListener mOnClickListener = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.btnScanner:
                    new IntentIntegrator(Scanner.this).initiateScan();
                    break;
                case R.id.btnEnvioLote:
                    Insert_ByLote(listScannedQRs.getAdapter());
                    break;

            }
        }
    };

    void Update_List(String escaneado){
        scannedQRs.add(escaneado);
        //preference = getSharedPreferences("STORE", Context.MODE_PRIVATE);
        preference.edit().remove("scannedQRs").apply();
        preference.edit().putStringSet("scannedQRs", scannedQRs).apply();
        Update_List();
    }
    void Update_List(){
        preference = getSharedPreferences("STORE", Context.MODE_PRIVATE);
        scannedQRs = preference.getStringSet("scannedQRs", new HashSet<String>());
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1,
                scannedQRs.toArray( new String[scannedQRs.size()]) );
        listScannedQRs.setAdapter(adapter);
    }
    void Insert_OnebyOne(String escaneado){
        mDatabaseReference.child("Codigo").push().setValue(escaneado);
    }
    void Insert_ByLote(ListAdapter list){
        int cant = list.getCount();
        Map<String,Object> lote;
        for(int i=0 ; i<cant ; i++){
            lote = new HashMap<>();
            lote.put("NumeroLote", CodigoLeido);
            lote.put("Codigo", list.getItem(i));
            mDatabaseReference.child("Lotes").push().setValue(lote);
        }
        Toast.makeText(Scanner.this,
                "Lote Enviado Correctamente",Toast.LENGTH_SHORT).show();

        ClearList();
        Update_NumLote();
        Update_List();
    }
    void Update_NumLote(){
        HashMap hmap = new HashMap();
        hmap.put("Numero", Integer.parseInt(CodigoLeido)+1);
        mDatabaseReference.child("Numero_Lote").updateChildren(hmap);
    }
    void ClearList(){
        preference.edit().remove("scannedQRs").apply();
    }
}
