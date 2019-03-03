# E-bike emoticon controller for a BBC MicroBit
# Recognises effort+acceleration+speed+exploration patterns for happy, healthy and safe ebike ezperiences.
# Shows the pattern in a human-recognizable form: An emoticon on the BBC micro:bit.
# Goals: Use the detected pattern to control brake lights, blinkers and ABS.
# Secret goal: Teach my kids the fun of real-time programming with micropython.

from microbit import *
from math import *

pin0.set_analog_period_microseconds(10000)
l = 0

images = [img for img in [
    getattr(Image, name) for name in dir(Image)
] if isinstance(img, Image)]
a0 = 1000
da = 50
A = accelerometer.get_values()
G = A[:]
b = 0.9
c = 0.99
d = 0.5
while True:
    sleep(10)
    acc = accelerometer.get_values()
    G = [c * a0 + (1 - c) * a for a0, a in zip(G, acc)]
    A = [b * a0 + (1 - b) * a for a0, a in zip(A, acc)]
    a_rms = sqrt(sum([(a - g) ** 2 for a, g in zip(A, G)]))
    i = (int(a_rms / da)) % len(images)
    display.show(images[i])  # 0: <3, 2: :)
    l = d * l + (1 - d) * (0x100 + 0x2FF * (i % 2))
    pin0.write_analog(l)
    print((int(a_rms / da), i))
