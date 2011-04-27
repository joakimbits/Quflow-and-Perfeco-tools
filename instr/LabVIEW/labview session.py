PythonWin 2.4.1 (#65, Mar 30 2005, 09:13:57) [MSC v.1310 32 bit (Intel)] on win32.
Portions Copyright 1994-2004 Mark Hammond (mhammond@skippinet.com.au) - see 'Help/About PythonWin' for further copyright information.
>>> import labview
>>> test = labview.Application
>>> test = test ()
>>> dir(test)
['CLSID', '__doc__', '__getattr__', '__init__', '__module__', '__repr__', '__setattr__', '_dispobj_', 'coclass_interfaces', 'coclass_sources', 'default_interface']
>>> test.__doc__
>>> test
<win32com.gen_py.None.Application>
>>> lv = test
>>> lv.GetVIReference
<bound method _Application.GetVIReference of <win32com.gen_py.LabVIEW 7.1 Type Library._Application instance at 0x19081416>>
>>> lv.ApplicationDirectory
u'C:\\Program Files\\National Instruments\\LabVIEW 7.1'
>>> path = u'C:\\Program Files\\National Instruments\\LabVIEW 7.1\\examples\\general\\graphs\\3dgraph.llb\\Bessel Function - Vibrating Membrane.vi'
>>> path
u'C:\\Program Files\\National Instruments\\LabVIEW 7.1\\examples\\general\\graphs\\3dgraph.llb\\Bessel Function - Vibrating Membrane.vi'
>>> vi = lv.GetVIReference (path, "", True)
>>> vi.Call ()
(-2147352572, -2147352572)
>>> vi.Call ()
(-2147352572, -2147352572)
>>> vi.FPWinOpen()
Traceback (most recent call last):
  File "<interactive input>", line 1, in ?
TypeError: 'bool' object is not callable
>>> vi.OpenFrontPanel()
Traceback (most recent call last):
  File "<interactive input>", line 1, in ?
  File "labview.py", line 201, in OpenFrontPanel
    , State)
com_error: (-2147352567, 'Exception occurred.', (6651, 'LabVIEW', 'LabVIEW : LabVIEW:  Invalid input for front panel state.\n', None, 0, 0), None)
>>> vi.OpenFrontPanel(True, True)
>>> vi.OpenFrontPanel(True, True)
>>> lv.BringToFront ()
>>> vi.Call ()
(-2147352572, -2147352572)
>>> 