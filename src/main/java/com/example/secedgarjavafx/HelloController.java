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
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Scanner;

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

    DecimalFormat decimalFormat = new DecimalFormat("#0.00"); // Example: Two decimal places

    NumberFormat nf = NumberFormat.getInstance(Locale.GERMANY); // or Locale.FRANCE


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

    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Welcome to JavaFX Application!");
    }

    @FXML
    private void handleIterateTickers() throws IOException {

        //Creating Scanner instance to read File in Java
        Scanner scnr = new Scanner(text);

        // while(scnr.hasNextLine())

        for(int i = 0; i < 12084; i++)  {
            String lineTemp = scnr.nextLine();
            String[] lineSplitted = lineTemp.split("\\s+");
            cikTickers.put(lineSplitted[0], lineSplitted[1]);

        }

        String tickers = cikInput.getText();
        String[] tickerArray = tickers.split("\\s+");

        // Create a StringBuilder to build the CSV content
        StringBuilder csvContent = new StringBuilder();

        // Iterate through the tickers
        for (String ticker : tickerArray) {
            // Append the ticker to the CSV content
            cikHolders.add(ticker);
            System.out.println(ticker);
        }



        // Specify the file path for the CSV file
        String filePath = "output/file.csv";


        // Write the CSV content to the file
        try (PrintWriter writer = new PrintWriter(filePath)) {
            String addTheseZeros = "";

             for (String ticker : tickerArray) {
                // Append the ticker to the CSV content
                 if(cikTickers.containsKey(ticker)) {
                     if (ticker.length() <= 10) {
                         int remainder = 10 - cikTickers.get(ticker).length();

                         for (int i = 0; i < remainder; i++) {
                             addTheseZeros += "0";
                         }
                     }
                 }
                 else {
                     //resultGenerated.setText("ERROR: Ticker not found: " + ticker);
                     throw new TickerNotFoundException(ticker);

                 }

                String tickerText = addTheseZeros+cikTickers.get(ticker);
                cikWithData.add(tickerText);
                System.out.println(ticker);
                System.out.println(tickerText);
                long netIncomeLoss = Long.parseLong(returnQueryResult(tickerText, "NetIncomeLoss").trim());
                long assets = Long.parseLong(returnQueryResult(tickerText, "Assets").trim());
                System.out.println("Net Income Loss: " + netIncomeLoss);
                System.out.println("Assets: " + assets);
                double roaWithDecimals = ((double)netIncomeLoss / assets)*100;
                String roa = decimalFormat.format(roaWithDecimals);
                System.out.println("ROA: " + roa);
                long operatingCashFlow = Long.parseLong(returnQueryResult(tickerText, "NetCashProvidedByUsedInOperatingActivities").trim());
                String cashFlowNetIncomeComparison = "";
                long longTermDebt = Long.parseLong(returnQueryResult(tickerText, "LongTermDebtNoncurrent").trim());
                double ltdTa = (double)longTermDebt/(double)assets;
                long currentAssets = Long.parseLong(returnQueryResult(tickerText, "AssetsCurrent").trim());
                long currentLiabilities = Long.parseLong(returnQueryResult(tickerText, "LiabilitiesCurrent").trim());
                double currentRatio = (double)currentAssets/(double)currentLiabilities;
                 long avgDilutedSharesOutstanding = 0;
                try {
                    avgDilutedSharesOutstanding = Long.parseLong(returnQueryResult(tickerText, "WeightedAverageNumberOfDilutedSharesOutstanding").trim());
                }
                catch(NumberFormatException e) {
                    cikInput1.appendText("Skipped AVG Shares Diluted for entry: "+ticker+"\n");

                }

                long grossProfit = 0;

                 try {
                     grossProfit = Long.parseLong(returnQueryResult(tickerText, "GrossProfit").trim());
                 }
                 catch(NumberFormatException e) {
                     cikInput1.appendText("Skipped Gross Margin for entry: "+ticker+"\n");
                 }

               /*  try {
                     //insert all existing code queries into try-catch blocks to avoid tickers that have no data from edgar
                 }*/




                long salesToCustomers = Long.parseLong(returnQueryResult(tickerText, "RevenueFromContractWithCustomerExcludingAssessedTax").trim());
                double grossMargin = ((double)grossProfit/(double)salesToCustomers)*100;



                 long netIncomeLossMinusOne = Long.parseLong(returnQueryResultMinusOne(tickerText, "NetIncomeLoss"));
                long assetsMinusOne = Long.parseLong(returnQueryResultMinusOne(tickerText, "Assets"));
                System.out.println("Net Income Loss-1: " + netIncomeLossMinusOne);
                System.out.println("Assets-1: " + assetsMinusOne);
                double roaMinusOneWithDecimals = ((double)netIncomeLossMinusOne/assetsMinusOne)*100;
                String roaMinusOne = decimalFormat.format(roaMinusOneWithDecimals);
                System.out.println("roa-1: "+roaMinusOne);
                long operatingCashFlowMinusOne = Long.parseLong(returnQueryResultMinusOne(tickerText, "NetCashProvidedByUsedInOperatingActivities").trim());
                String cashFlowNetIncomeComparisonMinusOne = "";
                long longTermDebtMinusOne = Long.parseLong(returnQueryResultMinusOne(tickerText, "LongTermDebtNoncurrent").trim());
                double ltdTaMinusOne = (double)longTermDebtMinusOne/(double)assetsMinusOne;
                long currentAssetsMinusOne = Long.parseLong(returnQueryResultMinusOne(tickerText, "AssetsCurrent").trim());
                long currentLiabilitiesMinusOne = Long.parseLong(returnQueryResultMinusOne(tickerText, "LiabilitiesCurrent").trim());
                double currentRatioMinusOne = (double)currentAssetsMinusOne/(double)currentLiabilitiesMinusOne;
                 long avgDilutedSharesOutstandingMinusOne = 0;
                 try {
                     avgDilutedSharesOutstandingMinusOne = Long.parseLong(returnQueryResultMinusOne(tickerText, "WeightedAverageNumberOfDilutedSharesOutstanding").trim());
                 }
                 catch(HttpStatusException e) {
                     cikInput1.appendText("Skipped AVG Shares Diluted for entry: "+ticker+"\n");
                 }

                 long grossProfitMinusOne= 0;

                 try {
                     grossProfit = Long.parseLong(returnQueryResultMinusOne(tickerText, "GrossProfit").trim());
                 }
                 catch(NumberFormatException e) {
                     cikInput1.appendText("Skipped Gross Margin for entry: "+ticker+"\n");
                 }

                 long salesToCustomersMinusOne = Long.parseLong(returnQueryResultMinusOne(tickerText, "RevenueFromContractWithCustomerExcludingAssessedTax").trim());
                double grossMarginMinusOne = ((double)grossProfitMinusOne/(double)salesToCustomersMinusOne)*100;



                 if(netIncomeLoss > assets) {
                    cashFlowNetIncomeComparison = "CF Higher than NI";
                }
                else {
                    cashFlowNetIncomeComparison = "CF Not higher than NI";
                }

                 if(netIncomeLoss > assets) {
                     cashFlowNetIncomeComparisonMinusOne = "CF Higher than NI";
                 }
                 else {
                     cashFlowNetIncomeComparisonMinusOne = "CF Not higher than NI";
                 }

                 double assetTurnover = (double)salesToCustomers/(((double)assets+(double)assetsMinusOne)/2);


                 csvContent.append(ticker+";")
                        .append(nf.format(netIncomeLoss)+" NET INCOME;")
                        .append(roaWithDecimals+" ROA;")
                        .append(roaMinusOneWithDecimals+" ROA-1;")
                        .append(nf.format(operatingCashFlow)+" OPERATING CASH FLOW;")
                        .append(nf.format(operatingCashFlowMinusOne)+" OPERATING CASH FLOW-1;")
                         .append(cashFlowNetIncomeComparison+";")
                         .append(cashFlowNetIncomeComparisonMinusOne+";")
                         .append(nf.format(ltdTa)+" LTD/TA;")
                         .append(nf.format(ltdTaMinusOne)+" LTD/TA-1;")
                         .append(nf.format(currentRatio)+" LATESTRATIO;")
                         .append(nf.format(currentRatioMinusOne)+" LATESTRATIO-1;")
                         .append(nf.format(avgDilutedSharesOutstanding)+" AVG DIL. SHARES OUT.;")
                         .append(nf.format(avgDilutedSharesOutstandingMinusOne)+" AVG DIL. SHARES OUT.-1;")
                         .append(nf.format(grossMargin)+" GROSS MARGIN;")
                         .append(nf.format(grossMarginMinusOne)+" GROSS MARGIN-1;")
                         .append(nf.format(assetTurnover)+" ASSET TURNOVER;")





                         .append(System.lineSeparator());
                addTheseZeros = "";

            }
            writer.write(csvContent.toString());
            resultGenerated.setText("CSV Generated");

        } catch (IOException e) {
            e.printStackTrace();
        }
        catch (TickerNotFoundException e) {
            cikInput1.appendText("ERROR: Ticker not found: " + e.getTicker());
        }
    }
}