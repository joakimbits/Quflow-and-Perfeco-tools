# -*- coding: cp1252 -*-
""" This modules provides a lightweight API to access Excel data.
    There are many ways to read Excel data, including ODBC. This module
    uses ADODB and has the advantage of only requiring a file name and a
    sheet name (no setup required).
http://aspn.activestate.com/ASPN/Cookbook/Python/Recipe/440661
"""

import sys
import modbus
import time
from exceldocument import ExcelDocument

class ExcelRecipy(ExcelDocument):
    """
    API for a baking recipy in Excel
    """
    starttime = None # Timestamp in seconds
    row = column = None # Row and start column for current report
    def __init__(self, *pars, **opts):
        super(ExcelRecipy, self).__init__(*pars, **opts)
        self['degCmin'] # Place cursor on program cells
        sheet, row, column, sheets, rows, columns = self.cursor
        Theader, theader = self[sheet,row,column,0,0,2]
        print Theader, theader
        assert u'\xb0C' in (Theader or ""), "Please select a cell header containing '°C''"
        assert 'min' in (theader or "").lower(), "Please define delays in 'minutes'!"
        report = self.Slice('report')
        self.row = report.Row
        self.column = report.Column
        self.starttime = time.time()
        self[self.row,self.column] = 25569. + (self.starttime/3600 + 1)/24
        self.row += 1
    def get_comport(self):
        port = str(self.comport)
        try:
            return port
        except:
            raise Error("Can't open serial port %s" % port)
    def get_temperature_time(self):
        degC, minutes = self.next()
        print degC, minutes
        self[self.row,self.column:self.column+2] = (
            (time.time() - self.starttime)/24./3600., degC)
        return degC, minutes
    def write_real_temperature(self, temperature):
        self.show_real_temperature(temperature)
        self.row += 1
    def show_real_temperature(self, temperature):
        self[self.row,self.column+2:self.column+4] = (
            (time.time() - self.starttime)/24./3600., temperature)
    def is_stopped(self):
        return self.stop

class TemperatureController:
    """
    COM server for remote control of baking from Excel.
    """
    _public_methods_ = ['Start', 'Stop'] # Remote controllable methods.
    _reg_clsid_ = '{B616313C-34BF-4F34-BF84-FCE046FF3D71}' # COM class ID = CreateGuid() 
    _reg_progid_ = 'Python.TemperatureController' # COM program ID
    processes = {} # Active recipies indexed by comport.
    def Start(self, *pars, **opts):
        "Start controlling oven"
        recipy = ExcelRecipy(*pars, **opts)
        comport = recipy.get_comport()
        self.processes[comport] = recipy
        recipy.stop = False
        regulator = modbus.ModBus(port=comport)
        regulator.openPort()
        (temperature, minutes) = recipy.get_temperature_time() #temperature = float = 20.0, minutes = int = 5
        seconds = 0
        while temperature and minutes and not recipy.stop: #Loop until we have no more excel values
            print 'Setting temperature to: %.1f °C' % temperature
            regulator.set_temp(temperature)
            recipy.show_real_temperature(regulator.get_temp())
            while minutes and not recipy.stop: #Loop until time is out
                real_temperature = regulator.get_temp()
                print 'Measured temperature is: %.1f °C. '\
                'Time to wait is %d minutes. Set temperature is %.1f °C.' % ( 
                    real_temperature, minutes, temperature)
                recipy.row += 1
                for i in range(int(min(1, minutes)*60)):
                    seconds += 1
                    time.sleep(recipy.starttime + seconds - time.time())
                    recipy.show_real_temperature(regulator.get_temp())
                    if recipy.stop:
                        break
                minutes = max(0, minutes - 1)
            (temperature, minutes) = recipy.get_temperature_time() #temperature = float = 20.0, minutes = int = 5
        if recipy.stop:
            print 'Aborted.'
        regulator.closePort()
        print 'Done.'
    def Stop(self, comport):
        "Stop controlling oven"
        self.processes[comport].stop = True

if __name__ == '__main__':
    import sys
    params = sys.argv[1:]
    controller = TemperatureController()
    """
    debugging = 1
    if debugging:
        from pythoncom import CreateGuid
        from win32com.server.dispatcher import DefaultDebugDispatcher
        useDispatcher = DefaultDebugDispatcher
    else:
        useDispatcher = None
    print "Registering Python.TemperatureController"
        from win32com.server.register import UseCommandLine
        UseCommandLine(TemperatureController, debug=debugging)
    """
    if len(params)>2:
        print "Start", params
        controller.Start(*params)
    else:
        controller.Start()
    print "Done!"
