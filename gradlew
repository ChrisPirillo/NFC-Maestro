#!/bin/sh
APP_HOME=$( cd "${0%[/\\]*}" && pwd -P )
CLASSPATH=$APP_HOME/gradle/wrapper/gradle-wrapper.jar
if [ ! -f "$CLASSPATH" ]; then
    mkdir -p "$APP_HOME/gradle/wrapper"
    curl -sL -o "$CLASSPATH" "https://raw.githubusercontent.com/gradle/gradle/v8.11.1/gradle/wrapper/gradle-wrapper.jar" 2>/dev/null || true
fi
if [ -n "$JAVA_HOME" ] ; then JAVACMD=$JAVA_HOME/bin/java; else JAVACMD=java; fi
exec "$JAVACMD" $JAVA_OPTS $GRADLE_OPTS -Dorg.gradle.appname="${0##*/}" -classpath "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"
