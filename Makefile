MACOS_VER_NUM			:= $(shell bash -c 'cat <(sw_vers -productVersion) <(xcrun --show-sdk-version) | sort -V | head -1')
MACOS_VER_MAJOR			:= $(shell echo $(MACOS_VER_NUM) | cut -f1 -d.)
MACOS_VER_MINOR			:= $(shell echo $(MACOS_VER_NUM) | cut -f2 -d.)
export MACOSX_DEPLOYMENT_TARGET := $(MACOS_VER_MAJOR).$(MACOS_VER_MINOR)

.PHONY: build run header class library clean test dylib jar package debug

test:
	mvn test

build class dylib library:
	mvn compile

run:
	mvn -q exec:java -Dexec.args="$$$$" -Djavah_cli_args=""

clean:
	mvn clean

jar package:
	mvn package

debug:
	open https://www.owsiak.org/jni-debugging-extreme-way/
