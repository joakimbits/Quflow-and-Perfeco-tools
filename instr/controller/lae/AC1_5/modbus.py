# -*- coding: utf-8 -*-
import serial
import unittest 

class ModBus(object):
    def __init__(self, b='9600', d='7', s='1', p='even', slaveid='1', port='COM29'):
        self.baudrate = b
        self.baudrate_values = ['1200','2400','4800','9600','19200','38400','57600','115200']
        self.data_bits =d
        self.data_bits_values = ['8']
        self.stop_bits = s
        self.stop_bits_values = ['1','2']
        self.parity = p
        self.parity_values = ['none','even','odd']
        self.slave_id = slaveid
        self.slave_id_values = map(lambda x: str(x),range(0,248)) #0 is broadcast address, 247 is max
        self.port = port 
        self.port_values = self.portScan()

    def portScan(self):
        ports = []
        for port_number in range(0,100):
            try:
                ser = serial.Serial(port_number)
                ports.append( ser.portstr )
                ser.close()
            except serial.SerialException:
                pass
        return ports

    def getSetup(self):
        "Return current modbus setup"
        return {'b':self.baudrate, 'd':self.data_bits, 's':self.stop_bits,
                'p':self.parity, 'slaveid':self.slave_id, 'port':self.port}
                
    def setSetup(self, dictS):
        "Set modbus parameters"
        for key in dictS:
            # not really elegant
            # self.key = dictS[key] # doesn't work
            if key == 'b':
                self.baudrate = dictS[key]
            elif key == 'd':
                self.data_bits = dictS[key]
            elif key == 's':
                self.stop_bits = dictS[key]
            elif key == 'p':
                self.parity = dictS[key]
            elif key == 'slaveid':
                self.slave_id = dictS[key]
            elif key == 'port':
                self.port = dictS[key]
            else:
                print "Warning, no parameter '%s' in modbus setup, ignored" % key

    def openPort(self):
        if self.data_bits == '7': #only 7 bits is OK for the AC1-5 
            _bytesize = serial.SEVENBITS
        else: 
            raise serial.SerialException
        
        if self.parity == 'none':
            _parity = serial.PARITY_NONE
        elif self.parity == 'even':
            _parity = serial.PARITY_EVEN
        elif self.parity == 'odd':
            _parity = serial.PARITY_ODD
        else:
            raise serial.SerialException
        
        if self.stop_bits == '1':
            _stop_bits = serial.STOPBITS_ONE
        elif self.stop_bits == '2':
            _stop_bits = serial.STOPBITS_TWO
        else:
            raise serial.SerialException
        
        self.ser = serial.Serial(
                                    port=self.port,
                                    baudrate=self.baudrate,
                                    bytesize=_bytesize, 
                                    parity=_parity,
                                    stopbits=_stop_bits,
                                    timeout=0.5, #seconds
                                    xonxoff = 0,
                                    rtscts= 0,
                                    writeTimeout=0.5,
                                    dsrdtr=0,
                                    interCharTimeout = 0.5
                                    )
        
    def closePort(self):
        try: 
            self.ser.close()
            self.ser = None
        except AttributeError:
            print 'Serial port could not be closed. Was it Open?'
        
    def receiveReplyPacket(self,automatic_length=True,payload_length=0):
        packet = ''
        if automatic_length: 
            packet = self.ser.read(size=1+2+2+2) #Read device id, function code and number of bytes.
        else: 
            packet = self.ser.read(size=1+2+2) #there is no lenght in packet 

        if len(packet)== 0:
                raise mexceptions.ModbusIOException('Inget svar mottaget') #Wrong adress used in modbus?

        if automatic_length: #we have a lenght indicator in packet
            payload_length = int(packet[5:7],16) #position contains number of bytes in message as ascii hex

        packet = packet + self.ser.read( size=payload_length*2 ) #Read remaining bytes. 2 characters/byte 
        packet = packet + self.ser.read( size=2 ) #Read LSR byte. 
        packet = packet + self.ser.read(2) #Read CR LF
        return packet

    
    def buildPacket(self, function_code,data_list):
        '''
        Creates a ready to send ASCII modbus packet 
        @param function code as an int
        @param data is a list of ints.
        '''
        packet = [ 
                  '%02x' % int(self.slave_id),
                  '%02x' % function_code
                  ]
        for i in data_list:
            packet.append('%02x' % i)
        packet = ''.join(packet)
        checksum = self.checksumLSR(packet)
        checksum = '%02x' % checksum
        packet = ':' + packet + checksum + '\r\n'
        packet = packet.upper()
        return packet

    def checksumLSR(self,packet):
        #ASCII checksum. Packet without leading : and without ending CRLF
        checksum = 0
        int_packet = []
        for i in range( len(packet) ):
            if not i % 2: #0, 2, 4
                str = packet[i] + packet[i+1]
                checksum = checksum + int(str,16)
        checksum = 0 - checksum
        checksum = checksum %256 #Modules 256 
        return checksum
 
    def holdingRegRead(self,start_address,number_of_regs):
        data = []
        data.append(start_address / 256 ) #Upper byte
        data.append(start_address % 256 ) #lower byte
        data.append(number_of_regs / 256 ) #upper byte
        data.append(number_of_regs % 256 ) #lower byte
        packet = self.buildPacket(3, data) #1 is function code for coils
        try:
            self.ser.flushInput() 
            self.ser.write(packet)
            received = self.receiveReplyPacket()
        except serial.SerialException:
            raise
        if received[0] != ':':
            raise mexceptions.ModbusException('Inget starttecken i position 0')
        else:
            received = received[1:]
        if received[-2:] != '\r\n':
            raise mexceptions.ModbusException('Inga CR LF som sluttecken')
        else:
            received = received[:-2]
        if self.checksumLSR(received):
            raise mexceptions.ModbusException('Fel på mottagen checksumma')
        else:
            received = received[:-2]
            
        return received[6:]   #Return string with ascii hex values, skip address, function, byte count

    def singleRegWrite(self,start_address,value):
        '''    start_address is an int
                value is an int '''
        data = []
        data.append(start_address / 256 ) #Upper byte
        data.append(start_address % 256 ) #lower byte
        data.append(value / 256 ) #upper byte
        data.append(value % 256 ) #lower byte
        packet = self.buildPacket(6, data) #6 is Preset Single Register
        try:
            self.ser.flushInput()   
            self.ser.write(packet)
            received = self.receiveReplyPacket(automatic_length=False,payload_length=4) #4 bytes regaddr + value
        except serial.SerialException:
            raise
        if received[0] != ':':
            raise mexceptions.ModbusException('Inget starttecken i position 0')
        else:
            received = received[1:]
        if received[-2:] != '\r\n':
            raise mexceptions.ModbusException('Inga CR LF som sluttecken')
        else:
            received = received[:-2]
        if self.checksumLSR(received):
            raise mexceptions.ModbusException('Fel på mottagen checksumma')
        else:
            received = received[:-2]
            
        return received[4:]   #Return string with ascii hex values, skip address, function, byte count

    def get_temp(self):    
        res = self.holdingRegRead(0,1)
        temperature = int(res,16) / 10.0  # Floating point div
        return temperature
    
    def set_temp(self,temperature = 20.0):
        temp = int(temperature * 10)
        res = self.singleRegWrite(203,temp)
        temp_high = res[4]+res[5] #characters in hex ascii string 
        temp_low = res[6]+res[7] 
        temperature = int(temp_high,16)*256 + int(temp_low,16)
        temperature = temperature / 10.0 #Convert to float with decimal
        return temperature
    
class TestCheckLSR(unittest.TestCase):
    def setUp(self):
        self.objModBus = ModBus()

    def test_checkLSR(self):
        res = self.objModBus.checksumLSR('010300000003F9')
        self.assertEqual(0,res)

    def test_checkLSR2(self):
        res = self.objModBus.checksumLSR('010302015E')
        self.assertEqual(int('9B',16),res)
        
class TestBuildPacket(unittest.TestCase):

    def setUp(self):
        self.objModBus = ModBus()

    def test_packet(self):
        res = self.objModBus.buildPacket(3, [00,00,00,03])
        self.assertEqual(res,':010300000003F9\r\n') #Ask temperature

    def test_packet2(self):
        res = self.objModBus.buildPacket(3, [0x02,0x01,0x5E])
        self.assertEqual(res,':010302015E9B\r\n') #Temp is 35.0 degrees

class TestModbusRead(unittest.TestCase):

    def setUp(self):
        self.objModBus = ModBus()
        self.objModBus.openPort()

    def tearDown(self):
        self.objModBus.closePort()

    def test_asktemp(self):
        res = self.objModBus.holdingRegRead(0,1)
        temperature = int(res,16) / 10.0  # Floating point div
        #print 'Temperature is: %.1f' % temperature
        self.failUnless(res, 'Temp does not exist?!?') 

# Testcases are not run in any particular order, so this might set a bad value.
class TestModbusWrite(unittest.TestCase):

    def setUp(self):
        self.objModBus = ModBus()
        self.objModBus.openPort()

    def tearDown(self):
        self.objModBus.closePort()

    def test_settemp(self):
        res = self.objModBus.singleRegWrite(203,350)
        self.assertEqual(res, '00CB015E') 

class TestModbusGetTemp(unittest.TestCase):

    def setUp(self):
        self.objModBus = ModBus()
        self.objModBus.openPort()

    def tearDown(self):
        self.objModBus.closePort()

    def test_gettemp(self):
        res = self.objModBus.get_temp()
        #print 'Temp is: %.1f' % res
        self.failUnless(res, 'Temp does not exist?!?')

class TestModbusSetTemp(unittest.TestCase):
    def setUp(self):
        self.objModBus = ModBus()
        self.objModBus.openPort()

    def tearDown(self):
        self.objModBus.closePort()

    def test_gettemp(self):
        res = self.objModBus.set_temp(30.0)
        #print 'Temp set to: %.1f' % res
        self.assertEqual(res, 30.0)

