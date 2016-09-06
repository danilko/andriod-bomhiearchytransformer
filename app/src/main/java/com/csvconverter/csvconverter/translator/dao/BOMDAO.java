package com.csvconverter.csvconverter.translator.dao;

import android.content.Context;

/**
 * Created by danilko on 1/16/16.
 */
public interface BOMDAO {
    public void translateBOM(String sourceBomFilePath, Context context);

    public void setSourceBomFilePath(String inputSourceBomFilePath);
    public void setLevelRowHeader(String inputLevelRowHeader);
    public void setItemNumberRowHeader(String inputItemNumberRowHeader);
    public void setItemNumberFatherRowHeader(String inputItemNumberFatherHeader);

    public String getStatus();
}
