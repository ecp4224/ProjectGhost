# Project Ghost Server
A server implementation of the game 

## Folder Structure

### API
This is the API for the server and contains common objects, utilities, and APIs used by the other server projects. The API is required by all
other server projects

### Gameserver
The Gameserver hosts matches created by the Matchmaking server. The Gameserver can host multiple games at once and multiple different kinds of games. To run a Gameserver, it must be connected to a Matchmaking server.

### Matchmaking
The Matchmaking server matches players against each other for multiple different queues. It also handles rank calculations, stores match timelines, and stores ranks on a local Mongodb server.

### TestServer
The TestServer is suppose to mimic the Gameserver and Matchmaking server in a single jar file and is used to do quick tests for new features in the API. The TestServer will soon be deprecated in favor of creating local Gameservers

## Protocol Specification

* [TCP/UDP Protocol](https://docs.google.com/spreadsheets/d/1Iphm_H0fq9s0AwXyqNvOabJbWijLylbJEGUvfGU65fo/edit?usp=sharing)

## Installation

All projects in this folder use maven, so importing the pom.xml file in the `server` folder will import all the other projects and dependencies

## Compiling

All projects in this folder use maven, so simply run

```
mvn clean install
```

in the `server` folder
