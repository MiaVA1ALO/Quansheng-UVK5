The images (and font) in the UVK5 firmware are stored as (8 bit) bytes. Each of these 8 bits is in binary - i.e. either a 0 or 1.
When we read data from the firmware starting and ending at a specific address, the images can be extracted.

Let's say the first byte extracted is 24 (in hexadecimal). That corresponds to 00100100. If we rotate this, it will look like so...
0
0
1
0
0
1
0
0
... and let's also say the remaining bytes are 0, 81, 42, 24, 18, the entire picture is...
001000
000100
100010
000001
000001
100010
000100
001000
... it's a smiley face :)
