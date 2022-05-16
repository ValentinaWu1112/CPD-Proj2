#!/bin/bash

read add
sudo ifconfig lo0 alias $add up
