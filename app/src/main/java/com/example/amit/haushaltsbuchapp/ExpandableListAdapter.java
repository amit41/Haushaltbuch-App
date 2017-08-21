package com.example.amit.haushaltsbuchapp;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ExpandableListAdapter extends BaseExpandableListAdapter {
    /**
     * This a adapter class for Expandable listview.
     * This class helps to show category and subcategory in Expandable listview
     */
    private Context context;
    private Map<String, List<String>> categoryCollection;
    private List<String> categories;
    private android.support.v7.app.AlertDialog alert;
    private DatabaseHelper db;

    /**
     * Constructor
     * @param context holds the context infromation
     * @param categories holds the list of category
     * @param categoryCollection holds the list of subcategory which belongs
     *                           to a corresponding category
     */
    public ExpandableListAdapter(Context context, List<String> categories,
                                 Map<String, List<String>> categoryCollection) {
        this.context = context;
        this.categoryCollection = categoryCollection;
        this.categories = categories;
        db = new DatabaseHelper(context);
    }

    /**
     * @return returns the total length of category
     */
    @Override
    public int getGroupCount() {
        return categories.size();
    }

    /**
     * @param groupPosition hold the position of the category
     * @return returns the total length of subcategory in a given category positon
     */
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

    /**
     * This method helps to show category in list
     */
    @Override
    public View getGroupView(final int groupPosition, boolean b, View convertView, ViewGroup viewGroup) {
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
            public void onClick(View v) {
                int childCount = getChildrenCount(groupPosition);
                modifyCategory(v, childCount, categoryName, groupPosition);
            }
        });
        return convertView;
    }

    /**
     * This method helps to show subcategory in list
     */
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

            public void onClick(View v) {
                modifySubcategory(v, subcategoryName, childPosition, groupPosition);
            }
        });

        return convertView;
    }

    /**
     * This method helps to modify category information
     * @param view holds the information of current view
     * @param childCount holds the total numer of subcategory
     * @param categoryName holds the category name
     * @param groupPosition holds the position of given category
     */
    private void modifyCategory(View view, int childCount, String categoryName, final int groupPosition) {
        LayoutInflater layoutInflater = LayoutInflater.from(view.getContext());
        View modifyDialogView = layoutInflater.inflate(R.layout.modify_category_layout, null);

        HashMap<String, String> category = db.getCategoryByName(categoryName);
        final String categoryId = category.get("CATEGORY_ID");

        final EditText category_name = (EditText) modifyDialogView.findViewById(R.id.etCategoryName);
        category_name.setText(category.get("CATEGORY_NAME"));

        final RadioGroup rdgExpenseIncome = (RadioGroup) modifyDialogView.findViewById(R.id.rdbGpExpensesIncome);
        RadioButton expense = (RadioButton) modifyDialogView.findViewById(R.id.rdbExpense);
        RadioButton income = (RadioButton) modifyDialogView.findViewById(R.id.rdbIncome);
        if(category.get("CATEGORY_TYPE").equals("Expense"))
        {
            expense.setChecked(true);
        }
        else if(category.get("CATEGORY_TYPE").equals("Income"))
        {
            income.setChecked(true);
        }

        final TextView parentCategoryLabel = (TextView) modifyDialogView.findViewById(R.id.tvParentCategoryLabel);
        TextView patentCategory = (TextView) modifyDialogView.findViewById(R.id.tvParentCategory);
        parentCategoryLabel.setVisibility(View.GONE);
        patentCategory.setVisibility(View.GONE);

        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(view.getContext());
        builder.setTitle("Modify Category")
                .setView(modifyDialogView)
                .setNeutralButton("Modify", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String type = getSelectedRadioButtonValue(rdgExpenseIncome.getCheckedRadioButtonId());
                        CategoryTable category = new CategoryTable(categoryId, category_name.getText().toString(), type);
                        Toast.makeText(context, "Category Name:" + category_name.getText().toString(), Toast.LENGTH_SHORT).show();
                        if(category_name.getText().toString().equals("")){
                            Toast.makeText(context, "Category name required.", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            if(db.updateCategory(category) == 1)
                            {
                                Toast.makeText(context, "Category is modified, click refresh to see effect.", Toast.LENGTH_SHORT).show();
                            }
                            else {
                                Toast.makeText(context, "Something went wrong by updating category.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
        if(childCount == 0 && db.getCategoryCount() > 1)
        {
            builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if(category_name.getText().toString().equals("")){
                        Toast.makeText(context, "Category name required.", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        if(db.deleteCategory(categoryId) == 1)
                        {
                            Toast.makeText(context, "Category is deleted.", Toast.LENGTH_SHORT).show();
                            categories.remove(groupPosition);
                            notifyDataSetChanged();
                        }
                        else
                        {
                            Toast.makeText(context, "Something went wrong by deleting category.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });
        }

        alert = builder.create();
        alert.show();
        db.close();
    }

    /**
     * @param index holds the index of the radiobutton value
     * @return returns the value of radiobutton for given index
     */
    private String getSelectedRadioButtonValue(int index)
    {
        String selectedValue = null;
        if(index == R.id.rdbExpense){
            selectedValue = "Expense";
        }
        else if(index == R.id.rdbIncome){
            selectedValue = "Income";
        }
        return selectedValue;
    }

    /**
     * This method helps to modify subcategory from the list
     * @param view holds the information of current view
     * @param subcategoryName holds the name of subcategory
     * @param childPosition holds the position of given subcategory in list
     * @param groupPosition holds position of parent category of the given subcategory
     */
    private void modifySubcategory(View view, String subcategoryName, final int childPosition, final int groupPosition) {
        LayoutInflater layoutInflater = LayoutInflater.from(view.getContext());
        View modifyDialogView = layoutInflater.inflate(R.layout.modify_category_layout, null);

        final HashMap<String, String> category = db.getSubcategoryByName(subcategoryName);
        final String subcategoryId = category.get("SUBCATEGORY_ID");
        final String parent_category = category.get("PARENT_CATEGORY_ID");

        final EditText categoryName = (EditText) modifyDialogView.findViewById(R.id.etCategoryName);
        categoryName.setText(category.get("SUBCATEGORY_NAME"));
        final TextView parentCategory = (TextView) modifyDialogView.findViewById(R.id.tvParentCategory);
        parentCategory.setText(db.getCategoryNameByID(parent_category));
        final RadioGroup rdgExpenseIncome = (RadioGroup) modifyDialogView.findViewById(R.id.rdbGpExpensesIncome);
        RadioButton expense = (RadioButton) modifyDialogView.findViewById(R.id.rdbExpense);
        RadioButton income = (RadioButton) modifyDialogView.findViewById(R.id.rdbIncome);
        if(category.get("CATEGORY_TYPE").equals("Expense"))
        {
            expense.setChecked(true);
        }
        else if(category.get("CATEGORY_TYPE").equals("Income"))
        {
            income.setChecked(true);
        }

        final android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(view.getContext());
        builder.setTitle("Modify Subcategory")
                .setView(modifyDialogView)
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if(categoryName.getText().toString().equals("")){
                            Toast.makeText(context, "Subcategory name required.", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            if(db.deleteSubcategory(subcategoryId) == 1)
                            {
                                Toast.makeText(context, "Subcategory is deleted.", Toast.LENGTH_SHORT).show();
                                List<String> child = categoryCollection.get(categories.get(groupPosition));
                                child.remove(childPosition);
                                notifyDataSetChanged();
                            }
                            else
                            {
                                Toast.makeText(context, "Something went wrong by deleting subcategory.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                })
                .setNeutralButton("Modify", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String type = getSelectedRadioButtonValue(rdgExpenseIncome.getCheckedRadioButtonId());
                        SubcategoryTable subcategory = new SubcategoryTable(subcategoryId, categoryName.getText().toString(),
                                                        parentCategory.getText().toString(), type);
                        if(categoryName.getText().toString().equals("")){
                            Toast.makeText(context, "Subcategory name required.", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            if(db.updateSubcategory(subcategory) == 1)
                            {
                                Toast.makeText(context, "Subcategory is modified, click refresh to see effect.", Toast.LENGTH_SHORT).show();
                            }
                            else {
                                Toast.makeText(context, "Something went wrong by updating subcategory.", Toast.LENGTH_SHORT).show();
                            }
                        }
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
        db.close();
    }

    @Override
    public boolean isChildSelectable(int i, int i1) {
        return true;
    }
}
