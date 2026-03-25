#!/bin/bash
echo "Setting up Gradle wrapper..."
if [ ! -f gradle/wrapper/gradle-wrapper.jar ]; then
    echo "Gradle wrapper JAR not found. Generating it..."
    # Generate wrapper using gradle if available
    if command -v gradle &> /dev/null; then
        gradle wrapper --gradle-version 7.6.1
    else
        echo "Please install Gradle first, then run: gradle wrapper --gradle-version 7.6.1"
        echo "Or download gradle-wrapper.jar manually from a working Gradle project."
        echo ""
        echo "Quick fix: Run the following command if you have any Gradle installation:"
        echo "  gradle wrapper --gradle-version 7.6.1"
        exit 1
    fi
fi
echo "Setup complete! Run: ./gradlew bootRun"
