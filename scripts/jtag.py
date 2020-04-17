from pyftdi.jtag import JtagEngine
from pyftdi.bits import BitSequence
import time

FTDI_BOARD = 'ftdi://ftdi:232h/1'
SPEED = 5
TCK_FREQ = 1e6
MAX_WIDTH = 255

def read_pwm():
    jtag.write_ir(RD_PWM)
    pwm = jtag.read_dr(1)
    print("PWM: 0x%x" % int(pwm))

# Define Instructions
IDCODE = BitSequence('00000100', msb=True, length=8)
WR_WIDTH = BitSequence('00000101', msb=True, length=8)
RD_PWM = BitSequence('00000110', msb=True, length=8)

# Initialize
jtag = JtagEngine(frequency=TCK_FREQ)
jtag.configure(FTDI_BOARD)

jtag.reset()    # Test Logic Reset (5 times of TMS=1)

# To Shift-IR, Exit1-IR and finally move to UpdateIR
jtag.write_ir(IDCODE)

# Move to Shift-DR, repeat Shift-DR, wait 15ms, and move to Test-Logic Reset
idcode = jtag.read_dr(32)
print("IDCODE (reset): 0x%x" % int(idcode))

width = 0
dir = 1
while True:
    jtag.write_ir(WR_WIDTH)
    jtag.write_dr(BitSequence(width, msb=False, length=8))

    width += dir * SPEED
    if width >= MAX_WIDTH:
        read_pwm()
        dir = -1
        width = MAX_WIDTH
    elif width <= 0:
        read_pwm()
        dir = 1
        width = 0

    time.sleep(0.001)
