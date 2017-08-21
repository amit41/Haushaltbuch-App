package com.example.amit.haushaltsbuchapp;

import android.content.Context;
import android.nfc.Tag;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

public class JSONHelper {
    /**
     * This class helps data to import/export in JSON dataformat
     */
    public static final String FILE_NAME = "haushaltsbuch.json";

    /**
     * This method helps to export expenses/income transactions in JSON file.
     * @param context holds the information of context
     * @param transactions holds list of transaction that are to be exported
     * @return returns true when successfully exported otherwise false
     */
    public static boolean exportToJSON(Context context, List<TransactionTable> transactions) {

        ExportImportData mData = new ExportImportData();
        mData.setExportedTransaction(transactions);

        // Gson API Object to data in JSON data format
        Gson gson = new Gson();
        String gsonString = gson.toJson(mData);

        // FileOutputStream Object to write the JSON data into file.
        FileOutputStream mFileOutput = null;
        File mFile = new File(Environment.getExternalStorageDirectory()+"/Download", FILE_NAME);

        try {
            mFileOutput = new FileOutputStream(mFile);
            mFileOutput.write(gsonString.getBytes());
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            try {
                if (mFileOutput != null) {
                    mFileOutput.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return false;
    }

    /**
     * @param context holds the information of context
     * @return returns list of transaction that are imported from JSON file
     */

    public static List<TransactionTable> importFromJSON(Context context) {

        FileReader reader = null;

        try {
            File file = new File(Environment.getExternalStorageDirectory()+"/Download", FILE_NAME);
            reader = new FileReader(file);

            Gson gson = new Gson();
            ExportImportData importData = gson.fromJson(reader, ExportImportData.class);

            return importData.getExportImportTransaction();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if(reader != null)
            {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return null;
    }

    /**
     * This class gives the information to Gson API to know the
     * the type of data that are exported to JSON data format
     */
    static class ExportImportData {
        List<TransactionTable> exportImportTransaction;

        public List<TransactionTable> getExportImportTransaction() {
            return exportImportTransaction;
        }

        public void setExportedTransaction(List<TransactionTable> exportedTransaction) {
            this.exportImportTransaction = exportedTransaction;
        }
    }
}
