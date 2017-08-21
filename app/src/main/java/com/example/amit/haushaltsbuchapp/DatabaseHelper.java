package com.example.amit.haushaltsbuchapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    /**
     * This is a Database class. All the methods, that are used to
     * save the data in database, are defined in this class
     */
    private Context context;
    private static final String DB_NAME = "haushaltbuch.db";
    private static final int DB_VERSION = 1;

    // Tables name
    private static final String TABLE_CATEGORY = "category";
    private static final String TABLE_SUBCATEGORY = "subcategory";
    private static final String TABLE_TRANSACTION = "transactionentry";

    // Common column
    public static final String ID = "_id";
    public static final String TYPE = "type";

    // Category table columns
    public static final String CATEGORY_NAME = "category_name";

    // Subcategory table columns
    public static final String SUBCATEGORY_NAME = "subcategory_name";
    public static final String PARENT_CATEGORY_ID = "parent_category_id";

    // Transaction table columns
    public static final String TRANSACTION_DATE = "transaction_date";
    public static final String TRANSACTION_TITLE = "transaction_title";
    public static final String CATEGORY_ID = "category_id";
    public static final String PAYMENT = "payment";
    public static final String AMOUNT = "amount";

    // Query to create category table
    private static final String CREATE_TABLE_CATEGORY = "CREATE TABLE "
            + TABLE_CATEGORY + "("
            + ID + " TEXT PRIMARY KEY,"
            + CATEGORY_NAME + " TEXT NOT NULL,"
            + TYPE + " TEXT NOT NULL"
            + ")";

    // Query to create Subcategory table
    private static final String CREATE_TABLE_SUBCATEGORY = "CREATE TABLE "
            + TABLE_SUBCATEGORY + "("
            + ID + " TEXT PRIMARY KEY,"
            + SUBCATEGORY_NAME + " TEXT NOT NULL,"
            + PARENT_CATEGORY_ID + " TEXT NOT NULL,"
            + TYPE + " TEXT NOT NULL,"
            + "FOREIGN KEY(" + PARENT_CATEGORY_ID +") REFERENCES "
            + TABLE_CATEGORY + "(" + ID +")"
            + ")";

    // Query to create Transaction table
    private static final String CREATE_TABLE_TRANSACTION = "CREATE TABLE "
            + TABLE_TRANSACTION + "("
            + ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + TRANSACTION_TITLE + " TEXT NOT NULL,"
            + TRANSACTION_DATE + " DATETIME NOT NULL,"
            + CATEGORY_ID + " TEXT NOT NULL,"
            + PAYMENT + " TEXT NOT NULL,"
            + AMOUNT + " DOUBLE NOT NULL,"
            + TYPE + " TEXT NOT NULL,"
            + "FOREIGN KEY(" + CATEGORY_ID +") REFERENCES "
            + TABLE_CATEGORY + "(" + ID +")"
            + ")";

    // Constructor
    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        this.context = context;
    }

    // This method creates database when the app runs
    @Override
    public void onCreate(SQLiteDatabase db) {
        // creating tables
        db.execSQL(CREATE_TABLE_CATEGORY);
        db.execSQL(CREATE_TABLE_SUBCATEGORY);
        db.execSQL(CREATE_TABLE_TRANSACTION);
    }

    /**
     * This method is executed only when some fileds of tables
     * are changed to update the database infromation
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // on upgrade drop older tables
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SUBCATEGORY);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRANSACTION);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORY);
    }

    /**
     * @param category holds the information of category which is to be saved
     * @return returns true when information is saved successfully otherwise false
     */
    public boolean insertCategory(CategoryTable category) {
        // opens database to save data
        SQLiteDatabase db = this.getWritableDatabase();

        // this Object of ContentValues helps to store data
        ContentValues cv = new ContentValues();

        cv.put(CATEGORY_NAME, category.getCategoryName());
        cv.put(TYPE, category.getType());

        // checks category id if exists
        String categoryId = isCategoryExist(category.getCategoryName());

        // When category id not exists then insert the data in database
        if(categoryId.equals("")){
            cv.put(ID, category.getCategoryId());
            db.insert(TABLE_CATEGORY, null, cv);
        }
        else {
            // When category id exists then update the data in database
            db.update(TABLE_CATEGORY, cv,ID + " = '" + categoryId +"'", null);
        }
        return true;
    }

    /**
     * @param subcategory holds the information of subcategory which is to be saved
     * @return returns true when information is saved successfully otherwise false
     */
    public boolean insertSubcategory(SubcategoryTable subcategory) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues cv = new ContentValues();

        cv.put(SUBCATEGORY_NAME, subcategory.getSubcategoryName());
        cv.put(PARENT_CATEGORY_ID, subcategory.getParentCategoryId());
        cv.put(TYPE, subcategory.getType());

        String subcategoryId = isSubcategoryExist(subcategory.getSubcategoryName());

        if(subcategoryId.equals("")){
            cv.put(ID, subcategory.getSubcategoryId());
            db.insert(TABLE_SUBCATEGORY, null, cv);
        }
        else {
            db.update(TABLE_SUBCATEGORY, cv, ID + " = '" + subcategoryId +"'", null);
        }
        return true;
    }

    /**
     * @param transaction holds the information of expense/income transaction which is to be saved
     * @return returns true when information is saved successfully otherwise false
     */
    public boolean insertTransaction(TransactionTable transaction) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(TRANSACTION_TITLE, transaction.getTitle());
        cv.put(TRANSACTION_DATE, Utils.convertDateToString(transaction.getTransactionDate()));
        cv.put(CATEGORY_ID, transaction.getCategory());
        cv.put(PAYMENT, transaction.getPayment());
        cv.put(AMOUNT, transaction.getAmount());
        cv.put(TYPE, transaction.getType());

        int transactionId = isTransactionExist(
                Utils.convertDateToString(transaction.getTransactionDate()), transaction.getTitle());

        if(transactionId == 0){
            db.insert(TABLE_TRANSACTION, null, cv);
            Toast.makeText(context, "Transaction inserted.", Toast.LENGTH_SHORT).show();
        }
        else {
            db.update(TABLE_TRANSACTION, cv, ID + " = " + transactionId, null);
            Toast.makeText(context, "Transaction updated.", Toast.LENGTH_SHORT).show();
        }
        return true;
    }

    /**
     * @param categoryName holds category name
     * @return returns information of the given category
     */
    public HashMap<String, String> getCategoryByName(String categoryName) {
        HashMap<String, String> category = new HashMap<String, String>();
        String selectQuery = "SELECT * FROM " + TABLE_CATEGORY + " WHERE " + CATEGORY_NAME + " = '" + categoryName + "'";
        // opens database to read the data
        SQLiteDatabase db = this.getReadableDatabase();

        // Cursor object helps to read the result of the query
        Cursor cursor = db.rawQuery(selectQuery, null);

        // query suppose to return only one row, otherwise duplicate entry exists
        if(cursor.getCount() == 1) {
            cursor.moveToFirst();
            category.put("CATEGORY_ID", cursor.getString(cursor.getColumnIndex(ID)));
            category.put("CATEGORY_NAME", cursor.getString(cursor.getColumnIndex(CATEGORY_NAME)));
            category.put("CATEGORY_TYPE", cursor.getString(cursor.getColumnIndex(TYPE)));
        }

        return category;
    }

    /**
     * @param subcategoryName holds subcategory name
     * @return returns information of the given subcategory
     */
    public HashMap<String, String> getSubcategoryByName(String subcategoryName) {
        HashMap<String, String> subcategory = new HashMap<String, String>();
        String selectQuery = "SELECT * FROM " + TABLE_SUBCATEGORY + " WHERE " + SUBCATEGORY_NAME + " = '" + subcategoryName + "'";
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(selectQuery, null);

        if(cursor.getCount() == 1) {
            cursor.moveToFirst();
            subcategory.put("SUBCATEGORY_ID", cursor.getString(cursor.getColumnIndex(ID)));
            subcategory.put("SUBCATEGORY_NAME", cursor.getString(cursor.getColumnIndex(SUBCATEGORY_NAME)));
            subcategory.put("PARENT_CATEGORY_ID", cursor.getString(cursor.getColumnIndex(PARENT_CATEGORY_ID)));
            subcategory.put("CATEGORY_TYPE", cursor.getString(cursor.getColumnIndex(TYPE)));
        }

        return subcategory;
    }

    /**
     * This method checks whether the given category exist
     * @param categoryName holds category name
     * @return returns category id for the given category name
     */
    private String isCategoryExist(String categoryName) {
        String categoryId = "";
        String selectQuery = "SELECT "+ ID + " FROM " + TABLE_CATEGORY + " WHERE " + CATEGORY_NAME + " = '" + categoryName + "'";
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(selectQuery, null);

        if(cursor.getCount() > 0)
        {
            cursor.moveToFirst();
            categoryId = cursor.getString(cursor.getColumnIndex(ID));
        }
        return categoryId;
    }

    /**
     * This method checks whether the given subcategory exist
     * @param subcategoryName holds given subcategory name
     * @return returns subcategory id for the given subcategory name
     */
    private String isSubcategoryExist(String subcategoryName) {
        String categoryId = "";
        String selectQuery = "SELECT "+ ID + " FROM " + TABLE_SUBCATEGORY + " WHERE " + SUBCATEGORY_NAME + " = '" + subcategoryName + "'";
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(selectQuery, null);

        if(cursor.getCount() > 0)
        {
            cursor.moveToFirst();
            categoryId = cursor.getString(cursor.getColumnIndex(ID));
        }
        return categoryId;
    }

    /**
     * This method checks whether expense/income transaction exist
     * for given date and transaction title
     * @param transactionDate holds transaction date
     * @param title holds transaction title
     * @return returns transation id for given date and title
     */
    private int isTransactionExist(String transactionDate, String title) {
        int transactionId = 0;
        String selectQuery = "SELECT "+ ID + " FROM " + TABLE_TRANSACTION + " WHERE "
                + TRANSACTION_TITLE + " = '" + title + "'" + " AND "
                + TRANSACTION_DATE + " = '" + transactionDate + "'";
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(selectQuery, null);

        if(cursor.getCount() > 0)
        {
            cursor.moveToFirst();
            transactionId = cursor.getInt(cursor.getColumnIndex(ID));
        }
        return transactionId;
    }

    /**
     * @return returns last category id to generate new id
     */
    public String getLastCategoryID()
    {
        String lastCategoryId = "";
        String selectQuery = "SELECT "+ ID + " FROM " + TABLE_CATEGORY + " ORDER BY " + ID + " ASC";
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(selectQuery, null);

        if(cursor.getCount() > 0)
        {
            cursor.moveToLast();
            lastCategoryId = cursor.getString(cursor.getColumnIndex(ID));
        }
        return lastCategoryId;
    }

    /**
     * @return returns last subcategory id to generate new subcategory id
     */
    public String getLastSubcategoryID()
    {
        String lastSubcategoryId = "";
        String selectQuery = "SELECT "+ ID + " FROM " + TABLE_SUBCATEGORY + " ORDER BY " + ID + " ASC";
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(selectQuery, null);

        if(cursor.getCount() > 0)
        {
            cursor.moveToLast();
            lastSubcategoryId = cursor.getString(cursor.getColumnIndex(ID));
        }
        return lastSubcategoryId;
    }

    /**
     * @return returns list of category sorted in ascending order by category name
     */
    public List<String> getAllCategory() {
        List<String> categoryList = new ArrayList<String>();
        String selectQuery = "SELECT "+ CATEGORY_NAME + " FROM " + TABLE_CATEGORY + " ORDER BY " + CATEGORY_NAME + " ASC";
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(selectQuery, null);

        if(cursor.getCount() > 0)
        {
            cursor.moveToFirst();
            do {
                categoryList.add(cursor.getString(cursor.getColumnIndex(CATEGORY_NAME)));
            } while (cursor.moveToNext());
        }
        else {
            categoryList = null;
        }
       return categoryList;
    }

    /**
     * @param categoryId holds category id
     * @return returns list of subcategory that belong to given category id
     */
    public List<String> getSubcategoryByCategoryID(String categoryId) {
        List<String> subcategoryList = new ArrayList<String>();
        String selectQuery = "SELECT "+ SUBCATEGORY_NAME + " FROM " + TABLE_SUBCATEGORY +
                             " WHERE " + PARENT_CATEGORY_ID + " = '" + categoryId + "'" +
                             " ORDER BY " + SUBCATEGORY_NAME + " ASC";
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(selectQuery, null);

        if(cursor.getCount() > 0)
        {
            cursor.moveToFirst();
            do {
                subcategoryList.add(cursor.getString(cursor.getColumnIndex(SUBCATEGORY_NAME)));
            } while (cursor.moveToNext());
        }
        else {
            subcategoryList = null;
        }
        return subcategoryList;
    }

    /**
     * This method searchs category id first in category table
     * then in subcategory table when the data does not exist in category table
     * for given category name
     * @param categoryName holds category name
     * @return returns category/subcategory id for given category
     */
    public String getCategoryIdByName(String categoryName) {
        String categoryId = "";
        String selectQuery = "SELECT "+ ID + " FROM " + TABLE_CATEGORY +
                " WHERE " + CATEGORY_NAME + " = '" + categoryName +"'";
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(selectQuery, null);

        if(cursor.getCount() == 1)
        {
            cursor.moveToFirst();
            categoryId = (cursor.getString(cursor.getColumnIndex(ID)));
        }
        else
        {
            selectQuery = "SELECT "+ ID + " FROM " + TABLE_SUBCATEGORY +
                    " WHERE " + SUBCATEGORY_NAME + " = '" + categoryName +"'";
            cursor = db.rawQuery(selectQuery, null);

            if(cursor.getCount() == 1)
            {
                cursor.moveToFirst();
                categoryId = (cursor.getString(cursor.getColumnIndex(ID)));
            }
        }
        return categoryId;
    }

    /**
     * This method searchs category name first in category table
     * then in subcategory table when the data does not exist in category table
     * for given category id
     * @param categoryId holds category id
     * @return returns category/subcategory name for given category id
     */
    public String getCategoryNameByID(String categoryId) {
        String categoryName = "";
        String selectQuery = "SELECT "+ CATEGORY_NAME + " FROM " + TABLE_CATEGORY +
                " WHERE " + ID + " = '" + categoryId +"'";
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(selectQuery, null);

        if(cursor.getCount() == 1)
        {
            cursor.moveToFirst();
            categoryName = (cursor.getString(cursor.getColumnIndex(CATEGORY_NAME)));
        }
        else {
            selectQuery = "SELECT "+ SUBCATEGORY_NAME + " FROM " + TABLE_SUBCATEGORY +
                    " WHERE " + ID + " = '" + categoryId +"'";
            cursor = db.rawQuery(selectQuery, null);

            if(cursor.getCount() == 1)
            {
                cursor.moveToFirst();
                categoryName = (cursor.getString(cursor.getColumnIndex(SUBCATEGORY_NAME)));
            }
        }
        return categoryName;
    }

    /**
     * This method updates category table
     * @param category holds category information in object of category table
     * @return returns 1 when successfully updated otherwise 0
     */
    public int updateCategory(CategoryTable category)
    {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues cv = new ContentValues();

        cv.put(CATEGORY_NAME, category.getCategoryName());
        cv.put(TYPE, category.getType());

        String categoryId = category.getCategoryId();

        return db.update(TABLE_CATEGORY, cv,ID + " = '" + categoryId +"'", null);
    }

    /**
     * This method updates subcategory table
     * @param subcategory holds subcategory information in object of subcategory table
     * @return returns 1 when successfully updated otherwise 0
     */
    public int updateSubcategory(SubcategoryTable subcategory)
    {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues cv = new ContentValues();

        cv.put(SUBCATEGORY_NAME, subcategory.getSubcategoryName());
        cv.put(TYPE, subcategory.getType());

        String subcategoryId = subcategory.getSubcategoryId();

        return db.update(TABLE_SUBCATEGORY, cv, ID + " = '" + subcategoryId +"'", null);
    }

    /**
     * This method updates expense/income transaction
     * @param transId holds transaction id
     * @param transaction holds expense/income transaction information
     *                    in object of transaction table
     * @return returns 1 when successfully updated otherwise 0
     */
    public int updateTransaction(int transId, TransactionTable transaction)    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(TRANSACTION_TITLE, transaction.getTitle());
        cv.put(TRANSACTION_DATE, Utils.convertDateToString(transaction.getTransactionDate()));
        cv.put(CATEGORY_ID, transaction.getCategory());
        cv.put(PAYMENT, transaction.getPayment());
        cv.put(AMOUNT, transaction.getAmount());
        cv.put(TYPE, transaction.getType());

        return db.update(TABLE_TRANSACTION, cv, ID + " = " + transId , null);
    }

    /**
     * This method deletes the category for given id
     * @param categoryId holds category id
     * @return returns 1 when successfully deleted otherwise 0
     */
    public int deleteCategory(String categoryId)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_CATEGORY, ID + " = '" + categoryId +"'", null);
    }

    /**
     * This method deletes the subcategory for given id
     * @param subcategoryId holds subcategory id
     * @return returns 1 when successfully deleted otherwise 0
     */
    public int deleteSubcategory(String subcategoryId)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_SUBCATEGORY, ID + " = '" + subcategoryId +"'", null);
    }

    /**
     * This method deletes transaction for given id
     * @param transactionId holds transaction id
     * @return returns 1 when successfully deleted otherwise 0
     */
    public int deleteTransaction(int transactionId)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_TRANSACTION, ID + " = '" + transactionId +"'", null);
    }

    /**
     * @return returns the total number of category
     */
    public int getCategoryCount()
    {
        int count = -1;
        SQLiteDatabase db = this.getWritableDatabase();
        String selectQuery = "SELECT COUNT(*) FROM " + TABLE_CATEGORY;

        Cursor cursor = db.rawQuery(selectQuery, null);

        if(cursor.getCount() > 0)
        {
            cursor.moveToFirst();
            count = cursor.getInt(0);
        }
        else {
            count = 0;
        }
        return count;
    }

    /**
     * @param firstDayOfMonth holds first date of month
     * @param lastDayOfMonth holds last date of month
     * @return returns a list of transaction entries that lies in between given dates
     */
    public List<TransactionTable> getAllTransaction(String firstDayOfMonth, String lastDayOfMonth) {
        List<TransactionTable> transactionList = new ArrayList<TransactionTable>();

        String selectQuery = "SELECT * FROM " + TABLE_TRANSACTION
                        + " WHERE " + TRANSACTION_DATE +" BETWEEN '" +firstDayOfMonth + "' AND '" + lastDayOfMonth + "'"
                        + " ORDER BY " + TRANSACTION_DATE + " DESC";
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(selectQuery, null);

        if(cursor.getCount() > 0)
        {
            cursor.moveToFirst();
            do {
                String categoryName = getCategoryNameByID(cursor.getString(cursor.getColumnIndex(CATEGORY_ID)));
                String title = cursor.getString(cursor.getColumnIndex(TRANSACTION_TITLE));
                Double amount = cursor.getDouble(cursor.getColumnIndex(AMOUNT));
                String payment = cursor.getString(cursor.getColumnIndex(PAYMENT));
                String type = cursor.getString(cursor.getColumnIndex(TYPE));
                Date transDate = Utils.convertStringToDate(cursor.getString(cursor.getColumnIndex(TRANSACTION_DATE)));
                transactionList.add(new TransactionTable(categoryName,title,transDate,amount,payment,type));
            } while (cursor.moveToNext());
        }
        else {
            transactionList = null;
        }
        return transactionList;
    }

    /**
     * @param firstDayOfMonth holds first date of month
     * @param lastDayOfMonth holds last date of month
     * @return returns total sum of expense and income for given month
     */
    public HashMap<String, String> transactionTotalSum(String firstDayOfMonth, String lastDayOfMonth)
    {
        HashMap<String, String> totalExpenseAndIncome = new HashMap<String, String>();
        String selectQuery = "SELECT " + TYPE +", SUM(" + AMOUNT +") FROM " + TABLE_TRANSACTION
                            + " WHERE " + TRANSACTION_DATE +" BETWEEN '" +firstDayOfMonth + "' AND '" + lastDayOfMonth + "'"
                            + " GROUP BY " + TYPE;
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(selectQuery, null);

        if(cursor.getCount() > 0)
        {
            cursor.moveToFirst();
            do {
                totalExpenseAndIncome.put(cursor.getString(cursor.getColumnIndex(TYPE)), String.format("%.2f", cursor.getDouble(1)));
            } while (cursor.moveToNext());
        }
        else {
            totalExpenseAndIncome = null;
        }
        return totalExpenseAndIncome;
    }

    /**
     * @param title holds transaction title
     * @param transDate holds transaction date
     * @return returns transaction id for given title and date
     */
    public int getTransactionByTitleAndDate(String title, String transDate) {
        int transactionId = 0;

        String selectQuery = "SELECT " +  ID +" FROM " + TABLE_TRANSACTION
                + " WHERE " + TRANSACTION_DATE +" = '" + transDate + "' AND " + TRANSACTION_TITLE
                + " = '" + title + "'";
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(selectQuery, null);

        if(cursor.getCount() == 1)
        {
            cursor.moveToFirst();
            transactionId = cursor.getInt(cursor.getColumnIndex(ID));
        }

        return transactionId;
    }

    /**
     * @param firstDayOfMonth holds first date of month
     * @param lastDayOfMonth holds last date of month
     * @param categoryId holds category id
     * @param type holds type of transaction(Expense/Income)
     * @return returns the list of transaction for the given month, category id and type
     */
    public List<TransactionTable> getTransactionsByCategory(String firstDayOfMonth, String lastDayOfMonth, String categoryId, String type) {
        List<TransactionTable> transactionList = new ArrayList<TransactionTable>();

        String selectQuery = "SELECT * FROM " + TABLE_TRANSACTION
                + " WHERE " + TRANSACTION_DATE +" BETWEEN '" +firstDayOfMonth + "' AND '" + lastDayOfMonth + "'"
                + " AND " + CATEGORY_ID + " = '" + categoryId + "'"
                + " AND " + TYPE + " = '" + type + "'"
                + " ORDER BY " + TRANSACTION_DATE + " DESC";
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(selectQuery, null);

        if(cursor.getCount() > 0)
        {
            cursor.moveToFirst();
            do {
                String categoryName = getCategoryNameByID(cursor.getString(cursor.getColumnIndex(CATEGORY_ID)));
                String title = cursor.getString(cursor.getColumnIndex(TRANSACTION_TITLE));
                Double amount = cursor.getDouble(cursor.getColumnIndex(AMOUNT));
                String payment = cursor.getString(cursor.getColumnIndex(PAYMENT));
                type = cursor.getString(cursor.getColumnIndex(TYPE));
                Date transDate = Utils.convertStringToDate(cursor.getString(cursor.getColumnIndex(TRANSACTION_DATE)));
                transactionList.add(new TransactionTable(categoryName,title,transDate,amount,payment,type));
            } while (cursor.moveToNext());
        }
        else {
            transactionList = null;
        }
        return transactionList;
    }

    /**
     * @param firstDayOfMonth holds first date of month
     * @param lastDayOfMonth holds last date of month
     * @param type holds the type of tranaction(Expense/Income)
     * @return returns sum of the category for given month and type
     */
    public HashMap<String, Double> getSumOfCategoryFromTransaction(String firstDayOfMonth, String lastDayOfMonth, String type){
        HashMap<String, Double> categoryWithAmt = new HashMap<String, Double>();

        String selectQuery = "SELECT " + CATEGORY_ID + ", SUM(" + AMOUNT +") FROM " + TABLE_TRANSACTION
                + " WHERE " + TRANSACTION_DATE +" BETWEEN '" +firstDayOfMonth + "' AND '" + lastDayOfMonth + "'"
                + " AND " + TYPE + " = '" + type + "'"
                + " GROUP BY " + CATEGORY_ID +", " + TYPE;
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(selectQuery, null);

        if(cursor.getCount() > 0)
        {
            cursor.moveToFirst();
            do {
                String key = getCategoryNameByID(cursor.getString(cursor.getColumnIndex(CATEGORY_ID)));
                Double value = cursor.getDouble(1);
                categoryWithAmt.put(key,value);
            } while (cursor.moveToNext());
        }
        else {
            categoryWithAmt = null;
        }
        return categoryWithAmt;

    }

    /**
     * @param firstDayOfMonth holds first date of month
     * @param lastDayOfMonth holds last date of month
     * @param type holds the type of tranaction(Expense/Income)
     * @return returns total sum of type for given month and type
     */
    public String getSumOfTypeFromTransaction(String firstDayOfMonth, String lastDayOfMonth, String type){
        String sum = "";

        String selectQuery = "SELECT SUM(" + AMOUNT +") FROM " + TABLE_TRANSACTION
                + " WHERE " + TRANSACTION_DATE +" BETWEEN '" +firstDayOfMonth + "' AND '" + lastDayOfMonth + "'"
                + " AND " + TYPE + " = '" + type + "'";
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(selectQuery, null);

        if(cursor.getCount() == 1)
        {
            cursor.moveToFirst();
            sum = String.format("%.2f",cursor.getDouble(0));
        }
        return sum;
    }
}
