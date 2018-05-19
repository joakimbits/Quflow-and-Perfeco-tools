# -*- coding: utf-8 -*-
import sys
import modbus
import excelbaker_dummy as excelbaker
import time

def main(argv):
    excel = excelbaker.ExcelDocument()
    comport = excel.get_comport() #'COM29'
    regulator = modbus.ModBus(port=comport)
    regulator.openPort()
    stop = False
    (temperature, minutes) = excel.get_temperature_time() #temperature = float = 20.0, minutes = int = 5
    while temperature and minutes and not stop: #Loop until we have no more excel values
        print 'Setting temperature to: %.1f °C' % temperature
        regulator.set_temp(temperature)
        while minutes and not stop: #Loop until time is out
            real_temperature = regulator.get_temp()
            print 'Measured temperature is: %.1f °C. '\
            'Time to wait is %d minutes. Set temperature is %.1f °C.' % ( 
                real_temperature, minutes, temperature)
            excel.write_real_temperature(real_temperature)
            for i in range(6):
                time.sleep(10)
                stop = excel.is_stopped()
                if stop:
                    print 'Stopping'
                    break
            minutes = minutes - 1 
        (temperature, minutes) = excel.get_temperature_time() #temperature = float = 20.0, minutes = int = 5
    regulator.closePort()
    print 'Done.'
    
if __name__ == "__main__":
    #Behöver parametrarna COM-port, excelfil-läsning, excel-filskrivning
#    if len(sys.argv) < 4:  #filnamn, comport, excel-läs, excel-skriv
#        print 'Too few arguments, use : tempcontrol COMPORT excel_conf.xls excel_log.xls'
#        sys.exit(1)
    main(sys.argv)
    