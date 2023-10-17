# Rubatone
Rubatone is an Android mobile application, combined with a wearable application, built for WearOS devices. Rubatone is made using Kotlin.

Rubatone creates music based on your biometric data inputs. The wearable sends sensor data to the phone app, which then uses this to create the music.

One way of generating music involves moving your arm around to change accelerometer and gyrometer values. These 'active' inputs are used to change the sound of a melody instrument. The accelerometer measures the force behind your arm movements, making louder sounds for more forceful movements and quieter sounds for gentler movements. The gyrometer changes the pitch based on the rotation of your arm - pointing it directly upwards creates a high pitch, and pointing it towards the ground creates a low pitch. Rotating your arm up or down will increase or decrease the pitch.

Another way to create music uses 'passive' inputs (heart rate, light intensity of the environment, and ambient temperature). These inputs are used to directly change other musical components (drums, bass, and chords) that are used as an 'instrumental' section to accompany your 'active' inputs!

Have fun trying out the Rubatone, and don't be afraid to break musical boundaries!


# Marking criteria:
TODO do this part later


# Getting started
TODO do this part later


# Files used in the project:
- Mobile app code is located here: ./android-companion-app/app/src/main/java/com/example/Heartbeats_by_Dr_Dre/MainActivity.kt
- Wearable app code is located here: ./android-companion-app/wearosapp/src/main/java/com/example/Heartbeats_by_Dr_Dre/MainActivity.kt
- The music synthesis component referenced by the mobile app can be found in this location: ./android-companion-app/app/src/main/res/raw/passive_inputs_test.zip
To open this file as a standalone Pure Data patch, the same file can be found (uncompressed) here: ./pd_patches/passive_inputs_test.pd

# Sources used:
- The 'Kortholt' library was used to integrate the music synthesis component with the mobile app: https://github.com/simonnorberg/kortholt
'Kortholt' utilises 'LibPD', a Pure Data wrapper for other programming languages: https://github.com/libpd/libpd
'Pure Data' is a visual programming language used for real-time music generation: http://puredata.info/
'Kortholt' also uses Android's 'Oboe' library for handling Audio I/O: https://github.com/google/oboe 
- 'Android SDK'/'Jetpack Compose': https://developer.android.com/


# Live implementation
Here is an example of how to build/run the project, and what the components are responsible for.

TODO add link to video showing this


# Music synthesis
The music component is made with patches using Pure Data, a visual programming language.
These patches interface with the mobile app's project code by using Kortholt. Kortholt combines LibPD (a Pure Data wrapper that converts patches into a compilable format for the Kotlin code) and Android's Oboe library (used to handle the audio I/O, drivers, and various other settings to optimise for low latency).
The mobile app code will send float values for sensor inputs to the Pure Data patch, via Kortholt methods.
You can also use the Pure Data (PD) patches on their own, if you would like to test them, or see exactly how the music synthesis works in more detail.

## Setting up Pure Data:
To open these, download the most recent version of PD vanilla here:
https://puredata.info/downloads/pure-data

### Setting up audio:

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


#### A pre-emptive word of warning:
PD can be dangerous - when modifying things, you can accidentally make very loud sounds happen very suddenly and very easily.
Make sure that your device output volume is set to a fairly low level before you turn DSP (Digital Signal Processing) on and create any sound.


## Files in pd_patches directory:

### pulse_mockup_one_file.pd
The patch pulse_mockup_one_file.pd contains a mockup synth controlled by a 'pulse' input analagous to a heart rate input (in beats per minute).
The pulsing sound can be turned on or off, and a variable tempo/heart rate value can be sent to change the pulsing frequency.
As well as this, a 'panic'/mute button is also included for disabling sound.
Both accelerometer and gyro inputs can be sent to the patch, to control loudness/dynamics and pitch of the synth tone respectively.
Info on how the patch works and what it does is included in comments inside of the patch.
This patch was originally used in the Minimum Viable Product (MvP) prototype. It was integrated into the file passive_inputs_test.pd when additional features and sensors were implemented. 

Receiver inputs used in the patch:
- appOnOff: toggles the audio on if a float value of 1.0 is sent. Toggles off if a float of 0.0 is sent.
- appHeartRate: receives a float value of the heart rate - changes the pulse tempo based on this value.
    Accepts all values, but bounds them to the range [50, 200] BPM.
- appAccelerometer: receives a float value of the intensity/force used for an arm movement, which is the magnitude of the linear acceleration across all 3 axes. Changes the volume/dynamics of the pulsing tone based on this value. More forceful arm movements will result in a louder sound; gentler arm movements result in a quieter sound.
    Currently configured to bound input values to the range 0 to 10. These values are then mapped from an exponential to a linear distribution, and rescaled to the range 0 to 1, to produce volume/amplitude values in this range. If there is no change in input values, or if values close to 0 are sent, the volume will gradually ramp down to 0 (within 500ms), as the implementation assumes that the user is no longer attempting to play a note. Will likely need to tweak this, I'm not 100% sure what range these input values can take, and not sure what is appropriate/suitable for a user to perform.
- appGyrometer: receives a float value of the vertical position of the arm movement, which is the gyroscope's approximate angle of orientation for rotations about the x axis of the wearable (pitch rotations). Changes the pitch/frequency of the pulsing tone based on this value. The given orientation values are relative to (and based on) the previously calculated orientation value.
    Currently configured to bound input values to the range ~(-pi)/2 to ~pi/2, to give pitches in the range of two octaves (notes between C4 and C6). Tilting the device in the y axis upwards by ~90 degrees will result in the highest note C6 being played. Tilting the device downwards in this axis by ~90 degrees will play the lowest note C4. If the device face is held completely level in the x axis, the middle note of this range (C5) should be played. Will likely need to tweak this and change the bound of input values, or the pitch range of the instrument itself. 
- appPitchControl: changes the form of pitch control being used. Has this behaviour when sent the following float values:
    - 0.0: Fully continuous pitch (not fixed to a scale/tuning)
    - 1.0: Discrete pitch (fixed to 12 tone tuning)
    - 2.0: Discrete pitch, with a 100ms portamento between each note

### passive_inputs_test.pd
This patch contains a modified version of the MvP melodic synth, as well as additional music components which are generated by passive inputs. This patch is the version used in the final codebase by the mobile app. It contains accelerometer, gyrometer, and heart rate sensor inputs, as well as temperature and light sensor inputs. For more information on how this works, please see the comments added to the main page of the patch.

Receiver inputs used in the patch:
- appOnOff: toggles the audio on if a float value of 1.0 is sent. Toggles off if a float of 0.0 is sent.
- appHeartRate: receives a float value of the heart rate - changes the pulse tempo based on this value.
    Accepts all values, but bounds them to the range [50, 200] BPM.
- appAccelerometer: receives a float value of the intensity/force used for an arm movement, which is the magnitude of the linear acceleration across all 3 axes. Changes the volume/dynamics of the pulsing tone based on this value. More forceful arm movements will result in a louder sound; gentler arm movements result in a quieter sound.
    Currently configured to bound input values to the range 7 to 10. These values are then mapped from an exponential to a linear distribution, and rescaled to the range 0 to 0.3, to produce volume/amplitude values in this range. If there is no change in input values, or if values close to 7 are sent, the volume will gradually ramp down to 0 (within 500ms), as the implementation assumes that the user is no longer attempting to play a note.
- appGyrometer: receives a float value of the vertical position of the arm movement, which is the gyroscope's approximate angle of orientation for rotations about the x axis of the wearable (pitch rotations). Changes the pitch/frequency of the pulsing tone based on this value. The given orientation values are relative to (and based on) the previously calculated orientation value.
    Currently configured to bound input values to the range ~(-pi)/2 to ~pi/2, to give pitches in the range of two octaves (notes between C4 and C6). Tilting the device in the y axis upwards by ~90 degrees will result in the highest note C6 being played. Tilting the device downwards in this axis by ~90 degrees will play the lowest note C4. If the device face is held completely level in the x axis, the middle note of this range (C5) should be played. 
- appPitchControl: changes the form of pitch control being used. Has this behaviour when sent the following float values:
    - 0.0: Fully continuous pitch (not fixed to a scale/tuning)
    - 1.0: Discrete pitch (fixed to 12 tone tuning)
    - 2.0: Discrete pitch, with a 100ms portamento between each note
- appTemperature: receives a float value of the ambient temperature (in degrees Celsius). Accepts all values, but bounds them to the range [0, 55] degrees Celsius.
- appLight: receives a float value of the light intensity (in lux). Accepts all values, but bounds them to the range [0, 5000] lux.

