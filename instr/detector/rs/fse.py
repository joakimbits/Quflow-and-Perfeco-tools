# Created by makepy.py version 0.4.93
# By python version 2.4.1 (#65, Mar 30 2005, 09:13:57) [MSC v.1310 32 bit (Intel)]
# From type library 'RsFs.dll'
# On Thu Sep 15 09:46:30 2005
"""IVI RsFs (Rohde & Schwarz) 1.0 Type Library"""
makepy_version = '0.4.93'
python_version = 0x20401f0

import win32com.client.CLSIDToClass, pythoncom
import win32com.client.util
from pywintypes import IID
from win32com.client import Dispatch

# The following 3 lines may need tweaking for the particular server
# Candidates are pythoncom.Missing and pythoncom.Empty
defaultNamedOptArg=pythoncom.Empty
defaultNamedNotOptArg=pythoncom.Empty
defaultUnnamedArg=pythoncom.Empty

CLSID = IID('{32E85EFD-039B-4D20-818A-F22DCFB84767}')
MajorVersion = 1
MinorVersion = 0
LibraryFlags = 8
LCID = 0x0

class constants:
	RsFsAcquisitionStatusComplete =0x1        # from enum RsFsAcquisitionStatusEnum
	RsFsAcquisitionStatusInProgress=0x0        # from enum RsFsAcquisitionStatusEnum
	RsFsAcquisitionStatusUnknown  =-1         # from enum RsFsAcquisitionStatusEnum
	RsFsActiveScreenA             =0x0        # from enum RsFsActiveScreenEnum
	RsFsActiveScreenB             =0x1        # from enum RsFsActiveScreenEnum
	RsFsAmplitudeUnitsDBM         =0x1        # from enum RsFsAmplitudeUnitsEnum
	RsFsAmplitudeUnitsDBMV        =0x2        # from enum RsFsAmplitudeUnitsEnum
	RsFsAmplitudeUnitsDBUV        =0x3        # from enum RsFsAmplitudeUnitsEnum
	RsFsAmplitudeUnitsVolt        =0x4        # from enum RsFsAmplitudeUnitsEnum
	RsFsAmplitudeUnitsWatt        =0x5        # from enum RsFsAmplitudeUnitsEnum
	RsFsDetectorTypeAutoPeak      =0x1        # from enum RsFsDetectorTypeEnum
	RsFsDetectorTypeAverage       =0x2        # from enum RsFsDetectorTypeEnum
	RsFsDetectorTypeMaxPeak       =0x3        # from enum RsFsDetectorTypeEnum
	RsFsDetectorTypeMinPeak       =0x4        # from enum RsFsDetectorTypeEnum
	RsFsDetectorTypeRMS           =0x6        # from enum RsFsDetectorTypeEnum
	RsFsDetectorTypeSample        =0x5        # from enum RsFsDetectorTypeEnum
	RsFsDisplayFormatSingleScreen =0x0        # from enum RsFsDisplayFormatEnum
	RsFsDisplayFormatSplitScreen  =0x1        # from enum RsFsDisplayFormatEnum
	E_RSFS_MARKER_NOT_ENABLED     =-2147220928 # from enum RsFsErrorCodesEnumErrorEnum
	E_RSFS_MAX_TIME_EXCEEDED      =-2147220896 # from enum RsFsErrorCodesEnumErrorEnum
	E_RSFS_NOT_DELTA_MARKER       =-2147220912 # from enum RsFsErrorCodesEnumErrorEnum
	E_RSFS_TRIGGER_NOT_SOFTWARE   =-2147220944 # from enum RsFsErrorCodesEnumErrorEnum
	S_RSFS_MEASURE_UNCALIBRATED   =-2147220976 # from enum RsFsErrorCodesEnumErrorEnum
	S_RSFS_OVER_RANGE             =-2147220960 # from enum RsFsErrorCodesEnumErrorEnum
	RsFsExternalTriggerSlopeNegative=0x2        # from enum RsFsExternalTriggerSlopeEnum
	RsFsExternalTriggerSlopePositive=0x1        # from enum RsFsExternalTriggerSlopeEnum
	RsFsInstrumentSettingFrequencyCenter=0x3        # from enum RsFsInstrumentSettingEnum
	RsFsInstrumentSettingReferenceLevel=0x5        # from enum RsFsInstrumentSettingEnum
	RsFsMarkerSearchHighest       =0x1        # from enum RsFsMarkerSearchEnum
	RsFsMarkerSearchMinimum       =0xa        # from enum RsFsMarkerSearchEnum
	RsFsMarkerSearchNextPeak      =0x2        # from enum RsFsMarkerSearchEnum
	RsFsMarkerSearchNextPeakLeft  =0x3        # from enum RsFsMarkerSearchEnum
	RsFsMarkerSearchNextPeakRight =0x4        # from enum RsFsMarkerSearchEnum
	RsFsMarkerTypeDelta           =0x2        # from enum RsFsMarkerTypeEnum
	RsFsMarkerTypeNormal          =0x1        # from enum RsFsMarkerTypeEnum
	RsFsMarkerTypeReference       =0x3        # from enum RsFsMarkerTypeEnum
	RsFsTraceTypeClearWrite       =0x1        # from enum RsFsTraceTypeEnum
	RsFsTraceTypeMaxHold          =0x2        # from enum RsFsTraceTypeEnum
	RsFsTraceTypeMinHold          =0x3        # from enum RsFsTraceTypeEnum
	RsFsTraceTypeStore            =0x6        # from enum RsFsTraceTypeEnum
	RsFsTraceTypeVideoAverage     =0x4        # from enum RsFsTraceTypeEnum
	RsFsTraceTypeView             =0x5        # from enum RsFsTraceTypeEnum
	RsFsTriggerSourceExternal     =0x1        # from enum RsFsTriggerSourceEnum
	RsFsTriggerSourceIFPower      =0x3e9      # from enum RsFsTriggerSourceEnum
	RsFsTriggerSourceImmediate    =0x2        # from enum RsFsTriggerSourceEnum
	RsFsTriggerSourceRFPower      =0x3ea      # from enum RsFsTriggerSourceEnum
	RsFsTriggerSourceSoftware     =0x3        # from enum RsFsTriggerSourceEnum
	RsFsTriggerSourceVideo        =0x5        # from enum RsFsTriggerSourceEnum
	RsFsVerticalScaleLinear       =0x1        # from enum RsFsVerticalScaleEnum
	RsFsVerticalScaleLogarithmic  =0x2        # from enum RsFsVerticalScaleEnum
	RsFsVideoTriggerSlopeNegative =0x2        # from enum RsFsVideoTriggerSlopeEnum
	RsFsVideoTriggerSlopePositive =0x1        # from enum RsFsVideoTriggerSlopeEnum

from win32com.client import CoClassBaseClass
# This CoClass is known by the name 'RsFs.RsFs.1'
class RsFs(CoClassBaseClass): # A CoClass
	# RsFs Class
	CLSID = IID('{3E3BADCF-2555-470C-868C-D6A9A81F1EA7}')
	coclass_sources = [
	]
	coclass_interfaces = [
	]

# This CoClass is known by the name 'RsFs.RsFsSpecAnTrace.1'
class RsFsSpecAnTrace(CoClassBaseClass): # A CoClass
	# RsFsSpecAnTrace Class
	CLSID = IID('{489BA02C-0FBE-475F-B444-E7F49FAE351A}')
	coclass_sources = [
	]
	coclass_interfaces = [
	]

# This CoClass is known by the name 'RsFs.RsFsTrace.1'
class RsFsTrace(CoClassBaseClass): # A CoClass
	# RsFsTrace Class
	CLSID = IID('{5FD0328A-578C-46B2-82BA-04ACD18D6778}')
	coclass_sources = [
	]
	coclass_interfaces = [
	]

IRsFs_vtables_dispatch_ = 0
IRsFs_vtables_ = [
	(( 'Acquisition' , 'pVal' , ), 1610743808, (1610743808, (), [ (16397, 10, None, "IID('{1105AB7B-F3CC-41BA-BDA7-947287E93AA6}')") , ], 1 , 2 , 4 , 0 , 36 , (3, 0, None, None) , 0 , )),
	(( 'Display' , 'pVal' , ), 1610743809, (1610743809, (), [ (16397, 10, None, "IID('{53A008BA-C2CC-4A11-85BA-D56B4039588A}')") , ], 1 , 2 , 4 , 0 , 40 , (3, 0, None, None) , 0 , )),
	(( 'Frequency' , 'pVal' , ), 1610743810, (1610743810, (), [ (16397, 10, None, "IID('{1AA16416-D27A-4FC1-8E50-974DA721F912}')") , ], 1 , 2 , 4 , 0 , 44 , (3, 0, None, None) , 0 , )),
	(( 'InstrumentIO' , 'pVal' , ), 1610743811, (1610743811, (), [ (16397, 10, None, "IID('{9C98FDA3-F036-4EB4-B344-83DA98709C92}')") , ], 1 , 2 , 4 , 0 , 48 , (3, 0, None, None) , 0 , )),
	(( 'IviSpecAn' , 'pVal' , ), 1610743812, (1610743812, (), [ (16397, 10, None, "IID('{47ED52D8-A398-11D4-BA58-000064657374}')") , ], 1 , 2 , 4 , 0 , 52 , (3, 0, None, None) , 0 , )),
	(( 'Level' , 'pVal' , ), 1610743813, (1610743813, (), [ (16397, 10, None, "IID('{8C3AD776-FC96-43BF-8D13-A3DFA0BD942F}')") , ], 1 , 2 , 4 , 0 , 56 , (3, 0, None, None) , 0 , )),
	(( 'List' , 'pVal' , ), 1610743814, (1610743814, (), [ (16397, 10, None, "IID('{41688FA8-4ABE-43DF-AF6C-A1E3978EB30A}')") , ], 1 , 2 , 4 , 0 , 60 , (3, 0, None, None) , 0 , )),
	(( 'Marker' , 'pVal' , ), 1610743815, (1610743815, (), [ (16397, 10, None, "IID('{ECAD6FD0-27FF-41FE-9919-BF8CE89D6A20}')") , ], 1 , 2 , 4 , 0 , 64 , (3, 0, None, None) , 0 , )),
	(( 'Service' , 'pVal' , ), 1610743816, (1610743816, (), [ (16397, 10, None, "IID('{10F6C4EE-8F37-45D0-B9B6-CD710D53CC84}')") , ], 1 , 2 , 4 , 0 , 68 , (3, 0, None, None) , 0 , )),
	(( 'SweepCoupling' , 'pVal' , ), 1610743817, (1610743817, (), [ (16397, 10, None, "IID('{0B9155D3-D830-443A-BA54-AC1AC19A6906}')") , ], 1 , 2 , 4 , 0 , 72 , (3, 0, None, None) , 0 , )),
	(( 'Traces' , 'pVal' , ), 1610743818, (1610743818, (), [ (16397, 10, None, "IID('{2882646A-4189-411B-9122-FA3FFF4A2616}')") , ], 1 , 2 , 4 , 0 , 76 , (3, 0, None, None) , 0 , )),
	(( 'Trigger' , 'pVal' , ), 1610743819, (1610743819, (), [ (16397, 10, None, "IID('{84DCA769-FA4E-41C0-8265-88CF67136B95}')") , ], 1 , 2 , 4 , 0 , 80 , (3, 0, None, None) , 0 , )),
]

IRsFsAcquisition_vtables_dispatch_ = 0
IRsFsAcquisition_vtables_ = [
	(( 'Configure' , 'SweepModeContinuous' , 'NumberOfSweeps' , 'DetectorTypeAuto' , 'DetectorType' , 
			'VerticalScale' , ), 1610678272, (1610678272, (), [ (11, 1, None, None) , (3, 1, None, None) , (11, 1, None, None) , 
			(3, 1, None, None) , (3, 1, None, None) , ], 1 , 1 , 4 , 0 , 12 , (3, 0, None, None) , 0 , )),
	(( 'DetectorType' , 'pVal' , ), 1610678273, (1610678273, (), [ (3, 1, None, None) , ], 1 , 4 , 4 , 0 , 16 , (3, 0, None, None) , 0 , )),
	(( 'DetectorType' , 'pVal' , ), 1610678273, (1610678273, (), [ (16387, 10, None, None) , ], 1 , 2 , 4 , 0 , 20 , (3, 0, None, None) , 0 , )),
	(( 'DetectorTypeAuto' , 'pVal' , ), 1610678275, (1610678275, (), [ (11, 1, None, None) , ], 1 , 4 , 4 , 0 , 24 , (3, 0, None, None) , 0 , )),
	(( 'DetectorTypeAuto' , 'pVal' , ), 1610678275, (1610678275, (), [ (16395, 10, None, None) , ], 1 , 2 , 4 , 0 , 28 , (3, 0, None, None) , 0 , )),
	(( 'NumberOfSweeps' , 'pVal' , ), 1610678277, (1610678277, (), [ (3, 1, None, None) , ], 1 , 4 , 4 , 0 , 32 , (3, 0, None, None) , 0 , )),
	(( 'NumberOfSweeps' , 'pVal' , ), 1610678277, (1610678277, (), [ (16387, 10, None, None) , ], 1 , 2 , 4 , 0 , 36 , (3, 0, None, None) , 0 , )),
	(( 'SweepModeContinuous' , 'pVal' , ), 1610678279, (1610678279, (), [ (11, 1, None, None) , ], 1 , 4 , 4 , 0 , 40 , (3, 0, None, None) , 0 , )),
	(( 'SweepModeContinuous' , 'pVal' , ), 1610678279, (1610678279, (), [ (16395, 10, None, None) , ], 1 , 2 , 4 , 0 , 44 , (3, 0, None, None) , 0 , )),
	(( 'VerticalScale' , 'pVal' , ), 1610678281, (1610678281, (), [ (16387, 10, None, None) , ], 1 , 2 , 4 , 0 , 48 , (3, 0, None, None) , 0 , )),
	(( 'VerticalScale' , 'pVal' , ), 1610678281, (1610678281, (), [ (3, 1, None, None) , ], 1 , 4 , 4 , 0 , 52 , (3, 0, None, None) , 0 , )),
]

IRsFsCalibration_vtables_dispatch_ = 0
IRsFsCalibration_vtables_ = [
	(( 'GetResults' , 'pVal' , ), 1610678272, (1610678272, (), [ (16392, 10, None, None) , ], 1 , 1 , 4 , 0 , 12 , (3, 0, None, None) , 0 , )),
	(( 'Initiate' , ), 1610678273, (1610678273, (), [ ], 1 , 1 , 4 , 0 , 16 , (3, 0, None, None) , 0 , )),
]

IRsFsDisplay_vtables_dispatch_ = 0
IRsFsDisplay_vtables_ = [
	(( 'ActiveScreen' , 'pVal' , ), 1610678272, (1610678272, (), [ (3, 1, None, None) , ], 1 , 4 , 4 , 0 , 12 , (3, 0, None, None) , 0 , )),
	(( 'ActiveScreen' , 'pVal' , ), 1610678272, (1610678272, (), [ (16387, 10, None, None) , ], 1 , 2 , 4 , 0 , 16 , (3, 0, None, None) , 0 , )),
	(( 'Format' , 'pVal' , ), 1610678274, (1610678274, (), [ (3, 1, None, None) , ], 1 , 4 , 4 , 0 , 20 , (3, 0, None, None) , 0 , )),
	(( 'Format' , 'pVal' , ), 1610678274, (1610678274, (), [ (16387, 10, None, None) , ], 1 , 2 , 4 , 0 , 24 , (3, 0, None, None) , 0 , )),
	(( 'NumberOfDivisions' , 'pVal' , ), 1610678276, (1610678276, (), [ (16387, 10, None, None) , ], 1 , 2 , 4 , 0 , 28 , (3, 0, None, None) , 0 , )),
	(( 'UnitsPerDivision' , 'pVal' , ), 1610678277, (1610678277, (), [ (16389, 10, None, None) , ], 1 , 2 , 4 , 0 , 32 , (3, 0, None, None) , 0 , )),
	(( 'UnitsPerDivision' , 'pVal' , ), 1610678277, (1610678277, (), [ (5, 1, None, None) , ], 1 , 4 , 4 , 0 , 36 , (3, 0, None, None) , 0 , )),
	(( 'Update' , 'pVal' , ), 1610678279, (1610678279, (), [ (11, 1, None, None) , ], 1 , 4 , 4 , 0 , 40 , (3, 0, None, None) , 0 , )),
	(( 'Update' , 'pVal' , ), 1610678279, (1610678279, (), [ (16395, 10, None, None) , ], 1 , 2 , 4 , 0 , 44 , (3, 0, None, None) , 0 , )),
]

IRsFsFrequency_vtables_dispatch_ = 0
IRsFsFrequency_vtables_ = [
	(( 'ConfigureCenterSpan' , 'CenterFrequency' , 'Span' , ), 1610678272, (1610678272, (), [ (5, 1, None, None) , 
			(5, 1, None, None) , ], 1 , 1 , 4 , 0 , 12 , (3, 0, None, None) , 0 , )),
	(( 'ConfigureStartStop' , 'StartFrequency' , 'StopFrequency' , ), 1610678273, (1610678273, (), [ (5, 1, None, None) , 
			(5, 1, None, None) , ], 1 , 1 , 4 , 0 , 16 , (3, 0, None, None) , 0 , )),
	(( 'Offset' , 'pVal' , ), 1610678274, (1610678274, (), [ (5, 1, None, None) , ], 1 , 4 , 4 , 0 , 20 , (3, 0, None, None) , 0 , )),
	(( 'Offset' , 'pVal' , ), 1610678274, (1610678274, (), [ (16389, 10, None, None) , ], 1 , 2 , 4 , 0 , 24 , (3, 0, None, None) , 0 , )),
	(( 'Start' , 'pVal' , ), 1610678276, (1610678276, (), [ (5, 1, None, None) , ], 1 , 4 , 4 , 0 , 28 , (3, 0, None, None) , 0 , )),
	(( 'Start' , 'pVal' , ), 1610678276, (1610678276, (), [ (16389, 10, None, None) , ], 1 , 2 , 4 , 0 , 32 , (3, 0, None, None) , 0 , )),
	(( 'Stop' , 'pVal' , ), 1610678278, (1610678278, (), [ (5, 1, None, None) , ], 1 , 4 , 4 , 0 , 36 , (3, 0, None, None) , 0 , )),
	(( 'Stop' , 'pVal' , ), 1610678278, (1610678278, (), [ (16389, 10, None, None) , ], 1 , 2 , 4 , 0 , 40 , (3, 0, None, None) , 0 , )),
]

IRsFsInstrumentIO_vtables_dispatch_ = 0
IRsFsInstrumentIO_vtables_ = [
	(( 'Query' , 'SCPICommand' , 'pResult' , ), 1610678272, (1610678272, (), [ (8, 1, None, None) , 
			(16392, 10, None, None) , ], 1 , 1 , 4 , 0 , 12 , (3, 0, None, None) , 0 , )),
	(( 'Read' , 'Count' , 'pResponse' , ), 1610678273, (1610678273, (), [ (3, 1, None, None) , 
			(16401, 10, None, None) , ], 1 , 1 , 4 , 0 , 16 , (3, 0, None, None) , 0 , )),
	(( 'Write' , 'SCPICommand' , ), 1610678274, (1610678274, (), [ (8, 1, None, None) , ], 1 , 1 , 4 , 0 , 20 , (3, 0, None, None) , 0 , )),
]

IRsFsLevel_vtables_dispatch_ = 0
IRsFsLevel_vtables_ = [
	(( 'AmplitudeUnits' , 'pVal' , ), 1610678272, (1610678272, (), [ (3, 1, None, None) , ], 1 , 4 , 4 , 0 , 12 , (3, 0, None, None) , 0 , )),
	(( 'AmplitudeUnits' , 'pVal' , ), 1610678272, (1610678272, (), [ (16387, 10, None, None) , ], 1 , 2 , 4 , 0 , 16 , (3, 0, None, None) , 0 , )),
	(( 'Attenuation' , 'pVal' , ), 1610678274, (1610678274, (), [ (5, 1, None, None) , ], 1 , 4 , 4 , 0 , 20 , (3, 0, None, None) , 0 , )),
	(( 'Attenuation' , 'pVal' , ), 1610678274, (1610678274, (), [ (16389, 10, None, None) , ], 1 , 2 , 4 , 0 , 24 , (3, 0, None, None) , 0 , )),
	(( 'AttenuationAuto' , 'pVal' , ), 1610678276, (1610678276, (), [ (11, 1, None, None) , ], 1 , 4 , 4 , 0 , 28 , (3, 0, None, None) , 0 , )),
	(( 'AttenuationAuto' , 'pVal' , ), 1610678276, (1610678276, (), [ (16395, 10, None, None) , ], 1 , 2 , 4 , 0 , 32 , (3, 0, None, None) , 0 , )),
	(( 'Configure' , 'AmplitudeUnits' , 'InputImpedance' , 'ReferenceLevel' , 'ReferenceLevelOffset' , 
			'AttenuationAuto' , 'Attenuation' , 'ElectronicAttenuationAuto' , 'ElectronicAttenuation' , 'ElectronicAttenuationEnabled' , 
			'RFPreamplifierEnabled' , ), 1610678278, (1610678278, (), [ (3, 1, None, None) , (5, 1, None, None) , (5, 1, None, None) , 
			(5, 1, None, None) , (11, 1, None, None) , (5, 1, None, None) , (5, 1, None, None) , (5, 1, None, None) , 
			(11, 1, None, None) , (11, 1, None, None) , ], 1 , 1 , 4 , 0 , 36 , (3, 0, None, None) , 0 , )),
	(( 'ElectronicAttenuation' , 'pVal' , ), 1610678279, (1610678279, (), [ (5, 1, None, None) , ], 1 , 4 , 4 , 0 , 40 , (3, 0, None, None) , 0 , )),
	(( 'ElectronicAttenuation' , 'pVal' , ), 1610678279, (1610678279, (), [ (16389, 10, None, None) , ], 1 , 2 , 4 , 0 , 44 , (3, 0, None, None) , 0 , )),
	(( 'ElectronicAttenuationAuto' , 'pVal' , ), 1610678281, (1610678281, (), [ (11, 1, None, None) , ], 1 , 4 , 4 , 0 , 48 , (3, 0, None, None) , 0 , )),
	(( 'ElectronicAttenuationAuto' , 'pVal' , ), 1610678281, (1610678281, (), [ (16395, 10, None, None) , ], 1 , 2 , 4 , 0 , 52 , (3, 0, None, None) , 0 , )),
	(( 'ElectronicAttenuationEnabled' , 'pVal' , ), 1610678283, (1610678283, (), [ (11, 1, None, None) , ], 1 , 4 , 4 , 0 , 56 , (3, 0, None, None) , 0 , )),
	(( 'ElectronicAttenuationEnabled' , 'pVal' , ), 1610678283, (1610678283, (), [ (16395, 10, None, None) , ], 1 , 2 , 4 , 0 , 60 , (3, 0, None, None) , 0 , )),
	(( 'InputImpedance' , 'pVal' , ), 1610678285, (1610678285, (), [ (5, 1, None, None) , ], 1 , 4 , 4 , 0 , 64 , (3, 0, None, None) , 0 , )),
	(( 'InputImpedance' , 'pVal' , ), 1610678285, (1610678285, (), [ (16389, 10, None, None) , ], 1 , 2 , 4 , 0 , 68 , (3, 0, None, None) , 0 , )),
	(( 'Reference' , 'pVal' , ), 1610678287, (1610678287, (), [ (5, 1, None, None) , ], 1 , 4 , 4 , 0 , 72 , (3, 0, None, None) , 0 , )),
	(( 'Reference' , 'pVal' , ), 1610678287, (1610678287, (), [ (16389, 10, None, None) , ], 1 , 2 , 4 , 0 , 76 , (3, 0, None, None) , 0 , )),
	(( 'ReferenceOffset' , 'pVal' , ), 1610678289, (1610678289, (), [ (5, 1, None, None) , ], 1 , 4 , 4 , 0 , 80 , (3, 0, None, None) , 0 , )),
	(( 'ReferenceOffset' , 'pVal' , ), 1610678289, (1610678289, (), [ (16389, 10, None, None) , ], 1 , 2 , 4 , 0 , 84 , (3, 0, None, None) , 0 , )),
	(( 'RFPreamplifierEnabled' , 'pVal' , ), 1610678291, (1610678291, (), [ (11, 1, None, None) , ], 1 , 4 , 4 , 0 , 88 , (3, 0, None, None) , 0 , )),
	(( 'RFPreamplifierEnabled' , 'pVal' , ), 1610678291, (1610678291, (), [ (16395, 10, None, None) , ], 1 , 2 , 4 , 0 , 92 , (3, 0, None, None) , 0 , )),
]

IRsFsList_vtables_dispatch_ = 0
IRsFsList_vtables_ = [
	(( 'Configure' , 'Frequency' , 'ReferenceLevel' , 'Attenuation' , 'ElectronicAttentuation' , 
			'RBWFilterType' , 'ResolutionBandwidth' , 'VideoBandwidth' , 'Sweeptime' , 'Detector' , 
			), 1610678272, (1610678272, (), [ (8197, 1, None, None) , (8197, 1, None, None) , (8197, 1, None, None) , (8197, 1, None, None) , 
			(8195, 1, None, None) , (8197, 1, None, None) , (8197, 1, None, None) , (8197, 1, None, None) , (8197, 1, None, None) , ], 1 , 1 , 4 , 0 , 12 , (3, 0, None, None) , 0 , )),
]

IRsFsMarker_vtables_dispatch_ = 0
IRsFsMarker_vtables_ = [
	(( 'ActiveMarker' , 'pVal' , ), 1610678272, (1610678272, (), [ (8, 1, None, None) , ], 1 , 4 , 4 , 0 , 12 , (3, 0, None, None) , 0 , )),
	(( 'ActiveMarker' , 'pVal' , ), 1610678272, (1610678272, (), [ (16392, 10, None, None) , ], 1 , 2 , 4 , 0 , 16 , (3, 0, None, None) , 0 , )),
	(( 'Amplitude' , 'pVal' , ), 1610678274, (1610678274, (), [ (16389, 10, None, None) , ], 1 , 2 , 4 , 0 , 20 , (3, 0, None, None) , 0 , )),
	(( 'ConfigureEnabled' , 'Enabled' , 'MarkerTraceName' , ), 1610678275, (1610678275, (), [ (11, 1, None, None) , 
			(8, 1, None, None) , ], 1 , 1 , 4 , 0 , 24 , (3, 0, None, None) , 0 , )),
	(( 'ConfigureSearch' , 'PeakExcursion' , 'MarkerThreshold' , ), 1610678276, (1610678276, (), [ (5, 1, None, None) , 
			(5, 1, None, None) , ], 1 , 1 , 4 , 0 , 28 , (3, 0, None, None) , 0 , )),
	(( 'Count' , 'pVal' , ), 1610678277, (1610678277, (), [ (16387, 10, None, None) , ], 1 , 2 , 4 , 0 , 32 , (3, 0, None, None) , 0 , )),
	(( 'DisableAll' , ), 1610678278, (1610678278, (), [ ], 1 , 1 , 4 , 0 , 36 , (3, 0, None, None) , 0 , )),
	(( 'Enabled' , 'pVal' , ), 1610678279, (1610678279, (), [ (11, 1, None, None) , ], 1 , 4 , 4 , 0 , 40 , (3, 0, None, None) , 0 , )),
	(( 'Enabled' , 'pVal' , ), 1610678279, (1610678279, (), [ (16395, 10, None, None) , ], 1 , 2 , 4 , 0 , 44 , (3, 0, None, None) , 0 , )),
	(( 'FrequencyCounter' , 'pVal' , ), 1610678281, (1610678281, (), [ (16397, 10, None, "IID('{29C38F28-042D-4690-A4FC-3FD3870A5C94}')") , ], 1 , 2 , 4 , 0 , 48 , (3, 0, None, None) , 0 , )),
	(( 'MakeDelta' , 'DeltaMarker' , ), 1610678282, (1610678282, (), [ (11, 1, None, None) , ], 1 , 1 , 4 , 0 , 52 , (3, 0, None, None) , 0 , )),
	(( 'Name' , 'Index' , 'pVal' , ), 1610678283, (1610678283, (), [ (3, 1, None, None) , 
			(16392, 10, None, None) , ], 1 , 2 , 4 , 0 , 56 , (3, 0, None, None) , 0 , )),
	(( 'PeakExcursion' , 'pVal' , ), 1610678284, (1610678284, (), [ (16389, 10, None, None) , ], 1 , 2 , 4 , 0 , 60 , (3, 0, None, None) , 0 , )),
	(( 'PeakExcursion' , 'pVal' , ), 1610678284, (1610678284, (), [ (5, 1, None, None) , ], 1 , 4 , 4 , 0 , 64 , (3, 0, None, None) , 0 , )),
	(( 'Position' , 'pVal' , ), 1610678286, (1610678286, (), [ (16389, 10, None, None) , ], 1 , 2 , 4 , 0 , 68 , (3, 0, None, None) , 0 , )),
	(( 'Position' , 'pVal' , ), 1610678286, (1610678286, (), [ (5, 1, None, None) , ], 1 , 4 , 4 , 0 , 72 , (3, 0, None, None) , 0 , )),
	(( 'Query' , 'MarkerPosition' , 'MarkerAmplitude' , ), 1610678288, (1610678288, (), [ (16389, 3, None, None) , 
			(16389, 3, None, None) , ], 1 , 1 , 4 , 0 , 76 , (3, 0, None, None) , 0 , )),
	(( 'QueryReference' , 'ReferencePosition' , 'ReferenceAmplitude' , ), 1610678289, (1610678289, (), [ (16389, 3, None, None) , 
			(16389, 3, None, None) , ], 1 , 1 , 4 , 0 , 80 , (3, 0, None, None) , 0 , )),
	(( 'ReferenceAmplitude' , 'pVal' , ), 1610678290, (1610678290, (), [ (16389, 10, None, None) , ], 1 , 2 , 4 , 0 , 84 , (3, 0, None, None) , 0 , )),
	(( 'ReferencePosition' , 'pVal' , ), 1610678291, (1610678291, (), [ (16389, 10, None, None) , ], 1 , 2 , 4 , 0 , 88 , (3, 0, None, None) , 0 , )),
	(( 'Search' , 'SearchType' , ), 1610678292, (1610678292, (), [ (3, 1, None, None) , ], 1 , 1 , 4 , 0 , 92 , (3, 0, None, None) , 0 , )),
	(( 'SetInstrumentFromMarker' , 'InstrumentSetting' , ), 1610678293, (1610678293, (), [ (3, 1, None, None) , ], 1 , 1 , 4 , 0 , 96 , (3, 0, None, None) , 0 , )),
	(( 'SignalTrackEnabled' , 'pVal' , ), 1610678294, (1610678294, (), [ (11, 1, None, None) , ], 1 , 4 , 4 , 0 , 100 , (3, 0, None, None) , 0 , )),
	(( 'SignalTrackEnabled' , 'pVal' , ), 1610678294, (1610678294, (), [ (16395, 10, None, None) , ], 1 , 2 , 4 , 0 , 104 , (3, 0, None, None) , 0 , )),
	(( 'Threshold' , 'pVal' , ), 1610678296, (1610678296, (), [ (16389, 10, None, None) , ], 1 , 2 , 4 , 0 , 108 , (3, 0, None, None) , 0 , )),
	(( 'Threshold' , 'pVal' , ), 1610678296, (1610678296, (), [ (5, 1, None, None) , ], 1 , 4 , 4 , 0 , 112 , (3, 0, None, None) , 0 , )),
	(( 'Trace' , 'pVal' , ), 1610678298, (1610678298, (), [ (16392, 10, None, None) , ], 1 , 2 , 4 , 0 , 116 , (3, 0, None, None) , 0 , )),
	(( 'Trace' , 'pVal' , ), 1610678298, (1610678298, (), [ (8, 1, None, None) , ], 1 , 4 , 4 , 0 , 120 , (3, 0, None, None) , 0 , )),
	(( 'Type' , 'pVal' , ), 1610678300, (1610678300, (), [ (16387, 10, None, None) , ], 1 , 2 , 4 , 0 , 124 , (3, 0, None, None) , 0 , )),
]

IRsFsMarkerFrequencyCounter_vtables_dispatch_ = 0
IRsFsMarkerFrequencyCounter_vtables_ = [
	(( 'Configure' , 'Enabled' , 'Resolution' , ), 1610678272, (1610678272, (), [ (11, 1, None, None) , 
			(5, 1, None, None) , ], 1 , 1 , 4 , 0 , 12 , (3, 0, None, None) , 0 , )),
	(( 'Enabled' , 'pVal' , ), 1610678273, (1610678273, (), [ (11, 1, None, None) , ], 1 , 4 , 4 , 0 , 16 , (3, 0, None, None) , 0 , )),
	(( 'Enabled' , 'pVal' , ), 1610678273, (1610678273, (), [ (16395, 10, None, None) , ], 1 , 2 , 4 , 0 , 20 , (3, 0, None, None) , 0 , )),
	(( 'Resolution' , 'pVal' , ), 1610678275, (1610678275, (), [ (5, 1, None, None) , ], 1 , 4 , 4 , 0 , 24 , (3, 0, None, None) , 0 , )),
	(( 'Resolution' , 'pVal' , ), 1610678275, (1610678275, (), [ (16389, 10, None, None) , ], 1 , 2 , 4 , 0 , 28 , (3, 0, None, None) , 0 , )),
]

IRsFsService_vtables_dispatch_ = 0
IRsFsService_vtables_ = [
	(( 'Calibration' , 'pVal' , ), 1610678272, (1610678272, (), [ (16397, 10, None, "IID('{AAC9A50C-C75B-4AD6-87D3-68C640CC0F67}')") , ], 1 , 2 , 4 , 0 , 12 , (3, 0, None, None) , 0 , )),
	(( 'Enabled' , ), 1610678273, (1610678273, (), [ (11, 1, None, None) , ], 1 , 4 , 4 , 0 , 16 , (3, 0, None, None) , 0 , )),
	(( 'InputCalEnabled' , 'pVal' , ), 1610678274, (1610678274, (), [ (11, 1, None, None) , ], 1 , 4 , 4 , 0 , 20 , (3, 0, None, None) , 0 , )),
	(( 'InputCalEnabled' , 'pVal' , ), 1610678274, (1610678274, (), [ (16395, 10, None, None) , ], 1 , 2 , 4 , 0 , 24 , (3, 0, None, None) , 0 , )),
	(( 'NoiseSourceEnabled' , 'pVal' , ), 1610678276, (1610678276, (), [ (11, 1, None, None) , ], 1 , 4 , 4 , 0 , 28 , (3, 0, None, None) , 0 , )),
	(( 'NoiseSourceEnabled' , 'pVal' , ), 1610678276, (1610678276, (), [ (16395, 10, None, None) , ], 1 , 2 , 4 , 0 , 32 , (3, 0, None, None) , 0 , )),
	(( 'ServiceFunction' , 'ServiceFunctionName' , ), 1610678278, (1610678278, (), [ (8, 1, None, None) , ], 1 , 1 , 4 , 0 , 36 , (3, 0, None, None) , 0 , )),
]

IRsFsSweepCoupling_vtables_dispatch_ = 0
IRsFsSweepCoupling_vtables_ = [
	(( 'Configure' , 'ResolutionBandwidthAuto' , 'ResolutionBandwidth' , 'VideoBandwidthAuto' , 'VideoBandwidth' , 
			'SweepTimeAuto' , 'Sweeptime' , ), 1610678272, (1610678272, (), [ (11, 1, None, None) , (5, 1, None, None) , 
			(11, 1, None, None) , (5, 1, None, None) , (11, 1, None, None) , (5, 1, None, None) , ], 1 , 1 , 4 , 0 , 12 , (3, 0, None, None) , 0 , )),
	(( 'ResolutionBandwidth' , 'pVal' , ), 1610678273, (1610678273, (), [ (5, 1, None, None) , ], 1 , 4 , 4 , 0 , 16 , (3, 0, None, None) , 0 , )),
	(( 'ResolutionBandwidth' , 'pVal' , ), 1610678273, (1610678273, (), [ (16389, 10, None, None) , ], 1 , 2 , 4 , 0 , 20 , (3, 0, None, None) , 0 , )),
	(( 'ResolutionBandwidthAuto' , 'pVal' , ), 1610678275, (1610678275, (), [ (11, 1, None, None) , ], 1 , 4 , 4 , 0 , 24 , (3, 0, None, None) , 0 , )),
	(( 'ResolutionBandwidthAuto' , 'pVal' , ), 1610678275, (1610678275, (), [ (16395, 10, None, None) , ], 1 , 2 , 4 , 0 , 28 , (3, 0, None, None) , 0 , )),
	(( 'Sweeptime' , 'pVal' , ), 1610678277, (1610678277, (), [ (5, 1, None, None) , ], 1 , 4 , 4 , 0 , 32 , (3, 0, None, None) , 0 , )),
	(( 'Sweeptime' , 'pVal' , ), 1610678277, (1610678277, (), [ (16389, 10, None, None) , ], 1 , 2 , 4 , 0 , 36 , (3, 0, None, None) , 0 , )),
	(( 'SweepTimeAuto' , 'pVal' , ), 1610678279, (1610678279, (), [ (11, 1, None, None) , ], 1 , 4 , 4 , 0 , 40 , (3, 0, None, None) , 0 , )),
	(( 'SweepTimeAuto' , 'pVal' , ), 1610678279, (1610678279, (), [ (16395, 10, None, None) , ], 1 , 2 , 4 , 0 , 44 , (3, 0, None, None) , 0 , )),
	(( 'VideoBandwidth' , 'pVal' , ), 1610678281, (1610678281, (), [ (5, 1, None, None) , ], 1 , 4 , 4 , 0 , 48 , (3, 0, None, None) , 0 , )),
	(( 'VideoBandwidth' , 'pVal' , ), 1610678281, (1610678281, (), [ (16389, 10, None, None) , ], 1 , 2 , 4 , 0 , 52 , (3, 0, None, None) , 0 , )),
	(( 'VideoBandwidthAuto' , 'pVal' , ), 1610678283, (1610678283, (), [ (11, 1, None, None) , ], 1 , 4 , 4 , 0 , 56 , (3, 0, None, None) , 0 , )),
	(( 'VideoBandwidthAuto' , 'pVal' , ), 1610678283, (1610678283, (), [ (16395, 10, None, None) , ], 1 , 2 , 4 , 0 , 60 , (3, 0, None, None) , 0 , )),
]

IRsFsTrace_vtables_dispatch_ = 0
IRsFsTrace_vtables_ = [
	(( 'FetchY' , 'Amplitude' , ), 1610678272, (1610678272, (), [ (24581, 3, None, None) , ], 1 , 1 , 4 , 0 , 12 , (3, 0, None, None) , 0 , )),
	(( 'ReadY' , 'MaxTime' , 'Amplitude' , ), 1610678273, (1610678273, (), [ (3, 1, None, None) , 
			(24581, 3, None, None) , ], 1 , 1 , 4 , 0 , 16 , (3, 0, None, None) , 0 , )),
	(( 'Size' , 'pVal' , ), 1610678274, (1610678274, (), [ (16387, 10, None, None) , ], 1 , 2 , 4 , 0 , 20 , (3, 0, None, None) , 0 , )),
	(( 'Type' , 'pVal' , ), 1610678275, (1610678275, (), [ (16387, 10, None, None) , ], 1 , 2 , 4 , 0 , 24 , (3, 0, None, None) , 0 , )),
	(( 'Type' , 'pVal' , ), 1610678275, (1610678275, (), [ (3, 1, None, None) , ], 1 , 4 , 4 , 0 , 28 , (3, 0, None, None) , 0 , )),
]

IRsFsTraces_vtables_dispatch_ = 0
IRsFsTraces_vtables_ = [
	(( 'Abort' , ), 1610678272, (1610678272, (), [ ], 1 , 1 , 4 , 0 , 12 , (3, 0, None, None) , 0 , )),
	(( 'AcquisitionStatus' , 'pVal' , ), 1610678273, (1610678273, (), [ (16387, 10, None, None) , ], 1 , 1 , 4 , 0 , 16 , (3, 0, None, None) , 0 , )),
	(( 'Count' , 'pVal' , ), 1610678274, (1610678274, (), [ (16387, 10, None, None) , ], 1 , 2 , 4 , 0 , 20 , (3, 0, None, None) , 0 , )),
	(( 'Initiate' , ), 1610678275, (1610678275, (), [ ], 1 , 1 , 4 , 0 , 24 , (3, 0, None, None) , 0 , )),
	(( 'Item' , 'Name' , 'pVal' , ), 1610678276, (1610678276, (), [ (8, 1, None, None) , 
			(16397, 10, None, "IID('{3E5B9739-D454-4585-9F8D-41D84CD795EE}')") , ], 1 , 2 , 4 , 0 , 28 , (3, 0, None, None) , 0 , )),
	(( 'Math' , 'pVal' , ), 1610678277, (1610678277, (), [ (16397, 10, None, "IID('{3B43B21E-D2D5-44F4-A425-02C95B5995EC}')") , ], 1 , 2 , 4 , 0 , 32 , (3, 0, None, None) , 0 , )),
	(( 'Name' , 'Index' , 'pVal' , ), 1610678278, (1610678278, (), [ (3, 1, None, None) , 
			(16392, 10, None, None) , ], 1 , 2 , 4 , 0 , 36 , (3, 0, None, None) , 0 , )),
	(( 'SendSoftwareTrigger' , ), 1610678279, (1610678279, (), [ ], 1 , 1 , 4 , 0 , 40 , (3, 0, None, None) , 0 , )),
]

IRsFsTracesMath_vtables_dispatch_ = 0
IRsFsTracesMath_vtables_ = [
	(( 'Add' , 'DestinationTrace' , 'Trace1' , 'Trace2' , ), 1610678272, (1610678272, (), [ 
			(8, 1, None, None) , (8, 1, None, None) , (8, 1, None, None) , ], 1 , 1 , 4 , 0 , 12 , (3, 0, None, None) , 0 , )),
	(( 'Copy' , 'DestinationTrace' , 'SourceTrace' , ), 1610678273, (1610678273, (), [ (8, 1, None, None) , 
			(8, 1, None, None) , ], 1 , 1 , 4 , 0 , 16 , (3, 0, None, None) , 0 , )),
	(( 'Exchange' , 'Trace1' , 'Trace2' , ), 1610678274, (1610678274, (), [ (8, 1, None, None) , 
			(8, 1, None, None) , ], 1 , 1 , 4 , 0 , 20 , (3, 0, None, None) , 0 , )),
	(( 'Subtract' , 'DestinationTrace' , 'Trace1' , 'Trace2' , ), 1610678275, (1610678275, (), [ 
			(8, 1, None, None) , (8, 1, None, None) , (8, 1, None, None) , ], 1 , 1 , 4 , 0 , 24 , (3, 0, None, None) , 0 , )),
]

IRsFsTrigger_vtables_dispatch_ = 0
IRsFsTrigger_vtables_ = [
	(( 'External' , 'pVal' , ), 1610678272, (1610678272, (), [ (16397, 10, None, "IID('{0B91CDA2-120E-4327-B802-2A9A23FA45E4}')") , ], 1 , 2 , 4 , 0 , 12 , (3, 0, None, None) , 0 , )),
	(( 'Source' , 'pVal' , ), 1610678273, (1610678273, (), [ (3, 1, None, None) , ], 1 , 4 , 4 , 0 , 16 , (3, 0, None, None) , 0 , )),
	(( 'Source' , 'pVal' , ), 1610678273, (1610678273, (), [ (16387, 10, None, None) , ], 1 , 2 , 4 , 0 , 20 , (3, 0, None, None) , 0 , )),
	(( 'TriggerIFPower' , 'pVal' , ), 1610678275, (1610678275, (), [ (16397, 10, None, "IID('{387C563C-B3EB-4C63-99B9-51A073F3B6B7}')") , ], 1 , 2 , 4 , 0 , 24 , (3, 0, None, None) , 0 , )),
	(( 'TriggerRFPower' , 'pVal' , ), 1610678276, (1610678276, (), [ (16397, 10, None, "IID('{398865DD-BF19-426E-9C7E-ACFD7313338C}')") , ], 1 , 2 , 4 , 0 , 28 , (3, 0, None, None) , 0 , )),
	(( 'Video' , 'pVal' , ), 1610678277, (1610678277, (), [ (16397, 10, None, "IID('{46B108AD-3F97-4C74-8658-989B05C1CA09}')") , ], 1 , 2 , 4 , 0 , 32 , (3, 0, None, None) , 0 , )),
]

IRsFsTriggerExternal_vtables_dispatch_ = 0
IRsFsTriggerExternal_vtables_ = [
	(( 'Slope' , 'pVal' , ), 1610678272, (1610678272, (), [ (3, 1, None, None) , ], 1 , 4 , 4 , 0 , 12 , (3, 0, None, None) , 0 , )),
	(( 'Slope' , 'pVal' , ), 1610678272, (1610678272, (), [ (16387, 10, None, None) , ], 1 , 2 , 4 , 0 , 16 , (3, 0, None, None) , 0 , )),
]

IRsFsTriggerIFPower_vtables_dispatch_ = 0
IRsFsTriggerIFPower_vtables_ = [
	(( 'Level' , 'pVal' , ), 1610678272, (1610678272, (), [ (5, 1, None, None) , ], 1 , 4 , 4 , 0 , 12 , (3, 0, None, None) , 0 , )),
	(( 'Level' , 'pVal' , ), 1610678272, (1610678272, (), [ (16389, 10, None, None) , ], 1 , 2 , 4 , 0 , 16 , (3, 0, None, None) , 0 , )),
]

IRsFsTriggerRFPower_vtables_dispatch_ = 0
IRsFsTriggerRFPower_vtables_ = [
	(( 'Level' , 'pVal' , ), 1610678272, (1610678272, (), [ (5, 1, None, None) , ], 1 , 4 , 4 , 0 , 12 , (3, 0, None, None) , 0 , )),
	(( 'Level' , 'pVal' , ), 1610678272, (1610678272, (), [ (16389, 10, None, None) , ], 1 , 2 , 4 , 0 , 16 , (3, 0, None, None) , 0 , )),
]

IRsFsTriggerVideo_vtables_dispatch_ = 0
IRsFsTriggerVideo_vtables_ = [
	(( 'Configure' , 'VideoTriggerLevel' , 'VideoTriggerSlope' , ), 1610678272, (1610678272, (), [ (5, 1, None, None) , 
			(3, 1, None, None) , ], 1 , 1 , 4 , 0 , 12 , (3, 0, None, None) , 0 , )),
	(( 'Level' , 'pVal' , ), 1610678273, (1610678273, (), [ (5, 1, None, None) , ], 1 , 4 , 4 , 0 , 16 , (3, 0, None, None) , 0 , )),
	(( 'Level' , 'pVal' , ), 1610678273, (1610678273, (), [ (16389, 10, None, None) , ], 1 , 2 , 4 , 0 , 20 , (3, 0, None, None) , 0 , )),
	(( 'Slope' , 'pVal' , ), 1610678275, (1610678275, (), [ (3, 1, None, None) , ], 1 , 4 , 4 , 0 , 24 , (3, 0, None, None) , 0 , )),
	(( 'Slope' , 'pVal' , ), 1610678275, (1610678275, (), [ (16387, 10, None, None) , ], 1 , 2 , 4 , 0 , 28 , (3, 0, None, None) , 0 , )),
]

RecordMap = {
}

CLSIDToClassMap = {
	'{489BA02C-0FBE-475F-B444-E7F49FAE351A}' : RsFsSpecAnTrace,
	'{3E3BADCF-2555-470C-868C-D6A9A81F1EA7}' : RsFs,
	'{5FD0328A-578C-46B2-82BA-04ACD18D6778}' : RsFsTrace,
}
CLSIDToPackageMap = {}
win32com.client.CLSIDToClass.RegisterCLSIDsFromDict( CLSIDToClassMap )
VTablesToPackageMap = {}
VTablesToClassMap = {
	'{CC5EAD4E-B151-49BD-9069-314C91A698DC}' : 'IRsFs',
	'{29C38F28-042D-4690-A4FC-3FD3870A5C94}' : 'IRsFsMarkerFrequencyCounter',
	'{1105AB7B-F3CC-41BA-BDA7-947287E93AA6}' : 'IRsFsAcquisition',
	'{9C98FDA3-F036-4EB4-B344-83DA98709C92}' : 'IRsFsInstrumentIO',
	'{1AA16416-D27A-4FC1-8E50-974DA721F912}' : 'IRsFsFrequency',
	'{398865DD-BF19-426E-9C7E-ACFD7313338C}' : 'IRsFsTriggerRFPower',
	'{387C563C-B3EB-4C63-99B9-51A073F3B6B7}' : 'IRsFsTriggerIFPower',
	'{53A008BA-C2CC-4A11-85BA-D56B4039588A}' : 'IRsFsDisplay',
	'{41688FA8-4ABE-43DF-AF6C-A1E3978EB30A}' : 'IRsFsList',
	'{AAC9A50C-C75B-4AD6-87D3-68C640CC0F67}' : 'IRsFsCalibration',
	'{46B108AD-3F97-4C74-8658-989B05C1CA09}' : 'IRsFsTriggerVideo',
	'{ECAD6FD0-27FF-41FE-9919-BF8CE89D6A20}' : 'IRsFsMarker',
	'{3B43B21E-D2D5-44F4-A425-02C95B5995EC}' : 'IRsFsTracesMath',
	'{2882646A-4189-411B-9122-FA3FFF4A2616}' : 'IRsFsTraces',
	'{0B91CDA2-120E-4327-B802-2A9A23FA45E4}' : 'IRsFsTriggerExternal',
	'{3E5B9739-D454-4585-9F8D-41D84CD795EE}' : 'IRsFsTrace',
	'{8C3AD776-FC96-43BF-8D13-A3DFA0BD942F}' : 'IRsFsLevel',
	'{84DCA769-FA4E-41C0-8265-88CF67136B95}' : 'IRsFsTrigger',
	'{0B9155D3-D830-443A-BA54-AC1AC19A6906}' : 'IRsFsSweepCoupling',
	'{10F6C4EE-8F37-45D0-B9B6-CD710D53CC84}' : 'IRsFsService',
}


NamesToIIDMap = {
	'IRsFsInstrumentIO' : '{9C98FDA3-F036-4EB4-B344-83DA98709C92}',
	'IRsFsDisplay' : '{53A008BA-C2CC-4A11-85BA-D56B4039588A}',
	'IRsFsService' : '{10F6C4EE-8F37-45D0-B9B6-CD710D53CC84}',
	'IRsFsTrace' : '{3E5B9739-D454-4585-9F8D-41D84CD795EE}',
	'IRsFsTrigger' : '{84DCA769-FA4E-41C0-8265-88CF67136B95}',
	'IRsFs' : '{CC5EAD4E-B151-49BD-9069-314C91A698DC}',
	'IRsFsTriggerRFPower' : '{398865DD-BF19-426E-9C7E-ACFD7313338C}',
	'IRsFsTriggerIFPower' : '{387C563C-B3EB-4C63-99B9-51A073F3B6B7}',
	'IRsFsSweepCoupling' : '{0B9155D3-D830-443A-BA54-AC1AC19A6906}',
	'IRsFsMarker' : '{ECAD6FD0-27FF-41FE-9919-BF8CE89D6A20}',
	'IRsFsAcquisition' : '{1105AB7B-F3CC-41BA-BDA7-947287E93AA6}',
	'IRsFsLevel' : '{8C3AD776-FC96-43BF-8D13-A3DFA0BD942F}',
	'IRsFsTraces' : '{2882646A-4189-411B-9122-FA3FFF4A2616}',
	'IRsFsTracesMath' : '{3B43B21E-D2D5-44F4-A425-02C95B5995EC}',
	'IRsFsList' : '{41688FA8-4ABE-43DF-AF6C-A1E3978EB30A}',
	'IRsFsMarkerFrequencyCounter' : '{29C38F28-042D-4690-A4FC-3FD3870A5C94}',
	'IRsFsCalibration' : '{AAC9A50C-C75B-4AD6-87D3-68C640CC0F67}',
	'IRsFsTriggerVideo' : '{46B108AD-3F97-4C74-8658-989B05C1CA09}',
	'IRsFsTriggerExternal' : '{0B91CDA2-120E-4327-B802-2A9A23FA45E4}',
	'IRsFsFrequency' : '{1AA16416-D27A-4FC1-8E50-974DA721F912}',
}

win32com.client.constants.__dicts__.append(constants.__dict__)

