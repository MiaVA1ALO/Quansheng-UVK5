Dumps the data stored in the part of RAM used to display the screen, converts it to an image, and displays it in a JFrame. It does this a specified number of times per second. This data is accessed via the standard programming cable normally used to flash firmware and the EEPROM.

Usage: java UVK5ScreenReader.java COMx
Requirements: Python, pyserial, Java

util_051f_ramreader.py taken from https://github.com/amnemonic/Quansheng_UV-K5_Firmware/ with a slight modification (actually takes in the COM port as an argument rather than defaulting to COM14)
