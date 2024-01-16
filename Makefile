MACOS_VER_NUM			:= $(shell bash -c 'cat <(sw_vers -productVersion) <(xcrun --show-sdk-version) | sort -V | head -1')
MACOS_VER_MAJOR			:= $(shell echo $(MACOS_VER_NUM) | cut -f1 -d.)
MACOS_VER_MINOR			:= $(shell echo $(MACOS_VER_NUM) | cut -f2 -d.)
export MACOSX_DEPLOYMENT_TARGET := $(MACOS_VER_MAJOR).$(MACOS_VER_MINOR)

.PHONY: test build class dylib library run clean jar package debug release install

test:
	mvn test

build class dylib library:
	mvn compile

run:
	mvn -q exec:java -Dexec.args="$$$$" -Djavah_cli_args=""

clean:
	mvn clean
	@rm -f pom.xml.* release.properties

jar package:
	mvn package

debug:
	open https://www.owsiak.org/jni-debugging-extreme-way/

release:
	mvn release:clean release:prepare
	mvn release:perform

install:
	mvn install

ghp:
	sed -e 's/artifactId>Getargv</artifactId>getargv</g' -i '' pom.xml
	sed -e 's/Getargv/getargv/g' -i '' src/main/java/cam/narzt/getargv/Getargv.java src/main/java/cam/narzt/getargv/Main.java src/main/native/cam_narzt_getargv_Getargv.c src/test/java/cam/narzt/getargv/GetargvParameterizedTest.java src/test/java/cam/narzt/getargv/GetargvTest.java
	mv src/main/java/cam/narzt/getargv/{G,g}etargv.java
	mv src/main/native/cam_narzt_getargv_{G,g}etargv.c
	mv src/test/java/cam/narzt/getargv/{G,g}etargvParameterizedTest.java
	mv src/test/java/cam/narzt/getargv/{G,g}etargvTest.java
