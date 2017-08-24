cmake_minimum_required(VERSION 3.4.1)

set(CMAKE_VERBOSE_MAKEFILE on)

set(CMAKE_CXX_FLAGS -std=c++11)


add_library(copy
    SHARED
    copy.cpp)

target_link_libraries(copy log)