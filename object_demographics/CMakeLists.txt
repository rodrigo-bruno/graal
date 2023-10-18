cmake_minimum_required(VERSION 3.16)
project(object_demographics_profiler)

set(CMAKE_CXX_STANDARD 14)

add_library(object_demographics_profiler SHARED library.cpp library.h callbacks.cpp callbacks.h)
include_directories(/home/rbruno/git/labs-openjdk-11/java_home/include/)
include_directories(/home/rbruno/git/labs-openjdk-11/java_home/include/linux)
