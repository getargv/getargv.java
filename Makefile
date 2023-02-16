export JAVA_HOME=$(shell /usr/libexec/java_home 2>/dev/null)

JAVAC := javac
CC := clang
NAMESPACE_SLASHES := cam/narzt/getargv/Getargv
NAMESPACE_DOTS := $(subst /,.,$(NAMESPACE_SLASHES))
NAMESPACE_UNDERSCORES := $(subst /,_,$(NAMESPACE_SLASHES))
INCLUDE_DIR := include
LIB_DIR := lib
OBJ_DIR := obj
CLASSPATH_DIR := classpath
CLASS := $(CLASSPATH_DIR)/$(NAMESPACE_SLASHES).class
LIBRARY := $(LIB_DIR)/lib$(NAMESPACE_UNDERSCORES).dylib
HEADER := $(INCLUDE_DIR)/$(NAMESPACE_UNDERSCORES).h

MACOS_VER_NUM			:= $(shell bash -c 'cat <(sw_vers -productVersion) <(xcrun --show-sdk-version) | sort -V | head -1')
MACOS_VER_MAJOR			:= $(shell echo $(MACOS_VER_NUM) | cut -f1 -d.)
MACOS_VER_MINOR			:= $(shell echo $(MACOS_VER_NUM) | cut -f2 -d.)
export MACOSX_DEPLOYMENT_TARGET := $(MACOS_VER_MAJOR).$(MACOS_VER_MINOR)

.PHONY: build run header class library clean
.PRECIOUS: $(OBJ_DIR)/%.o

$(LIB_DIR)/lib%.dylib: $(OBJ_DIR)/%.o $(LIB_DIR)
	$(CC) -dynamiclib -g -o $@ $< -lgetargv

$(OBJ_DIR)/%.o: src/%.c $(HEADER) $(OBJ_DIR)
	$(CC) -c -g -fPIC -I${JAVA_HOME}/include -I${JAVA_HOME}/include/darwin -I$(INCLUDE_DIR) $< -o $@

$(HEADER): ./$(NAMESPACE_SLASHES).java $(INCLUDE_DIR)
	$(JAVAC) -d $(CLASSPATH_DIR) -h $(INCLUDE_DIR) $<

$(CLASSPATH_DIR)/%.class: %.java $(CLASSPATH_DIR)
	$(JAVAC) -d $(CLASSPATH_DIR) $<

$(INCLUDE_DIR) $(LIB_DIR) $(OBJ_DIR) $(CLASSPATH_DIR):
	mkdir -p $@

build: $(LIBRARY)

run: $(LIBRARY) $(CLASS)
	java -cp $(CLASSPATH_DIR) -Djava.library.path=$(LIB_DIR) $(NAMESPACE_DOTS)

header: $(HEADER)

class: $(CLASS)

library: $(LIBRARY)

clean:
	rm -rf $(INCLUDE_DIR) $(LIB_DIR) $(OBJ_DIR) $(CLASSPATH_DIR)