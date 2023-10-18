#!/bin/bash

javac App.java

java -agentpath:../liboiprofiler.so App
