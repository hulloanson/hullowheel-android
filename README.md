# hullowheel-android
The client part of HulloWheel, a virtual gaming wheel for Android + Linux

## Why & Story
https://github.com/hulloanson/hullowheel-server/#why

## Installing
HulloWheel Android is now in its first release (v0.1). You can find the APK in https://github.com/hulloanson/hullowheel-android/releases

## Build
You can also build it yourself by cloning the master branch, importing it into Android Studio and build it.

## Run

1. Start the app
2. Start the server (see https://github.com/hulloanson/hullowheel-server)
3. Set the address of the server
     - In the form of `[IP]:[PORT]` e.g. `192.168.43.66:20000`
     - Basically any reachable IP and PORT works. However, due to latency, typical setup would be to connect your phone and the server to the same LAN network.
4. Press "Start" at the top-right corner

## TODOs
### Known Issues & Improvements
1. [x] [Issue] Does not handle disconnects with server well.
     - Solved by restarting automatically at the server side
2. [x] [Improvement] Lag problem (just a bit, not noticeable after you've got used to it)
     - Looked like the problem of my crappy wifi from my phone.
     - ~~Proposal: Send only the modified inputs~~
3. [x] [Issue] UDP server address and port hard-coded. Add activity to configure the address.
     - Solved by adding a preference screen to input the address
4. [ ] [Improvement] Normalize all values in the app. Before sending.
5. [ ] [Issue] Sometimes button presses do not deliver. Probably a normal finger press is too short. Find a way to make sure a press event lasts at least a certain duration.
6. [ ] Add visual effects when button pressed.

### Wish to have features
1. [ ] iOS app
2. [ ] More beautiful interface
