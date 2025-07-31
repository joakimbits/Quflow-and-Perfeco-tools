# micro:bit on a Quattrovelo bike steering arm

A DG412 quad switch and a 2x3 pin header is soldered directly onto micro:bit pads.

## Wiring
```
Bike USB powers micro:bit USB.
DG412 S1, V-, GND are soldered onto GND pads.
DG412 S4 is soldered onto P20.
DG412 D4, IN4 are soldered onto +3V3 pads.
DG412 S2 is patched with yellow wire onto nearest P2 pad.
DG412 VL is patched with red wire onto nearest +3V3 pad.
DG412 S3 is patched with orange wire onto nearest P1 pad.
Pin 1 purple reserved GROUND is soldered onto a cut pad patched to GND.
Pin 2 blue BRAKE +10V is patched to DG412 IN2, V+, IN3.
Pin 3 green reserved MOTOR DATA is soldered onto a P15 pad.
Pin 4 yellow HORN is patched to DG412 D2.
Pin 5 orange reserved CONTROL DATA is soldered onto a P13 pad.
Pin 6 red BRAKE LIGHTS is patched to DG412 D3.
```

## Operation
1. Power up micro:bit when steering arm is parked to activate the touch alarm.
2. Power down micro:bit to deactivate the touch alarm.
3. Power up micro:bit when driving normally to just activate brake lights.
4. (De-)Accelerate to lit up the brake lights.

## Settings on the display
* A button selects max horn time per cycle: Bar row 1.
* B button selects G-force needed for max value: Bar row 2.
* micro:bit touch selects a warning sound and nulls the g-force: Bar row 3.
* Bar row 4 is proportional to actual horn time.
* Bar row 5 is proportional to actual brake light intensity.
