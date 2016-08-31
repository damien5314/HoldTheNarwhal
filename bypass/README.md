# Building Bypass (OSX) #

## Install Boost ##
http://www.boost.org/doc/libs/1_61_0/more/getting_started/unix-variants.html
Download Boost archive boost_1_61_0.tar.bz2

`cd /boost/installation/directory`
`tar --bzip2 -xf /path/to/boost_1_61_0.tar.bz2`

`cd /boost/parent/directory`

`./bootstrap.sh`
`./b2`
`./b2 install`

## Build Bypass ##

`git clone https://github.com/Uncodin/bypass.git`
`cd ./platform/android/library`
`ndk-build`
