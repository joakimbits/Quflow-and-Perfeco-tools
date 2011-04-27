# Created by makepy.py version 0.4.94
# By python version 2.4.2 (#67, Sep 28 2005, 12:41:11) [MSC v.1310 32 bit (Intel)]
# From type library 'labview.tlb'
# On Tue Nov 01 11:07:29 2005
"""LabVIEW 7.1 Type Library"""
makepy_version = '0.4.94'
python_version = 0x20402f0

import win32com.client.CLSIDToClass, pythoncom
import win32com.client.util
from pywintypes import IID
from win32com.client import Dispatch

# The following 3 lines may need tweaking for the particular server
# Candidates are pythoncom.Missing and pythoncom.Empty
defaultNamedOptArg=pythoncom.Empty
defaultNamedNotOptArg=pythoncom.Empty
defaultUnnamedArg=pythoncom.Empty

CLSID = IID('{9A872073-0A06-11D1-90B7-00A024CE2744}')
MajorVersion = 5
MinorVersion = 4
LibraryFlags = 8
LCID = 0x409

class constants:
	eCustomKind                   =0x6        # from enum AppKindEnum
	eDevSysKind                   =0x1        # from enum AppKindEnum
	eEmbeddedKind                 =0x4        # from enum AppKindEnum
	eEvaluationKind               =0x5        # from enum AppKindEnum
	eInvalidAppKind               =0x0        # from enum AppKindEnum
	eRunTimeSysKind               =0x2        # from enum AppKindEnum
	eStudEdKind                   =0x3        # from enum AppKindEnum
	eAlphaCPU                     =0x7        # from enum AppTargCPUEnum
	eIntelx86CPU                  =0x3        # from enum AppTargCPUEnum
	eInvalidTargCPU               =0x0        # from enum AppTargCPUEnum
	eMIPSCPU                      =0x6        # from enum AppTargCPUEnum
	eMotorola68kCPU               =0x1        # from enum AppTargCPUEnum
	ePARISCCPU                    =0x5        # from enum AppTargCPUEnum
	ePowerPCCPU                   =0x2        # from enum AppTargCPUEnum
	eSPARCCPU                     =0x4        # from enum AppTargCPUEnum
	eAIXOS                        =0xc        # from enum AppTargOSEnum
	eBeOS                         =0xb        # from enum AppTargOSEnum
	eCarbonOS                     =0x10       # from enum AppTargOSEnum
	eHPUXOS                       =0x6        # from enum AppTargOSEnum
	eInvalidTargOS                =0x0        # from enum AppTargOSEnum
	eIrixOS                       =0x9        # from enum AppTargOSEnum
	eLinuxOS                      =0x8        # from enum AppTargOSEnum
	eMacOS                        =0x1        # from enum AppTargOSEnum
	eOSF1OS                       =0xd        # from enum AppTargOSEnum
	ePharlapOS                    =0xf        # from enum AppTargOSEnum
	ePowerMaxOS                   =0x7        # from enum AppTargOSEnum
	eRTXOS                        =0x11       # from enum AppTargOSEnum
	eRhapsodyOS                   =0xa        # from enum AppTargOSEnum
	eSolaris1OS                   =0x4        # from enum AppTargOSEnum
	eSolaris2OS                   =0x5        # from enum AppTargOSEnum
	eVxWorksOS                    =0xe        # from enum AppTargOSEnum
	eWin31OS                      =0x2        # from enum AppTargOSEnum
	eWin95NTOS                    =0x3        # from enum AppTargOSEnum
	eBad                          =0x0        # from enum ExecStateEnum
	eIdle                         =0x1        # from enum ExecStateEnum
	eRunTopLevel                  =0x2        # from enum ExecStateEnum
	eRunning                      =0x3        # from enum ExecStateEnum
	eDefaultFPBehavior            =0x1        # from enum FPBehaviorEnum
	eFloating                     =0x2        # from enum FPBehaviorEnum
	eFloatingAutoHide             =0x3        # from enum FPBehaviorEnum
	eInvalidFPBehavior            =0x0        # from enum FPBehaviorEnum
	eModal                        =0x4        # from enum FPBehaviorEnum
	eFPClosed                     =0x2        # from enum FPStateEnum
	eFPHidden                     =0x3        # from enum FPStateEnum
	eFPMaximized                  =0x5        # from enum FPStateEnum
	eFPMinimized                  =0x4        # from enum FPStateEnum
	eFPStandard                   =0x1        # from enum FPStateEnum
	eInvalidFPState               =0x0        # from enum FPStateEnum
	eGIF                          =0x2        # from enum HTMLImageFormatEnum
	eJPEG                         =0x1        # from enum HTMLImageFormatEnum
	ePNG                          =0x0        # from enum HTMLImageFormatEnum
	eImgDataFmtEMF                =0x2        # from enum ImgDataToFmtEnum
	eImgDataFmtJPEG               =0x1        # from enum ImgDataToFmtEnum
	eImgDataFmtPNG                =0x0        # from enum ImgDataToFmtEnum
	eLandscape                    =0x1        # from enum PageOrientationEnum
	ePortrait                     =0x0        # from enum PageOrientationEnum
	eRotatedLandscape             =0x3        # from enum PageOrientationEnum
	eRotatedPortrait              =0x2        # from enum PageOrientationEnum
	eComplete                     =0x4        # from enum PrintFormatEnum
	eCustom                       =0x0        # from enum PrintFormatEnum
	eStandard                     =0x1        # from enum PrintFormatEnum
	eUsingPanel                   =0x2        # from enum PrintFormatEnum
	eUsingSubVI                   =0x3        # from enum PrintFormatEnum
	eBitmapMethod                 =0x2        # from enum PrintMethodsEnum
	ePostscriptMethod             =0x1        # from enum PrintMethodsEnum
	eStandardMethod               =0x0        # from enum PrintMethodsEnum
	eESysDAQ                      =0x4        # from enum VIExecSysEnum
	eESysInstrIO                  =0x3        # from enum VIExecSysEnum
	eESysInvalid                  =0x0        # from enum VIExecSysEnum
	eESysNormal                   =0x2        # from enum VIExecSysEnum
	eESysOther1                   =0x5        # from enum VIExecSysEnum
	eESysOther2                   =0x6        # from enum VIExecSysEnum
	eESysSameAsCaller             =0x7        # from enum VIExecSysEnum
	eESysUserInterface            =0x1        # from enum VIExecSysEnum
	eInvalidLockState             =0x0        # from enum VILockStateEnum
	eLockedNoPwdState             =0x2        # from enum VILockStateEnum
	ePwdProtectedState            =0x3        # from enum VILockStateEnum
	eUnlockedState                =0x1        # from enum VILockStateEnum
	ePriAboveNormal               =0x3        # from enum VIPriorityEnum
	ePriBackground                =0x1        # from enum VIPriorityEnum
	ePriCritical                  =0x5        # from enum VIPriorityEnum
	ePriHigh                      =0x4        # from enum VIPriorityEnum
	ePriInvalid                   =0x0        # from enum VIPriorityEnum
	ePriNormal                    =0x2        # from enum VIPriorityEnum
	ePriSubroutine                =0x6        # from enum VIPriorityEnum
	eConfigurationVIType          =0x5        # from enum VITypeEnum
	eControlVIType                =0x2        # from enum VITypeEnum
	eGlobalVIType                 =0x3        # from enum VITypeEnum
	eInvalidVIType                =0x0        # from enum VITypeEnum
	ePolymorphicVIType            =0x4        # from enum VITypeEnum
	eStandardVIType               =0x1        # from enum VITypeEnum
	eSubSystemVIType              =0x6        # from enum VITypeEnum

from win32com.client import DispatchBaseClass
class Generic(DispatchBaseClass):
	"""Generic DispInterface"""
	CLSID = IID('{00018C9E-0A06-11D1-90B7-00A024CE2744}')
	coclass_clsid = None

	_prop_map_get_ = {
		"ClassID": (103970816, 2, (3, 0), (), "ClassID", None),
		"ClassName": (103970819, 2, (8, 0), (), "ClassName", None),
		# Method 'Owner' returns object of type 'Generic'
		"Owner": (103970817, 2, (9, 0), (), "Owner", '{00018C9E-0A06-11D1-90B7-00A024CE2744}'),
		# Method 'OwningVI' returns object of type 'VirtualInstrument'
		"OwningVI": (103970818, 2, (9, 0), (), "OwningVI", '{9A872074-0A06-11D1-90B7-00A024CE2744}'),
	}
	_prop_map_put_ = {
	}

class VirtualInstrument(DispatchBaseClass):
	"""VI DispInterface"""
	CLSID = IID('{9A872074-0A06-11D1-90B7-00A024CE2744}')
	coclass_clsid = None

	def Abort(self):
		"""Aborts the VI execution."""
		return self._oleobj_.InvokeTypes(1004, LCID, 1, (24, 0), (),)

	def Call(self, paramNames=defaultNamedOptArg, paramVals=defaultNamedOptArg):
		"""Call the VI as a subVI"""
		return self._ApplyTypes_(1015, 1, (24, 0), ((16396, 19), (16396, 19)), 'Call', None,paramNames
			, paramVals)

	def Call2(self, paramNames=defaultNamedNotOptArg, paramVals=defaultNamedNotOptArg, openFP=defaultNamedNotOptArg, CloseFPAfterCall=defaultNamedNotOptArg
			, SuspendOnCall=defaultNamedNotOptArg, bringAppToFront=defaultNamedNotOptArg):
		"""Calls the specified vi as a sub vi. Call2 allows you to optionally open the front panel before call, closes it afterwards or suspend the VI at the start of call. You can also optionally make LabVIEW as the active application whenever the front panel """
		return self._ApplyTypes_(1030, 1, (24, 0), ((16396, 19), (16396, 19), (11, 17), (11, 17), (11, 17), (11, 17)), 'Call2', None,paramNames
			, paramVals, openFP, CloseFPAfterCall, SuspendOnCall, bringAppToFront
			)

	def CloseFrontPanel(self):
		"""Closes the front panel window."""
		return self._oleobj_.InvokeTypes(1061, LCID, 1, (24, 0), (),)

	def ExportVIStrings(self, stringFile=defaultNamedNotOptArg, interactive=defaultNamedNotOptArg, logFile=defaultNamedNotOptArg, captions=defaultNamedNotOptArg
			, diagram=defaultNamedNotOptArg):
		"""Exports the following strings about VI and front panel objects to a tagged text file: VI name and description, object caption labels, object free labels, default data (string, table, path, and array default data), private data (listbox item names, ta"""
		return self._oleobj_.InvokeTypes(1000, LCID, 1, (24, 0), ((8, 1), (11, 17), (8, 17), (11, 17), (11, 17)),stringFile
			, interactive, logFile, captions, diagram)

	def GetControlValue(self, controlName=defaultNamedNotOptArg):
		"""Get the value of the named control. The first time you call this method on a VI whose front panel is not open, this method returns the default values of the control rather than the actual values. Thereafter, it returns the actual value."""
		return self._ApplyTypes_(1013, 1, (12, 0), ((8, 1),), 'GetControlValue', None,controlName
			)

	def GetLockState(self, pwdInCache=pythoncom.Missing):
		"""Returns the lock state of the VI and indicates whether the password for the VI is in the password cache."""
		return self._ApplyTypes_(1021, 1, (3, 0), ((16395, 18),), 'GetLockState', None,pwdInCache
			)

	def GetPanelImage(self, visibleOnly=defaultNamedNotOptArg, imgDepth=defaultNamedNotOptArg, img=pythoncom.Missing, colors=pythoncom.Missing
			, bounds=pythoncom.Missing):
		"""LabVIEW no longer supports this method. Use the Get Panel Image method instead."""
		return self._ApplyTypes_(1016, 1, (24, 0), ((11, 17), (3, 17), (16396, 18), (16396, 18), (16396, 18)), 'GetPanelImage', None,visibleOnly
			, imgDepth, img, colors, bounds)

	def ImportVIStrings(self, stringFile=defaultNamedNotOptArg, interactive=defaultNamedNotOptArg, logFile=defaultNamedNotOptArg):
		"""Imports the following strings about VI and front panel objects from a tagged text file: VI name and description, object caption labels, object free labels, default data (string, table, path, and array default data), private data (listbox item names, """
		return self._oleobj_.InvokeTypes(1001, LCID, 1, (24, 0), ((8, 1), (11, 17), (8, 17)),stringFile
			, interactive, logFile)

	def MakeCurValueDefault(self):
		"""Changes the defaults of all controls on the front panel to be the current values. This method is available only in edit mode."""
		return self._oleobj_.InvokeTypes(1011, LCID, 1, (24, 0), (),)

	def OldOpenFrontPanel(self, Activate=defaultNamedNotOptArg, State=defaultNamedNotOptArg):
		"""Opens the front panel window and returns an error if the front panel is already open. LabVIEW no longer supports this method. Use the Open FP method instead."""
		return self._oleobj_.InvokeTypes(1060, LCID, 1, (24, 0), ((11, 17), (3, 17)),Activate
			, State)

	def OpenFrontPanel(self, Activate=defaultNamedNotOptArg, State=defaultNamedNotOptArg):
		"""Opens the front panel window. If the front panel is already open, this method changes the state of the front panel window to the state you wire to this method."""
		return self._oleobj_.InvokeTypes(1080, LCID, 1, (24, 0), ((11, 17), (3, 17)),Activate
			, State)

	def PrintPanel(self, entirePanel=defaultNamedNotOptArg):
		"""Prints just the front panel to the current printer. You cannot use this method to print a block diagram, list of controls, or polymorphic VI front panel."""
		return self._oleobj_.InvokeTypes(1019, LCID, 1, (24, 0), ((11, 17),),entirePanel
			)

	def PrintVIToHTML(self, htmlFilePath=defaultNamedNotOptArg, append=defaultNamedNotOptArg, format=defaultNamedNotOptArg, imageFormat=defaultNamedNotOptArg
			, imageDepth=defaultNamedNotOptArg, imageDirectory=defaultNamedNotOptArg):
		"""Prints the VI information to an HTML file and saves the graphics in external files. You can use the Open URL in Default Browser VI to display the HTML file in the default Web browser."""
		return self._oleobj_.InvokeTypes(1006, LCID, 1, (24, 0), ((8, 1), (11, 17), (3, 17), (3, 17), (3, 17), (8, 17)),htmlFilePath
			, append, format, imageFormat, imageDepth, imageDirectory
			)

	def PrintVIToPrinter(self, format=defaultNamedNotOptArg, scalePanel=defaultNamedNotOptArg, scaleDiagram=defaultNamedNotOptArg, pageHeaders=defaultNamedNotOptArg
			, pageBreaks=defaultNamedNotOptArg, sectionHeaders=defaultNamedNotOptArg):
		"""Prints the VI information to a printer."""
		return self._oleobj_.InvokeTypes(1005, LCID, 1, (24, 0), ((3, 17), (11, 17), (11, 17), (11, 17), (11, 17), (11, 17)),format
			, scalePanel, scaleDiagram, pageHeaders, pageBreaks, sectionHeaders
			)

	def PrintVIToRTF(self, rtfFilePath=defaultNamedNotOptArg, append=defaultNamedNotOptArg, format=defaultNamedNotOptArg, imageFormat=defaultNamedNotOptArg
			, imageDepth=defaultNamedNotOptArg, imageDirectory=defaultNamedNotOptArg, helpFormat=defaultNamedNotOptArg):
		"""Prints the VI information to an RTF file."""
		return self._oleobj_.InvokeTypes(1007, LCID, 1, (24, 0), ((8, 1), (11, 17), (3, 17), (3, 17), (3, 17), (8, 17), (11, 17)),rtfFilePath
			, append, format, imageFormat, imageDepth, imageDirectory
			, helpFormat)

	def PrintVIToText(self, textFilePath=defaultNamedNotOptArg, append=defaultNamedNotOptArg, format=defaultNamedNotOptArg):
		"""Prints the VI information to a text file."""
		return self._oleobj_.InvokeTypes(1008, LCID, 1, (24, 0), ((8, 1), (11, 17), (3, 17)),textFilePath
			, append, format)

	def ReinitializeAllToDefault(self):
		"""Changes the current values of all controls on the front panel to their defaults."""
		return self._oleobj_.InvokeTypes(1012, LCID, 1, (24, 0), (),)

	def Revert(self):
		"""Discards changes and reloads a VI from disk."""
		return self._oleobj_.InvokeTypes(1018, LCID, 1, (24, 0), (),)

	def Run(self, async=defaultNamedNotOptArg):
		"""Run the VI as a top level VI."""
		return self._oleobj_.InvokeTypes(1017, LCID, 1, (24, 0), ((11, 17),),async
			)

	def SaveForPrevious(self, viPath=defaultNamedNotOptArg, warnings=pythoncom.Missing):
		"""Saves a copy of the VI that is readable by LabVIEW version 7.0 and later."""
		return self._ApplyTypes_(1024, 1, (24, 0), ((8, 1), (16392, 18)), 'SaveForPrevious', None,viPath
			, warnings)

	def SaveInstrument(self, viPath=defaultNamedNotOptArg, saveACopy=defaultNamedNotOptArg, withoutDiagram=defaultNamedNotOptArg):
		"""Saves a VI."""
		return self._oleobj_.InvokeTypes(1002, LCID, 1, (24, 0), ((8, 17), (11, 17), (11, 17)),viPath
			, saveACopy, withoutDiagram)

	def SaveRunTimeMenu(self, filePath=defaultNamedNotOptArg):
		"""Saves the run-time menu to a file specified by Path. This method works only when the VI is running. It saves only menu items with valid tags."""
		return self._oleobj_.InvokeTypes(1043, LCID, 1, (24, 0), ((8, 1),),filePath
			)

	def SetControlValue(self, controlName=defaultNamedNotOptArg, value=defaultNamedNotOptArg):
		"""Set the value of the named control."""
		return self._oleobj_.InvokeTypes(1014, LCID, 1, (24, 0), ((8, 1), (12, 1)),controlName
			, value)

	def SetLockState(self, lockState=defaultNamedNotOptArg, interactive=defaultNamedNotOptArg, password=defaultNamedNotOptArg, putInCache=defaultNamedNotOptArg):
		"""Sets the lock state of a VI. If interactive is FALSE (default), you can use password to either unlock a password-protected VI or set the password of an unprotected VI. If interactive is TRUE, LabVIEW ignores password and displays a dialog box that pr"""
		return self._oleobj_.InvokeTypes(1022, LCID, 1, (24, 0), ((3, 1), (11, 17), (8, 17), (11, 17)),lockState
			, interactive, password, putInCache)

	def SetVIIcon(self, imageFile=defaultNamedNotOptArg):
		"""Sets the image of a VI icon from a file."""
		return self._oleobj_.InvokeTypes(1031, LCID, 1, (24, 0), ((8, 1),),imageFile
			)

	_prop_map_get_ = {
		"AllowDebugging": (518, 2, (11, 0), (), "AllowDebugging", None),
		"BDModificationBitSet": (539, 2, (3, 0), (), "BDModificationBitSet", None),
		"BDSize": (543, 2, (3, 0), (), "BDSize", None),
		"Callees": (545, 2, (12, 0), (), "Callees", None),
		"Callers": (544, 2, (12, 0), (), "Callers", None),
		"CloseFPAfterCall": (523, 2, (11, 0), (), "CloseFPAfterCall", None),
		"CodeSize": (540, 2, (3, 0), (), "CodeSize", None),
		"DataSize": (541, 2, (3, 0), (), "DataSize", None),
		"Description": (502, 2, (8, 0), (), "Description", None),
		"EditMode": (558, 2, (11, 0), (), "EditMode", None),
		"ExecPriority": (526, 2, (3, 0), (), "ExecPriority", None),
		"ExecState": (557, 2, (3, 0), (), "ExecState", None),
		"ExpandWhenDroppedAsSubVI": (585, 2, (11, 0), (), "ExpandWhenDroppedAsSubVI", None),
		"FPAllowRTPopup": (508, 2, (11, 0), (), "FPAllowRTPopup", None),
		"FPAutoCenter": (511, 2, (11, 0), (), "FPAutoCenter", None),
		"FPBehavior": (601, 2, (3, 0), (), "FPBehavior", None),
		"FPHiliteReturnButton": (509, 2, (11, 0), (), "FPHiliteReturnButton", None),
		"FPKeepWinProps": (575, 2, (11, 0), (), "FPKeepWinProps", None),
		"FPMinimizeable": (583, 2, (11, 0), (), "FPMinimizeable", None),
		"FPModificationBitSet": (538, 2, (3, 0), (), "FPModificationBitSet", None),
		"FPResizable": (507, 2, (11, 0), (), "FPResizable", None),
		"FPResizeable": (584, 2, (11, 0), (), "FPResizeable", None),
		"FPShowMenuBar": (513, 2, (11, 0), (), "FPShowMenuBar", None),
		"FPShowScrollBars": (512, 2, (11, 0), (), "FPShowScrollBars", None),
		"FPSize": (542, 2, (3, 0), (), "FPSize", None),
		"FPSizeToScreen": (510, 2, (11, 0), (), "FPSizeToScreen", None),
		"FPState": (608, 2, (3, 0), (), "FPState", None),
		"FPTitleBarVisible": (505, 2, (11, 0), (), "FPTitleBarVisible", None),
		"FPWinBounds": (536, 2, (12, 0), (), "FPWinBounds", None),
		"FPWinClosable": (506, 2, (11, 0), (), "FPWinClosable", None),
		"FPWinCustomTitle": (561, 2, (11, 0), (), "FPWinCustomTitle", None),
		"FPWinIsFrontMost": (535, 2, (11, 0), (), "FPWinIsFrontMost", None),
		"FPWinOpen": (534, 2, (11, 0), (), "FPWinOpen", None),
		"FPWinOrigin": (552, 2, (12, 0), (), "FPWinOrigin", None),
		"FPWinPanelBounds": (556, 2, (12, 0), (), "FPWinPanelBounds", None),
		"FPWinTitle": (553, 2, (8, 0), (), "FPWinTitle", None),
		"HelpDocumentPath": (533, 2, (8, 0), (), "HelpDocumentPath", None),
		"HelpDocumentTag": (532, 2, (8, 0), (), "HelpDocumentTag", None),
		"HistAddCommentsAtSave": (528, 2, (11, 0), (), "HistAddCommentsAtSave", None),
		"HistPromptAtClose": (529, 2, (11, 0), (), "HistPromptAtClose", None),
		"HistPromptForCommentsAtSave": (530, 2, (11, 0), (), "HistPromptForCommentsAtSave", None),
		"HistRecordAppComments": (531, 2, (11, 0), (), "HistRecordAppComments", None),
		"HistUseDefaults": (527, 2, (11, 0), (), "HistUseDefaults", None),
		"HistoryText": (503, 2, (8, 0), (), "HistoryText", None),
		"IsProbe": (618, 2, (11, 0), (), "IsProbe", None),
		"IsReentrant": (525, 2, (11, 0), (), "IsReentrant", None),
		"LogAtFinish": (550, 2, (11, 0), (), "LogAtFinish", None),
		"LogFilePath": (549, 2, (8, 0), (), "LogFilePath", None),
		"Name": (500, 2, (8, 0), (), "Name", None),
		"Path": (501, 2, (8, 0), (), "Path", None),
		"PreferredExecSystem": (559, 2, (3, 0), (), "PreferredExecSystem", None),
		"PrintHeaderDatePrint": (592, 2, (11, 0), (), "PrintHeaderDatePrint", None),
		"PrintHeaderModifyDate": (593, 2, (11, 0), (), "PrintHeaderModifyDate", None),
		"PrintHeaderPageNumber": (594, 2, (11, 0), (), "PrintHeaderPageNumber", None),
		"PrintHeaderVIIcon": (595, 2, (11, 0), (), "PrintHeaderVIIcon", None),
		"PrintHeaderVIName": (590, 2, (11, 0), (), "PrintHeaderVIName", None),
		"PrintLogFileAtFinish": (551, 2, (11, 0), (), "PrintLogFileAtFinish", None),
		"PrintMargins": (589, 2, (12, 0), (), "PrintMargins", None),
		"PrintingBDScaling": (610, 2, (11, 0), (), "PrintingBDScaling", None),
		"PrintingFPScaling": (588, 2, (11, 0), (), "PrintingFPScaling", None),
		"PrintingHeaderVIPath": (596, 2, (11, 0), (), "PrintingHeaderVIPath", None),
		"PrintingHeaders": (587, 2, (11, 0), (), "PrintingHeaders", None),
		"PrintingOrientation": (586, 2, (3, 0), (), "PrintingOrientation", None),
		"RevisionNumber": (564, 2, (3, 0), (), "RevisionNumber", None),
		"RunOnOpen": (524, 2, (11, 0), (), "RunOnOpen", None),
		"RunTimeMenuPath": (580, 2, (8, 0), (), "RunTimeMenuPath", None),
		"ShowFPOnCall": (522, 2, (11, 0), (), "ShowFPOnCall", None),
		"ShowFPOnLoad": (521, 2, (11, 0), (), "ShowFPOnLoad", None),
		"SuspendOnCall": (555, 2, (11, 0), (), "SuspendOnCall", None),
		"TBShowAbortButton": (517, 2, (11, 0), (), "TBShowAbortButton", None),
		"TBShowFreeRunButton": (516, 2, (11, 0), (), "TBShowFreeRunButton", None),
		"TBShowRunButton": (515, 2, (11, 0), (), "TBShowRunButton", None),
		"TBVisible": (514, 2, (11, 0), (), "TBVisible", None),
		"VIModificationBitSet": (537, 2, (3, 0), (), "VIModificationBitSet", None),
		"VIType": (562, 2, (3, 0), (), "VIType", None),
		"_BDWinOSWindow": (616, 2, (3, 0), (), "_BDWinOSWindow", None),
		"_CPTM_CRC": (614, 2, (3, 0), (), "_CPTM_CRC", None),
		"_FPWinOSWindow": (615, 2, (3, 0), (), "_FPWinOSWindow", None),
	}
	_prop_map_put_ = {
		"AllowDebugging": ((518, LCID, 4, 0),()),
		"CloseFPAfterCall": ((523, LCID, 4, 0),()),
		"Description": ((502, LCID, 4, 0),()),
		"EditMode": ((558, LCID, 4, 0),()),
		"ExecPriority": ((526, LCID, 4, 0),()),
		"ExpandWhenDroppedAsSubVI": ((585, LCID, 4, 0),()),
		"FPAllowRTPopup": ((508, LCID, 4, 0),()),
		"FPAutoCenter": ((511, LCID, 4, 0),()),
		"FPBehavior": ((601, LCID, 4, 0),()),
		"FPHiliteReturnButton": ((509, LCID, 4, 0),()),
		"FPKeepWinProps": ((575, LCID, 4, 0),()),
		"FPMinimizeable": ((583, LCID, 4, 0),()),
		"FPResizable": ((507, LCID, 4, 0),()),
		"FPResizeable": ((584, LCID, 4, 0),()),
		"FPShowMenuBar": ((513, LCID, 4, 0),()),
		"FPShowScrollBars": ((512, LCID, 4, 0),()),
		"FPSizeToScreen": ((510, LCID, 4, 0),()),
		"FPState": ((608, LCID, 4, 0),()),
		"FPTitleBarVisible": ((505, LCID, 4, 0),()),
		"FPWinBounds": ((536, LCID, 4, 0),()),
		"FPWinClosable": ((506, LCID, 4, 0),()),
		"FPWinCustomTitle": ((561, LCID, 4, 0),()),
		"FPWinIsFrontMost": ((535, LCID, 4, 0),()),
		"FPWinOpen": ((534, LCID, 4, 0),()),
		"FPWinOrigin": ((552, LCID, 4, 0),()),
		"FPWinPanelBounds": ((556, LCID, 4, 0),()),
		"FPWinTitle": ((553, LCID, 4, 0),()),
		"HelpDocumentPath": ((533, LCID, 4, 0),()),
		"HelpDocumentTag": ((532, LCID, 4, 0),()),
		"HistAddCommentsAtSave": ((528, LCID, 4, 0),()),
		"HistPromptAtClose": ((529, LCID, 4, 0),()),
		"HistPromptForCommentsAtSave": ((530, LCID, 4, 0),()),
		"HistRecordAppComments": ((531, LCID, 4, 0),()),
		"HistUseDefaults": ((527, LCID, 4, 0),()),
		"IsReentrant": ((525, LCID, 4, 0),()),
		"LogAtFinish": ((550, LCID, 4, 0),()),
		"LogFilePath": ((549, LCID, 4, 0),()),
		"Name": ((500, LCID, 4, 0),()),
		"PreferredExecSystem": ((559, LCID, 4, 0),()),
		"PrintHeaderDatePrint": ((592, LCID, 4, 0),()),
		"PrintHeaderModifyDate": ((593, LCID, 4, 0),()),
		"PrintHeaderPageNumber": ((594, LCID, 4, 0),()),
		"PrintHeaderVIIcon": ((595, LCID, 4, 0),()),
		"PrintHeaderVIName": ((590, LCID, 4, 0),()),
		"PrintLogFileAtFinish": ((551, LCID, 4, 0),()),
		"PrintMargins": ((589, LCID, 4, 0),()),
		"PrintingBDScaling": ((610, LCID, 4, 0),()),
		"PrintingFPScaling": ((588, LCID, 4, 0),()),
		"PrintingHeaderVIPath": ((596, LCID, 4, 0),()),
		"PrintingHeaders": ((587, LCID, 4, 0),()),
		"PrintingOrientation": ((586, LCID, 4, 0),()),
		"RevisionNumber": ((564, LCID, 4, 0),()),
		"RunOnOpen": ((524, LCID, 4, 0),()),
		"RunTimeMenuPath": ((580, LCID, 4, 0),()),
		"ShowFPOnCall": ((522, LCID, 4, 0),()),
		"ShowFPOnLoad": ((521, LCID, 4, 0),()),
		"SuspendOnCall": ((555, LCID, 4, 0),()),
		"TBShowAbortButton": ((517, LCID, 4, 0),()),
		"TBShowFreeRunButton": ((516, LCID, 4, 0),()),
		"TBShowRunButton": ((515, LCID, 4, 0),()),
		"TBVisible": ((514, LCID, 4, 0),()),
	}

class _Application(DispatchBaseClass):
	"""Application DispInterface"""
	CLSID = IID('{9A872072-0A06-11D1-90B7-00A024CE2744}')
	coclass_clsid = IID('{9A872070-0A06-11D1-90B7-00A024CE2744}')

	def BringToFront(self):
		"""On Windows and Mac OS, brings the application windows to the front."""
		return self._oleobj_.InvokeTypes(2014, LCID, 1, (24, 0), (),)

	def BrowseDataSocket(self, Prompt=defaultNamedNotOptArg, SelectedURL=pythoncom.Missing):
		"""Launches the DataSocket Browser dialog box."""
		return self._ApplyTypes_(2043, 1, (24, 0), ((8, 17), (16392, 18)), 'BrowseDataSocket', None,Prompt
			, SelectedURL)

	def CheckConnection(self):
		"""Checks if the VI Server connection is responsive."""
		return self._oleobj_.InvokeTypes(2039, LCID, 1, (24, 0), (),)

	def ConnInfo(self, pingDelay=defaultNamedNotOptArg, prevPingDelay=pythoncom.Missing, pingTimeout=defaultNamedNotOptArg, prevPingTimeout=pythoncom.Missing):
		"""Gets and sets how often LabVIEW checks if a VI Server connection is responsive."""
		return self._ApplyTypes_(2040, 1, (24, 0), ((3, 17), (16387, 18), (3, 17), (16387, 18)), 'ConnInfo', None,pingDelay
			, prevPingDelay, pingTimeout, prevPingTimeout)

	def DisconnectFromSlave(self):
		"""Disconnects a LabVIEW RT Module from the RT Engine to which it is targeted."""
		return self._oleobj_.InvokeTypes(2019, LCID, 1, (24, 0), (),)

	# Result is of type VirtualInstrument
	def GetVIReference(self, viPath=defaultNamedNotOptArg, password=defaultNamedNotOptArg, resvForCall=defaultNamedNotOptArg, options=defaultNamedNotOptArg):
		"""Get Reference to a VI specified by path."""
		ret = self._oleobj_.InvokeTypes(2004, LCID, 1, (9, 0), ((8, 1), (8, 17), (11, 17), (3, 17)),viPath
			, password, resvForCall, options)
		if ret is not None:
			ret = Dispatch(ret, 'GetVIReference', '{9A872074-0A06-11D1-90B7-00A024CE2744}', UnicodeToString=0)
		return ret

	def GetVIVersion(self, viPath=defaultNamedNotOptArg, VersNum=pythoncom.Missing):
		"""Gets the version of LabVIEW in which the VI was last saved. If you specify a path to a file that is not a VI, LabVIEW returns error 1059."""
		return self._ApplyTypes_(2041, 1, (8, 0), ((8, 1), (16387, 2)), 'GetVIVersion', None,viPath
			, VersNum)

	def MassCompile(self, directory=defaultNamedNotOptArg, logFile=defaultNamedNotOptArg, appendLog=defaultNamedNotOptArg, viCacheSize=defaultNamedNotOptArg
			, reloadLVSBs=defaultNamedNotOptArg):
		"""Loads and compiles all the VIs in a directory and all its subdirectories."""
		return self._oleobj_.InvokeTypes(2005, LCID, 1, (24, 0), ((8, 1), (8, 17), (11, 17), (3, 17), (11, 17)),directory
			, logFile, appendLog, viCacheSize, reloadLVSBs)

	def Quit(self):
		"""Quits the Application"""
		return self._oleobj_.InvokeTypes(2013, LCID, 1, (24, 0), (),)

	def ResolveSymbolicPath(self, SymbolicPath=defaultNamedNotOptArg, ActualPath=pythoncom.Missing):
		"""If the input path is a LabVIEW symbolic path, such as those that the Document Path property returns, this method converts it into an absolute path. For example, if you wire :glang.chm, this method returns C:Program Files
ational InstrumentsLabVIE"""
		return self._ApplyTypes_(2044, 1, (24, 0), ((8, 1), (16392, 2)), 'ResolveSymbolicPath', None,SymbolicPath
			, ActualPath)

	def _CompareTypes(self, typeDesc1=defaultNamedNotOptArg, typeDesc2=defaultNamedNotOptArg, options=defaultNamedNotOptArg):
		"""Compares 2 type descriptors. The return value is a bit mask. Look for 'sUnknown' in labview.h to figure out what the bits correspond to. Options is also a bit mask. Look for 'kAllNameChk' in datatype.cpp to specify the bits."""
		return self._oleobj_.InvokeTypes(2028, LCID, 1, (3, 0), ((12, 1), (12, 1), (2, 17)),typeDesc1
			, typeDesc2, options)

	def _CreateLVClsInst(self, ClassID=defaultNamedNotOptArg):
		"""Creates an instance of a LabVIEW class object, in particularly, an IDispatch* to an IAnalogWaveForm, IDigitalWaveForm, or IWaveForms object of one of the available waveform types"""
		return self._ApplyTypes_(2057, 1, (12, 0), ((3, 1),), '_CreateLVClsInst', None,ClassID
			)

	def _DropControlOrFunction(self, ctlFuncName=defaultNamedNotOptArg):
		"""This is an internal method."""
		return self._oleobj_.InvokeTypes(2042, LCID, 1, (11, 0), ((8, 1),),ctlFuncName
			)

	def _GetPalMenuInfo(self, palMenuPath=defaultNamedNotOptArg):
		"""Read the given palette menu's information"""
		return self._ApplyTypes_(2062, 1, (12, 0), ((8, 17),), '_GetPalMenuInfo', None,palMenuPath
			)

	def _HilitePalMenuItem(self, funcname=defaultNamedNotOptArg, Position=defaultNamedOptArg):
		"""Highlights a control or function in a control/function palette."""
		return self._oleobj_.InvokeTypes(2045, LCID, 1, (11, 0), ((8, 1), (12, 17)),funcname
			, Position)

	_prop_map_get_ = {
		"AllVIsInMemory": (28, 2, (12, 0), (), "AllVIsInMemory", None),
		"AppDefltDataLoc": (87, 2, (8, 0), (), "AppDefltDataLoc", None),
		"AppKind": (3, 2, (3, 0), (), "AppKind", None),
		"AppName": (0, 2, (8, 0), (), "AppName", None),
		"AppTargetCPU": (5, 2, (3, 0), (), "AppTargetCPU", None),
		"AppTargetOS": (4, 2, (3, 0), (), "AppTargetOS", None),
		"ApplicationDirectory": (25, 2, (8, 0), (), "ApplicationDirectory", None),
		"AutomaticClose": (35, 2, (11, 0), (), "AutomaticClose", None),
		"CmdArgs": (79, 2, (12, 0), (), "CmdArgs", None),
		"ExportedVIs": (8, 2, (12, 0), (), "ExportedVIs", None),
		"Language": (99, 2, (8, 0), (), "Language", None),
		"OSName": (6, 2, (8, 0), (), "OSName", None),
		"OSVersion": (7, 2, (8, 0), (), "OSVersion", None),
		"PrintDefaultPrinter": (73, 2, (8, 0), (), "PrintDefaultPrinter", None),
		"PrintMethod": (74, 2, (3, 0), (), "PrintMethod", None),
		"PrintSetupCustomConnector": (12, 2, (11, 0), (), "PrintSetupCustomConnector", None),
		"PrintSetupCustomControlDesc": (17, 2, (11, 0), (), "PrintSetupCustomControlDesc", None),
		"PrintSetupCustomControlTypes": (18, 2, (11, 0), (), "PrintSetupCustomControlTypes", None),
		"PrintSetupCustomControls": (16, 2, (11, 0), (), "PrintSetupCustomControls", None),
		"PrintSetupCustomDescription": (13, 2, (11, 0), (), "PrintSetupCustomDescription", None),
		"PrintSetupCustomDiagram": (19, 2, (11, 0), (), "PrintSetupCustomDiagram", None),
		"PrintSetupCustomDiagramHidden": (20, 2, (11, 0), (), "PrintSetupCustomDiagramHidden", None),
		"PrintSetupCustomDiagramRepeat": (21, 2, (11, 0), (), "PrintSetupCustomDiagramRepeat", None),
		"PrintSetupCustomExpressVIConfigInfo": (92, 2, (11, 0), (), "PrintSetupCustomExpressVIConfigInfo", None),
		"PrintSetupCustomHierarchy": (23, 2, (11, 0), (), "PrintSetupCustomHierarchy", None),
		"PrintSetupCustomHistory": (24, 2, (11, 0), (), "PrintSetupCustomHistory", None),
		"PrintSetupCustomPanel": (14, 2, (11, 0), (), "PrintSetupCustomPanel", None),
		"PrintSetupCustomPanelBorder": (15, 2, (11, 0), (), "PrintSetupCustomPanelBorder", None),
		"PrintSetupCustomSubVIs": (22, 2, (11, 0), (), "PrintSetupCustomSubVIs", None),
		"PrintSetupFileWrapText": (9, 2, (3, 0), (), "PrintSetupFileWrapText", None),
		"PrintSetupJPEGQuality": (11, 2, (3, 0), (), "PrintSetupJPEGQuality", None),
		"PrintSetupPNGCompressLevel": (10, 2, (3, 0), (), "PrintSetupPNGCompressLevel", None),
		"PrintersAvailable": (75, 2, (12, 0), (), "PrintersAvailable", None),
		"PrintingColorDepth": (72, 2, (11, 0), (), "PrintingColorDepth", None),
		"RTHostConnected": (90, 2, (11, 0), (), "RTHostConnected", None),
		"ShowFPTipStrips": (63, 2, (11, 0), (), "ShowFPTipStrips", None),
		"UserName": (1, 2, (8, 0), (), "UserName", None),
		"VIServerPort": (93, 2, (3, 0), (), "VIServerPort", None),
		"Version": (2, 2, (8, 0), (), "Version", None),
		"_ModuleHandle": (101, 2, (3, 0), (), "_ModuleHandle", None),
		"_ParentWindowForDialogs": (54, 2, (3, 0), (), "_ParentWindowForDialogs", None),
		"_ProcessID": (80, 2, (3, 0), (), "_ProcessID", None),
		"_WindowForPanelOrigin": (100, 2, (3, 0), (), "_WindowForPanelOrigin", None),
	}
	_prop_map_put_ = {
		"AutomaticClose": ((35, LCID, 4, 0),()),
		"PrintDefaultPrinter": ((73, LCID, 4, 0),()),
		"PrintMethod": ((74, LCID, 4, 0),()),
		"PrintSetupCustomConnector": ((12, LCID, 4, 0),()),
		"PrintSetupCustomControlDesc": ((17, LCID, 4, 0),()),
		"PrintSetupCustomControlTypes": ((18, LCID, 4, 0),()),
		"PrintSetupCustomControls": ((16, LCID, 4, 0),()),
		"PrintSetupCustomDescription": ((13, LCID, 4, 0),()),
		"PrintSetupCustomDiagram": ((19, LCID, 4, 0),()),
		"PrintSetupCustomDiagramHidden": ((20, LCID, 4, 0),()),
		"PrintSetupCustomDiagramRepeat": ((21, LCID, 4, 0),()),
		"PrintSetupCustomExpressVIConfigInfo": ((92, LCID, 4, 0),()),
		"PrintSetupCustomHierarchy": ((23, LCID, 4, 0),()),
		"PrintSetupCustomHistory": ((24, LCID, 4, 0),()),
		"PrintSetupCustomPanel": ((14, LCID, 4, 0),()),
		"PrintSetupCustomPanelBorder": ((15, LCID, 4, 0),()),
		"PrintSetupCustomSubVIs": ((22, LCID, 4, 0),()),
		"PrintSetupFileWrapText": ((9, LCID, 4, 0),()),
		"PrintSetupJPEGQuality": ((11, LCID, 4, 0),()),
		"PrintSetupPNGCompressLevel": ((10, LCID, 4, 0),()),
		"PrintingColorDepth": ((72, LCID, 4, 0),()),
		"ShowFPTipStrips": ((63, LCID, 4, 0),()),
		"UserName": ((1, LCID, 4, 0),()),
		"VIServerPort": ((93, LCID, 4, 0),()),
		"_ParentWindowForDialogs": ((54, LCID, 4, 0),()),
		"_WindowForPanelOrigin": ((100, LCID, 4, 0),()),
	}
	# Default property for this class is 'AppName'
	def __call__(self):
		return self._ApplyTypes_(*(0, 2, (8, 0), (), "AppName", None))
	# str(ob) and int(ob) will use __call__
	def __unicode__(self, *args):
		try:
			return unicode(self.__call__(*args))
		except pythoncom.com_error:
			return repr(self)
	def __str__(self, *args):
		return str(self.__unicode__(*args))
	def __int__(self, *args):
		return int(self.__call__(*args))

class _IApplication(DispatchBaseClass):
	"""Application Interface"""
	CLSID = IID('{9A872071-0A06-11D1-90B7-00A024CE2744}')
	coclass_clsid = IID('{9A872070-0A06-11D1-90B7-00A024CE2744}')

	_prop_map_get_ = {
		"AllVIsInMemory": (28, 2, (12, 0), (), "AllVIsInMemory", None),
		"AppDefltDataLoc": (87, 2, (8, 0), (), "AppDefltDataLoc", None),
		"AppKind": (3, 2, (3, 0), (), "AppKind", None),
		"AppName": (0, 2, (8, 0), (), "AppName", None),
		"AppTargetCPU": (5, 2, (3, 0), (), "AppTargetCPU", None),
		"AppTargetOS": (4, 2, (3, 0), (), "AppTargetOS", None),
		"ApplicationDirectory": (25, 2, (8, 0), (), "ApplicationDirectory", None),
		"AutomaticClose": (35, 2, (11, 0), (), "AutomaticClose", None),
		"CmdArgs": (79, 2, (12, 0), (), "CmdArgs", None),
		"ExportedVIs": (8, 2, (12, 0), (), "ExportedVIs", None),
		"Language": (99, 2, (8, 0), (), "Language", None),
		"OSName": (6, 2, (8, 0), (), "OSName", None),
		"OSVersion": (7, 2, (8, 0), (), "OSVersion", None),
		"PrintDefaultPrinter": (73, 2, (8, 0), (), "PrintDefaultPrinter", None),
		"PrintMethod": (74, 2, (3, 0), (), "PrintMethod", None),
		"PrintSetupCustomConnector": (12, 2, (11, 0), (), "PrintSetupCustomConnector", None),
		"PrintSetupCustomControlDesc": (17, 2, (11, 0), (), "PrintSetupCustomControlDesc", None),
		"PrintSetupCustomControlTypes": (18, 2, (11, 0), (), "PrintSetupCustomControlTypes", None),
		"PrintSetupCustomControls": (16, 2, (11, 0), (), "PrintSetupCustomControls", None),
		"PrintSetupCustomDescription": (13, 2, (11, 0), (), "PrintSetupCustomDescription", None),
		"PrintSetupCustomDiagram": (19, 2, (11, 0), (), "PrintSetupCustomDiagram", None),
		"PrintSetupCustomDiagramHidden": (20, 2, (11, 0), (), "PrintSetupCustomDiagramHidden", None),
		"PrintSetupCustomDiagramRepeat": (21, 2, (11, 0), (), "PrintSetupCustomDiagramRepeat", None),
		"PrintSetupCustomExpressVIConfigInfo": (92, 2, (11, 0), (), "PrintSetupCustomExpressVIConfigInfo", None),
		"PrintSetupCustomHierarchy": (23, 2, (11, 0), (), "PrintSetupCustomHierarchy", None),
		"PrintSetupCustomHistory": (24, 2, (11, 0), (), "PrintSetupCustomHistory", None),
		"PrintSetupCustomPanel": (14, 2, (11, 0), (), "PrintSetupCustomPanel", None),
		"PrintSetupCustomPanelBorder": (15, 2, (11, 0), (), "PrintSetupCustomPanelBorder", None),
		"PrintSetupCustomSubVIs": (22, 2, (11, 0), (), "PrintSetupCustomSubVIs", None),
		"PrintSetupFileWrapText": (9, 2, (3, 0), (), "PrintSetupFileWrapText", None),
		"PrintSetupJPEGQuality": (11, 2, (3, 0), (), "PrintSetupJPEGQuality", None),
		"PrintSetupPNGCompressLevel": (10, 2, (3, 0), (), "PrintSetupPNGCompressLevel", None),
		"PrintersAvailable": (75, 2, (12, 0), (), "PrintersAvailable", None),
		"PrintingColorDepth": (72, 2, (11, 0), (), "PrintingColorDepth", None),
		"RTHostConnected": (90, 2, (11, 0), (), "RTHostConnected", None),
		"ShowFPTipStrips": (63, 2, (11, 0), (), "ShowFPTipStrips", None),
		"UserName": (1, 2, (8, 0), (), "UserName", None),
		"VIServerPort": (93, 2, (3, 0), (), "VIServerPort", None),
		"Version": (2, 2, (8, 0), (), "Version", None),
		"_ModuleHandle": (101, 2, (3, 0), (), "_ModuleHandle", None),
		"_ParentWindowForDialogs": (54, 2, (3, 0), (), "_ParentWindowForDialogs", None),
		"_ProcessID": (80, 2, (3, 0), (), "_ProcessID", None),
		"_WindowForPanelOrigin": (100, 2, (3, 0), (), "_WindowForPanelOrigin", None),
	}
	_prop_map_put_ = {
		"AutomaticClose": ((35, LCID, 4, 0),()),
		"PrintDefaultPrinter": ((73, LCID, 4, 0),()),
		"PrintMethod": ((74, LCID, 4, 0),()),
		"PrintSetupCustomConnector": ((12, LCID, 4, 0),()),
		"PrintSetupCustomControlDesc": ((17, LCID, 4, 0),()),
		"PrintSetupCustomControlTypes": ((18, LCID, 4, 0),()),
		"PrintSetupCustomControls": ((16, LCID, 4, 0),()),
		"PrintSetupCustomDescription": ((13, LCID, 4, 0),()),
		"PrintSetupCustomDiagram": ((19, LCID, 4, 0),()),
		"PrintSetupCustomDiagramHidden": ((20, LCID, 4, 0),()),
		"PrintSetupCustomDiagramRepeat": ((21, LCID, 4, 0),()),
		"PrintSetupCustomExpressVIConfigInfo": ((92, LCID, 4, 0),()),
		"PrintSetupCustomHierarchy": ((23, LCID, 4, 0),()),
		"PrintSetupCustomHistory": ((24, LCID, 4, 0),()),
		"PrintSetupCustomPanel": ((14, LCID, 4, 0),()),
		"PrintSetupCustomPanelBorder": ((15, LCID, 4, 0),()),
		"PrintSetupCustomSubVIs": ((22, LCID, 4, 0),()),
		"PrintSetupFileWrapText": ((9, LCID, 4, 0),()),
		"PrintSetupJPEGQuality": ((11, LCID, 4, 0),()),
		"PrintSetupPNGCompressLevel": ((10, LCID, 4, 0),()),
		"PrintingColorDepth": ((72, LCID, 4, 0),()),
		"ShowFPTipStrips": ((63, LCID, 4, 0),()),
		"UserName": ((1, LCID, 4, 0),()),
		"VIServerPort": ((93, LCID, 4, 0),()),
		"_ParentWindowForDialogs": ((54, LCID, 4, 0),()),
		"_WindowForPanelOrigin": ((100, LCID, 4, 0),()),
	}
	# Default property for this class is 'AppName'
	def __call__(self):
		return self._ApplyTypes_(*(0, 2, (8, 0), (), "AppName", None))
	# str(ob) and int(ob) will use __call__
	def __unicode__(self, *args):
		try:
			return unicode(self.__call__(*args))
		except pythoncom.com_error:
			return repr(self)
	def __str__(self, *args):
		return str(self.__unicode__(*args))
	def __int__(self, *args):
		return int(self.__call__(*args))

class _IGeneric(DispatchBaseClass):
	"""Generic Interface"""
	CLSID = IID('{00018C9D-0A06-11D1-90B7-00A024CE2744}')
	coclass_clsid = None

	_prop_map_get_ = {
		"ClassID": (103970816, 2, (3, 0), (), "ClassID", None),
		"ClassName": (103970819, 2, (8, 0), (), "ClassName", None),
		# Method 'Owner' returns object of type 'Generic'
		"Owner": (103970817, 2, (9, 0), (), "Owner", '{00018C9E-0A06-11D1-90B7-00A024CE2744}'),
		# Method 'OwningVI' returns object of type 'VirtualInstrument'
		"OwningVI": (103970818, 2, (9, 0), (), "OwningVI", '{9A872074-0A06-11D1-90B7-00A024CE2744}'),
	}
	_prop_map_put_ = {
	}

class _IVI(DispatchBaseClass):
	"""VI Interface"""
	CLSID = IID('{9A872075-0A06-11D1-90B7-00A024CE2744}')
	coclass_clsid = None

	_prop_map_get_ = {
		"AllowDebugging": (518, 2, (11, 0), (), "AllowDebugging", None),
		"BDModificationBitSet": (539, 2, (3, 0), (), "BDModificationBitSet", None),
		"BDSize": (543, 2, (3, 0), (), "BDSize", None),
		"Callees": (545, 2, (12, 0), (), "Callees", None),
		"Callers": (544, 2, (12, 0), (), "Callers", None),
		"CloseFPAfterCall": (523, 2, (11, 0), (), "CloseFPAfterCall", None),
		"CodeSize": (540, 2, (3, 0), (), "CodeSize", None),
		"DataSize": (541, 2, (3, 0), (), "DataSize", None),
		"Description": (502, 2, (8, 0), (), "Description", None),
		"EditMode": (558, 2, (11, 0), (), "EditMode", None),
		"ExecPriority": (526, 2, (3, 0), (), "ExecPriority", None),
		"ExecState": (557, 2, (3, 0), (), "ExecState", None),
		"ExpandWhenDroppedAsSubVI": (585, 2, (11, 0), (), "ExpandWhenDroppedAsSubVI", None),
		"FPAllowRTPopup": (508, 2, (11, 0), (), "FPAllowRTPopup", None),
		"FPAutoCenter": (511, 2, (11, 0), (), "FPAutoCenter", None),
		"FPBehavior": (601, 2, (3, 0), (), "FPBehavior", None),
		"FPHiliteReturnButton": (509, 2, (11, 0), (), "FPHiliteReturnButton", None),
		"FPKeepWinProps": (575, 2, (11, 0), (), "FPKeepWinProps", None),
		"FPMinimizeable": (583, 2, (11, 0), (), "FPMinimizeable", None),
		"FPModificationBitSet": (538, 2, (3, 0), (), "FPModificationBitSet", None),
		"FPResizable": (507, 2, (11, 0), (), "FPResizable", None),
		"FPResizeable": (584, 2, (11, 0), (), "FPResizeable", None),
		"FPShowMenuBar": (513, 2, (11, 0), (), "FPShowMenuBar", None),
		"FPShowScrollBars": (512, 2, (11, 0), (), "FPShowScrollBars", None),
		"FPSize": (542, 2, (3, 0), (), "FPSize", None),
		"FPSizeToScreen": (510, 2, (11, 0), (), "FPSizeToScreen", None),
		"FPState": (608, 2, (3, 0), (), "FPState", None),
		"FPTitleBarVisible": (505, 2, (11, 0), (), "FPTitleBarVisible", None),
		"FPWinBounds": (536, 2, (12, 0), (), "FPWinBounds", None),
		"FPWinClosable": (506, 2, (11, 0), (), "FPWinClosable", None),
		"FPWinCustomTitle": (561, 2, (11, 0), (), "FPWinCustomTitle", None),
		"FPWinIsFrontMost": (535, 2, (11, 0), (), "FPWinIsFrontMost", None),
		"FPWinOpen": (534, 2, (11, 0), (), "FPWinOpen", None),
		"FPWinOrigin": (552, 2, (12, 0), (), "FPWinOrigin", None),
		"FPWinPanelBounds": (556, 2, (12, 0), (), "FPWinPanelBounds", None),
		"FPWinTitle": (553, 2, (8, 0), (), "FPWinTitle", None),
		"HelpDocumentPath": (533, 2, (8, 0), (), "HelpDocumentPath", None),
		"HelpDocumentTag": (532, 2, (8, 0), (), "HelpDocumentTag", None),
		"HistAddCommentsAtSave": (528, 2, (11, 0), (), "HistAddCommentsAtSave", None),
		"HistPromptAtClose": (529, 2, (11, 0), (), "HistPromptAtClose", None),
		"HistPromptForCommentsAtSave": (530, 2, (11, 0), (), "HistPromptForCommentsAtSave", None),
		"HistRecordAppComments": (531, 2, (11, 0), (), "HistRecordAppComments", None),
		"HistUseDefaults": (527, 2, (11, 0), (), "HistUseDefaults", None),
		"HistoryText": (503, 2, (8, 0), (), "HistoryText", None),
		"IsProbe": (618, 2, (11, 0), (), "IsProbe", None),
		"IsReentrant": (525, 2, (11, 0), (), "IsReentrant", None),
		"LogAtFinish": (550, 2, (11, 0), (), "LogAtFinish", None),
		"LogFilePath": (549, 2, (8, 0), (), "LogFilePath", None),
		"Name": (500, 2, (8, 0), (), "Name", None),
		"Path": (501, 2, (8, 0), (), "Path", None),
		"PreferredExecSystem": (559, 2, (3, 0), (), "PreferredExecSystem", None),
		"PrintHeaderDatePrint": (592, 2, (11, 0), (), "PrintHeaderDatePrint", None),
		"PrintHeaderModifyDate": (593, 2, (11, 0), (), "PrintHeaderModifyDate", None),
		"PrintHeaderPageNumber": (594, 2, (11, 0), (), "PrintHeaderPageNumber", None),
		"PrintHeaderVIIcon": (595, 2, (11, 0), (), "PrintHeaderVIIcon", None),
		"PrintHeaderVIName": (590, 2, (11, 0), (), "PrintHeaderVIName", None),
		"PrintLogFileAtFinish": (551, 2, (11, 0), (), "PrintLogFileAtFinish", None),
		"PrintMargins": (589, 2, (12, 0), (), "PrintMargins", None),
		"PrintingBDScaling": (610, 2, (11, 0), (), "PrintingBDScaling", None),
		"PrintingFPScaling": (588, 2, (11, 0), (), "PrintingFPScaling", None),
		"PrintingHeaderVIPath": (596, 2, (11, 0), (), "PrintingHeaderVIPath", None),
		"PrintingHeaders": (587, 2, (11, 0), (), "PrintingHeaders", None),
		"PrintingOrientation": (586, 2, (3, 0), (), "PrintingOrientation", None),
		"RevisionNumber": (564, 2, (3, 0), (), "RevisionNumber", None),
		"RunOnOpen": (524, 2, (11, 0), (), "RunOnOpen", None),
		"RunTimeMenuPath": (580, 2, (8, 0), (), "RunTimeMenuPath", None),
		"ShowFPOnCall": (522, 2, (11, 0), (), "ShowFPOnCall", None),
		"ShowFPOnLoad": (521, 2, (11, 0), (), "ShowFPOnLoad", None),
		"SuspendOnCall": (555, 2, (11, 0), (), "SuspendOnCall", None),
		"TBShowAbortButton": (517, 2, (11, 0), (), "TBShowAbortButton", None),
		"TBShowFreeRunButton": (516, 2, (11, 0), (), "TBShowFreeRunButton", None),
		"TBShowRunButton": (515, 2, (11, 0), (), "TBShowRunButton", None),
		"TBVisible": (514, 2, (11, 0), (), "TBVisible", None),
		"VIModificationBitSet": (537, 2, (3, 0), (), "VIModificationBitSet", None),
		"VIType": (562, 2, (3, 0), (), "VIType", None),
		"_BDWinOSWindow": (616, 2, (3, 0), (), "_BDWinOSWindow", None),
		"_CPTM_CRC": (614, 2, (3, 0), (), "_CPTM_CRC", None),
		"_FPWinOSWindow": (615, 2, (3, 0), (), "_FPWinOSWindow", None),
	}
	_prop_map_put_ = {
		"AllowDebugging": ((518, LCID, 4, 0),()),
		"CloseFPAfterCall": ((523, LCID, 4, 0),()),
		"Description": ((502, LCID, 4, 0),()),
		"EditMode": ((558, LCID, 4, 0),()),
		"ExecPriority": ((526, LCID, 4, 0),()),
		"ExpandWhenDroppedAsSubVI": ((585, LCID, 4, 0),()),
		"FPAllowRTPopup": ((508, LCID, 4, 0),()),
		"FPAutoCenter": ((511, LCID, 4, 0),()),
		"FPBehavior": ((601, LCID, 4, 0),()),
		"FPHiliteReturnButton": ((509, LCID, 4, 0),()),
		"FPKeepWinProps": ((575, LCID, 4, 0),()),
		"FPMinimizeable": ((583, LCID, 4, 0),()),
		"FPResizable": ((507, LCID, 4, 0),()),
		"FPResizeable": ((584, LCID, 4, 0),()),
		"FPShowMenuBar": ((513, LCID, 4, 0),()),
		"FPShowScrollBars": ((512, LCID, 4, 0),()),
		"FPSizeToScreen": ((510, LCID, 4, 0),()),
		"FPState": ((608, LCID, 4, 0),()),
		"FPTitleBarVisible": ((505, LCID, 4, 0),()),
		"FPWinBounds": ((536, LCID, 4, 0),()),
		"FPWinClosable": ((506, LCID, 4, 0),()),
		"FPWinCustomTitle": ((561, LCID, 4, 0),()),
		"FPWinIsFrontMost": ((535, LCID, 4, 0),()),
		"FPWinOpen": ((534, LCID, 4, 0),()),
		"FPWinOrigin": ((552, LCID, 4, 0),()),
		"FPWinPanelBounds": ((556, LCID, 4, 0),()),
		"FPWinTitle": ((553, LCID, 4, 0),()),
		"HelpDocumentPath": ((533, LCID, 4, 0),()),
		"HelpDocumentTag": ((532, LCID, 4, 0),()),
		"HistAddCommentsAtSave": ((528, LCID, 4, 0),()),
		"HistPromptAtClose": ((529, LCID, 4, 0),()),
		"HistPromptForCommentsAtSave": ((530, LCID, 4, 0),()),
		"HistRecordAppComments": ((531, LCID, 4, 0),()),
		"HistUseDefaults": ((527, LCID, 4, 0),()),
		"IsReentrant": ((525, LCID, 4, 0),()),
		"LogAtFinish": ((550, LCID, 4, 0),()),
		"LogFilePath": ((549, LCID, 4, 0),()),
		"Name": ((500, LCID, 4, 0),()),
		"PreferredExecSystem": ((559, LCID, 4, 0),()),
		"PrintHeaderDatePrint": ((592, LCID, 4, 0),()),
		"PrintHeaderModifyDate": ((593, LCID, 4, 0),()),
		"PrintHeaderPageNumber": ((594, LCID, 4, 0),()),
		"PrintHeaderVIIcon": ((595, LCID, 4, 0),()),
		"PrintHeaderVIName": ((590, LCID, 4, 0),()),
		"PrintLogFileAtFinish": ((551, LCID, 4, 0),()),
		"PrintMargins": ((589, LCID, 4, 0),()),
		"PrintingBDScaling": ((610, LCID, 4, 0),()),
		"PrintingFPScaling": ((588, LCID, 4, 0),()),
		"PrintingHeaderVIPath": ((596, LCID, 4, 0),()),
		"PrintingHeaders": ((587, LCID, 4, 0),()),
		"PrintingOrientation": ((586, LCID, 4, 0),()),
		"RevisionNumber": ((564, LCID, 4, 0),()),
		"RunOnOpen": ((524, LCID, 4, 0),()),
		"RunTimeMenuPath": ((580, LCID, 4, 0),()),
		"ShowFPOnCall": ((522, LCID, 4, 0),()),
		"ShowFPOnLoad": ((521, LCID, 4, 0),()),
		"SuspendOnCall": ((555, LCID, 4, 0),()),
		"TBShowAbortButton": ((517, LCID, 4, 0),()),
		"TBShowFreeRunButton": ((516, LCID, 4, 0),()),
		"TBShowRunButton": ((515, LCID, 4, 0),()),
		"TBVisible": ((514, LCID, 4, 0),()),
	}

from win32com.client import CoClassBaseClass
# This CoClass is known by the name 'LabVIEW.Application.7'
class Application(CoClassBaseClass): # A CoClass
	# Application CoClass
	CLSID = IID('{9A872070-0A06-11D1-90B7-00A024CE2744}')
	coclass_sources = [
	]
	coclass_interfaces = [
		_IApplication,
		_Application,
	]
	default_interface = _Application

RecordMap = {
}

CLSIDToClassMap = {
	'{9A872070-0A06-11D1-90B7-00A024CE2744}' : Application,
	'{9A872071-0A06-11D1-90B7-00A024CE2744}' : _IApplication,
	'{9A872072-0A06-11D1-90B7-00A024CE2744}' : _Application,
	'{9A872074-0A06-11D1-90B7-00A024CE2744}' : VirtualInstrument,
	'{9A872075-0A06-11D1-90B7-00A024CE2744}' : _IVI,
	'{00018C9D-0A06-11D1-90B7-00A024CE2744}' : _IGeneric,
	'{00018C9E-0A06-11D1-90B7-00A024CE2744}' : Generic,
}
CLSIDToPackageMap = {}
win32com.client.CLSIDToClass.RegisterCLSIDsFromDict( CLSIDToClassMap )
VTablesToPackageMap = {}
VTablesToClassMap = {
}


NamesToIIDMap = {
	'VirtualInstrument' : '{9A872074-0A06-11D1-90B7-00A024CE2744}',
	'_IVI' : '{9A872075-0A06-11D1-90B7-00A024CE2744}',
	'Generic' : '{00018C9E-0A06-11D1-90B7-00A024CE2744}',
	'_IGeneric' : '{00018C9D-0A06-11D1-90B7-00A024CE2744}',
	'_Application' : '{9A872072-0A06-11D1-90B7-00A024CE2744}',
	'_IApplication' : '{9A872071-0A06-11D1-90B7-00A024CE2744}',
}

win32com.client.constants.__dicts__.append(constants.__dict__)

