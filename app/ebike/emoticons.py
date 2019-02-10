# E-bike emoticon controller for a BBC MicroBit
# Recognises effort+acceleration+speed+exploration patterns for happy, healthy and safe ebike ezperiences.
# Shows the pattern in a human-recognizable form: An emoticon on the BBC micro:bit.
# Secret goal: Teach my kids the fun of real-time programming with micropython.
# Next goal: Use the detected pattern to control brake lights, blinkers and ABS.


from microbit import *
from math import *

images = [img for img in [
    getattr(Image, name) for name in dir(Image)
    ] if isinstance(img, Image)]
a0 = 1000
da = 50
A = [a0/sqrt(3) for i in range(3)]
G = A[:]
b = 0.9
c = 0.99
while True:
    sleep(20)
    G = [c * a0 + (1 - c) * a
         for a0, a in zip(A, accelerometer.get_values())]
    A = [b * a0 + (1 - b) * a
         for a0, a in zip(A, accelerometer.get_values())]
    a_rms = sqrt(sum([(a-g)**2 for a, g in zip(A, G)]))
    i = (int(a_rms / da)) % len(images)
    display.show(images[i])
    print((int(a_rms / da), i))