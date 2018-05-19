print("Timesheet app spreadsheets --> Google calendar events")
from glob import glob
from os.path import isfile

from pandas import read_excel, concat, DataFrame

Fm = glob(r'C:\Users\Joakim\Dropbox\JOIN\*.xlsm') # Processed (pivoted and macro-enabled) timeheets
F = [f[:-1] for f in Fm]  # Possible app timesheets
F = list(filter(isfile, F))  # Actual app timesheets
print("Importing %d spreadheets" % len(F))
dfs = list(map(read_excel, F))  # Separate DataFrames
df = concat(dfs)  # Merged DataFrame
df = df.fillna('')  # Undefined cells are cleared
print("Converting jobs to events")
df2 = DataFrame({  # Translated DataFrame
    'Subject': df['Arbetsgivare/Klient'],
    'Start Date': df['Datum'],
    'Start Time': df['Starttid'],
    'End Date': df['Datum'],
    'End Time': df['Stopptid'],
    'All Day Event': [False]*len(df),
    'Description': df['Projekt'] + '\n' + df['Beskrivning'],
    'Location': ['']*len(df)})
print("Saving events.csv for calendar.google.com")
df2.to_csv(r'C:\Users\Joakim\Dropbox\JOIN\events.csv')
