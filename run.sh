#!/bin/sh
#
#
#

DIRNAME=`dirname "$0"`

# Read an optional running configuration file
if [ "x$RUN_CONF" = "x" ]; then
    RUN_CONF="$DIRNAME/conf/run.conf"
fi
if [ -r "$RUN_CONF" ]; then
    . "$RUN_CONF"
fi

# Setup the JVM
if [ "x$JAVA" = "x" ]; then
    if [ "x$JAVA_HOME" != "x" ]; then
        JAVA="$JAVA_HOME/bin/java"
    else
        JAVA="java"
    fi
fi

JBOSS_CLUSER_HOME=`cd "$DIRNAME/."; pwd`

# Display our environment
echo ""
echo "========================================================================="
echo ""
echo "  JBoss Mod Cluster Proxy Bootstrap Environment"
echo ""
echo "  JBOSS_CLUSER_HOME: $JBOSS_CLUSER_HOME"
echo ""
echo "  JAVA: $JAVA"
echo ""
echo "  JAVA_OPTS: $JAVA_OPTS"
echo ""
echo "========================================================================="
echo ""

eval \"$JAVA\" $JAVA_OPTS \
         -jar target/jboss-mod-cluster-proxy.jar
         "$@"
      JBOSS_CLUSER_STATUS=$?

#java -server -Xms256m -Xmx2048m -XX:MaxPermSize=1024m -Djava.util.logging.config.file=conf/logging.properties -jar target/jboss-mod-cluster-proxy.jar