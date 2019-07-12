#!/usr/bin/python
# -*- coding: iso-8859-15 -*-
"""
Macros for book-keeping invoices and expenses in excel.

The first marco is for generating an expense report from selected text in pdf document.
It prints the selected page to a file named as the selected text. 
Use the Sign/Add Text tool in Adobe Acrobat Viewer to create a custom text with the following format: 
<Date> <Description>. The complete text field will be used as file name.  

The second macro is for registering selected invoice/expense files into an open excel document. 
It extracts the date and description ffrom the file name, fills in column C with the date, column D with either 
"Förbrukningsmaterial <description>" or "Försäljning <description with 'Faktura' removed>", renames the files so 
they have the id in column B between date and description, and finally opens these renamed files so that you can 
declare its accounts and amounts. Files with same file name up to a '(' character are grouped together into the 
same id. 

Bokföring 2017.xlsx is a template for using the second macro. Most accounting can be achieved by leaving the default
'Förbrukningsmaterial' or modifyint it to any other account name defined in cells L2:AE2, changing the VAT rate in 
column F, or declaring additional account amounts directly in the corrisponing cell. Column A will signal a red value
if the debit and credit amounts do not match such as when no matching account name can be found.

Requires pywin32, traitsui, PyQt4 and pywinauto on Windows:
    > conda install pywin32 traitsui pyqt=4
    > pip install pywinauto

pywinauto is only used for pressing the Enter button.
"""

from time import sleep
import re, os, shutil, ctypes

from traits.api import HasTraits, Button
from traitsui.api import View, Item
import win32api, win32ui, win32com, win32gui, win32clipboard

from lib.Windows.keystrokes import keystroke, keystrokes, VK_HOME, VK_END, VK_RETURN, VK_DOWN
from lib.Excel.exceldocument import ExcelDocument


def explorer_fileselection(folder = None):
    working_dir = os.getcwd()
    shell = win32com.client.Dispatch("Shell.Application")
    files = []
    try:
        for window in shell.Windows():
            if str(window) == "Windows Explorer":
                if not folder or folder == window.LocationURL:
                    selected_files = window.Document.SelectedItems()
                    for file in range(selected_files.Count):
                        files.append(selected_files.Item(file).Path)
    except:   #Ugh, need a better way to handle this one
        win32ui.MessageBox("Välj åtminstone en fil i ekonomimappen", "Hallå där!")
    return files


class Window:
    """Encapsulates some calls to the winapi for window management"""
    def __init__ (self, name):
        """Constructor"""
        self.find_window_wildcard(name)

    def find_window(self, class_name, window_name = None):
        """find a window by its class_name"""
        self._handle = win32gui.FindWindow(class_name, window_name)

    def _window_enum_callback(self, hwnd, wildcard):
        '''Pass to win32gui.EnumWindows() to check all the opened windows'''
        if re.match(wildcard, str(win32gui.GetWindowText(hwnd))) != None:
            self._handle = hwnd

    def find_window_wildcard(self, wildcard):
        self._handle = None
        win32gui.EnumWindows(self._window_enum_callback, wildcard)
        assert self._handle, "Can't find any open %s window" % wildcard


    def set_foreground(self):
        """put the window in the foreground"""
        win32gui.SetForegroundWindow(self._handle)


class WaitWindow(Window):
    def __init__ (self, name, dt, T):
        for t in range(0, int(T/dt)):
            try:
                self.find_window_wildcard(name)
                break
            except:
                sleep(dt)
        else:
            self.find_window_wildcard(name)


class Macros(HasTraits):

    register_current_text = Button(u'Skriv utläggskommentar i Adobe Reader till dagboksrad i Excel')
    def _register_current_text_fired(self):
        excel = ExcelDocument()
        sheet, row, column, sheets, rows, columns = excel.cursor
        regID = "A%d" % excel[row, 2]
        scan = Window(".* - Adobe Reader")
        scan.set_foreground()
        keystroke(VK_HOME)  # Go to first character in text at cursor
        keystroke(VK_HOME, shift = True)  # Select to last character in text
        keystroke('c', ctrl = True)  # Copy text
        sleep(.1)
        keystroke('p', ctrl = True)  # Print
        sleep(1)
        print_dialog = WaitWindow("Print", .1, 1)
        print_dialog.set_foreground()
        keystrokes('Cu')             # Select CutePDF printer
        keystroke('u', alt = True)   # Current page
        keystrokes('\t'*10)          # Tab to select the Print button
        sleep(1)
        keystroke(' ')               # Try Space
        sleep(1)
        keystroke('\r')              # Try Enter
        sleep(1)
        keystroke('\n')              # Try Ctrl-Enter
        sleep(1)
        print_dialog.set_foreground()
        win32api.keybd_event(VK_RETURN, 0, 0, 0)  # Try bypassing keystroke function
        sleep(11)
        cutepdf_dialog = WaitWindow("Save As", 1, 10)
        cutepdf_dialog.set_foreground()
        keystroke('n', alt = True)   # Filename:
        keystrokes(regID + ' ')      #  Register ID
        keystroke('v', ctrl = True)  #  Comment
        keystroke('s', alt = True)   # Save
        #sleep(1)
        win32clipboard.OpenClipboard()
        comment = win32clipboard.GetClipboardData(win32clipboard.CF_UNICODETEXT)
        win32clipboard.CloseClipboard()
        print(regID, comment)
        i = comment.index(' ')
        excel[row, 3] = comment[:i]
        excel[row, 4] = u'Förbrukningsmaterial' + comment[i:]

    save_current_text = Button(u'Skriv en utskriftskommentar')
    def _save_current_text_fired(self):
        self.scan = Window(".*pappersunderlag.*")
        self.scan.set_foreground()
        sleep(.1)
        keystroke(VK_HOME)  # Go to first character in text at cursor
        keystroke(VK_END, shift = True)  # Select to last character in text
        sleep(.1)
        keystroke('c', ctrl = True)  # Copy text
        keystroke('p', ctrl = True)  # Print
        print_dialog = WaitWindow("Print", .1, 1)
        print_dialog.set_foreground()
        keystroke('n', alt = True)   # Printer selection
        keystroke('C')               # Select CutePDF printer
        keystroke('u', alt = True)   # Current page button
        sleep(.1)
        keystroke(' ')               # Toggle
        keystroke('\r')              # Print
        cutepdf_dialog = WaitWindow("Save As", 1, 10)
        cutepdf_dialog.set_foreground()
        sleep(1)
        keystroke('n', alt = True)   # Filename:
        keystroke('v', ctrl = True)  #  Comment
        keystroke('s', alt = True)   # Save
        sleep(3)
        win32clipboard.OpenClipboard()
        self.comment = win32clipboard.GetClipboardData(win32clipboard.CF_UNICODETEXT)
        win32clipboard.CloseClipboard()
        print(self.comment)

    def text_in_field(self):
        keystroke(VK_HOME)  # Go to first character in text at cursor
        keystroke(VK_END, shift = True)  # Select to last character in text
        sleep(.1)
        keystroke('c', ctrl = True)  # Copy text
        sleep(.1)
        win32clipboard.OpenClipboard()
        text = win32clipboard.GetClipboardData(win32clipboard.CF_UNICODETEXT)
        win32clipboard.CloseClipboard()
        return text

    save_all_texts = Button(u'Skriv utskriftskommentarer')
    def _save_all_texts_fired(self):
        while True:
            self._save_current_text_fired()
            self.scan.set_foreground()
            text = None
            while not text:
                keystroke(VK_DOWN)
                sleep(1)
                text = self.text_in_field()
            if text == self.comment:
                break

    register_current_file = Button(u'Bokför filer till dagboksrader i Excel')
    def _register_current_file_fired(self):
        excel = ExcelDocument()
        sheet, row, column, sheets, rows, columns = excel.cursor
        startID = excel[row, 2]
        accounts = excel['L2:Z2'][0]
        paths = explorer_fileselection()
        i = -1
        comment_i = ''
        for path in sorted(paths):
            folder, name = os.path.split(path)
            name = " ".join(name.split(' A0 '))  # Fixup after bug
            comment, ext = os.path.splitext(name)
            assert ' ' in comment, path + " saknar datum"
            _space = comment.index(' ')
            assert _space == 10, path + " saknar datum"
            date = comment[:_space]
            _paranthesis = comment.index(' (') if ' (' in comment else len(comment)
            _rparanthesis = comment.index(') ') if ') ' in comment else len(comment)
            if comment[:_paranthesis] != comment_i:
                i += 1
                comment_i = comment[:_paranthesis]
            regID = "A%d" % (startID + i)
            new_name = name[:_space+1] + regID + name[_space:]
            shutil.move(path, os.path.join(folder, new_name))
            if comment[_space:_space+9] == ' Faktura ':
                account = u'Försäljning'
                explanation = comment[_space+8:_paranthesis]
            elif '(' in comment:
                account = comment[_paranthesis + 2:_rparanthesis]
                explanation = comment[_space:_paranthesis] + comment[_rparanthesis + 1:]
                print(repr((account, explanation)))
            else:
                account = u'Förbrukningsmaterial'
                for a in accounts:
                    if a[1:-2] in comment:
                        account = a
                        break
                explanation = comment[_space:_paranthesis]
            words = explanation.split(' ')
            brutto = 0
            vat = 0.25
            fake_vat = (u'Import varor EU', u'Import tjänster EU', u'Import varor ej EU', u'Import tjänster ej EU')
            for j, number in enumerate(reversed(words)):
                number = number.replace(',', '.')
                if len(number) == 0:
                    break
                elif number[-1] == '%':
                    vat = int(number[:-1])/100
                else:
                    try:
                        netto = float(number)
                        brutto = netto * (1 + (0 if account in fake_vat else vat))
                        explanation = ' '.join(words[:len(words) - 1 - j])
                        break
                    except:
                        pass
            excel[row + i, 3] = date
            excel[row + i, 4] = account + explanation
            if brutto:
                excel[row + i, 5] = brutto
                excel[row + i, 6] = vat
            """win32api.ShellExecute(
                0,
                "open",
                new_name,
                "",
                folder,
                0)"""

    view = View(Item('save_current_text', show_label = False),
                Item('save_all_texts', show_label=False),
                Item('register_current_file', show_label = False))


macros = Macros()
macros.configure_traits()
