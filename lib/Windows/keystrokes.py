"""
Convert text to keystroke events in Windows 7
"""
from win32api import keybd_event, MapVirtualKey

KEYEVENT_KEYDN = 0x0
KEYEVENT_KEYUP = 0x2
VK_SHIFT = 0x10
VK_CTRL = 0x11
VK_ALT = 0x12
VK_RETURN = 0x0D
VK_END = 0x23
VK_HOME = 0x24
VK_UP = 0x26
VK_DOWN = 0x28
VK_TAB = 0x09
VK_OEM_COMMA = 0xBC
VK_OEM_1 = 0xBA
VK_ESCAPE = 0x1B
VK_OEM_PLUS = 0xBB
VK_OEM_MINUS = 0xBD
VK_OEM_PERIOD = 0xBE
VK_SPACE = 0x20

def keystrokes(text): map(keystroke, text)

def keystroke(src, ctrl = False, shift = False, alt = False):
    if ctrl: keybd_event(VK_CTRL, 0, 0, 0)
    if alt: keybd_event(VK_ALT, 0, 0, 0)
    if isinstance(src, int):
        if shift: keybd_event(VK_SHIFT, 0, 0, 0)
        keybd_event(src, 0, 0, 0)
        keybd_event(src, 0, KEYEVENT_KEYUP, 0)
    elif src == u',':
        if shift: keybd_event(VK_SHIFT, 0, 0, 0)
        keybd_event(VK_OEM_COMMA, 0, 0, 0)
        keybd_event(VK_OEM_COMMA, 0, KEYEVENT_KEYUP, 0)
    elif src == u'+':
        shift = True
        keybd_event(VK_SHIFT, 0, 0, 0)
        keybd_event(VK_OEM_PLUS, 0, 0, 0)
        keybd_event(VK_OEM_PLUS, 0, KEYEVENT_KEYUP, 0)
    elif src == u'=':
        keybd_event(VK_OEM_PLUS, 0, 0, 0)
        keybd_event(VK_OEM_PLUS, 0, KEYEVENT_KEYUP, 0)
    elif src == u'-':
        if shift: keybd_event(VK_SHIFT, 0, 0, 0)
        keybd_event(VK_OEM_MINUS, 0, 0, 0)
        keybd_event(VK_OEM_MINUS, 0, KEYEVENT_KEYUP, 0)
    elif src == u'_':
        shift = True
        if shift: keybd_event(VK_SHIFT, 0, 0, 0)
        keybd_event(VK_OEM_MINUS, 0, 0, 0)
        keybd_event(VK_OEM_MINUS, 0, KEYEVENT_KEYUP, 0)
    elif src == u' ':
        if shift: keybd_event(VK_SHIFT, 0, 0, 0)
        keybd_event(VK_SPACE, 0, 0, 0)
        keybd_event(VK_SPACE, 0, KEYEVENT_KEYUP, 0)
    elif src == u'.':
        if shift: keybd_event(VK_SHIFT, 0, 0, 0)
        keybd_event(VK_OEM_PERIOD, 0, 0, 0)
        keybd_event(VK_OEM_PERIOD, 0, KEYEVENT_KEYUP, 0)
    elif src.isdigit():
        chr = ord(src)
        keybd_event(chr, chr, 0, 0)
        keybd_event(chr, chr, KEYEVENT_KEYUP, 0)
    else:
        chr = ord(src.upper())
        if src.isupper(): shift = True
        if shift: keybd_event(VK_SHIFT, 0, 0, 0)
        keybd_event(chr, MapVirtualKey(chr,0), 0, 0)
        keybd_event(chr, MapVirtualKey(chr,0), KEYEVENT_KEYUP, 0)
        
    if shift: keybd_event(VK_SHIFT,0, KEYEVENT_KEYUP, 0)
    if alt: keybd_event(VK_ALT,0, KEYEVENT_KEYUP, 0)
    if ctrl: keybd_event(VK_CTRL,0, KEYEVENT_KEYUP, 0)