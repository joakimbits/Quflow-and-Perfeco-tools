# -*- coding: cp1252 -*-
"""
Use cell ranges in Excel as 3D, 2D, 1D and 0D array objects.

Gives easy access to values and attributes in the cell range as well as named
cells and named cell ranges by automatic mapping of attributes across sheets.
"""

from win32com.client import Dispatch
from numpy import ndarray, array, asarray, asanyarray

class Slice(ndarray):
    "Represents an n-dimensional slice of an m-dimensional dataset"
    def __repr__(self):
        return self.__class__.__name__ + repr((self.path,) +  self.cells)
    def __init__(self, path=None, sheet=None, row=None, column=None,
                 sheets=0, rows=0, columns=2, new=False):
        raise(NotImplementedError)

class ExcelRange(Slice):
    r""" Represents an opened Excel workbook with an active cell range.

    Depending on how many of the parameters 'sheets', 'rows' and 'columns'
    are non-zero, the cell range can be either 0D, 1D, 2D or 3D.

    Iterations are over consecutive row ranges in the workbook and start below
    the active cell range (which typically contain the column headings).

    The sheet indices (or sheet and row indices) can be omitted when indexing.
    In this case these missing indices are copied from the indexed object.

    >>> book = ExcelRange() # Active document and cell selection.
    Traceback (most recent call last):
      File "C:\Python27\Lib\doctest.py", line 1289, in __run
        compileflags, 1) in test.globs
      File "<doctest __main__.ExcelDocument[0]>", line 1, in <module>
        recipy = ExcelDocument() # Connects to active document.
      File "D:\genovis-temp-regler\src\exceldocument.py", line 41, in __init__
        assert wb, "No ActiveWorkbook in Excel!"
    AssertionError: No ActiveWorkbook in Excel!
    >>> book = ExcelRange(r'D:\genovis-temp-regler\src\bakrecept.xls')
    >>> recipy.cursor
    (1, 1, 9, 0, 0, 2)
    """
    name        = None # Name of document
    path        = None # Path to document
    cells       = None # Cell range (sheet, row, column, sheets, rows, columns)
    _app        = None # Connection to application
    _book       = None # Connection to document in application
    _sheets     = None # Connections to sheets in document
    def __repr__(self):
        return self.__class__.__name__ + repr((self.path,) +  self.cells)
    def __init__(self, path=None, sheet=None, row=None, column=None,
                 sheets=0, rows=0, columns=2, new=False):
        self._app = Dispatch("Excel.Application")
        self._app.Visible = 1
        if path:
            for self._book in self._app.Workbooks:
                if str(self._book.FullName) == str(path):
                    break
            else:
                try:
                    self._book = self._app.Workbooks.Open(path)
                except:
                    self._book = self._app.Workbooks.Add()
                    self._book.SaveAs(path)
        else:
            self._book = (self._app.ActiveWorkbook if not new else None
                          ) or self._app.Workbooks.Add()
            path = self._book.FullName
        self.path = path
        self.name = self._book.Name
        Sheets = self._book.Sheets
        self._sheets = [None] + [Sheets(i) for i in range(1, Sheets.count+1)]
        if sheet:
            s = self._sheets[sheet]
        else:
            s = self._book.ActiveSheet
            sheet = self._sheets.index(s)
        s.Activate
        c = s.ActiveCell
        self.cells = (sheet, row or C.Row, column or c.Column,
                      sheets, rows, columns)
    def __array__(self):
        return self.Value
    def __getattr__(self, attr):
        """Reads a named cell value
        >>> recipy = ExcelDocument() # Sheet opened earlier, with named cells
        >>> recipy.comport
        u'COM28'
        """
        try:
            return self._book.Range(attr).Value
        except:
            raise(AttributeError('No cell named %s found in %r' % (attr, self)))
    def __setattr__(self, attr, value):
        """Writes a named cell value
        >>> recipy = ExcelDocument() # Sheet opened earlier, with named cells
        >>> recipy.comport = 'COM1'
        """
        if hasattr(ExcelDocument, attr):
            super(ExcelDocument, self).__setattr__(attr, value)
        else:
            try:
                self._book.Range(attr).Value = value
            except:
                raise(AttributeError('No cell named %s found in %r' % (attr, self)))
    def __getitem__(self, coord):
        r"""Move cursor to a cell or cell range and read value(s) there.
        >>> recipy = ExcelDocument()
        >>> recipy[1,1,9:11]
        (u'Styrning (\xb0C)', u'minuter')
        >>> recipy.cursor
        (1, 1, 9, 0, 0, 2)
        """
        sheet, row, column, sheets, rows, columns = self.cursor
        if isinstance(coord, slice): #[(1,1,11):(1,3,13)]
            a, b, step = coord.start, coord.stop, coord.step
            assert step == None, "Cells must be consecutive"
            sheet, row, column = (sheet, row)[:3-len(a)] + a
            sheets, rows, columns = (sheets, rows)[:3-len(b)] + (bi-ai for ai,bi in zip(a,b))
            v = (s.Range(s.Cells(row, column),
                         s.Cells(row+(rows or 1)-1, column+(columns or 1)-1)).Value
                 for s in self.__sheets[sheet:sheet+(sheets or 1)])
        elif isinstance(coord, tuple):
            if len(coord) == 6: #[1,1,9,0,0,2]
                sheet, row, column, sheets, rows, columns = coord
                c0 = self._sheets[sheet].Cells(row, column)
                c1 = self._sheets[sheet+(sheets or 1)-1
                                  ].Cells(row+(rows or 1)-1, column+(columns or 1)-1)
                c = self._book.Range(c0, c1)
                v = c.Value
                if rows == columns == 0:
                    v = v[0,0]
                elif rows == 0:
                    v = v[0]
                elif columns == 0:
                    v = v[:,0]
            else:
                assert len(coord) == 2, "Only 2D coordinates are supported"
                a, b = coord
                if isinstance(a, slice) and isinstance(b, slice): #[1:3:9:11]
                    a1, a2, astep = a.start, a.stop, a.step
                    b1, b2, bstep = b.start, b.stop, b.step
                    assert astep == bstep == None, "Cell ranges do not support intervals"
                    c = self._sheet.Range(Cells(a1,b1), Cells(a2-1,b2-1))
                    v, rows, columns = c.Value, c.Rows.Count, c.Columns.Count
                elif isinstance(a, slice): #[1:3,9]
                    a1, a2, astep = a.start, a.stop, a.step
                    assert astep == None, "Cell ranges do not support intervals"
                    c = self._sheet.Range(Cells(a1,b), Cells(a2-1,b))
                    v, rows, columns = c.Value[:,0], c.Rows.Count, 0
                elif isinstance(b, slice): #[1,9:11]
                    b1, b2, bstep = b.start, b.stop, b.step
                    assert bstep == None, "Cell ranges do not support intervals"
                    c = self._sheet.Range(Cells(a,b1), Cells(a,b2-1))
                    v, rows, columns = c.Value[0], 0, c.Columns.Count
                else:
                    c = Cells(a,b) # [1,9]
                    v, rows, columns = c.Value, 0, 0
        else:
            try:
                c = self._sheet.Cells(coord) # ["A1"], ["comport"] etc
                v, rows, columns = c.Value, 0, 0
            except:
                c = self._sheet.Range(coord) # ["A1:B2"], ["degCmin"] etc
                v, rows, columns = c.Value, c.Rows.Count, c.Columns.Count
        self._sheet = s = c.Parent
        self.cursor = (s.Name, c.Row, c.Column, rows, columns)
        return v
    def __setitem__(self, coord, value):
        """Write value(s) to a cell or cell range (does not move cursor)
        >>> recipy = ExcelDocument()
        >>> recipy[1,11] = 1; recipy[1,11]
        1.0
        >>> recipy[1,11:13] = [2, 3]; recipy[1:3,11:13]
        ((2.0, 3.0), (None, None))
        >>> recipy[1:3,11] = [[4], [5]]; recipy[1:3,11:13]
        ((4.0, 3.0), (5.0, None))
        >>> recipy[1:3,11:13] = [[1, 2], [3, 4]]; recipy[1:3,11:13]
        ((1.0, 2.0), (3.0, 4.0))
        >>> recipy[(1,11):(3,13)] = [[5, 6], [7, 8]]; recipy[1:3,11:13]
        ((5.0, 6.0), (7.0, 8.0))
        """
        self.Slice(coord).Value = value
    def Slice(self, coord):
        """COM connection to a cell or range of cells
        >>> recipy = ExcelDocument()
        >>> recipy.Slice("A1").Value
        u'Port'
        >>> recipy.Slice(("Sheet1",1,1,1,2)).Value
        ((u'Port', u'COM28'),)
        >>> recipy.Slice("comport").Value
        u'COM28'
        """
        Cells = self._sheet.Cells
        if isinstance(coord, slice): #[(1,11):(3,13)]
            a, b, step = coord.start, coord.stop, coord.step
            assert step == None, "Cell ranges do not support intervals"
            return self._sheet.Range(Cells(*([a] if not isinstance(a,tuple) else a)),
                                     Cells(*([b] if not isinstance(b,tuple) else b)).Offset(0,0))
        if isinstance(coord, tuple):
            if len(coord) == 5: #["Sheet1",1,9,1,2]
                sheet, row, column, rows, columns = coord
                s = self._book.Worksheets(sheet)
                Cells = s.Cells
                return s.Range(Cells(row, column),
                               Cells(row+rows-1, column+columns-1))
            assert len(coord) == 2, "Only 2D coordinates are supported"
            a, b = coord
            if isinstance(a, slice) and isinstance(b, slice): #[1:3:9:11]
                a1, a2, astep = a.start, a.stop, a.step
                b1, b2, bstep = b.start, b.stop, b.step
                assert astep == bstep == None, "Cell ranges do not support intervals"
                return self._sheet.Range(Cells(a1,b1), Cells(a2-1,b2-1))
            if isinstance(a, slice): #[1:3,9]
                a1, a2, astep = a.start, a.stop, a.step
                assert astep == None, "Cell ranges do not support intervals"
                return self._sheet.Range(Cells(a1,b), Cells(a2-1,b))
            if isinstance(b, slice): #[1,9:11]
                b1, b2, bstep = b.start, b.stop, b.step
                assert bstep == None, "Cell ranges do not support intervals"
                return self._sheet.Range(Cells(a,b1), Cells(a,b2-1))
            return Cells(a,b) # [1,9]
        return self._sheet.Range(coord) # ["A1"], ["comport"] etc
    def __iter__(self):
        r"""Iterate over rows under cursor
        >>> recipy = ExcelDocument()
        >>> for row in recipy:
        ...     break
        ...     if row == [None, None]: break
        ...     print row
        ... 
        """
        return self
    def next(self):
        r"""Move cursor down (by rows) and return value(s) there
        >>> recipy = ExcelDocument()
        >>> recipy[1,9:11]
        (u'Styrning (\xb0C)', u'minuter')
        >>> recipy.next()
        (30.0, 1.0)
        """
        sheetname, row, column, rows, columns = self.cursor
        row += rows or 1
        return self[sheetname, row, column, rows, columns]

if __name__ == '__main__':
    import doctest
    doctest.testmod(verbose=True)
