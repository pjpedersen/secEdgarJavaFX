package com.example.secedgarjavafx;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Scanner;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;


public class HelloController {
    @FXML
    private Label welcomeText;

    @FXML
    private TextArea cikInput;

    @FXML
    private Label resultGenerated;

    @FXML
    private TextArea cikInput1;

    ArrayList<String> cikHolders = new ArrayList<>();

    ArrayList<String> cikWithData = new ArrayList<>();


    HashMap<String, String> cikTickers = new HashMap<String, String>();

    DecimalFormatSymbols dfs = new DecimalFormatSymbols(Locale.US);
    DecimalFormat decimalFormat = new DecimalFormat("#0.00", dfs); // Example: Two decimal places

    NumberFormat nf = NumberFormat.getInstance(Locale.US); // or Locale.FRANCE


    //creating File instance to reference text file in Java
    File text = new File("src/main/resources/com/example/secedgarjavafx/ticker.txt");

    public String returnQueryResultMinusOne(String ticker, String xbrlTag) throws HttpStatusException{
        String connectString = "https://data.sec.gov/api/xbrl/companyconcept/CIK" + ticker + "/us-gaap/" + xbrlTag + ".json";
        System.out.println(connectString);
        String val = "";
        Document document = null;
        try {
            // Connect to the URL and fetch JSON data
            document = Jsoup.connect(connectString)
                    .data("query", "Java")
                    .userAgent("Test-1.0")

                    .timeout(3000)
                    .ignoreContentType(true)
                    .get();
        } catch(HttpStatusException e) {
            resultGenerated.setText("Avg shares diluted skipped for some entries");
        } catch (IOException e) {
            throw new RuntimeException(e);

        }
        try{

            JsonElement jsonElement = JsonParser.parseString(document.text());
            System.out.println("JSON Response: " + jsonElement);

            // Parse JSON using Gson
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            System.out.println("Parsed JSON Object: " + jsonObject);

            // Get the 'units' object
            JsonObject units = jsonObject.getAsJsonObject("units");
            JsonArray usdArray = null;

            // Get the 'USD' array
            if(units.getAsJsonArray("shares")!=null) {
                usdArray = units.getAsJsonArray("shares");
            }
            else if (units.getAsJsonArray("USD")!=null) {
                usdArray = units.getAsJsonArray("USD");
            }



            System.out.println("USDARRAY PRINTED:"+usdArray);
            // Find the latest 10-K filing year
            int latestTenKYearNumeric = 0;
            int previousYear;
            for (int i = usdArray.size() - 1; i >= 0; i--) {
                JsonObject usdObject = usdArray.get(i).getAsJsonObject();
                String form = usdObject.get("form").getAsString();
                String fy = usdObject.get("filed").getAsString().substring(0, 4);

                if (form.equals("10-K")) {
                    latestTenKYearNumeric = Integer.parseInt(fy);
                    break;
                }
            }



            // Calculate the previous year
            if(latestTenKYearNumeric < Calendar.getInstance().get(Calendar.YEAR)) {
                previousYear = latestTenKYearNumeric - 1;
            }
            else {
                previousYear = latestTenKYearNumeric - 2;
            }
            System.out.println("TESTUSDARRAY BEFORE LAST LOOP"+usdArray);
            // Find the corresponding data for the previous year
            for (int i = usdArray.size() - 1; i >= 0; i--) {
                JsonObject usdObject = usdArray.get(i).getAsJsonObject();
                String form = usdObject.get("form").getAsString();
                String fy = usdObject.get("fy").getAsString().substring(0, 4);

                System.out.println(usdArray.get(i)); // TEST THE OBJECT

                if (form.equals("10-K") && Integer.parseInt(fy) == previousYear) {
                    String accn = usdObject.get("accn").getAsString();
                    String filed = usdObject.get("filed").getAsString();
                    val = usdObject.get("val").getAsString();

                    System.out.println("10-K Filing (Previous Year):");
                    System.out.println("ACCN: " + accn);
                    System.out.println("Filed: " + filed);
                    System.out.println("Value: " + val);
                    break;
                }

            }
        } catch (Exception e) {
            resultGenerated.setText("Error");
        }

        if (val.isEmpty()) {
            // Set a default value or handle it differently
            val = "0"; // Assuming zero as the default value
        }

        return val;
    }

    public String returnQueryResult(String ticker, String xbrlTag) throws HttpStatusException{

    String connectString = "https://data.sec.gov/api/xbrl/companyconcept/CIK"+ticker+"/us-gaap/"+xbrlTag+".json";
        System.out.println(connectString);
    String val = "";
        Document document = null;
        try {
            // Connect to the URL and fetch JSON data
            document = Jsoup.connect(connectString)
                    .data("query", "Java")
                    .userAgent("Test-1.0")

                    .timeout(3000)
                    .ignoreContentType(true)
                    .get();
        } catch(HttpStatusException e) {
            resultGenerated.setText("Avg shares diluted skipped for some entries");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try{

        JsonElement jsonElement = JsonParser.parseString(document.text());

        // Parse JSON using Gson
        JsonObject jsonObject = jsonElement.getAsJsonObject();

        // Get the array of entries

// Get the 'units' object
        JsonObject units = jsonObject.getAsJsonObject("units");

// Get the 'USD' array
        JsonArray usdArray = null;

        // Get the 'USD' array
        if(units.getAsJsonArray("shares")!=null) {
            usdArray = units.getAsJsonArray("shares");
        }
        else if (units.getAsJsonArray("USD")!=null) {
            usdArray = units.getAsJsonArray("USD");
        }

// Iterate over the 'USD' array in reverse order to find the latest 10-K filing
        for (int i = usdArray.size() - 1; i >= 0; i--) {
            JsonObject usdObject = usdArray.get(i).getAsJsonObject();
            String form = usdObject.get("form").getAsString();

            // Check if the form is '10-K'
            if (form.equals("10-K")) {
                // Extract the required data from the 10-K filing
                String accn = usdObject.get("accn").getAsString();
                String filed = usdObject.get("filed").getAsString();
                val = usdObject.get("val").getAsString();

                System.out.println("10-K Filing:");
                System.out.println("ACCN: " + accn);
                System.out.println("Filed: " + filed);
                System.out.println("Value: " + val);

                // Exit the loop after finding the latest 10-K filing
                break;
            }
        }
    } catch(Exception e) {
            System.out.println("error");
        }

        return val;
    }

    private long parseLongOrZero(String numberString) throws NumberFormatException {
        if (numberString == null || numberString.trim().isEmpty()) {
            throw new NumberFormatException("Empty or null string");
        }
        return Long.parseLong(numberString.trim());
    }



    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Welcome to JavaFX Application!");
    }

    @FXML
    public void handleIterateTickers() {
        readTickersFromFile();

        String tickers = cikInput.getText();
        String[] tickerArray = tickers.split("\\s+");

        StringBuilder csvContent = new StringBuilder();
        String filePath = "output/file.csv";

        try (PrintWriter writer = new PrintWriter(filePath)) {
            for (String ticker : tickerArray) {
                try {
                    String tickerText = formatTicker(ticker);
                    processTickerData(tickerText, csvContent, ticker);
                } catch (NumberFormatException | TickerNotFoundException e) {
                    cikInput1.appendText("Skipped processing for entry: " + ticker + " due to " + e.getMessage() + "\n");
                }
            }
            writer.write(csvContent.toString());
            resultGenerated.setText("CSV Generated");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readTickersFromFile() {
        try (Scanner scnr = new Scanner(text)) {
            for (int i = 0; i < 12084; i++) {
                String line = scnr.nextLine();
                String[] lineSplitted = line.split("\\s+");
                cikTickers.put(lineSplitted[0], lineSplitted[1]);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private String formatTicker(String ticker) throws TickerNotFoundException {
        if (!cikTickers.containsKey(ticker)) {
            throw new TickerNotFoundException(ticker);
        }
        String zeros = "0".repeat(10 - cikTickers.get(ticker).length());
        return zeros + cikTickers.get(ticker);
    }
    private void processTickerData(String tickerText, StringBuilder csvContent, String ticker) {
        ticker = ticker.toUpperCase();
        long netIncomeLoss = 0, assets = 0, operatingCashFlow = 0, longTermDebt = 0;
        long currentAssets = 0, currentLiabilities = 0, avgDilutedSharesOutstanding = 0, grossProfit = 0;
        long salesToCustomers = 0;
        String cashFlowNetIncomeComparison = "";


        long netIncomeLossMinusOne = 0, assetsMinusOne = 0, operatingCashFlowMinusOne = 0, longTermDebtMinusOne = 0;
        long currentAssetsMinusOne = 0, currentLiabilitiesMinusOne = 0, avgDilutedSharesOutstandingMinusOne = 0, grossProfitMinusOne = 0;
        long salesToCustomersMinusOne = 0;
        String cashFlowNetIncomeComparisonMinusOne = "";



        // Try-catch blocks for current year data
        try {
            netIncomeLoss = parseLongOrZero(returnQueryResult(tickerText, "NetIncomeLoss").trim());
        } catch (NumberFormatException e) {
            cikInput1.appendText("Error parsing NetIncomeLoss for " + ticker + "\n");
        } catch (HttpStatusException e) {
            throw new RuntimeException(e);
        }

        try {
            assets = parseLongOrZero(returnQueryResult(tickerText, "Assets").trim());
        } catch (NumberFormatException e) {
            cikInput1.appendText("Error parsing Assets for " + ticker + "\n");
        } catch (HttpStatusException e) {
            throw new RuntimeException(e);
        }

        try {
            operatingCashFlow = parseLongOrZero(returnQueryResult(tickerText, "NetCashProvidedByUsedInOperatingActivities").trim());
        } catch (NumberFormatException e) {
            cikInput1.appendText("Error parsing OperatingCashFlow for " + ticker + "\n");
        } catch (HttpStatusException e) {
            throw new RuntimeException(e);
        }

        try {
            longTermDebt = parseLongOrZero(returnQueryResult(tickerText, "LongTermDebtNoncurrent").trim());
        } catch (NumberFormatException e) {
            cikInput1.appendText("Error parsing LongTermDebt for " + ticker + "\n");
        } catch (HttpStatusException e) {
            throw new RuntimeException(e);
        }

        try {
            currentAssets = parseLongOrZero(returnQueryResult(tickerText, "AssetsCurrent").trim());
        } catch (NumberFormatException e) {
            cikInput1.appendText("Error parsing CurrentAssets for " + ticker + "\n");
        } catch (HttpStatusException e) {
            throw new RuntimeException(e);
        }

        try {
            currentLiabilities = parseLongOrZero(returnQueryResult(tickerText, "LiabilitiesCurrent").trim());
        } catch (NumberFormatException e) {
            cikInput1.appendText("Error parsing CurrentLiabilities for " + ticker + "\n");
        } catch (HttpStatusException e) {
            throw new RuntimeException(e);
        }

        try {
            avgDilutedSharesOutstanding = parseLongOrZero(returnQueryResult(tickerText, "WeightedAverageNumberOfDilutedSharesOutstanding").trim());
        } catch (NumberFormatException e) {
            cikInput1.appendText("Error parsing AvgDilutedSharesOutstanding for " + ticker + "\n");
        } catch (HttpStatusException e) {
            throw new RuntimeException(e);
        }

        try {
            grossProfit = parseLongOrZero(returnQueryResult(tickerText, "GrossProfit").trim());
        } catch (NumberFormatException e) {
            cikInput1.appendText("Error parsing GrossProfit for " + ticker + "\n");
        } catch (HttpStatusException e) {
            throw new RuntimeException(e);
        }

        try {
            salesToCustomers = parseLongOrZero(returnQueryResult(tickerText, "RevenueFromContractWithCustomerExcludingAssessedTax").trim());
        } catch (NumberFormatException | HttpStatusException e) {
            cikInput1.appendText("Error parsing SalesToCustomers for " + ticker + "\n");
        }
        // Try-catch blocks for previous year data

        try {
            netIncomeLossMinusOne = parseLongOrZero(returnQueryResultMinusOne(tickerText, "NetIncomeLoss").trim());
        } catch (NumberFormatException e) {
            cikInput1.appendText("Error parsing NetIncomeLoss for " + ticker + "\n");
        } catch (
                HttpStatusException e) {
            throw new RuntimeException(e);
        }

        try {
            assetsMinusOne = parseLongOrZero(returnQueryResultMinusOne(tickerText, "Assets").trim());
        } catch (NumberFormatException e) {
            cikInput1.appendText("Error parsing Assets for " + ticker + "\n");
        } catch (HttpStatusException e) {
            throw new RuntimeException(e);
        }

        try {
            operatingCashFlowMinusOne = parseLongOrZero(returnQueryResultMinusOne(tickerText, "NetCashProvidedByUsedInOperatingActivities").trim());
        } catch (NumberFormatException e) {
            cikInput1.appendText("Error parsing OperatingCashFlow for " + ticker + "\n");
        } catch (HttpStatusException e) {
            throw new RuntimeException(e);
        }

        try {
            longTermDebtMinusOne = parseLongOrZero(returnQueryResultMinusOne(tickerText, "LongTermDebtNoncurrent").trim());
        } catch (NumberFormatException e) {
            cikInput1.appendText("Error parsing LongTermDebt for " + ticker + "\n");
        } catch (HttpStatusException e) {
            throw new RuntimeException(e);
        }

        try {
            currentAssetsMinusOne = parseLongOrZero(returnQueryResultMinusOne(tickerText, "AssetsCurrent").trim());
        } catch (NumberFormatException e) {
            cikInput1.appendText("Error parsing CurrentAssets for " + ticker + "\n");
        } catch (HttpStatusException e) {
            throw new RuntimeException(e);
        }

        try {
            currentLiabilitiesMinusOne = parseLongOrZero(returnQueryResultMinusOne(tickerText, "LiabilitiesCurrent").trim());
        } catch (NumberFormatException e) {
            cikInput1.appendText("Error parsing CurrentLiabilities for " + ticker + "\n");
        } catch (HttpStatusException e) {
            throw new RuntimeException(e);
        }

        try {
            avgDilutedSharesOutstandingMinusOne = parseLongOrZero(returnQueryResultMinusOne(tickerText, "WeightedAverageNumberOfDilutedSharesOutstanding").trim());
        } catch (NumberFormatException e) {
            cikInput1.appendText("Error parsing AvgDilutedSharesOutstanding for " + ticker + "\n");
        } catch (HttpStatusException e) {
            throw new RuntimeException(e);
        }

        try {
            grossProfitMinusOne = parseLongOrZero(returnQueryResultMinusOne(tickerText, "GrossProfit").trim());
        } catch (NumberFormatException e) {
            cikInput1.appendText("Error parsing GrossProfit for " + ticker + "\n");
        } catch (HttpStatusException e) {
            throw new RuntimeException(e);
        }

        try {
            salesToCustomersMinusOne = parseLongOrZero(returnQueryResultMinusOne(tickerText, "RevenueFromContractWithCustomerExcludingAssessedTax").trim());
        } catch (NumberFormatException | HttpStatusException e) {
            cikInput1.appendText("Error parsing SalesToCustomers for " + ticker + "\n");
        }

        // Perform calculations for both current year and previous year
        double roa = calculateRatio(netIncomeLoss, assets);
        double ltdTa = calculateRatio(longTermDebt, assets);
        double currentRatio = calculateRatio(currentAssets, currentLiabilities);
        double grossMargin = calculateRatio(grossProfit, salesToCustomers);

        double roaMinusOne = calculateRatio(netIncomeLossMinusOne, assetsMinusOne);
        double ltdTaMinusOne = calculateRatio(longTermDebtMinusOne, assetsMinusOne);
        double currentRatioMinusOne = calculateRatio(currentAssetsMinusOne, currentLiabilitiesMinusOne);
        double grossMarginMinusOne = calculateRatio(grossProfitMinusOne, salesToCustomersMinusOne);

        if(netIncomeLoss > assets) {
            cashFlowNetIncomeComparison = "CF Higher than NI";
        }
        else {
            cashFlowNetIncomeComparison = "CF Not higher than NI";
        }

        if(netIncomeLoss > assets) {
            cashFlowNetIncomeComparisonMinusOne = "CF Higher than NI-1";
        }
        else {
            cashFlowNetIncomeComparisonMinusOne = "CF Not higher than NI-1";
        }


        // Append formatted data to csvContent
        csvContent.append(ticker).append(";")
                // Current year data
                .append(nf.format(netIncomeLoss)).append(" NET INCOME;")
                .append(nf.format(netIncomeLossMinusOne)).append(" NET INCOME-1;")
                .append(decimalFormat.format(roa)).append(" ROA;")
                .append(decimalFormat.format(roaMinusOne)).append(" ROA-1;")
                .append(nf.format(operatingCashFlow)).append(" OPERATING CASH FLOW;")
                .append(nf.format(operatingCashFlowMinusOne)).append(" OPERATING CASH FLOW-1;")
                .append(cashFlowNetIncomeComparison+";")
                .append(cashFlowNetIncomeComparisonMinusOne+";")
                .append(nf.format(longTermDebt)).append(" LONG TERM DEBT;")
                .append(nf.format(longTermDebtMinusOne)).append(" LONG TERM DEBT-1;")
                .append(nf.format(ltdTa)+" LTD/TA;")
                .append(nf.format(ltdTaMinusOne)+" LTD/TA-1;")
                .append(nf.format(currentAssets)).append(" CURRENT ASSETS;")
                .append(nf.format(currentAssetsMinusOne)).append(" CURRENT ASSETS-1;")
                .append(nf.format(currentLiabilities)).append(" CURRENT LIABILITIES;")
                .append(nf.format(currentLiabilitiesMinusOne)).append(" CURRENT LIABILITIES-1;")
                .append(nf.format(currentRatio)+" LATESTRATIO;")
                .append(nf.format(currentRatioMinusOne)+" LATESTRATIO-1;")
                .append(nf.format(avgDilutedSharesOutstanding)).append(" AVG DIL. SHARES OUT;")
                .append(nf.format(avgDilutedSharesOutstandingMinusOne)).append(" AVG DIL. SHARES OUT-1;")
                .append(nf.format(grossProfit)).append(" GROSS PROFIT;")
                .append(nf.format(grossProfitMinusOne)).append(" GROSS PROFIT-1;")
                .append(nf.format(salesToCustomers)).append(" SALES TO CUSTOMERS;")
                .append(nf.format(salesToCustomersMinusOne)).append(" SALES TO CUSTOMERS-1;")
                .append(decimalFormat.format(grossMargin)).append(" GROSS MARGIN;")
                .append(decimalFormat.format(grossMarginMinusOne)).append(" GROSS MARGIN-1;")

                // Previous year data
                .append(System.lineSeparator());
    }

    private double calculateRatio(long numerator, long denominator) {
        return (denominator != 0) ? (double) numerator / denominator * 100 : 0;
    }


}