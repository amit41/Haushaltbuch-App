package com.example.amit.haushaltsbuchapp;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.List;

public class TransactionsListAdapterForExport extends ArrayAdapter<TransactionTable> {
    /**
     * This adapter class helps to show list of transaction to export
     */
    private Context context;
    private List<TransactionTable> mTransactionsList;
    private SimpleDateFormat mDateFormat = new SimpleDateFormat("EEE, d MMM yyyy");

    public TransactionsListAdapterForExport(Context context) {
        super(context, R.layout.fragment_transactions_layout);
    }

    public TransactionsListAdapterForExport(Context context, List<TransactionTable> mTransactions) {
        super(context, R.layout.activity_export_data);
        this.context = context;
        this.mTransactionsList = mTransactions;
    }

    private class ViewHolder {
        TextView mDate = null, mCategory = null, mTitle = null, mAmount = null, mPayment;
        CheckedTextView checkedItem;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.transactions_item_export_layout, null);
            holder = new ViewHolder();

            holder.mCategory = (TextView) convertView.findViewById(R.id.tvCategory);
            holder.mTitle = (TextView) convertView.findViewById(R.id.tvTransactionTitle);
            holder.mAmount = (TextView) convertView.findViewById(R.id.tvAmount);
            holder.mPayment = (TextView) convertView.findViewById(R.id.tvPaymentType);
            holder.checkedItem = (CheckedTextView) convertView.findViewById(R.id.chbxItem);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final CheckedTextView checkedItem = holder.checkedItem;
        checkedItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkedItem.isChecked()) {
                    checkedItem.setChecked(false);

                } else {
                    checkedItem.setChecked(true);
                }
            }
        });

        TransactionTable transaction = (TransactionTable) getItem(position);
        holder.checkedItem.setText(mDateFormat.format(transaction.getTransactionDate()));
        holder.mCategory.setText(transaction.getCategory());
        holder.mTitle.setText(transaction.getTitle());
        holder.mAmount.setText(String.valueOf(transaction.getAmount()) + " €");
        holder.mPayment.setText(transaction.getPayment());

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

    /**
     * @param pos holds the position of transaction item
     * @param listView holds the information of transaction list view
     * @return returns view item i.e the transaction item
     */
    public View getViewByPosition(int pos, ListView listView) {
        final int firstListItemPosition = listView.getFirstVisiblePosition();
        final int lastListItemPosition = firstListItemPosition + listView.getChildCount() - 1;

        if (pos < firstListItemPosition || pos > lastListItemPosition ) {
            return listView.getAdapter().getView(pos, null, listView);
        } else {
            final int childIndex = pos - firstListItemPosition;
            return listView.getChildAt(childIndex);
        }
    }
}
