# -*- coding: cp1252 -*-
""" This modules provides a lightweight API to access Excel data.
    There are many ways to read Excel data, including ODBC. This module
    uses ADODB and has the advantage of only requiring a file name and a
    sheet name (no setup required).
http://aspn.activestate.com/ASPN/Cookbook/Python/Recipe/440661
"""

from win32com.client import Dispatch

class ExcelDocument(object):
    r""" Represents an opened Excel document with an active range.

    >>> book = ExcelDocument(r'D:\genovis-temp-regler\src\bakrecept.xls')
    >>> book.cursor
    (1, 1, 9, 0, 0, 2)
    """
    name        = None # Name of document
    path        = None # Path to document
    cursor      = None # Current sheet, row, column, sheets, rows, columns
    _app        = None # Connection to application
    _book       = None # Connection to document in application
    _sheet      = None # Connection to sheet in document
    def __repr__(self): return self.__class__.__name__ + repr(
        (self.path,) +  self.cursor)
    def __init__(self, fullname=None, sheet=None, row=None, column=None,
                 sheets=0, rows=0, columns=2, new=False):
        self._app = app = Dispatch("Excel.Application")
        app.Visible = 1
        if fullname:
            for book in app.Workbooks:
                if book.FullName == fullname:
                    break
            else:
                try:
                    book = app.Workbooks.Open(fullname)
                except:
                    book = app.Workbooks.Add()
                    book.SaveAs(fullname)
        else:
            book = (app.ActiveWorkbook if not new else None
                    ) or app.Workbooks.Add()
            fullname = book.FullName
        self._book = book
        self.path = fullname
        self.name = book.Name
        if sheet:
            s = book.Sheets(sheet)
        else:
            s = book.ActiveSheet
            sheet = s.Index
        self._sheet = s
        s.Activate()
        c = app.ActiveCell
        self.cursor = (sheet, row or c.Row, column or c.Column, sheets, rows, columns)
    def __getattr__(self, attr):
        r"""Reads a named cell value
        >>> book = ExcelDocument(r'D:\genovis-temp-regler\src\bakrecept.xls')
        >>> book.comport
        u'COM28'
        """
        try:
            return self._sheet.Range(attr).Value
        except:
            raise(AttributeError('No cell named %s found in %r' % (attr, self)))
    def __setattr__(self, attr, value):
        r"""Writes a named cell value
        >>> book = ExcelDocument(r'D:\genovis-temp-regler\src\bakrecept.xls')
        >>> book.comport = 'COM1'
        """
        if hasattr(self.__class__, attr):
            super(ExcelDocument, self).__setattr__(attr, value)
        else:
            try:
                self._sheet.Range(attr).Value = value
            except:
                raise(AttributeError('No cell named %s found in %r' % (attr, self)))
    def __getitem__(self, coord):
        r"""Move cursor to a cell or cell range and read value(s) there.
        >>> book = ExcelDocument(r'D:\genovis-temp-regler\src\bakrecept.xls')
        >>> book["degCmin"]
        ((u'Program (\xb0C)', u'minuter'),)
        >>> book.cursor
        (1, 1, 9, 0, 1, 2)
        >>> book[1, 1, 9, 0, 0, 2]
        (u'Program (\xb0C)', u'minuter')
        """
        sheet, row, column, sheets, rows, columns = self.cursor
        Cells = self._sheet.Cells
        if isinstance(coord, slice): #[(1,11):(3,13)]
            a, b, step = coord.start, coord.stop, coord.step
            assert step == None, "Cell ranges do not support intervals"
            c = self._sheet.Range(Cells(*([a] if not isinstance(a,tuple) else a)),
                                  Cells(*([b] if not isinstance(b,tuple) else b)).Offset(0,0))
            v = c.Value
        elif isinstance(coord, tuple):
            if len(coord) == 6: #[1,1,9,0,0,2]
                sheet, row, column, sheets, rows, columns = coord
                assert sheets==0, "Cell ranges can not span across sheets!"
                if self.cursor[0] == sheet:
                    s = self._sheet
                else:
                    s = self._sheet = self._book.Sheets(sheet)
                Cells = s.Cells
                c = s.Range(Cells(row, column),
                            Cells(row+(rows or 1)-1, column+(columns or 1)-1))
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
        self.cursor = (sheet, c.Row, c.Column, sheets, rows, columns)
        return v
    def __setitem__(self, coord, value):
        """Write value(s) to a cell or cell range (does not move cursor)
        >>> recipy = ExcelDocument()
        >>> recipy[2,11] = 1; recipy[2,11]
        1.0
        >>> recipy[2,11:13] = [2, 3]; recipy[2:4,11:13]
        ((2.0, 3.0), (None, None))
        >>> recipy[2:4,11] = [[4], [5]]; recipy[2:4,11:13]
        ((4.0, 3.0), (5.0, None))
        >>> recipy[2:4,11:13] = [[1, 2], [3, 4]]; recipy[2:4,11:13]
        ((1.0, 2.0), (3.0, 4.0))
        >>> recipy[(2,11):(4,13)] = [[5, 6], [7, 8]]; recipy[2:4,11:13]
        ((5.0, 6.0), (7.0, 8.0))
        """
        self.Slice(coord).Value = value
    def Slice(self, coord):
        """COM connection to a cell or range of cells
        >>> recipy = ExcelDocument()
        >>> recipy.Slice("A1").Value
        u'Port'
        >>> recipy.Slice((1,1,1,0,0,2)).Value
        ((u'Port', u'COM28'),)
        >>> recipy.Slice("comport").Value
        u'COM28'
        """
        sheet, row, column, sheets, rows, columns = self.cursor
        Cells = self._sheet.Cells
        if isinstance(coord, slice): #[(1,11):(3,13)]
            a, b, step = coord.start, coord.stop, coord.step
            assert step == None, "Cell ranges do not support intervals"
            return self._sheet.Range(Cells(*([a] if not isinstance(a,tuple) else a)),
                                     Cells(*([b] if not isinstance(b,tuple) else b)).Offset(0,0))
        if isinstance(coord, tuple):
            if len(coord) == 6: #[1,1,9,0,0,2]
                sheet, row, column, sheets, rows, columns = coord
                assert sheets==0, "Cell ranges can not span across sheets! ExcelBook will..."
                if self.cursor[0] == sheet:
                    s = self._sheet
                else:
                    s = self._book.Sheets(sheet)
                Cells = s.Cells
                return s.Range(Cells(row, column),
                               Cells(row+(rows or 1)-1, column+(columns or 1)-1))
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
        try:
            return Cells(coord) # ["A1"], ["comport"] etc
        except:
            return self._sheet.Range(coord) # ["A1:B2"], ["degCmin"] etc
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
        >>> book = ExcelDocument(r'D:\genovis-temp-regler\src\bakrecept.xls')
        >>> book[1,9:11]
        (u'Program (\xb0C)', u'minuter')
        >>> book.next()
        (30.0, 1.0)
        """
        sheet, row, column, sheets, rows, columns = self.cursor
        row += rows or 1
        return self[sheet, row, column, sheets, rows, columns]
    def close(self, **opts):
        """Close workbook in Excel
        >>> book = ExcelDocument()
        >>> book.close(SaveChanges=False)
        """
        self._book.Close(**opts)

if __name__ == '__main__':
    import doctest
    doctest.testmod(verbose=True)
