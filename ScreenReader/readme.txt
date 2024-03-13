Dumps the data stored in the part of RAM used to display the screen, converts it to an image, and displays it in a JFrame. It does this a specified number of times per second. This data is accessed via the standard programming cable normally used to flash firmware and the EEPROM.

Usage: java UVK5ScreenReader COMx
Requirements: Python, pyserial, Java, v2.01.26 firmware with 051f mod (provided, it is "custom_firmware_2.01.26.bin").

The provided firmware has the following mods:
051f mod, allows you to read any data easily from the COM port
Regular VFO limits set from normal bands to 0-65536 MHz (not actual limit, but the radio thinks it can.)
Broadcast FM receiver VFO limits set from 0-6553.6 MHz (again, not the actual limit, but...)


util_051f_ramreader.py taken from https://github.com/amnemonic/Quansheng_UV-K5_Firmware/ with a slight modification (actually takes in the COM port as an argument rather than defaulting to COM14)
