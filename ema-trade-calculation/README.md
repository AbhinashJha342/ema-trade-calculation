The project first extracts the required data and creates a csv file which is passed for next stage of processing.
The incoming file is the csv file created using the data from yahoo finance.
The incoming file is picked from the folder <b>RawDataFromYahooFinance</b> folder. The file created is then put in <b>Simplified_historical_data</b> folder.
The next stages of processing are done using the folder mentioned last.

# Header of the incoming file
"Date","Open","High","Low","Close","Volume","high - low %","close near low","open-close %","high-close %","fast ema","22-d ema","EMA diff %","low - fast ema %","% age move before coming back to fast ema","","high-fast ema %","close below last day","daily close % age increase","consolidated","EMA appreciation %"

Date -  should be in "General" format.

Errors: If the first column, "Date" in this case, is read as null. The do the following:
1. File->File properties->Remove BOM