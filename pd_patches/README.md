These are patches made using Pure Data, a visual programming language.

To open these, download the most recent version of pd vanilla here:
https://puredata.info/downloads/pure-data

# SETTING UP AUDIO:

1. Open up pd
2. In the menu bar, go to Media > Audio Settings...
3. Under Output Devices, select the audio output device that you're computer is currently using.
4. Click 'Apply', then click 'OK'

To test that these settings work:
0. Turn your volume down to a fairly low level.
1. In the top right hand corner, click on the 'DSP' button to tick it. Text next to the button should now say 'Audio on'
2. In the menu bar, go to Media > Test Audio and MIDI...
3. A pd patch will be opened. On the left hand side, under the 'TEST GAIN (dB)' heading, select the radio button with '60' next to it.
If you hear a sine tone, then the sound works. 
If not, the audio device is not configured correctly. Try changing the audio output device selected and/or the device sample rate settings in the Media > Audio Settings... menu.

For more info on audio setup, check here:
http://msp.ucsd.edu/Pd_documentation/x3.htm


## A PRE-EMPTIVE WORD OF WARNING:
pd can be dangerous - when modifying things, you can accidentally make very loud sounds happen very suddenly and very easily.
Make sure that your device output volume is set to a fairly low level before you turn DSP (Digital Signal Processing) on and create any sound.


--------------------------------------------------------------------------------


The patch pulse_mockup.pd contains a mockup synth controlled by a 'pulse' input analagous to a heart rate input (in beats per minute).
The pulsing sound can be turned on or off, and a variable tempo/heart rate value can be sent to change the pulsing frequency.
As well as this, a 'panic'/mute button is also included for disabling sound.
Info on how the patch works and what it does is included in comments inside of the patch.