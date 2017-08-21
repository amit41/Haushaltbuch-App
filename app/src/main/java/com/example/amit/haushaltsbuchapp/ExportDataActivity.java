package com.example.amit.haushaltsbuchapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class ExportDataActivity extends AppCompatActivity {
    private ListView mTransactionList;
    private List<TransactionTable> mTransactions;
    private DatabaseHelper db;
    private Button export;
    private Calendar cal;
    private TextView prev, next, monthlyTransaction;
    private TransactionsListAdapterForExport transListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_export_data);

        initLayout();

        monthlyTransaction.setText(Utils.getMonthlyTranstion(cal));
        loadListView(cal);

        export.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<TransactionTable> exportDataList = new ArrayList<TransactionTable>();
                CheckedTextView checkedItem;
                for (int i = 0; i < mTransactionList.getAdapter().getCount(); i++) {
                    checkedItem = (CheckedTextView) ((LinearLayout) transListAdapter.
                            getViewByPosition(i, mTransactionList)).findViewById(R.id.chbxItem);
                    if (checkedItem.isChecked()) {
                        String category = transListAdapter.getItem(i).getCategory();
                        transListAdapter.getItem(i).setCategory(db.getCategoryIdByName(category));
                        exportDataList.add(transListAdapter.getItem(i));
                    }
                }

                boolean result = JSONHelper.exportToJSON(getApplicationContext(), exportDataList);
                if (result) {
                    Toast.makeText(ExportDataActivity.this,
                            "Data is exported in external storage.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ExportDataActivity.this,
                            "Exporting data to external storage failed.", Toast.LENGTH_SHORT).show();
                }
                finish();
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cal.add(Calendar.MONTH, 1);
                monthlyTransaction.setText(Utils.getMonthlyTranstion(cal));
                loadListView(cal);
            }
        });
        prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cal.add(Calendar.MONTH, -1);
                monthlyTransaction.setText(Utils.getMonthlyTranstion(cal));
                loadListView(cal);
            }
        });
    }
    private void initLayout()
    {
        mTransactionList= (ListView) findViewById(R.id.lvTransactionList);
        export = (Button) findViewById(R.id.btnExport);
        prev = (TextView) findViewById(R.id.tvPrevious);
        next = (TextView) findViewById(R.id.tvNext);
        monthlyTransaction = (TextView) findViewById(R.id.tvMonthlyTransaction);

        db = new DatabaseHelper(getApplicationContext());
        cal = Calendar.getInstance();
    }

    private void loadListView(Calendar cal)
    {
        //mTransactions = new ArrayList<TransactionTable>();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        String firstDayOfMonth = Utils.convertDateToString(cal.getTime());

        cal.set(Calendar.DATE, cal.getActualMaximum(Calendar.DATE));
        String lastDayOfMonth = Utils.convertDateToString(cal.getTime());
        mTransactions = db.getAllTransaction(firstDayOfMonth, lastDayOfMonth);
        db.getSumOfCategoryFromTransaction(firstDayOfMonth, lastDayOfMonth,"Expense");
        //mTransactions.add(new TransactionTable("Food","Weekend Shoping", Utils.convertStringToDate("2016-12-09"),50.00,"Cash","Expense"));
        //mTransactions.add(new TransactionTable("Electronics","Samsung Mobile", Utils.convertStringToDate("2016-12-10"),250.00,"Debit card","Expense"));

        if(mTransactions == null) {
            Toast.makeText(getApplicationContext(), "No Transaction found.", Toast.LENGTH_SHORT).show();
            mTransactionList.setAdapter(null);
        }
        else {
            transListAdapter = new TransactionsListAdapterForExport(getApplicationContext(), mTransactions);
            mTransactionList.setAdapter(transListAdapter);
        }
    }
}
