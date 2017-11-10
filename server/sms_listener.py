#!/usr/bin/env python
# -*- coding: utf-8 -*-

import serial
from threading import Thread, Timer, Lock
import sched
import re
import signal
import time
import os
import sys
import subprocess

ser = None

def ser_open(num):
    global ser
    ser = serial.Serial(
        '/dev/ttyUSB' + str(num),
        baudrate=115200, 
        bytesize=serial.EIGHTBITS, 
        parity=serial.PARITY_NONE,
        stopbits=serial.STOPBITS_ONE 
        )
    ser.open()
    ser.write('at\r\n')

try:
    ser_open(1)
except:
    try:
        ser_open(2)
    except:
        try:
            ser_open(3)
        except:
            ser_open(4)    

# SMS header
cmgl_pattern=re.compile('\+CMGL: (\d)+,.*,"(\+?\d+)",,.*')

# Whitelist of phone numbers allowed to transmit commands
allowed_phone_numbers = ['+41791234567', '0791234567']

# Unfortunately, Futures are not available in Python 2.7
# then we use a lock to avoid writing two things at the same time
send_lock = Lock()

def send_command(command):
    if send_lock.acquire(True): # True makes it wait until lock is available
        ser.write(command)
        send_lock.release()


def receive_loop():
    # If this is true, we know the next line read will be the SMS text
    next_line_is_sms_text = False
    sms = {}

    while(True):
        line = ser.readline()

        if next_line_is_sms_text:
            next_line_is_sms_text = False
            sms['text'] = line[:-2] # trim '\r\n' at the end

            # process the SMS
            Thread(target=process_sms, args=(sms,)).start()

        else:
            match = cmgl_pattern.match(line)
            if match != None:
                sms['id'] = match.group(1)
                sms['sender'] = match.group(2)
                next_line_is_sms_text = True


def send_loop():
    send_command('at+cmgl="ALL"\r\n')

    Timer(2, send_loop).start()


# sms is a dict with keys id, sender and text
def process_sms(sms):
    # ignore service messages for 'Low balance' is any
    # first condition because when balance is low, we get the SMS every minute or so...
    if sms['sender'] == '123':
        return
    elif sms['sender'] in allowed_phone_numbers:
        cmd_message = False

        text = sms['text'].lower()
        if 'open' in text:
            cmd_message = True
            open_garage()
        elif 'close' in text:
            cmd_message = True
            close_garage()
        elif 'toggle' in text:
            cmd_message = True
            toggle_garage()
        elif 'shutdown' in text:
            cmd_message = True
            shutdown_system()

        # delete sms
        if cmd_message:
            send_command('at+cmgd=' + sms['id'] + '\r\n')


def open_garage():
    print 'Open the garage'
    subprocess.call(['/home/pi/work/relay/relay.sh', 'open'])

import time
last_time = 0;

def toggle_garage():
    global last_time
    if time.time() - last_time > 10:
        last_time = time.time()
        do_toggle_garage() 

def do_toggle_garage():
    print '"Toggle" the garage'
    subprocess.call(['/home/pi/work/relay/relay.sh', 'open'])
    subprocess.call(['/home/pi/work/relay/relay.sh', 'close'])

def close_garage():
    print 'Close the garage!'
    subprocess.call(['/home/pi/work/relay/relay.sh', 'close'])

#    subprocess.call(['/home/mathieu/work/read_sms/do.sh', 'Mathieu'])

def shutdown_system():
    print 'Shutdown the system'
    subprocess.call(['/sbin/halt'])

if __name__ == '__main__':
    ser.open()
    ser.write('atz\r\n') # soft reset
    time.sleep(1)
    ser.write('ate0\r\n') # disable echo
    time.sleep(1)
    ser.write('at+cmgf=1\r\n') # Message format: 1 (text)
    time.sleep(1)

    try: 
        pid = os.fork() 
        if pid > 0:
            # exit first parent
            sys.exit(0) 
    except OSError, e: 
        print >>sys.stderr, "fork #1 failed: %d (%s)" % (e.errno, e.strerror) 
        sys.exit(1)

    # decouple from parent environment
    os.chdir("/") 
    os.setsid() 
    os.umask(0) 

    # do second fork
    try: 
        pid = os.fork() 
        if pid > 0:
            # exit from second parent, print eventual PID before
            print "Daemon PID %d" % pid 
            f = open('/home/pi/work/garage_sms_listener.pid', 'w')
            f.write('%s' % pid)
            f.close()
            sys.exit(0) 
    except OSError, e: 
        print >>sys.stderr, "fork #2 failed: %d (%s)" % (e.errno, e.strerror) 
        sys.exit(1) 

    receive_thread = Thread(target=receive_loop)
    receive_thread.start()

    send_thread = Thread(target=send_loop)
    send_thread.start()
