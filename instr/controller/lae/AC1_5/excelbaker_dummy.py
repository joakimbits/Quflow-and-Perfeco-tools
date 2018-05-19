'''
Created on 3 jan 2013

@author: Anders
'''
class ExcelDocument(object):
    def __init__(self):
        self.comport = 'COM29'
        self.temp_time_list = [(30.0,1), (35.0,2),(40.0,3),(20.0,1)]
         
    def get_comport(self):
        return self.comport
    def get_temperature_time(self):
        temp,time = (0.0,0)
        if len(self.temp_time_list):
            temp,time = self.temp_time_list[0]
            self.temp_time_list = self.temp_time_list[1:]
        return temp,time
    def write_real_temperature(self,temperature):
        print 'Writing...'
    def is_stopped(self):
        return False
    