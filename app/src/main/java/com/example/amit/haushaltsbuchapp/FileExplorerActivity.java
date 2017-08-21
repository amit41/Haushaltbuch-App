package com.example.amit.haushaltsbuchapp;

import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class FileExplorerActivity extends AppCompatActivity {
    /**
     * This class shows path and the name of file that is imported in App
     */
    private TextView tvFileName, tvFilePath;
    private Button btnImport;
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_explorer);
        this.setTitle("Import file");

        tvFileName = (TextView) findViewById(R.id.tvFileName);
        tvFilePath = (TextView) findViewById(R.id.tvFilePath);

        tvFileName.setText(JSONHelper.FILE_NAME);
        tvFilePath.setText(Environment.getExternalStorageDirectory().toString());

        btnImport = (Button) findViewById(R.id.btnImport);

        db = new DatabaseHelper(getApplicationContext());

        btnImport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<TransactionTable> importedData = JSONHelper.importFromJSON(getApplicationContext());
                if(importedData != null)
                {
                    // imported data are inserted in transaction table
                    for (TransactionTable trans: importedData) {
                        db.insertTransaction(trans);
                    }
                    Toast.makeText(FileExplorerActivity.this,
                            "Imported data has been inserted.", Toast.LENGTH_SHORT).show();
                    finish();
                }
                else
                {
                    Toast.makeText(FileExplorerActivity.this,
                            "Importing data failed.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
