# Pepper Task Synchroniser

This Android Library is designed to synchronise a task across multiple Pepper robots, or in other words, to execute an action or task simultaneously.

The library relies on a 3rd party solution called [Chirp](https://chirp.io/). This is a product which sends encoded ultrasonic sounds from a **sender** device with a speaker (such as a laptop) and the **receiver** application (Pepper), which when detected are used to run a task. 
This solution has several advantages over traditional network based synchronisation methods:

 - The sender and receiver can be both in Airplane mode, the only requirement is a device with a speaker
 - The range of the signal is limited only by the power of the speaker
 - The sound sent is ultrasonic, which means it is undetectable to the human ear
 - The solution is scalable, there is no upper limit to the number of robots that can simultaneously hear the signal
 - Once started, the task can easily be stopped by sending a STOP signal. This is not always the case with network triggered tasks. 

## Video

You can see an example of this project being used in this video [here](https://www.youtube.com/watch?v=dWBhFKQlBk4).

## Getting Started

### Prerequisites 

Before you start, go [here](https://developers.chirp.io/applications) and generate your API keys to use the Chirp service. These keys will be embedded into the sender and receiver, and these keys are used to encode and decode the signal. They must match on both sides, otherwise the ultrasonic signal will not be able to be decoded.

### Usage

Is is important to understand that there are 2 parts to this library. 

 - The **sender** is a python script that can be executed on a computer. We chose to use this as it is easy to manipulate, and will run in most environments. When ran, it will send out the encoded ultrasonic signal, which will be picked up by any listening devices setup to decode this signal.
 - The **receiver** is an Android application that runs on Pepper. It uses the tablet microphone to listen, and if it receives a given command, it will execute a block of code in which you can run your task (animation, dance, speech etc.)

There is a README file in each the **sender** and the **receiver** projects. Please refer to these files for specific instructions on how to implement the code.

You do not have to use the python application to send the signal. Indeed, you can use any device with a speaker if it is supported by Chirp. For example, you can use an Android or iOS phone to send the signal which is placed next to the robots. While we do not give specific instructions for this, you can find more information in the [developer documentation](https://developers.chirp.io/docs) on the Chirp website.

### Testing Notes

Your results may vary, but we have tested the strength of the signal and observed the following:

 - Using a MacBook Pro 13 (2018) with the volume turned to the maximum, the range at which the robot can hear the signal is around 20 - 25 meters, in a room with background noise of 40 - 45 decibels (normal office working environment). 
 - The robot configuration must use the tablet microphone so ensure it remains uncovered. The head microphones are not used, as there is no API to route audio from the head to the tablet.
 - You cannot use a Pepper robot as a sender device, as the signal is scrambled when passing from the tablet to the head speakers.

This can be increased by the use of external speakers, or bringing the sender device closer to the robots.

## License

This project is licensed under the BSD 3-Clause "New" or "Revised" License- see the [COPYING](COPYING.md) file for details
