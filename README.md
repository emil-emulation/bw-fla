[![Build Status](https://travis-ci.org/eaas-framework/bw-fla.svg?branch=master)](https://travis-ci.org/eaas-framework/bw-fla)

## Prerequisites and Preparation
+ Java 7
+ Apache Maven > 3.x
+ Apache Ant > 1.8x

Clone the repository
```
git clone https://github.com/eaas-framework/bw-fla.git
```
Create and edit (if required) the `build.properties` file in `src/root`, e.g.
```
cp ${BW-FLA}/src/root/build.properties{.template,}
```

## Build 
```
cd ${BW-FLA}/src/root
mvn install
```

NOTE: First usage of any maven plugin which is inside goals like "install" or "clean" 
will trigger a download of plugins, which will be placed under "$HOME/.m2". 
Subsequent usage of maven will not trigger any downloads.


## Server Start
```
cd ${BW-FLA}/src/root
ant start        // choose this to run on a "localhost" 
ant start:public // choose this to run on an IP defined in "${BW-FLA}/src/root/build.properties" 
```

## Runtime Configuration
Most modules require a runtime configuration in `~/.bwFLA`. In the src directories of each module there is 
an template to be edited and copied to `~/.bwFLA` (without the template suffix!).

