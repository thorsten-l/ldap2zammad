#!/bin/sh

mvn -DskipTests clean package spring-boot:build-image
