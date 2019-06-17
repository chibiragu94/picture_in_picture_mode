Android 8.0 (API level 26) allows activities to launch in picture-in-picture (PIP) mode. PIP is a special type of multi-window mode mostly used for video playback. It lets the user watch a video in a small window pinned to a corner of the screen while navigating between apps or browsing content on the main screen.

PIP leverages the multi-window APIs available in Android 7.0 to provide the pinned video overlay window. To add PIP to your app, you need to register your activities that support PIP, switch your activity to PIP mode as needed, and make sure UI elements are hidden and video playback continues when the activity is in PIP mode.

The PIP window appears in the top-most layer of the screen, in a corner chosen by the system. You can drag the PIP window to another location. When you tap on the window two special controls appear: a full-screen toggle (in the center of the window) and a close button (an "X" in the upper right corner).
