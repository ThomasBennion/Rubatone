Hello :)
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


The patch pulse_mockup_one_file.pd contains a mockup synth controlled by a 'pulse' input analagous to a heart rate input (in beats per minute).
The pulsing sound can be turned on or off, and a variable tempo/heart rate value can be sent to change the pulsing frequency.
As well as this, a 'panic'/mute button is also included for disabling sound.
Both accelerometer and gyro inputs can be sent to the patch, to control loudness/dynamics and pitch of the synth tone respectively.
Info on how the patch works and what it does is included in comments inside of the patch.

Receiver inputs used in the batch:
- appOnOff: toggles the audio on if a float value of 1.0 is sent. Toggles off if a float of 0.0 is sent.
- appHeartRate: receives a float value of the heart rate - changes the pulse tempo based on this value.
    Accepts all values, but bounds them to the range [50, 200] BPM.
- appAccelerometer: receives a float value of the intensity/force used for an arm movement, which is the magnitude of the linear acceleration across all 3 axes. Changes the volume/dynamics of the pulsing tone based on this value. More forceful arm movements will result in a louder sound; gentler arm movements result in a quieter sound.
    Currently configured to bound input values to the range 0 to 10. These values are then mapped from an exponential to a linear distribution, and rescaled to the range 0 to 1, to produce volume/amplitude values in this range. If there is no change in input values, or if values close to 0 are sent, the volume will gradually ramp down to 0 (within 500ms), as the implementation assumes that the user is no longer attempting to play a note. Will likely need to tweak this, I'm not 100% sure what range these input values can take, and not sure what is appropriate/suitable for a user to perform.
- appGyrometer: receives a float value of the vertical position of the arm movement, which is the gyroscope's approximate angle of orientation for rotations about the x axis of the wearable (pitch rotations). Changes the pitch/frequency of the pulsing tone based on this value. The given orientation values are relative to (and based on) the previously calculated orientation value.
    Currently configured to bound input values to the range ~(-pi)/4 to ~pi/4, to give pitches in the range of two octaves (notes between C4 and C6). Tilting the device in the x axis upwards by ~90 degrees will result in the highest note C6 being played. Tilting the device downwards in this axis by ~90 degrees will play the lowest note C4. If the device face is held completely level in the x axis, the middle note of this range (C5) should be played. Will likely need to tweak this and change the bound of input values, or the pitch range of the instrument itself. 
- appPitchControl: changes the form of pitch control being used. Has this behaviour when sent the following float values:
    - 0.0: Fully continuous pitch (not fixed to a scale/tuning)
    - 1.0: Discrete pitch (fixed to 12 tone tuning)
    - 2.0: Discrete pitch, with a 100ms portamento between each note