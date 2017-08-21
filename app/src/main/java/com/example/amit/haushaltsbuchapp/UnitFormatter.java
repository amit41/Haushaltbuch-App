package com.example.amit.haushaltsbuchapp;

import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.text.DecimalFormat;

public class UnitFormatter extends PercentFormatter {
    /**
     * This class overrides the method of PercentFormatter of MPAndroidChart API
     * to show euro symbol in amount
     */
    protected DecimalFormat mFormat;
    protected String unitChar;

    public UnitFormatter(String unitChar) {
        this.mFormat = new DecimalFormat("###,###,##0.0");
        this.unitChar = unitChar;
    }

    public UnitFormatter(DecimalFormat format) {
        this.mFormat = format;
    }

    public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
        return this.mFormat.format((double)value) + unitChar;
    }

    public String getFormattedValue(float value, YAxis yAxis) {
        return this.mFormat.format((double)value) + unitChar;
    }
}
