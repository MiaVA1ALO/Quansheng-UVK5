# really only a slight modification - it actually takes the COM port as an arg now.
import serial
import struct
import sys


if len(sys.argv)<4: print('util_051f_ramreader.py <COMx> <address> <len> [filename]') ; exit(1)
com_port = sys.argv[1]
address  = int(sys.argv[2],0)
data_len = int(sys.argv[3],0)

if len(sys.argv)>4:
    filename = sys.argv[4]
else:
    filename = None

def payload_xor(payload):
    XOR_ARRAY = bytes.fromhex('166c14e62e910d402135d5401303e980')
    XOR_LEN   = len(XOR_ARRAY)

    ba=bytearray(payload)
    for i in range(0,len(ba)):
        ba[i] ^= XOR_ARRAY[i%XOR_LEN]
    return bytes(ba)
    
Crc16Tab = [0, 4129, 8258, 12387, 16516, 20645, 24774, 28903, 33032, 37161, 41290, 45419, 49548, 53677, 57806, 61935, 4657, 528, 12915, 8786, 21173, 17044, 29431, 25302,
            37689, 33560, 45947, 41818, 54205, 50076, 62463, 58334, 9314, 13379, 1056, 5121, 25830, 29895, 17572, 21637, 42346, 46411, 34088, 38153, 58862, 62927, 50604, 54669, 13907,
            9842, 5649, 1584, 30423, 26358, 22165, 18100, 46939, 42874, 38681, 34616, 63455, 59390, 55197, 51132, 18628, 22757, 26758, 30887, 2112, 6241, 10242, 14371, 51660, 55789,
            59790, 63919, 35144, 39273, 43274, 47403, 23285, 19156, 31415, 27286, 6769, 2640,14899, 10770, 56317, 52188, 64447, 60318, 39801, 35672, 47931, 43802, 27814, 31879,
            19684, 23749, 11298, 15363, 3168, 7233, 60846, 64911, 52716, 56781, 44330, 48395,36200, 40265, 32407, 28342, 24277, 20212, 15891, 11826, 7761, 3696, 65439, 61374,
            57309, 53244, 48923, 44858, 40793, 36728, 37256, 33193, 45514, 41451, 53516, 49453, 61774, 57711, 4224, 161, 12482, 8419, 20484, 16421, 28742, 24679, 33721, 37784, 41979,
            46042, 49981, 54044, 58239, 62302, 689, 4752, 8947, 13010, 16949, 21012, 25207, 29270, 46570, 42443, 38312, 34185, 62830, 58703, 54572, 50445, 13538, 9411, 5280, 1153, 29798,
            25671, 21540, 17413, 42971, 47098, 34713, 38840, 59231, 63358, 50973, 55100, 9939, 14066, 1681, 5808, 26199, 30326, 17941, 22068, 55628, 51565, 63758, 59695, 39368,
            35305, 47498, 43435, 22596, 18533, 30726, 26663, 6336, 2273, 14466, 10403, 52093, 56156, 60223, 64286, 35833, 39896, 43963, 48026, 19061, 23124, 27191, 31254, 2801,
            6864, 10931, 14994, 64814, 60687, 56684, 52557, 48554, 44427, 40424, 36297, 31782, 27655, 23652, 19525, 15522, 11395, 7392, 3265, 61215, 65342, 53085, 57212, 44955,
            49082, 36825, 40952, 28183, 32310, 20053, 24180, 11923, 16050, 3793, 7920]


def crc16_ccitt(data):
    i2 = 0
    for i3 in range(0, len(data)):
        out = Crc16Tab[((i2 >> 8) ^ data[i3]) & 255]
        i2 = out ^ (i2 << 8)
    return 65535 & i2

#======================================================================================================================================


CMD_051F = b'\x1F\x05'
req_type = 1            # read memory
req_arg1 = address      # read memory start address
req_arg2 = data_len     # read memory bytes count
req_arg3 = 0x00000000   # unused


#wrapping everytihng to command understandable to radio routine
cmd = struct.pack('<HIII',req_type, req_arg1, req_arg2, req_arg3)
cmd_len = len(cmd)
cmd = CMD_051F + struct.pack('<H',cmd_len) + cmd
cmd_crc = struct.pack('<H',crc16_ccitt(cmd))
cmd= b'\xAB\xCD' + struct.pack('<H',cmd_len+4) + cmd + cmd_crc + b'\xDC\xBA'
cmd=cmd[:4] + payload_xor(cmd[4:-2]) + cmd[-2:]


#send/receive by serial
serial = serial.Serial()
serial.baudrate = 38400
serial.timeout=1
serial.port = com_port
serial.open()
serial.write(cmd)
reply = serial.read(data_len)
#print(reply.hex())
#print(reply)

if filename is not None:
    open(filename,'wb').write(reply)
else:
    print(reply.hex())