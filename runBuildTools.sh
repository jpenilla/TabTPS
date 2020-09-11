#!/bin/sh
mkdir BuildTools
cd BuildTools
rm ./BuildTools.jar
wget -O BuildTools.jar https://hub.spigotmc.org/jenkins/job/BuildTools/lastSuccessfulBuild/artifact/target/BuildTools.jar
mkdir 18; mkdir 19; mkdir 110; mkdir 111; mkdir 112; mkdir 113; mkdir 114; mkdir 115; mkdir 116
cd 18; java -jar ../BuildTools.jar --compile-if-changed --rev 1.8.8
cd ../19; java -jar ../BuildTools.jar --compile-if-changed --rev 1.9.4
cd ../110; java -jar ../BuildTools.jar --compile-if-changed --rev 1.10.2
cd ../111; java -jar ../BuildTools.jar --compile-if-changed --rev 1.11.2
cd ../112; java -jar ../BuildTools.jar --compile-if-changed --rev 1.12.2
cd ../113; java -jar ../BuildTools.jar --compile-if-changed --rev 1.13.2
cd ../114; java -jar ../BuildTools.jar --compile-if-changed --rev 1.14.4
cd ../115; java -jar ../BuildTools.jar --compile-if-changed --rev 1.15.2
cd ../116; java -jar ../BuildTools.jar --compile-if-changed --rev 1.16.3