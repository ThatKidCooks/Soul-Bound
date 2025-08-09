#!/bin/bash

# Get the JAR file path from command line or use default
JAR_FILE=${1:-"build/libs/SoulBound-1.0-SNAPSHOT.jar"}

echo "Checking contents of $JAR_FILE..."

# Get list of all packages (directories) in the JAR
PACKAGES=$(jar -tf "$JAR_FILE" | grep -v "/$" | cut -d/ -f1 | sort | uniq)

# Check for third-party packages
THIRD_PARTY=$(echo "$PACKAGES" | grep -v "site" | grep -v "plugin.yml")

if [ -z "$THIRD_PARTY" ]; then
    echo "✅ SUCCESS: JAR contains only your own packages!"
else
    echo "❌ ERROR: JAR contains third-party packages:"
    echo "$THIRD_PARTY"
    echo ""
    echo "Detailed listing of non-site packages:"
    jar -tf "$JAR_FILE" | grep -v "site/" | grep -v "plugin.yml"
fi

echo ""
echo "Your packages:"
jar -tf "$JAR_FILE" | grep "site/" | head -n 10
if [ $(jar -tf "$JAR_FILE" | grep "site/" | wc -l) -gt 10 ]; then
    echo "... and more"
fi
