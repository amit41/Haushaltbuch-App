package com.example.amit.haushaltsbuchapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class TransactionsFragment extends Fragment {

    /**
     * This class helps to show list of expense/income transaction
     * list when the app is opened
     */
    private ListView mTransactionList;
    private List<TransactionTable> mTransactions;
    private DatabaseHelper db;
    private TextView sumExpense, sumIncome;
    private Calendar cal;
    private TextView prev, next, monthlyTransaction;
    private TransactionsListAdapter transListAdapter;
    public TransactionsFragment(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_transactions_layout, container, false);
        initLayout(view);
        return view;
    }

    /**
     * This method initialise required components for layout
     * @param view holds the information of current view
     */
    private void initLayout(View view)
    {
        mTransactionList= (ListView) view.findViewById(R.id.lvTransactionList);
        sumExpense = (TextView) view.findViewById(R.id.tvTransactionExpense);
        sumIncome = (TextView) view.findViewById(R.id.tvTransactionIncome);
        prev = (TextView) view.findViewById(R.id.tvPrevious);
        next = (TextView) view.findViewById(R.id.tvNext);
        monthlyTransaction = (TextView) view.findViewById(R.id.tvMonthlyTransaction);
        db = new DatabaseHelper(getContext());
        cal = Calendar.getInstance();
    }
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        monthlyTransaction.setText(Utils.getMonthlyTranstion(cal));
        loadListView(cal);
        // set the click listener for each item of transaction list
        mTransactionList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, final int position, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setMessage("Do you want to modify or delete transaction?");
                builder.setCancelable(false);
                builder.setPositiveButton("Delete",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                String title = transListAdapter.getItem(position).getTitle();
                                Date transDate = transListAdapter.getItem(position).getTransactionDate();
                                String transDateString = Utils.convertDateToString(transDate);
                                int transactionId = db.getTransactionByTitleAndDate(title, transDateString);
                                if(db.deleteTransaction(transactionId) == 1)
                                {
                                    loadListView(cal);
                                }
                                else
                                {
                                    Toast.makeText(getContext(), "Transaction is not deleted.", Toast.LENGTH_SHORT).show();
                                }

                            }
                        });
                builder.setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        });
                builder.setNeutralButton("Modify", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(getContext(), "Modify the transaction.", Toast.LENGTH_SHORT).show();
                        Intent tabActivityIntent = new Intent(getContext(), TabActivity.class);

                        tabActivityIntent.putExtra("MODIFY", "Modify");
                        tabActivityIntent.putExtra("TRANSID",String.valueOf(db.getTransactionByTitleAndDate(transListAdapter.getItem(position).getTitle(),
                                Utils.convertDateToString(transListAdapter.getItem(position).getTransactionDate()))));
                        tabActivityIntent.putExtra("AMOUNT",String.valueOf(transListAdapter.getItem(position).getAmount()));
                        tabActivityIntent.putExtra("TYPE",transListAdapter.getItem(position).getType());
                        tabActivityIntent.putExtra("CATEGORY",transListAdapter.getItem(position).getCategory());
                        tabActivityIntent.putExtra("TRANSTITLE",transListAdapter.getItem(position).getTitle());
                        tabActivityIntent.putExtra("TRANSDATE",Utils.dateInGermanFormat(transListAdapter.getItem(position).getTransactionDate()));
                        tabActivityIntent.putExtra("PAYMENT",transListAdapter.getItem(position).getPayment());

                        startActivity(tabActivityIntent);
                    }
                });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });
        // navigiate to next month
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cal.add(Calendar.MONTH, 1);
                monthlyTransaction.setText(Utils.getMonthlyTranstion(cal));
                loadListView(cal);
            }
        });
        // navigate to previous month
        prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cal.add(Calendar.MONTH, -1);
                monthlyTransaction.setText(Utils.getMonthlyTranstion(cal));
                loadListView(cal);
            }
        });
    }

    @Override
    public void onResume(){
        super.onResume();
        loadListView(cal);
    }

    /**
     * This method loads all the transaction in list view
     * @param cal holds the information of given calendar
     */
    private void loadListView(Calendar cal)
    {
        cal.set(Calendar.DAY_OF_MONTH, 1);
        String firstDayOfMonth = Utils.convertDateToString(cal.getTime());

        cal.set(Calendar.DATE, cal.getActualMaximum(Calendar.DATE));
        String lastDayOfMonth = Utils.convertDateToString(cal.getTime());
        mTransactions = db.getAllTransaction(firstDayOfMonth, lastDayOfMonth);
        db.getSumOfCategoryFromTransaction(firstDayOfMonth, lastDayOfMonth,"Expense");

        if(mTransactions == null) {
            Toast.makeText(getContext(), "Click plus symbol to add transaction.",
                    Toast.LENGTH_SHORT).show();
            mTransactionList.setAdapter(null);
        }
        else {
            transListAdapter = new TransactionsListAdapter(getContext(), mTransactions);
            mTransactionList.setAdapter(transListAdapter);
        }
        HashMap<String, String> sumValue = db.transactionTotalSum(firstDayOfMonth, lastDayOfMonth);

        String totalSumText = "";
        if(sumValue != null)
        {
            if(sumValue.size() == 1){
                if(sumValue.containsKey("Expense"))
                {
                    totalSumText += "Expense: " + sumValue.get("Expense") + " €";
                    sumExpense.setVisibility(View.VISIBLE);
                    sumExpense.setText(totalSumText);
                    sumIncome.setVisibility(View.GONE);
                }
                else if(sumValue.containsKey("Income"))
                {
                    totalSumText += "Income: " + sumValue.get("Income")+ " €";
                    sumIncome.setText(totalSumText);
                    sumIncome.setVisibility(View.VISIBLE);
                    sumExpense.setVisibility(View.GONE);
                }
            }
            else if(sumValue.size() == 2)
            {
                sumExpense.setVisibility(View.VISIBLE);
                sumExpense.setText("Expense: " + sumValue.get("Expense") + " €");
                sumExpense.setTextColor(Color.RED);
                sumIncome.setVisibility(View.VISIBLE);
                sumIncome.setText("Income: " + sumValue.get("Income")+ " €");
                sumIncome.setTextColor(Color.GREEN);
            }
        }
        else
        {
            sumExpense.setVisibility(View.GONE);
            sumIncome.setVisibility(View.GONE);
        }
    }
}
