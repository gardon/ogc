#!/bin/sh

for i in `find testes/ -type f|grep out|sort`;do rm $i;done
