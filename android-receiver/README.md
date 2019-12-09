# Synchroniser Receiver

The receiver takes the form of an Android application running on Pepper. Once setup, it will use the tablet microphone to listen to the ultrasonic signal sent from the sender application. 

The sample application included in this library uses a dance to demonstrate the usage, but you can use this library to synchronise any task across multiple robots. This README will explain the integration instructions to have the robot listen to the signal, for a specific example using the QiSDK and an animation, please refer to the sample application.

## Using the library

### Creating the Project

*Our Android libraries are distributed through Maven which makes implementation much simpler. You can also clone the project from GIT and include it as a project dependency in your project. If you wish to do this, follow [these instructions](https://developer.android.com/studio/projects/android-library)*

 1. Create a new Android project, if you intend on using the QiSDK make
    sure the minimum version is API 23 Marshmellow.
 2. Install the Synchroniser library using Jitpack, [instructions are here for this library](https://jitpack.io/#softbankrobotics-labs/pepper-task-synchroniser). From the releases tab, pick a version, usually the latest. 
 3. Add the following to your project `build.gradle` file, in the allprojects, repositories block:
    ```
    allprojects {
        repositories {
            ...
            maven { url "https://maven.chirp.io/release" }
        }
    }
    ```
 4. Sync your gradle files, the synchroniser library should be added as a dependency to your project.

### Embed the Keys

As with the sender application, you will need to use the keys that have been generated for your application [here](https://developers.chirp.io/applications).

You will need to add these keys to a file called `apikey.properties` which will be located in the root of the Android project (the same level as the `gradle.properties`). It should not be committed to GIT as it contains sensitive information, but you will create this file using this format:

```
CHIRP_KEY=""  
CHIRP_SECRET=""  
CHIRP_CONFIG=""
```

Once added, add the following lines to your `build.gradle` at the module level.

Just below the plugins at the top of the file:
```
def apikeyPropertiesFile = rootProject.file("apikey.properties")  
def apikeyProperties = new Properties()  
apikeyProperties.load(new FileInputStream(apikeyPropertiesFile))
```

And in the default config:
```
android {
    ...
    defaultConfig {
        ...
        buildConfigField("String", "APPKey", apikeyProperties['CHIRP_KEY'])  
        buildConfigField("String", "APPSecret", apikeyProperties['CHIRP_SECRET'])  
        buildConfigField("String", "APPConfig", apikeyProperties['CHIRP_CONFIG'])
    }
}
```
The project will not work until this file has been added with the above keys. Once added, sync your gradle files. 

## Usage

### Requesting Microphone Permission

In Android, you will need to ask the user to accept usage of the microphone. You can do this in `onResume()`. It only needs to be done once on the first launch of the application and if the permission is granted, the user will not be asked again.

```
private val REQUEST_RECORD_AUDIO = 1

override fun onResume() {
    super.onResume()

    if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), REQUEST_RECORD_AUDIO)
    }
}
```

The following is a callback to check if the permission has been granted by the user:

```
override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
    when (requestCode) {
        REQUEST_RECORD_AUDIO -> {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
            }
            return
        }
    }
}
```

### Initialisation

In your activity, instantiate the TaskSynchroniser with the activity context:

```
private val taskSynchroniser: TaskSynchroniser by lazy {  
    TaskSynchroniser(this, BuildConfig.APPKey, BuildConfig.APPSecret, BuildConfig.APPConfig)
}
```

The first thing to do is connect the callbacks. Create a method called `initialiseSDK()` and call this in `onCreate()`

```
private fun initialiseSDK() {
    taskSynchroniser.chirpConnect.onReceiving { channel: Int ->
        Log.v("Synchroniser", "onReceiving on channel: $channel")
    }

    taskSynchroniser.chirpConnect.onReceived { payload: ByteArray?, channel: Int ->
        val hexData = payload?.let { String(it) }
        Log.v("Synchroniser", "onReceived: $hexData on channel: $channel")
    }
}
```

These callbacks are called when the signal is in the process of being received, and once it is successfully received. You can decode the signal into a String, and based upon what is sent by the sender application, you can perform an action such as starting or stopping a task.

### Starting and Stopping

It is good practice to use a button or UI element to start and stop the SDK, and track the current state. You can see an example of this in the sample application. After initialising the SDK you can start the SDK using a `startSDK()` method:

```
private fun startSDK() {  
    if (taskSynchroniser.start()) {  
        // Callback if SDK successfully started
    } else {  
        // Callback if the SDK failed to start
    }  
}
```
 
 The SDK can be stopped in the same way:

```
private fun stopSDK() {  
    if (taskSynchroniser.stop()) {  
        // Callback if SDK successfully stopped
    } else {  
        // Callback if SDK failed to stop
    }  
}
```

It is good practice to stop the SDK in the `onStop()` method, and call the following in `onDestroy()`:

```
taskSynchroniser.close()
```