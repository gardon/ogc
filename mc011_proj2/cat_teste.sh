#!/bin/sh

for i in `find testes/ -type f|grep out|sort`;do echo $i; cat $i;done
