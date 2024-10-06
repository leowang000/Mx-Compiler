# Makefile for a Java project

# Variables
SRC_DIR := src
BIN_DIR := bin
LIB_DIR := /ulib
CLASSPATH := $(shell find $(LIB_DIR) -name '*.jar' | tr '\n' ':')$(BIN_DIR)
MAIN_CLASS := Compiler

# Find all Java files
JAVA_FILES := $(shell find $(SRC_DIR) -name '*.java')

# Find all class files
CLASSES := $(JAVA_FILES:$(SRC_DIR)/%.java=$(BIN_DIR)/%.class)

# Default target
all: build

# Build target
build: $(CLASSES)

$(BIN_DIR)/%.class: $(SRC_DIR)/%.java
	@mkdir -p $(BIN_DIR)
	@find $(SRC_DIR) -name '*.java' | xargs javac -d $(BIN_DIR) -cp $(CLASSPATH)

# Run target
run: build
	java -cp $(CLASSPATH) $(MAIN_CLASS) -output-builtin

# Clean build artifacts
clean:
	@rm -rf $(BIN_DIR)

# Phony targets
.PHONY: all build run clean