cmake_minimum_required(VERSION 3.4.1)

set(CMAKE_VERBOSE_MAKEFILE on)

set(CMAKE_CXX_FLAGS -std=c++11)


add_library(camera
    SHARED
    camera.cpp)

target_link_libraries(camera log GLESv3)