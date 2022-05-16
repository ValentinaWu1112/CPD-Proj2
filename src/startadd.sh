#!/usr/bin/env python3

import sys
import os

os.system("sudo ifconfig lo0 alias "+sys.argv[1]+" up");
