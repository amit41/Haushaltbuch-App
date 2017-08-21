package com.example.amit.haushaltsbuchapp;

public class SubcategoryTable {
    /**
     * This class helps to store the information of subcategory
     */
    private String subcategoryId, subcategoryName, parentCategoryId, type;

    public SubcategoryTable() {}

    // constructor
    public SubcategoryTable(String subcategoryId, String subcategoryName, String parentCategoryId, String type) {
        this.subcategoryId = subcategoryId;
        this.subcategoryName = subcategoryName;
        this.parentCategoryId = parentCategoryId;
        this.type = type;
    }

    public String getSubcategoryId() {
        return subcategoryId;
    }

    public String getSubcategoryName() {
        return subcategoryName;
    }

    public String getParentCategoryId() {
        return parentCategoryId;
    }

    public String getType() {
        return type;
    }

    /**
     * @param lastId holds the last id of subcategory
     * @return returns new id for subcategory
     */
    public String generateSubcategoryID(String lastId) {
        String newID = "";
        if(lastId.equals(""))
        {
            newID = "SUB1";
        }
        else {
            int id = Integer.parseInt(lastId.substring(3, lastId.length()));
            newID = "SUB" + String.valueOf(++id);
        }
        return newID;
    }
}
