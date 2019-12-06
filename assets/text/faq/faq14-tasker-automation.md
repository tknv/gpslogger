## How does this integrate with Tasker/Llama or other automation frameworks?

## How to automate GPSLogger?

### Controlling GPSLogger

If your automation app can send intents, you can use those intents to control GPSLogger and get it to perform a few actions. 

To invoke it from Tasker, create a new action under Misc > Send Intent. 

>Action: `lab.tknv.gpslogger.TASKER_COMMAND`
Extra: `immediatestart:true` (others below)  
Package: `lab.tknv.gpslogger`
Class: `lab.tknv.gpslogger.TaskerReceiver`
Target: `Broadcast Receiver`

To invoke it from Automate (LlamaLab), create a Send Broadcast block:

>Package: `lab.tknv.gpslogger`
Receiver Class: `lab.tknv.gpslogger.TaskerReceiver`
Action: `lab.tknv.gpslogger.TASKER_COMMAND`
Extras: `{"immediatestart" as Boolean:"true"}`

To invoke it from your own Android code:

    Intent i = new Intent("lab.tknv.gpslogger.TASKER_COMMAND");
    i.setPackage("lab.tknv.gpslogger");
    i.putExtra("immediatestart", true);
    sendBroadcast(i);


**These are the extras you can send to GPSLogger**:

>`immediatestart` - (true/false) Start logging    
> `immediatestop` - (true/false) Stop logging  
> `setnextpointdescription` - (text) Sets the annotation text to use for the next point logged  
> `settimebeforelogging` - (number) Sets preference for logging interval option    
> `setdistancebeforelogging` - (number) Sets preference for distance before logging option  
> `setkeepbetweenfix` - (true/false) Sets preference whether to keep GPS on between fixes  
> `setretrytime` - (number) Sets preference for duration to match accuracy  
> `setabsolutetimeout` - (number) Sets preference for absolute timeout  
  > `setprefercelltower` - (true/false) Enables or disables the GPS or celltower listeners  
> `logonce` - (true/false) Log a single point, then stop  
> `switchprofile` - (text) The name of the profile to switch to  
> `getstatus` - (true) Asks GPSLogger to send its current events broadcast  

### Shortcuts

The app comes with a Start and a Stop **shortcut** (long press home screen, add widget), you can invoke those from some automation apps.


### GPSLogger Events Broadcast

### Listening to GPSLogger


(Experimental feature) GPSLogger sends a broadcast start/stop of logging, which you can receive as an event.
  
In Tasker, this would look like:  
  
> Event: Intent Received  
  Action: lab.tknv.gpslogger.EVENT
  
From there in your task, you can look at the following variables
 
 * `%gpsloggerevent` - `started` or `stopped`
 * `%filename` - the base filename that was chosen (no extension)
 * `%startedtimestamp` - timestamp when logging was started (epoch)

In a custom application, receive the `lab.tknv.gpslogger.EVENT` broadcast and have a look inside the extras.