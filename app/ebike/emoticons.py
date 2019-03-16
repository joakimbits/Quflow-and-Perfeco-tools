# Write your code here :-)
# E-bike emoticon controller for a BBC MicroBit
# Recognises effort+acceleration+speed+exploration patterns for happy, healthy and safe ebike ezperiences.
# Shows the pattern in a human-recognizable form: An emoticon on the BBC micro:bit.
# Goals: Use the detected pattern to control brake lights, blinkers and ABS.
# Secret goal: Teach my kids the fun of real-time programming with micropython.

from microbit import *
from math import *
import radio

headlight_control = button_a
emo_control = button_b
radio.on()

headlight = pin0
breaklight = pin1
headlight.write_digital(1)
breaklight.write_digital(1)

headlight_mode = 1
values = [0x100, 0x3FF]
emo_mode = 0
images = [img for img in [
    getattr(Image, name) for name in dir(Image)
] if isinstance(img, Image)]

brakelight_value = 0
a0 = 1000
da = 50
A = accelerometer.get_values()
G = A[:]
b = 0.9
c = 0.99
d = 0.5
breaklight.set_analog_period_microseconds(10000)
breaklight.set_analog_period_microseconds(10000)
while True:
    headlight_mode = (headlight_mode + headlight_control.get_presses()) % len(values)
    headlight.write_analog(values[headlight_mode])
    emo_mode0 = emo_mode
    emo_mode = (emo_mode + emo_control.get_presses()) % len(images)
    if emo_mode != emo_mode0:
        radio.send("emo_mode = %d" % emo_mode)
    message = radio.receive()
    if message:
        try:
            exec(message, globals(), locals())
        except:
            pass
    acc = accelerometer.get_values()
    G = [c * a0 + (1 - c) * a for a0, a in zip(G, acc)]
    A = [b * a0 + (1 - b) * a for a0, a in zip(A, acc)]
    a_rms = sqrt(sum([(a - g) ** 2 for a, g in zip(A, G)]))
    i = (emo_mode + int(a_rms / da)) % len(images)
    display.show(images[i])
    brakelight_value = d * brakelight_value + (1 - d) * (0x100 + 0x2FF * (i % 2))
    breaklight.write_analog(brakelight_value)
    print((headlight_mode, int(a_rms / da), i))
    sleep(10)
