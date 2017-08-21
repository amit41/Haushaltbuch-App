package com.example.amit.haushaltsbuchapp;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class ReportFragment extends Fragment {
    /**
     * This class helps to draw pie chart for expense/income
     */
    private PieChart mPiechart;
    private DatabaseHelper db;
    private Calendar cal;
    private HashMap<String, Double> categoryWithSum;
    private TextView prev, next, monthlyTransaction, tvSelectedCategory, tvSelectedCategoryContent;
    private List<String> keysCategory;
    private List<Double> valuesSum;
    private String[] categories;
    private double[] sum;
    private List<TransactionTable> mTransByCategory;
    private String firstDayOfMonth, lastDayOfMonth;
    private RadioGroup rdExpenseIncome;
    private RadioButton rdbExpense, rdbIncome;
    private String type;
    private String totalSum;

    public ReportFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.report_layout, container, false);
        initLayout(view);
        return view;
    }

    /**
     * This method helps to initialise layout components
     *
     * @param view holds the current view information
     */
    private void initLayout(View view) {
        prev = (TextView) view.findViewById(R.id.tvPrevious);
        next = (TextView) view.findViewById(R.id.tvNext);
        monthlyTransaction = (TextView) view.findViewById(R.id.tvMonthlyTransactionChart);
        mPiechart = (PieChart) view.findViewById(R.id.piechart);
        tvSelectedCategory = (TextView) view.findViewById(R.id.shortDetailsHeader);
        tvSelectedCategoryContent = (TextView) view.findViewById(R.id.shortDetailsContent);
        rdExpenseIncome = (RadioGroup) view.findViewById(R.id.rdbGpExpensesIncome);
        rdbExpense = (RadioButton) view.findViewById(R.id.rdbExpense);
        rdbIncome = (RadioButton) view.findViewById(R.id.rdbIncome);
        db = new DatabaseHelper(getContext());
        cal = Calendar.getInstance();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);

        mPiechart.setDescription("");
        mPiechart.setRotationEnabled(true);

        mPiechart.setDrawHoleEnabled(true);
        mPiechart.setHoleColor(Color.WHITE);

        mPiechart.setTransparentCircleColor(Color.WHITE);
        mPiechart.setTransparentCircleAlpha(110);

        mPiechart.setHoleRadius(40f);
        mPiechart.setTransparentCircleRadius(45f);

        mPiechart.setCenterTextSize(10);

        monthlyTransaction.setText(Utils.getMonthlyTranstion(cal));
        updateDataSet(cal);

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cal.add(Calendar.MONTH, 1);
                monthlyTransaction.setText(Utils.getMonthlyTranstion(cal));
                updateDataSet(cal);
            }
        });
        prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cal.add(Calendar.MONTH, -1);
                monthlyTransaction.setText(Utils.getMonthlyTranstion(cal));
                updateDataSet(cal);
            }
        });

        rdbExpense.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateDataSet(cal);
                totalSum = db.getSumOfTypeFromTransaction(firstDayOfMonth, lastDayOfMonth, type);
                mPiechart.setCenterText("" + Utils.getMonthlyTranstion(cal) + "\nMonthly Expense" + "\n Summe:" + totalSum + " €");
                Toast.makeText(getContext(), "Expense clicked.", Toast.LENGTH_SHORT).show();
            }
        });

        rdbIncome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateDataSet(cal);
                totalSum = db.getSumOfTypeFromTransaction(firstDayOfMonth, lastDayOfMonth, type);
                mPiechart.setCenterText("" + Utils.getMonthlyTranstion(cal) + "\nIncome" + "\n Summe:" + totalSum + " €");
                Toast.makeText(getContext(), "Income clicked.", Toast.LENGTH_SHORT).show();
            }
        });

        /*
        This listener helps to show the list of transaction for the selected category
         */
        mPiechart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry entry, int i, Highlight highlight) {
                Log.i("Entry: ", String.valueOf(entry.getVal()));
                Log.i("Category: ", categories[highlight.getXIndex()]);
                //Log.i("Highlight: ", highlight.toString());
                mTransByCategory = db.getTransactionsByCategory(firstDayOfMonth, lastDayOfMonth,
                        db.getCategoryIdByName(categories[highlight.getXIndex()]), type);
                tvSelectedCategory.setText("Selected Category: " + categories[highlight.getXIndex()] +
                        "        Sum: " + String.valueOf(entry.getVal()) + " €");
                String contents = "";

                for (int j = 0; j < mTransByCategory.size(); j++) {
                    contents += (j + 1) + ". " + mTransByCategory.get(j).getTitle() + ":   " +
                            mTransByCategory.get(j).getAmount() + " €\n";
                }

                tvSelectedCategoryContent.setText(contents);

            }

            @Override
            public void onNothingSelected() {
                //Toast.makeText(getContext(), "Nothing Selected.", Toast.LENGTH_SHORT).show();
                tvSelectedCategory.setText("Selected Category: ");
                tvSelectedCategoryContent.setText("");
            }
        });
    }

    /**
     * This method helps to draw the pie chart when this class is called
     *
     * @param cal holds the information of the given Calendar
     */
    private void updateDataSet(Calendar cal) {
        tvSelectedCategory.setText("Selected Category: ");
        tvSelectedCategoryContent.setText("");
        cal.set(Calendar.DAY_OF_MONTH, 1);
        firstDayOfMonth = Utils.convertDateToString(this.cal.getTime());

        cal.set(Calendar.DATE, this.cal.getActualMaximum(Calendar.DATE));
        lastDayOfMonth = Utils.convertDateToString(this.cal.getTime());
        type = getSelectedRadioButtonValue(rdExpenseIncome.getCheckedRadioButtonId());
        // holds the list of category with its total summ
        categoryWithSum = db.getSumOfCategoryFromTransaction(firstDayOfMonth, lastDayOfMonth, type);

        // holds the total sum of transaction type(Expense/income)
        totalSum = db.getSumOfTypeFromTransaction(firstDayOfMonth, lastDayOfMonth, type);
        mPiechart.setCenterText("" + Utils.getMonthlyTranstion(cal) +
                "\nMonthly Expense" + "\n Summe:" + totalSum + " €");

        if (categoryWithSum != null) {
            keysCategory = new ArrayList<String>();
            keysCategory.addAll(categoryWithSum.keySet());

            // categories is xData
            categories = new String[keysCategory.size()];
            keysCategory.toArray(categories); // fill the array

            valuesSum = new ArrayList<Double>();
            valuesSum.addAll(categoryWithSum.values());

            // sum is yData
            sum = new double[valuesSum.size()];
            for (int j = 0; j < sum.length; j++) {
                sum[j] = valuesSum.get(j);
            }

            ArrayList<Entry> entries = new ArrayList<>();

            for (int i = 0; i < categories.length; i++) {
                // add the sum of every category as entry
                entries.add(new Entry((float) (sum[i]), i));
            }

            // set the entry as dataset
            PieDataSet dataSet = new PieDataSet(entries, "");
            dataSet.setSliceSpace(3f);
            dataSet.setSelectionShift(5f);

            // add a lot of colors
            ArrayList<Integer> colors = new ArrayList<Integer>();

            for (int c : ColorTemplate.VORDIPLOM_COLORS)
                colors.add(c);

            for (int c : ColorTemplate.JOYFUL_COLORS)
                colors.add(c);

            for (int c : ColorTemplate.COLORFUL_COLORS)
                colors.add(c);

            for (int c : ColorTemplate.LIBERTY_COLORS)
                colors.add(c);

            for (int c : ColorTemplate.PASTEL_COLORS)
                colors.add(c);

            colors.add(ColorTemplate.getHoloBlue());

            dataSet.setColors(colors);

            // add categories and entry as data to draw pie chart
            PieData data = new PieData(categories, dataSet);
            data.setValueFormatter(new UnitFormatter(" €"));
            data.setValueTextSize(10f);
            data.setValueTextColor(Color.BLACK);
            mPiechart.setData(data);

            // undo all highlights
            mPiechart.highlightValues(null);
            mPiechart.invalidate();
        } else {
            mPiechart.clear();
            mPiechart.setOnChartGestureListener(null);
        }
    }

    /**
     * @param index hold the position of selected radio button
     * @return returns the value of the selected radio button
     */
    private String getSelectedRadioButtonValue(int index) {
        String selectedValue = null;
        if (index == R.id.rdbExpense)
            selectedValue = "Expense";
        else if (index == R.id.rdbIncome)
            selectedValue = "Income";

        return selectedValue;
    }
}
