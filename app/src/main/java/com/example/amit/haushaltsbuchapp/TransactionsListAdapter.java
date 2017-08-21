package com.example.amit.haushaltsbuchapp;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.List;

public class TransactionsListAdapter extends ArrayAdapter<TransactionTable> {
    /**
     * This adapter class helps to display list of transaction
     */
    private Context context;
    private List<TransactionTable> mTransactionsList;
    private SimpleDateFormat mDateFormat = new SimpleDateFormat("EEE, d MMM yyyy");

    public TransactionsListAdapter(Context context, List<TransactionTable> mTransactions) {
        super(context, R.layout.fragment_transactions_layout);
        this.context = context;
        this.mTransactionsList = mTransactions;
    }

    private class ViewHolder {
        TextView mDate = null, mCategory = null, mTitle = null, mAmount = null, mPayment;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.transactions_item_layout, null);
            holder = new ViewHolder();

            holder.mDate = (TextView) convertView.findViewById(R.id.tvTransactionDate);
            holder.mCategory = (TextView) convertView.findViewById(R.id.tvCategory);
            holder.mTitle = (TextView) convertView.findViewById(R.id.tvTransactionTitle);
            holder.mAmount = (TextView) convertView.findViewById(R.id.tvAmount);
            holder.mPayment = (TextView) convertView.findViewById(R.id.tvPaymentType);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        TransactionTable transaction = (TransactionTable) getItem(position);
        holder.mDate.setText(mDateFormat.format(transaction.getTransactionDate()));
        holder.mCategory.setText(transaction.getCategory());
        holder.mTitle.setText(transaction.getTitle());
        holder.mAmount.setText(String.valueOf(transaction.getAmount()) + " €");
        holder.mPayment.setText(transaction.getPayment());
        if(transaction.getType().equals("Income"))
        {
            holder.mAmount.setTextColor(Color.GREEN);
        }
        else if(transaction.getType().equals("Expense"))
        {
            holder.mAmount.setTextColor(Color.RED);
        }

        return convertView;
    }

    @Override
    public int getCount() {
        return mTransactionsList.size();
    }

    @Override
    public TransactionTable getItem(int i) {
        return mTransactionsList.get(i);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }
}
