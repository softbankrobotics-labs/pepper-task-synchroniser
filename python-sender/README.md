# Synchroniser Sender

### Building the application package

*More detailed integration instructions are available on the [Chirp developer website](https://developers.chirp.io/docs/getting-started/python)*

In order to package your application, you will need first to install the
external dependencies.

*Step one depends on your operating system:*
##### Mac OS
```
brew install portaudio libsndfile
```
##### Ubuntu
```
sudo apt-get install python3-dev python3-setuptools portaudio19-dev libffi-dev libsndfile1
```
##### Windows
```
# Install sounddevice from the link below  
# https://www.lfd.uci.edu/~gohlke/pythonlibs/#sounddevice 
pip3 install sounddevice.whl
```
*Step two:*

The python packages listed in the `requirements.txt` file contain the ones that the robot doesn't have by default. To be used in the application, we need to provide them inside the package.

Navigate to the folder containing the `requirements.txt` file and execute the command:
```
pip3 install -r requirements.txt -t external_dependencies
```

### Embed the keys

Use the keys that have been generated for your application [here](https://developers.chirp.io/applications) and add these the the `.chirprc` file which is included as a template in the project. 

Move this file to your root directory so it is located here: `~/.chirprc`.

If you are unsure where your root directory is, you can open a python script in a terminal and run the following:
```
import os;
os.path.expanduser('~/')
```

### Running the application

Navigate to the **scripts** folder where the `main.py` file is located. There are 2 ways to run the script depending on if the script is executable or not.
 - `./main.py`
or
 - `python3 main.py`

The script takes an argument in the form of `-u` as the string of data it will send. This will depend on how you have set your receiver application up, but for this example you can send one of the following:
 - `go` - This will execute the command on the robot
 - `stop` - This will tell the robot to stop executing the command and go back to listening
 - `close` - This will disconnect the SDK running on the robot, and stop the robot listening to any further commands

### Examples

 - `./main.py -u go`
 - `./main.py -u stop`
 - `./main.py -u close`

### Additional parameters

The Chirp SDK allows you to specify additional parameters when sending the signal. These are as follows:

 - `-c` - The configuration block [name] in your `~/.chirprc` file
 - `-o` - Output device index
 - `-b` - Block size
 - `-s` - Sample rate

We do not use these additional parameters so they are not documented here, but please refer to the python documentation on the [Chirp developer site](https://developers.chirp.io/docs/getting-started/python) for more information.