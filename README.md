# hullowheel-android
The client part of HulloWheel, a virtual gaming wheel for Android + Linux

## Why & Story
https://github.com/hulloanson/hullowheel-server/#why

## Build and run
This is super-alpha. You will need to build it yourself for now. No APKs now I'm sorry.
## Build
1. Import into Android Studio
2. Build an APK, or press run to install it to an connected, USB-debugging enabled Android device
## Run
1. Start the app
2. Start the server
3. If no inputs was received, try stopping both the app and the server, then start the app, then the server. It is due to my ignorance on UDP. See issue below and also https://github.com/hulloanson/hullowheel-server/#known-issues.

## TODOs
### Known Issues & Improvements
1. [ ] [Issue] Does not handle disconnects with server well.
2. [ ] [Improvement] Lag problem (just a bit, not noticeable after you've got used to it)
     - Proposal: Send only the modified inputs
3. [ ] [Issue] UDP server address and port hard-coded. Add activity to configure the address.
4. [ ] [Improvement] Normalize all values in the app. Before sending.
5. [ ] [Issue] Sometimes button presses do not deliver. Probably a normal finger press is too short. Find a way to make sure a press event lasts at least a certain duration.
6. [ ] Add visual effects when button pressed.

### Wish to have features
1. [ ] iOS app
2. [ ] More beautiful interface
