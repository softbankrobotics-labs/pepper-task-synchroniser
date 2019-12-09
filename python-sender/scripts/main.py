#!/usr/bin/env python3

import argparse
import sys
sys.path.insert(0, "../external_dependencies")
import time

from chirpsdk import ChirpSDK, CallbackSet


class Callbacks(CallbackSet):

    def on_sending(self, payload, channel):
        """ Called when a chirp has started to be transmitted """
        print('Sending: {data} [ch{ch}]'.format(data=list(payload), ch=channel))

    def on_sent(self, payload, channel):
        """ Called when the entire chirp has been sent """
        print('Sent: {data} [ch{ch}]'.format(data=list(payload), ch=channel))

def main(block_name, output_device,
         block_size, sample_rate, command):
    
    # Initialise ConnectSDK
    sdk = ChirpSDK(block=block_name)

    print(str(sdk))
    print('Protocol: {protocol} [v{version}]'.format(
        protocol=sdk.protocol_name,
        version=sdk.protocol_version))
    print(sdk.audio.query_devices())

    if command is not None:
        print("Command is %s" % command)

    # Configure audio
    sdk.audio.output_device = output_device
    sdk.audio.block_size = block_size
    sdk.input_sample_rate = sample_rate
    sdk.output_sample_rate = sample_rate

    # Set callback functions
    sdk.set_callbacks(Callbacks())

    if command:
        message = command.encode('utf-8')
        payload = sdk.new_payload(message)
    else:
        payload = sdk.random_payload()

    sdk.start(send=True, receive=False)
    sdk.send(payload, blocking=True)

    print('Exiting')

    sdk.stop()

def finished():
    pass


if __name__ == '__main__':
    parser = argparse.ArgumentParser(
        description='ChirpSDK Example',
        epilog='Sends a random chirp payload, then continuously listens for chirps'
    )
    parser.add_argument('-c', help='The configuration block [name] in your ~/.chirprc file (optional)')
    parser.add_argument('-o', type=int, default=None, help='Output device index (optional)')
    parser.add_argument('-b', type=int, default=0, help='Block size (optional)')
    parser.add_argument('-s', type=int, default=44100, help='Sample rate (optional)')
    parser.add_argument('-u', type=str, default=None, help='Sending String')
    args = parser.parse_args()

    main(args.c, args.o, args.b, args.s, args.u)
