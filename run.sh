#! /bin/sh

if [ -z "$JAVA_HOME" ]; then
  if [ -z "$GRAALVM_HOME" ]; then
    JAVA_HOME=$HOME/tools/graalvm
  else
    JAVA_HOME=$GRAALVM_HOME
  fi
fi

JAVA_OPTS=

for arg in "$@"; do
  if [ "$arg" = "-trace" ]; then
    rm -fr $HOME/.swt
    JAVA_OPTS="$JAVA_OPTS -agentlib:native-image-agent=config-output-dir=res/META-INF/native-image"
  elif [ "$arg" = "-debug" ]; then
    JAVA_OPTS="$JAVA_OPTS -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=8000"
  else
    JAVA_OPTS="$JAVA_OPTS $arg"
  fi
done

$JAVA_HOME/bin/java $JAVA_OPTS -Djava.library.path=. -cp bin:../third-party/org.eclipse.swt/swt.jar:skija.jar com.spket.skiawt.Main
