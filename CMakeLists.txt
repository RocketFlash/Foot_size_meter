cmake_minimum_required(VERSION 3.6)
project(TryFit)

find_package( OpenCV REQUIRED )
set(CMAKE_CXX_STANDARD 11)

set(SOURCE_FILES mainNew.cpp guidedfilter.cpp geomTools.cpp parameters.cpp)
add_executable(TryFit ${SOURCE_FILES})
target_link_libraries( TryFit ${OpenCV_LIBS} )