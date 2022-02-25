#!/usr/bin/env bash

WORD=$1

curl \
  -X 'GET' \
  -H "accept: */*" \
  http://localhost:8080/word/$WORD