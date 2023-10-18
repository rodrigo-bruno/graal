#!/bin/bash

mvn clean package
java -cp target/jol-samples.jar pt.ulisboa.tecnico.jol.maps.JOL_Maps
