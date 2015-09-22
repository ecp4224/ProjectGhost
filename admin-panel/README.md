Super Secret Game Launcher
==============
The game launcher

# Quick start

The only development dependency of this project is Node.js. So just make sure you have it installed.

1. Clone/download this repository.
2. Install dependencies with `npm install` (it will also download NW.js runtime).
3. Run `npm start` to launch the application.

# Structure of the project

There are two `package.json` files:  

#### 1. Development package.json
Placed in root directory. This file contains:
- Node modules used for development (They are not needed in real application, so why to pollute it with them?).
- Declaration for NW.js runtime. This is the most interesting part:
```
"config": {
  "nw": {
    "version": "0.12.0",
    "downloadUrls": {
      "osx": "http://dl.nwjs.io/v{{version}}/nwjs-v{{version}}-osx-x64.zip",
      "linux": "https://dl.nwjs.io/v{{version}}/nwjs-v{{version}}-linux-x64.tar.gz",
      "windows": "https://dl.nwjs.io/v{{version}}/nwjs-{{version}}-win-ia32.zip"
    }
  }
},
```
You declare here which version of NW.js you want to use and the URLs from where NW binaries should be downloaded.

#### 2. Application package.json
Placed in **app** directory. This is real manifest of your application, as specified by [NW wiki](https://github.com/nwjs/nw.js/wiki/Manifest-format). Declare your app dependencies there.

There is one extra field in this file: `prettyName`. Unlike the `name` field, which have to be file-path-freindly (no spaces and strange characters), `prettyName` could have any characters you like, and it's used as the app name displayed to the user.

### Project's folders

- `app` - code of your application goes here.
- `build` - in this folder lands built, runnable application.
- `nw` - downloaded NW.js binaries.
- `os` - application files specific for particular operating system.
- `releases` - ready to distribute installers will land here.
- `tasks` - build and development environment scripts.


# Development

#### Installation

```
npm install
```
It will also download NW runtime, and install dependencies for `package.json` inside `app` folder.

#### Starting the app

```
npm start
```

#### Module loader

How about splitting your JavaScript code into modules? This project uses [es6-module-transpiler](https://github.com/esnext/es6-module-transpiler) for that. It translates new ES6 syntax (which is very cool) into AMD (RequireJS) modules. The main advantage of this setup is that we can use ES6/RequireJS for modules authored by us, and at the same time have normal access to node's `require()` to obtain stuff from npm.
```javascript
// Browser modules are required through new ES6 syntax.
// It will be translated into AMD definition.
import foo1 from './foo';
// Node.js (npm) modules are required the same way as always.
var foo2 = require('foo');
```

#### Helper scripts

There are helper scripts in `app/vendor/nwbp` folder. Those are scripts with convenient hooks wou will need  anyway (like window size and position preservation). Just browse this folder to see what you get.

#### Unit tests

Comes with a preconfigured unit test runner ([jasmine](http://jasmine.github.io/2.0/introduction.html)). To run it go with standard:
```
npm test
```
You don't have to declare paths to spec files in any particular place. The runner will search throu the project for all `*.spec.js` files and include them automatically.


# Making a release

There are various icon and bitmap files in `os` directory. They are used in installers. Replace them with your own of the same size and file type (if bmp is used, it has to be bmp format).

To make a release use command:
```
npm run release
```
It will start the packaging process for operating system you are running this command on. Ready for distribution file will be outputted to `releases` directory.

You can create Windows installer only when running on Windows, the same is true for Linux and OSX. So to generate all three installers you need all three operating systems.


# Precautions for particular operating system

## Windows
As installer [NSIS](http://nsis.sourceforge.net/Main_Page) is used. You have to install it (version 3.0), and add NSIS folder to PATH in Environment Variables (so it is reachable to scripts in this project). You know, path should look something like `C:/Program Files (x86)/NSIS`.

## Linux
This project requires for node.js to be reachable under `node` name in command line. For example by default in Ubuntu it is `nodejs`, so you should manully add alias to `node`.

For now only deb packaging is supported. It should work on any Linux distribution from debian family (but was tested only on Ubuntu).

## OSX
This project uses [appdmg](https://github.com/LinusU/node-appdmg) for creating pretty dmg images. While installing this library it could ask you for some additional development libraries on what you have to agree.  
**BTW** installation of this library fails on other operating systems (Windows and Linux) when you type `npm install`. No worries, it's needed only on OSX.