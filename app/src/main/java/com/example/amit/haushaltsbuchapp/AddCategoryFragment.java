package com.example.amit.haushaltsbuchapp;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AddCategoryFragment extends Fragment {

    /**
     * This class helps to insert new Category/Subcategory
     * in database and shows them in Expandable listview
     */
    private List<String> categoryList;
    private List<String> subcategoryList;
    private Map<String, List<String>> categoryCollection;
    private ExpandableListView expListView;
    private DatabaseHelper db;
    private Button refresh, save;
    private ExpandableListAdapter expListAdapter;
    private TextView parentCategory;
    private AlertDialog alert;
    private EditText categoryName;
    private RadioGroup rdExpenseIncome;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_category_layout, container, false);

        initLayout(view);

        createCategoryList();
        createCategoryCollection();

        return view;
    }

    /**
     * collect the list of subcategory corresponding to a category
     * in a varialbe categoryCollection
     */
    private void createCategoryCollection() {
    // preparing category collection(subcategory)
        categoryCollection = new LinkedHashMap<String, List<String>>();
        for (String category : categoryList) {
            subcategoryList = new ArrayList<String>();
            subcategoryList = db.getSubcategoryByCategoryID(db.getCategoryIdByName(category));
            categoryCollection.put(category, subcategoryList);
        }
    }

    /**
     * create a category list
     */
    private void createCategoryList() {
        categoryList = new ArrayList<String>();
        categoryList = db.getAllCategory();
    }

    /**
     * initialise components in layout
     */
    private void initLayout(View view){
        categoryName = (EditText) view.findViewById(R.id.etCategoryName);
        parentCategory = (TextView) view.findViewById(R.id.tvParentCategory);
        rdExpenseIncome = (RadioGroup) view.findViewById(R.id.rdbGpExpensesIncome);
        save = (Button) view.findViewById(R.id.btnSave);
        refresh = (Button) view.findViewById(R.id.btnRefresh);
        expListView = (ExpandableListView) view.findViewById(R.id.expLvCategory_list);
        db = new DatabaseHelper(getContext());
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


        expListAdapter= new ExpandableListAdapter(getContext(), categoryList, categoryCollection);

        expListView.setGroupIndicator(null);
        expListView.setAdapter(expListAdapter);

        parentCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCategoryDialog(view);
            }
        });

        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateCategoryListView();
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveCategory();
            }
        });
    }

    @Override
    public void onResume()
    {
        super.onResume();
    }

    /**
     * Save category in database
     */
    private void saveCategory() {
        String category_name = categoryName.getText().toString();
        String parent_category = parentCategory.getText().toString();
        String type = getSelectedRadioButtonValue(rdExpenseIncome.getCheckedRadioButtonId());
        if(category_name.equals("")){
            categoryName.setError("Required Category name");
        }
        else {
            if(parent_category.equals("") || parent_category.equals("None")){
                CategoryTable category = new CategoryTable();
                String categoryId = category.generateCategoryID(db.getLastCategoryID());
                if(db.insertCategory(new CategoryTable(categoryId,category_name,type))){
                    Toast.makeText(getContext(), "Category: " + category_name +
                            " is created, click refresh to see effect.", Toast.LENGTH_SHORT).show();
                }
            }
            else {
                SubcategoryTable category = new SubcategoryTable();
                String subcategoryId = category.generateSubcategoryID(db.getLastSubcategoryID());
                if(db.insertSubcategory(new SubcategoryTable(
                        subcategoryId,category_name,db.getCategoryIdByName(parent_category),type)))
                {
                    Toast.makeText(getContext(), "Subcategory: " + category_name +
                            " is created, click refresh to see effect." , Toast.LENGTH_SHORT).show();
                }
            }
        }
        categoryName.setText("");
        parentCategory.setText("");
    }

    /**
     * @param index takes the position of the value in radio button
     * @return selected value from radio button
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

    /**
     * display the category dialog to choose parent category
     */
    private void showCategoryDialog(View view) {
        List<String> categoryList = db.getAllCategory();
        categoryList.add("None");
        String[] tempCategory = new String[categoryList.size()];
        tempCategory = categoryList.toArray(tempCategory);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        final String[] selectedCategory = {parentCategory.getText().toString()};
        final String[] category = tempCategory;

        if(selectedCategory[0].equals("")){
            selectedCategory[0] = category[0];
        }

        int selected = getItemArrayID(selectedCategory[0], category);

        builder.setTitle("Choose Category")
                .setSingleChoiceItems(category, selected,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int position) {
                                selectedCategory[0] = category[position];
                            }
                        })
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        parentCategory.setText( selectedCategory[0]);
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
     * @param selectedItem takes the selected Category
     * @param categoryList is list of Category
     * @return the position of selected Category
     */

    private int getItemArrayID(String selectedItem, String[] categoryList) {
        int id = -1;
         for (String choice:categoryList) {
            id += 1;
            if(choice.equals(selectedItem)){
                break;
            }
        }
        return id;
    }

    /**
     * This method is loaded to show the Category and
     * subcategory in Expandable listview.
     */
    private void updateCategoryListView() {
        createCategoryList();
        createCategoryCollection();

        expListAdapter= new ExpandableListAdapter(getContext(),
                categoryList, categoryCollection);

        expListView.setGroupIndicator(null);
        expListView.setAdapter(expListAdapter);
    }
}
