# Project Ghost Client
The client implementation

## Folder Structure

### Android
The android launcher for the client.

### Core
This is the core client used by all other launchers. Different launchers will usually start different handlers. For example the Desktop launcher will launch the `GameHandler` while the Android launcher might launch the `QueueHandler`

### Desktop
The desktop launcher for the client. The desktop launcher has a few required command-line arguments

```
java -jar game.jar <ip> <session>
```

for replays
```
java -jar game.jar --replay <replay-file>
```

for test server
```
java -jar game.jar <ip> --test
```

### iOS
The iOS launcher for the client


## Protocol Specification

* [TCP/UDP Protocol](https://docs.google.com/spreadsheets/d/1Iphm_H0fq9s0AwXyqNvOabJbWijLylbJEGUvfGU65fo/edit?usp=sharing)

## Installation

All projects in this folder use gradle, so simply importing the `build.gradle` file into Intellij or Eclipse should work. For more information check out [the libgdx documentation on setting up](https://github.com/libgdx/libgdx/wiki/Setting-up-your-Development-Environment-%28Eclipse%2C-Intellij-IDEA%2C-NetBeans%29) for a more detailed guide