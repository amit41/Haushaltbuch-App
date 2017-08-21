package com.example.amit.haushaltsbuchapp;

public class CategoryTable {

    /**
     * This class create Category with it's properties: categoryId
     * categoryName and Type to save in database.
     */
    private String categoryId, categoryName, type;

    public CategoryTable() {}

    /**
     * Constructor
     * @param id holds category id
     * @param categoryName holds category name
     * @param type holds category type: Expense/Income
     */
    public CategoryTable(String id, String categoryName, String type) {
        this.categoryId = id;
        this.categoryName = categoryName;
        this.type = type;
    }

    /**
     *  @return returns category id
     */
    public String getCategoryId() {
        return categoryId;
    }

    /**
     * @return returns category name
     */
    public String getCategoryName() {
        return categoryName;
    }

    /**
     * @return returns type of Category
     */
    public String getType() {
        return type;
    }

    /**
     * This method generate category id
     * @param lastId holds the last Category id
     * @return
     */
    public String generateCategoryID(String lastId) {
        String newID = "";

        if(lastId.equals(""))
        {
            newID = "CAT1";
        }
        else {
            // takes only the number characters from category id to generate id
            int id = Integer.parseInt(lastId.substring(3, lastId.length()));
            newID = "CAT" + String.valueOf(++id);
        }
        return newID;
    }
}
