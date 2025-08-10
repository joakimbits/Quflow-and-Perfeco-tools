"""
micro:bit v2 controlling e-bike horn and sound alarms and brake lights

Horn and sound is default off.
G-force is default sensitive.

* (De-)Accelerate to activate horn, sound and brake lights.
* A button (bar row 1) selects max horn time in milliseconds [0, 3, 10, 30, 100].
* B button (bar row 2) selects G-force needed for max value [0.03, 0.1, 0.3]
* micro:bit touch (bar row 3) selects sound and nulls the g-force.
* bar row 4 shows actual horn time.
* bar row 5 shows actual brake light intensity.
"""

from microbit import *
from math import *
import radio

analog_bits = 10
analog_values = 1024

# Hear selected horn times (1 ms resolution, ~1 s cycle)
horn = pin2
horn.set_analog_period(1024)
horn_control = button_a
horn_ms = [0, 3, 10, 30, 100]
horn_ms_index = 0

# See brake light intensities (1 us resolution, ~1 ms cycle)
brake_light = pin1
brake_light.set_analog_period_microseconds(1024)
brake_light_control = button_b

# Hear selected sound > 0 at same intensity as brake light
sound_control = pin_logo
sound_control.set_touch_mode(sound_control.CAPACITIVE)
sound_control_was_touched = False
sounds = tuple(Sound.__dict__.values())[2:]
sound_index = 0
sound_index0 = 0

# Selectable G force for max rear light
g_forces = [0.03, 0.1, 0.3]
g_force_index = 0

# Bar graph with 12 levels in five LEDs
I = '0123456789'
bars = ['9' * (i // 10) + I[i % 10] + '0' * (4 - i // 10) for i in range(50)]

radio.on()

dt = 3
T = 100
t = 0
acc_noise = 0.02
A0 = G0 = accelerometer.get_values()[1:3]
g_memory = 0.99
a_memory = 0.9
parked = None
min_parked_steering_arm_angle = 50

def hmi():
    global horn_ms_index, g_force_index, sound_index, sound_index0, sound_control_was_touched
    global A, A0, G0, t, parked

    # G-force
    A = accelerometer.get_values()[1:3]
    A0 = [a_memory * a0 + (1 - a_memory) * a for a0, a in zip(A0, A)]
    sleep(dt)
    t += dt
    if t > T:
        t = 0
        acc = sqrt(sum([((a0 - g0) / 1024) ** 2 for a0, g0 in zip(A0, G0)]))
        g_force = acc if acc > acc_noise else 0

        # Mode detection

        # Settings
        horn_ms_index = (horn_ms_index + horn_control.get_presses()) % len(horn_ms)
        g_force_index = (g_force_index + brake_light_control.get_presses()) % len(g_forces)
        if sound_control.is_touched():
            if not sound_control_was_touched:
                G0 = A0
                sound_index = (sound_index + 1) % (1 + len(sounds))
                sound_control_was_touched = True
        else:
            sound_control_was_touched = False

        # Horn, sound, brake light and display
        horn_ms_level = horn_ms[horn_ms_index]
        g_force_level = g_forces[g_force_index]
        analog_value = int(min(1023, analog_values * g_force // g_force_level))
        horn_ms_value = horn_ms_level * analog_value // analog_values
        brake_light.write_analog(analog_value)
        horn.write_analog(horn_ms_value)
        if g_force and sound_index:
            set_volume(256 * analog_value // analog_values)
            speaker.on()
            if not audio.is_playing() or sound_index != sound_index0:
                audio.play(sounds[sound_index - 1], wait=False)
        else:
            speaker.off()

        display.show(Image(':'.join([
            bars[len(bars) * horn_ms_index // len(horn_ms)],
            bars[len(bars) * g_force_index // len(g_forces)],
            bars[len(bars) * sound_index // (len(sounds) + 1)],
            bars[len(bars) * horn_ms_value // horn_ms[-1]],
            bars[len(bars) * analog_value // analog_values],
        ])))

        # Remote control
        message = radio.receive()
        if message:
            try:
                exec(message, globals(), locals())
                print(message)
            except Exception as error:
                print(error)

        # Update globals not yet calculated
        sound_index0 = sound_index
        G0 = [g_memory * g0 + (1 - g_memory) * a0 for g0, a0 in zip(G0, A0)]
        if parked is None:
            steering_arm_angle = -atan2(G0[-1], G0[-2]) * 180 / pi
            parked = steering_arm_angle > min_parked_steering_arm_angle
            if parked:
                horn_ms_index = 1
                sound_index = 1

            print('Steering arm angle', steering_arm_angle)
            print("Not parked" if not parked else "Parked")

while True:
    hmi()
