# !/bin/bash

SIGAR=/Users/christoph/Downloads/sensor-sigar-1.5-sources/hyperic-sigar-1.6.5/sigar-bin/lib
OFFICE=/Applications/LibreOffice.app/Contents/

mvn -Djava.library.path=$SIGAR -Doffice.home=$OFFICE -DskipTests clean install assembly:single
