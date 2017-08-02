#!/usr/bin/env bash
echo "Building ClaimChunk..."
cd ..
rm -r target
mvn clean package
cp target/*.jar run/plugins/claimchunk.jar
echo "Done!"
cd run
echo "Starting server..."
java -Xmx1G -jar spigot.jar
echo "Done!"
