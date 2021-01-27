#! /bin/sh

if [ -z "$GRAALVM_HOME" ]; then
	GRAALVM_HOME=$HOME/tools/graalvm
fi

if [ "$1" = "-native" ]; then
	$GRAALVM_HOME/bin/native-image -cp bin:res:../third-party/org.eclipse.swt/swt.jar:skija.jar com.spket.skiawt.Main skiawt
else
	$GRAALVM_HOME/bin/javac -cp ../third-party/org.eclipse.swt/swt.jar:skija.jar -d bin -sourcepath src src/com/spket/skiawt/Main.java
fi
