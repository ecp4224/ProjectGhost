# Project Ghost

![logo](https://i.imgur.com/OYVGaHn.png)

A 2d online multiplayer arena game

## General Overview

Project Ghost is a multiplayer arena game. The goal is simple, you have 3 lives and you must kill your opponent using the weaopn of your choosing, but there's a catch, your enemy is invisible unless they fire their weapon! There are 5 weapons; Blaster, Laser, Vortext, Fast Boots and Boomerang. Each weapon provides a unique play style, each having there advantages and disadvantages (i.e Laser's chargeup freezes the player, making them an open target while it charges up). Players become visible when they fire their weapon AND when they take damage. If a player stays in one place for to long then they become visible. Items add another layer to the game, allowing the player to "fake out" the enemy by getting them to reveal their position (i.e a shield item can be used to absorb a hit).

The game backend is programmed entirelly in Java using netty.io, the game frontend is programmed in Java/Kotlin using LibGDX (I may end up switching to Unity or Unreal engine however). The server handles all the game logic and is capable of running many game instances at once. 

The current goal is to get a public beta out to the general public, while the game is playable, it is very buggy. 

A general overview in video format of Project Ghost can be found [here](https://www.youtube.com/watch?v=o3mM3TjHs9E) 

## Contributing

Contributions are always welcome! Please read the [CONTRIBUTE.md](#) file before submitting your pull request

## Testing

There is currently no central server hosting the game, so testing the game involves self hosting your own test server. Check out [the wiki](#) for instructions on how to setup a test server.

Once a test server is setup, you can pass these command-line arguments to the game client to connect to it

```
java -jar client.jar <ip:port> --test --debug
```

The `--test` field tells the client this is a *TEST SERVER* and will not attempt to authenticate. The `--debug` field is optional and simply logs debug info to the console.

## Compiling

This repo uses gradle to build and test.

### CLI

Building:

`gradle`

Starting a game server:

`gradle :server:gameserver:run`

Starting a matchmaking server

`gradle :server:matchmaking:run`

### IntelliJ

To import the repo, simply select the `build.gradle` as the file to import. This will import all relavent projects into IntelliJ.

### Eclipse

_Coming Soon_


## Repo Structure

* [Game Client](https://github.com/hypereddie/Ghost-Shadow-Warrior-Phantom-Assault-DX/tree/master/client)
* [Server](https://github.com/hypereddie/Ghost-Shadow-Warrior-Phantom-Assault-DX/tree/master/server)

## Protocol Specification

* [TCP/UDP Protocol](https://docs.google.com/spreadsheets/d/1Iphm_H0fq9s0AwXyqNvOabJbWijLylbJEGUvfGU65fo/edit?usp=sharing)
* Login Server API: _Proper documentation coming soon_
* Matchmaking Server API: _Proper documentation coming soon_
