#!/bin/sh
for i in `find testes/ -type f|grep -v svn|sort`;do (echo $i;java main.Main < $i;echo) &>$i.out;done
