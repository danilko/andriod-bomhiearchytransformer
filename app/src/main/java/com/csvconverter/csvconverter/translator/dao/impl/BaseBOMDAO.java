package com.csvconverter.csvconverter.translator.dao.impl;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.os.Environment;

import com.csvconverter.csvconverter.translator.dao.BOMDAO;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by danilko on 1/16/16.
 */
public class BaseBOMDAO implements BOMDAO {

    private static String LEVEL_ROW_HEADER_DEFAULT = "Level";
    private static String ITEM_NUMBER_ROW_HEADER_DEFAULT = "Item Number";
    private static String ITEM_NUMBER_FATHER_HEADER_DEFAULT = "Father Number";

    private String sourceBomFilePath;

    private String levelRowHeader;
    private String itemNumberRowHeader;
    private String itemNumberFatherRowHeader;

    private static String CACHE_SOURCE_FILE = "cache_source_bom.csv";
    private static String CACHE_DEST_FILE = "cache_dest_bom.csv";

    private String currentStatus = null;

    public BaseBOMDAO() {
        levelRowHeader = LEVEL_ROW_HEADER_DEFAULT;
        itemNumberRowHeader = ITEM_NUMBER_ROW_HEADER_DEFAULT;
        itemNumberFatherRowHeader = ITEM_NUMBER_FATHER_HEADER_DEFAULT;
    }

    private void copyFile(File fileSrc, File fileDest) throws IOException {
        InputStream in = new FileInputStream(fileSrc);
        OutputStream out = new FileOutputStream(fileDest);

        byte[] buffer = new byte[1024];

        int length = 0;

        while ((length = in.read(buffer)) > 0) {
            out.write(buffer, 0, length);
        }
        in.close();
        out.flush();
        out.close();
    }

    @Override
    public void translateBOM(String sourceBOMFilePath, Context context) {
        try {
            sourceBomFilePath = sourceBOMFilePath;

            setStatus("Prepare Input");

            File sdCard = Environment.getExternalStorageDirectory();
            copyFile(new File(sdCard, sourceBOMFilePath), new File(context.getFilesDir(), CACHE_SOURCE_FILE));
            // use first row as header; otherwise defaults are fine
            CsvMapper mapper = new CsvMapper();
            CsvSchema inputSchema = CsvSchema.emptySchema().withHeader();
            MappingIterator<Map<String, String>> it = mapper.readerFor(Map.class)
                    .with(inputSchema)
                    .readValues(new File(context.getFilesDir(), CACHE_SOURCE_FILE));

            // New file path
            String outputBOMFilePath = sourceBOMFilePath.replace(".csv", "_output.csv");
            File targetFile = new File(sdCard, outputBOMFilePath);

            // Remove the original target file
            targetFile.deleteOnExit();


            // Level Mapping
            Map<Integer, String> bomOBjectRelationMapping = new HashMap<Integer, String>();

            // List to track the rows
            List<Map<String, String>> resultList = new ArrayList<Map<String, String>>();

            int currentRowCount = 1;

            while (it.hasNext()) {
                Map<String, String> rowAsMap = it.next();

                int currentLevel = 0;
                String columnString = null;

                try {
                    String currentLevelString = rowAsMap.get(levelRowHeader);
                    currentLevel = Integer.parseInt(currentLevelString);
                } catch (NumberFormatException e) {
                    throw new IOException("Error in retrieving values from following column: " + levelRowHeader + " at row " + currentRowCount + " with value: " + columnString + ", may cause by invalid column name or invalid value");
                }

                columnString = rowAsMap.get(itemNumberRowHeader);

                if (columnString == null || columnString.length() == 0) {
                    throw new IOException("Error in retrieving values from following column: " + itemNumberRowHeader + " at row " + currentRowCount + " with value: " + columnString + ", may cause by invalid column name or invalid value");
                }

                bomOBjectRelationMapping.put(currentLevel, columnString);

                String topBomNumber = null;

                if (currentLevel == 0) {
                    topBomNumber = "";
                } else {
                    topBomNumber = bomOBjectRelationMapping.get(currentLevel - 1);
                }

                rowAsMap.put(itemNumberFatherRowHeader, topBomNumber);

                resultList.add(rowAsMap);

                // Increment row count
                currentRowCount++;
            }

            // IF the list is not empty, copy them into new file
            if (resultList.size() > 0) {

                // Ouptut Schema Mapper
                CsvSchema.Builder outputSchema = new CsvSchema.Builder();
                outputSchema = outputSchema.setColumnSeparator(',').setUseHeader(true);

                // Ensure the new column is the first one
                outputSchema.addColumn(itemNumberFatherRowHeader, CsvSchema.ColumnType.STRING);
                Map<String, String> map = resultList.get(0);
                // Create rest of the header
                for (String value : map.keySet()) {
                    if (!value.equalsIgnoreCase(itemNumberFatherRowHeader)) {
                        outputSchema.addColumn(value, CsvSchema.ColumnType.STRING);
                    }
                }

                ObjectWriter objectWriter = new CsvMapper().writerFor(Map.class).with(outputSchema.build());


                objectWriter.writeValues(new File(context.getFilesDir(), CACHE_DEST_FILE)).writeAll(resultList);

                copyFile(new File(context.getFilesDir(), CACHE_DEST_FILE), targetFile);


                setStatus("New file created at " + outputBOMFilePath + ", if connected with the computer through usb, please detach the device from computer and connected again");

                MediaScannerConnection.scanFile(context, new String[] { targetFile.getAbsolutePath() }, null, null);
            } else {
                setStatus("No appropriate rows find in input table");
            }

        } catch (IOException e) {
            setStatus("Error: " + e.getMessage());
        }
    }

    @Override
    public void setSourceBomFilePath(String inputSourceBomFilePath)
    {
        sourceBomFilePath = inputSourceBomFilePath;
    }

    @Override
    public void setLevelRowHeader(String inputLevelRowHeader)
    {
        levelRowHeader = inputLevelRowHeader;
    }

    @Override
    public void setItemNumberRowHeader(String inputItemNumberRowHeader)
    {
        itemNumberRowHeader= inputItemNumberRowHeader;
    }

    @Override
    public void setItemNumberFatherRowHeader(String inputItemNumberFatherRowHeader)
    {
        itemNumberFatherRowHeader = inputItemNumberFatherRowHeader;
    }

    private void setStatus(String inputStatus)
    {
        currentStatus = inputStatus;
    }

    @Override
    public String getStatus() {
        return currentStatus;
    }
}
