#!/bin/bash

#
# Copyright 2023 Thorsten Ludewig (t.ludewig@gmail.com).
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

export JAVA_HOME="/opt/graalvm/graalvm-jdk-17"
export PATH=$JAVA_HOME/bin:$PATH

echo JAVA_HOME=$JAVA_HOME
# for native javascript support in graalvm 
$JAVA_HOME/bin/gu install js llvm-toolchain
sleep 3

./mvnw -Pnative clean native:compile -DskipTests=true
