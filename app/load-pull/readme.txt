This code module contains a test suite for 3GPP spurious emissions (a.k.a. PA stability) at mismatch. It was implemented in Python towards the 3GPP specification document and a custom-built GPIB-controlled phase tuner capable of VSWR > 15:1. It was used with a prototype PA at VSWR 15:1 and was able to automatically catch and manually reproduce a few spuriouses close to the 3GPP requirements as a proof of concept. 

./requirements:
Python-implementation of all 3GPP spurious emission requirements.

./applications:
Test cases in Python and resulting measurement results as Python object files.

./drivers:
Instrument drivers in Python for the following functionality:
	* interact with LabVIEW VIs
	* control amplitude and frequency of a signal generator
	* measure transmission spectra using a spectrum analyzer
	* tune the phase of a load impedance

./setups:
Python and LabVIEW code for automatic calibration of detection losses when using a highly frequency-dependent probe, and resulting calibration results as Python object files.

./software:
Installation instructions for Python and some add-on modules that are used by the code module. 

The test suite is designed for any product that has requirements on spurious emissions at mismatch.

For missing information, contact the author Joakim Pettersson at joakim.pettersson@quflow.com.