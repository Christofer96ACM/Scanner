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
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;


import java.util.HashMap;
import java.util.Map;

import java.util.HashSet;
import java.util.Set;


public class Scanner extends AppCompatActivity {

    SharedPreferences preference;

    private Button btnScanner;
    private TextView tvBarCode;

    private TextView tvCodigo;
    DatabaseReference mDatabaseReference = FirebaseDatabase.getInstance().getReference();
    DatabaseReference mRootChild = mDatabaseReference.child("Codigo");

    private ListView listScannedQRs;

    private Set<String> scannedQRs;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);
        btnScanner = findViewById(R.id.btnScanner);
        tvBarCode = findViewById(R.id.tvBarCode);
        tvCodigo = findViewById(R.id.tvCodigo);
        btnScanner.setOnClickListener(mOnClickListener);
        listScannedQRs = findViewById(R.id.lIstScannedQR);

        preference = getSharedPreferences("STORE", Context.MODE_PRIVATE);
        scannedQRs = preference.getStringSet("scannedQRs", new HashSet<String>());

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, scannedQRs.toArray( new String[scannedQRs.size()]) );
        listScannedQRs.setAdapter(adapter);

        Log.i("SCANNED-QRS", "onCreate: " + scannedQRs);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mRootChild.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String CodigoLeido = dataSnapshot.getValue().toString();
                tvBarCode.setText(CodigoLeido);
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


                Map<String,Object> lote = new HashMap<>();
                lote.put("Codigo",1);
                lote.put("Lectura",result.getContents());

                tvCodigo.setText("El Código es: \n" + result.getContents());
                mDatabaseReference.child("Codigo").push().setValue(result.getContents());
                mDatabaseReference.child("Lotes").push().setValue(lote);
                tvBarCode.setText("El Código es: \n" + result.getContents());




                //ALVARO
                scannedQRs.add(result.getContents());
                preference = getSharedPreferences("STORE", Context.MODE_PRIVATE);
                preference.edit().remove("scannedQRs").commit();
                preference.edit().putStringSet("scannedQRs", scannedQRs).commit();
                Log.i("SCANNED-QRS", "onScan: " + scannedQRs);

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
            }
        }
    };
}
