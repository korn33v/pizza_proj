#!/bin/sh
# ----------------------------------------------------------------------------
# Apache Maven Wrapper startup script
# ----------------------------------------------------------------------------

# [SEMINAR-19] Infra tooling: Maven Wrapper делает сборку воспроизводимой в команде/CI.
# Почему это важно в учебном проекте: чтобы архитектуру обсуждать, а не "у меня не собирается".

set -euf

# Resolve project base directory (directory containing this script)
PRG="$0"
while [ -h "$PRG" ]; do
  ls=`ls -ld "$PRG"`
  link=`expr "$ls" : '.*-> \(.*\)$'`
  if expr "$link" : '/.*' > /dev/null; then
    PRG="$link"
  else
    PRG=`dirname "$PRG"`"/$link"
  fi
done

BASEDIR=`cd "$(dirname "$PRG")" && pwd`

WRAPPER_LAUNCHER=org.apache.maven.wrapper.MavenWrapperMain
WRAPPER_DIR="$BASEDIR/.mvn/wrapper"
WRAPPER_JAR="$WRAPPER_DIR/maven-wrapper.jar"
WRAPPER_PROPERTIES="$WRAPPER_DIR/maven-wrapper.properties"

# Download maven-wrapper.jar if not present
if [ ! -f "$WRAPPER_JAR" ]; then
  if [ -z "${MVNW_VERBOSE:-}" ]; then
    QUIET="-q"
  else
    QUIET=""
  fi
  WRAPPER_URL=`grep -E "^wrapperUrl=" "$WRAPPER_PROPERTIES" | cut -d'=' -f2-`
  if [ -z "$WRAPPER_URL" ]; then
    WRAPPER_URL="https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.2.0/maven-wrapper-3.2.0.jar"
  fi

  mkdir -p "$WRAPPER_DIR"

  if command -v curl >/dev/null 2>&1; then
    curl $QUIET -o "$WRAPPER_JAR" "$WRAPPER_URL"
  elif command -v wget >/dev/null 2>&1; then
    wget $QUIET -O "$WRAPPER_JAR" "$WRAPPER_URL"
  else
    echo "ERROR: Need curl or wget to download $WRAPPER_URL"
    exit 1
  fi
fi

# Java executable
if [ -n "${JAVA_HOME:-}" ]; then
  JAVA_EXE="$JAVA_HOME/bin/java"
else
  JAVA_EXE="java"
fi

exec "$JAVA_EXE"   ${MAVEN_OPTS:-}   -classpath "$WRAPPER_JAR"   "-Dmaven.multiModuleProjectDirectory=$BASEDIR"   $WRAPPER_LAUNCHER "$@"
