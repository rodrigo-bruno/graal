#!/bin/bash

export JAVA_HOME=/home/rbruno/git/labs-openjdk-11/java_home/

g++ -c \
    -fPIC \
    -g \
    -I"$JAVA_HOME/include" \
    -I"$JAVA_HOME/include/linux" \
    -o library.o library.cpp

g++ -c \
    -fPIC \
    -g \
    -I"$JAVA_HOME/include" \
    -I"$JAVA_HOME/include/linux" \
    -o callbacks.o callbacks.cpp

g++ -fPIC \
    -shared \
    -g \
    -o liboiprofiler.so \
    library.o callbacks.o
