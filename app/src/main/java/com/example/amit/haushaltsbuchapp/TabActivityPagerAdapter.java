package com.example.amit.haushaltsbuchapp;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class TabActivityPagerAdapter extends FragmentStatePagerAdapter {
    /**
     * This class helps to show the layout for Expense and AddCategory in tab
     */
    public TabActivityPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {

        switch (position)
        {
            case 0:
                return new TransactionEntryFragment();
            case 1:
                return new AddCategoryFragment();
        }
        return null;
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "Expenses";
            case 1:
                return "Add Category";
        }
        return null;
    }
}
