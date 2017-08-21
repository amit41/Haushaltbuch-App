package com.example.amit.haushaltsbuchapp;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.mariuszgromada.math.mxparser.*;


public class TransactionEntryFragment extends Fragment implements View.OnClickListener {

    /**
     * This class helps to save/modify the expense/income transaction in database.
     */
    private TextView transactionTitle, transactionPayment, transactionDate, category, result, typedValue;
    private Calendar mCal;
    private EditText expenseCost;
    private Button transactionSave;
    private Button one, two, three, four, five, six, seven, eight, nine, zero;
    private Button plus, subtract, divide, multiply;
    private Button dot, equal, del, cancel, ok, clear;
    public AlertDialog alert;
    private String currentDisplayedInput = "";
    private Expression exp;
    private DatabaseHelper db;
    private List<String> categoryList;
    private List<String> subcategoryList;
    private Map<String, List<String>> categoryCollection;
    private ExpandableListView expListView;
    private ExpandableCategoryListAdapter expListAdapter;
    private RadioGroup rdExpenseIncome;
    private RadioButton rdbExpense, rdbIncome;
    private int transactionId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_expenses_layout, container, false);
        initLayout(view);
        return view;
    }

    /**
     * This method initialises required components in layout
     * @param view holds the information of view
     */
    private void initLayout(View view){
        transactionTitle = (TextView) view.findViewById(R.id.tvTitle);
        transactionPayment = (TextView) view.findViewById(R.id.tvPayment);
        transactionDate = (TextView) view.findViewById(R.id.tvDate);
        rdExpenseIncome = (RadioGroup) view.findViewById(R.id.rdbGpExpensesIncome);
        rdbExpense = (RadioButton) view.findViewById(R.id.rdbExpense);
        rdbIncome = (RadioButton) view.findViewById(R.id.rdbIncome);
        category = (TextView) view.findViewById(R.id.tvCategory);
        expenseCost = (EditText) view.findViewById(R.id.etExpensesCost);
        transactionSave = (Button) view.findViewById(R.id.btnSaveTransaction);
        mCal = Calendar.getInstance();
        db = new DatabaseHelper(getContext());
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Intent transIntent = getActivity().getIntent();
        String modify = transIntent.getStringExtra("MODIFY");
        if(modify != null){
            if(modify.equals("Modify"))
            {
                String amount, type, transCategory, title, transDate, payment;

                amount = Utils.truncZero(Double.parseDouble(transIntent.getStringExtra("AMOUNT")));
                type = transIntent.getStringExtra("TYPE");
                transCategory = transIntent.getStringExtra("CATEGORY");
                title = transIntent.getStringExtra("TRANSTITLE");
                transDate = transIntent.getStringExtra("TRANSDATE");
                payment = transIntent.getStringExtra("PAYMENT");
                transactionId = Integer.parseInt(transIntent.getStringExtra("TRANSID"));
                Toast.makeText(getContext(), "transId: " + transactionId, Toast.LENGTH_SHORT).show();

                expenseCost.setText(amount + " €");
                if(type.equals("Expense"))
                {
                    rdbExpense.setChecked(true);
                }
                else if(type.equals("Income"))
                {
                    rdbIncome.setChecked(true);
                }
                category.setText(transCategory);
                transactionTitle.setText(title);
                transactionDate.setText(transDate);
                transactionPayment.setText(payment);
                transactionSave.setText("Modify Transaction");
            }
        }
        else {
            transactionDate.setText(Utils.dateInGermanFormat(mCal.getTime()));
        }


        transactionTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTransactionTitleDialog();
            }
        });
        transactionPayment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPaymentDialog();
            }
        });

        transactionDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCalendarDialog(view);
            }
        });
        category.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCategoryDialog();
            }
        });

        expenseCost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCalculatorDialog();
            }
        });

        transactionSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(transactionSave.getText().toString().equals("Save Transaction")){
                    saveTransaction();
                }
                else if(transactionSave.getText().toString().equals("Modify Transaction")){
                    modifyTransaction();
                    getActivity().finish();
                }

            }
        });
    }

    /**
     * This methods helps to save the transaction in transaction table
     */
    private void saveTransaction() {
        String categoryName = category.getText().toString();
        String title = transactionTitle.getText().toString();
        String tempAmt = expenseCost.getText().toString();
        Double amount = Double.parseDouble(tempAmt.substring(0,tempAmt.length()-1));
        String payment = transactionPayment.getText().toString();
        String type = getSelectedRadioButtonValue(rdExpenseIncome.getCheckedRadioButtonId());
        Date transDate = Utils.dateFromGermanFormat(transactionDate.getText().toString());

        TransactionTable transactionEntry = new TransactionTable(
                db.getCategoryIdByName(categoryName), title, transDate, amount, payment, type);
        if(amount == 0){
            Toast.makeText(getContext(), "Amount is required.", Toast.LENGTH_SHORT).show();
        }
        else if(categoryName.equals(""))
        {
            Toast.makeText(getContext(), "Category is required.", Toast.LENGTH_SHORT).show();
        }
        else if(title.equals(""))
        {
            Toast.makeText(getContext(), "Transaction Title is required.", Toast.LENGTH_SHORT).show();
        }
        else {
            db.insertTransaction(transactionEntry); // save transaction database
            getActivity().finish();
        }
    }

    /**
     * This method helps to modify transaction of database
     */
    private void modifyTransaction() {
        String categoryName = category.getText().toString();
        String title = transactionTitle.getText().toString();
        String tempAmt = expenseCost.getText().toString();
        Double amount = Double.parseDouble(tempAmt.substring(0,tempAmt.length()-1));
        String payment = transactionPayment.getText().toString();
        String type = getSelectedRadioButtonValue(rdExpenseIncome.getCheckedRadioButtonId());
        Date transDate = Utils.dateFromGermanFormat(transactionDate.getText().toString());

        TransactionTable transactionEntry = new TransactionTable(
                db.getCategoryIdByName(categoryName), title, transDate, amount, payment, type);
        if(amount == 0){
            Toast.makeText(getContext(), "Amount is required.", Toast.LENGTH_SHORT).show();
        }
        else if(categoryName.equals(""))
        {
            Toast.makeText(getContext(), "Category is required.", Toast.LENGTH_SHORT).show();
        }
        else if(title.equals(""))
        {
            Toast.makeText(getContext(), "Transaction Title is required.", Toast.LENGTH_SHORT).show();
        }
        else {
            // update transaction in database
            if(db.updateTransaction(transactionId, transactionEntry) == 1)
            {
                Toast.makeText(getContext(), "Transaction modified.", Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(getContext(), "Transaction modify failed.", Toast.LENGTH_SHORT).show();
            }
        }
    }
    /**
     * @param index hold the position of selected radio button
     * @return returns the value of the selected radio button
     */
    private String getSelectedRadioButtonValue(int index)
    {
        String selectedValue = null;
        if(index == R.id.rdbExpense)
            selectedValue = "Expense";
        else if(index == R.id.rdbIncome)
            selectedValue = "Income";

        return selectedValue;
    }
    public void setCategory(String categoryName){
        category.setText(categoryName);
    }

    /**
     * This method shows calculator dialog
     */
    private void showCalculatorDialog() {
        LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
        View calculatorDialogView = layoutInflater.inflate(R.layout.calculator_layout, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Calculator")
                .setView(calculatorDialogView);

        initCalculatorButtons(calculatorDialogView);

        alert = builder.create();
        alert.show();
    }

    /**
     * This methods initialise the calculator buttons in layout
     * @param calcView holds the information of calculator view
     */
    private void initCalculatorButtons(View calcView) {

        result = (TextView) calcView.findViewById(R.id.tvResult);
        typedValue = (TextView) calcView.findViewById(R.id.tvTypedValues);
        one = (Button)calcView.findViewById(R.id.btn1);
        two = (Button)calcView.findViewById(R.id.btn2);
        three = (Button)calcView.findViewById(R.id.btn3);
        four = (Button)calcView.findViewById(R.id.btn4);
        five = (Button)calcView.findViewById(R.id.btn5);
        six = (Button)calcView.findViewById(R.id.btn6);
        seven = (Button)calcView.findViewById(R.id.btn7);
        eight = (Button)calcView.findViewById(R.id.btn8);
        nine = (Button)calcView.findViewById(R.id.btn9);
        zero = (Button)calcView.findViewById(R.id.btn0);
        plus = (Button)calcView.findViewById(R.id.btnPlus);
        subtract = (Button)calcView.findViewById(R.id.btnMinus);
        divide = (Button)calcView.findViewById(R.id.btnDivide);
        multiply = (Button)calcView.findViewById(R.id.btnMultiply);
        dot = (Button)calcView.findViewById(R.id.btnDecimal);
        equal = (Button)calcView.findViewById(R.id.btnEqual);

        clear = (Button)calcView.findViewById(R.id.btnC);
        del = (Button)calcView.findViewById(R.id.btnDel);
        cancel = (Button)calcView.findViewById(R.id.btnCancel);
        ok = (Button)calcView.findViewById(R.id.btnOk);

        one.setOnClickListener((View.OnClickListener) this);
        two.setOnClickListener((View.OnClickListener) this);
        three.setOnClickListener((View.OnClickListener) this);
        four.setOnClickListener((View.OnClickListener) this);
        five.setOnClickListener((View.OnClickListener) this);
        six.setOnClickListener((View.OnClickListener) this);
        seven.setOnClickListener((View.OnClickListener) this);
        eight.setOnClickListener((View.OnClickListener) this);
        nine.setOnClickListener((View.OnClickListener) this);
        zero.setOnClickListener((View.OnClickListener) this);
        plus.setOnClickListener((View.OnClickListener) this);
        subtract.setOnClickListener((View.OnClickListener) this);
        divide.setOnClickListener((View.OnClickListener) this);
        multiply.setOnClickListener((View.OnClickListener) this);
        dot.setOnClickListener((View.OnClickListener) this);
        equal.setOnClickListener((View.OnClickListener) this);

        clear.setOnClickListener((View.OnClickListener) this);
        del.setOnClickListener((View.OnClickListener) this);
        cancel.setOnClickListener((View.OnClickListener) this);
        ok.setOnClickListener((View.OnClickListener) this);
    }

    @Override
    public void onClick(View view) {
        Button button = (Button) view;
        String data = button.getText().toString();
        // mathematical expression created from input of calculator
        exp = new Expression(typedValue.getText().toString());
        if(data.equals("C")){
            result.setText("0");
            typedValue.setText("");
            currentDisplayedInput = "";
        }
        else if(data.equals("Del")){
            onDelete();
        }
        else if(data.equals("=")){
            result.setText(calculate());
        }
        else if(data.equals("Cancel")){
            currentDisplayedInput = "";
            alert.dismiss();
        }
        else if(data.equals("Ok")){
            if(typedValue.getText().equals("")){
                if(!result.getText().equals("NaN")){
                    expenseCost.setText(result.getText().toString() + " €");
                }
                else {
                    expenseCost.setText("0 €");
                }
                currentDisplayedInput = "";
                alert.dismiss();
            }
            else {
                if(exp.checkSyntax()){
                    if(!calculate().equals("NaN")){
                        expenseCost.setText(calculate() + " €");
                    }
                    else {
                        expenseCost.setText("0 €");
                    }
                    currentDisplayedInput = "";
                    alert.dismiss();
                }
                else {
                    Toast.makeText(getContext(), "Wrong syntax.", Toast.LENGTH_SHORT).show();
                }
            }
        }
        else {
            obtainInputValues(data);
        }
    }

    /**
     * This method checks the mathematical expression and returns result
     * of expression
     * @return returns the result of expression
     */
    private String calculate()
    {
        String finalResult = result.getText().toString();

        if(!typedValue.getText().equals("")){
            if(exp.checkSyntax())
            {
                double calculatedValue = exp.calculate();
                finalResult = Utils.truncZero(calculatedValue);
                currentDisplayedInput = "";
            }
            else {
                Toast.makeText(getContext(), "Wrong syntax.", Toast.LENGTH_SHORT).show();
            }
        }
        return finalResult;
    }

    private void onDelete() {
        // Delete works like backspace; remove the last character from the expression.
        String typeValueText = typedValue.getText().toString();
        int textLength = typeValueText.length();
        if (textLength > 0) {
            typedValue.setText(typeValueText.substring(0, textLength - 1));
            currentDisplayedInput = typedValue.getText().toString();
        }
    }

    /**
     * This method shows the current input of calculator in display field
     * @param input holds the current input
     */
    private void obtainInputValues(String input){
        switch (input){
            case "0":
                currentDisplayedInput += "0";
                break;
            case "1":
                currentDisplayedInput += "1";
                break;
            case "2":
                currentDisplayedInput += "2";
                break;
            case "3":
                currentDisplayedInput += "3";
                break;
            case "4":
                currentDisplayedInput += "4";
                break;
            case "5":
                currentDisplayedInput += "5";
                break;
            case "6":
                currentDisplayedInput += "6";
                break;
            case "7":
                currentDisplayedInput += "7";
                break;
            case "8":
                currentDisplayedInput += "8";
                break;
            case "9":
                currentDisplayedInput += "9";
                break;
            case ".":
                currentDisplayedInput += ".";
                break;
            case "+":
                currentDisplayedInput += "+";
                break;
            case "-":
                currentDisplayedInput += "-";
                break;
            case "/":
                currentDisplayedInput += "/";

                break;
            case "*":
                currentDisplayedInput += "*";
                break;
        }
        typedValue.setText(currentDisplayedInput);
    }

    /**
     * This method shows the dialog to input transaction title
     */
    private void showTransactionTitleDialog() {
        LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
        View editDialogView = layoutInflater.inflate(R.layout.transaction_title_layout, null);

        final EditText etTitle = (EditText) editDialogView.findViewById(R.id.etTransactionTitle);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Transaction Title")
                .setView(editDialogView)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        transactionTitle.setText(etTitle.getText().toString());
                    }
                })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        alert = builder.create();
        alert.show();
    }

    /**
     * This method show the art of payment dialog
     */
    private void showPaymentDialog()
    {
        final String[] transacPayment = getResources().getStringArray(R.array.payment);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final String[] selectedPayment = {transactionPayment.getText().toString()};

        int selected = getItemArrayID(selectedPayment[0],"payment");
        builder.setTitle("Choose Payment")
                .setSingleChoiceItems(transacPayment, selected,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int position) {
                        selectedPayment[0] = transacPayment[position].toString();
                    }
                })
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        transactionPayment.setText( selectedPayment[0]);
                    }
                })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        alert = builder.create();
        alert.show();
    }

    /**
     * @param selectedItem holds the selected value of Category/payment
     * @param arrayName holds the name of array
     * @return returns the position of selected value from given array
     */
    private int getItemArrayID(String selectedItem, String arrayName) {
        int id = -1;
        String[] selectedArray = null;
        if(arrayName.equals("payment")) {
            selectedArray = getResources().getStringArray(R.array.payment);
        }
        else if(arrayName.equals("category")) {
            selectedArray = getResources().getStringArray(R.array.category);
        }

        for (String choice:selectedArray) {
            id += 1;
            if(choice.equals(selectedItem)){
                break;
            }
        }
        return id;
    }

    /**
     * This method shows the Datepicker to select the date of transaction
     * @param view holds the information of view
     */
    private void showCalendarDialog(View view){
        DatePickerDialog.OnDateSetListener mDatePickerDialog = new DatePickerDialog.OnDateSetListener(){
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                mCal.set(Calendar.YEAR, year);
                mCal.set(Calendar.MONTH, monthOfYear);
                mCal.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                transactionDate.setText(Utils.dateInGermanFormat(mCal.getTime()));
            }
        };
        DatePickerDialog datepicker = new DatePickerDialog(view.getContext(),mDatePickerDialog, mCal.get(Calendar.YEAR),
                mCal.get(Calendar.MONTH), mCal.get(Calendar.DAY_OF_MONTH));
        datepicker.setTitle("Select Date");
        datepicker.show();
    }

    /**
     * This method shows category dialog
     */
    private  void showCategoryDialog() {
        LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
        View categoryDialogView = layoutInflater.inflate(R.layout.category_layout, null);
        expListView = (ExpandableListView) categoryDialogView.findViewById(R.id.expLvCategory_list);
        createCategoryList();
        createCategoryCollection();

        expListAdapter= new ExpandableCategoryListAdapter(getContext(),
                categoryList, categoryCollection);

        expListView.setGroupIndicator(null);
        expListView.setAdapter(expListAdapter);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle("Choose Category")
                .setView(categoryDialogView); // category list is set up in dialog

        alert = builder.create();
        alert.show();
    }

    private void createCategoryCollection() {
        // preparing category collection(subcategory)
        categoryCollection = new LinkedHashMap<String, List<String>>();
        for (String category : categoryList) {
            subcategoryList = new ArrayList<String>();
            subcategoryList = db.getSubcategoryByCategoryID(db.getCategoryIdByName(category));
            categoryCollection.put(category, subcategoryList);
        }
    }
    // This method create category list
    private void createCategoryList() {
        categoryList = new ArrayList<String>();
        categoryList = db.getAllCategory();
    }

    public class ExpandableCategoryListAdapter extends BaseExpandableListAdapter {
        /**
         * This class helps to combine Expandable list in dialog
         */
        private Context context;
        private Map<String, List<String>> categoryCollection;
        private List<String> categories;
        private DatabaseHelper db;

        public ExpandableCategoryListAdapter(Context context, List<String> categories,
                                             Map<String, List<String>> categoryCollection) {
            this.context = context;
            this.categoryCollection = categoryCollection;
            this.categories = categories;
            db = new DatabaseHelper(context);
        }

        @Override
        public int getGroupCount() {
            return categories.size();
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            if(categoryCollection.get(categories.get(groupPosition)) == null)
            {
                return 0;
            }
            return categoryCollection.get(categories.get(groupPosition)).size();
        }

        @Override
        public Object getGroup(int groupPosition) {
            return categories.get(groupPosition);
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return categoryCollection.get(categories.get(groupPosition)).get(childPosition);
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        // This method helps to show category for list
        @Override
        public View getGroupView(final int groupPosition, final boolean b, View convertView, ViewGroup viewGroup) {
            final String categoryName = ((String) getGroup(groupPosition));
            if (convertView == null) {
                LayoutInflater infalInflater = (LayoutInflater) context
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = infalInflater.inflate(R.layout.category_single_item_layout,
                        null);
            }
            TextView initialLetter = (TextView) convertView.findViewById(R.id.tvInitialLetter);
            TextView item = (TextView) convertView.findViewById(R.id.tvCategoryItem);

            item.setTypeface(null, Typeface.BOLD);
            item.setText(categoryName);
            initialLetter.setText((""+categoryName.charAt(0)).toUpperCase());

            item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(getChildrenCount(groupPosition) == 0){
                        setCategory(categoryName);
                        alert.dismiss();
                    }
                    else
                    {
                        if(expListView.isGroupExpanded(groupPosition))
                        {
                            expListView.collapseGroup(groupPosition);
                        }
                        else {
                            expListView.expandGroup(groupPosition);
                        }
                    }

                }
            });
            return convertView;
        }

        // This method helps to show the subcategory for the category in list
        @Override
        public View getChildView(final int groupPosition, final int childPosition, boolean b, View convertView, ViewGroup viewGroup) {
            final String subcategoryName = ((String) getChild(groupPosition, childPosition));
            LayoutInflater inflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            if (convertView == null) {
                convertView = inflater.inflate(R.layout.subcategory_single_item_layout, null);
            }

            TextView initialLetter = (TextView) convertView.findViewById(R.id.tvInitialLetter);
            TextView item = (TextView) convertView.findViewById(R.id.tvSubcategoryItem);

            item.setTypeface(null, Typeface.BOLD);
            item.setText(subcategoryName);
            initialLetter.setText((""+subcategoryName.charAt(0)).toUpperCase());
            item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                        setCategory(subcategoryName);
                        alert.dismiss();
                }
            });
            return convertView;
        }

        @Override
        public boolean isChildSelectable(int i, int i1) {
            return true;
        }
    }

}
