#(C): Janne V. Kujala

# Listen on tcp port argv[1] and accept one connection, then
# start the program argv[2:] with stdin/out redirected to the tcp
# socket

import os
import sys
import socket

HOST = ''                 # Symbolic name meaning the local host
PORT = int(sys.argv[1])
s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
s.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
s.bind((HOST, PORT))
s.listen(1)
conn, addr = s.accept()
print >> sys.stderr, 'Connected by', addr
s.close()

if conn.fileno() != 0:
    os.dup2(conn.fileno(), 0)
    conn.close()
os.dup2(0, 1)

os.execlp(sys.argv[2], *sys.argv[2:])
#os.system(" ".join(sys.argv[2:]))
#conn.shutdown(2)

