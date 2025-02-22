# -*- coding: utf-8 -*-
# This example shows how new databases can be created.
#
# Documentation: http://docs.basex.org/wiki/Clients
#
# (C) BaseX Team 2005-12, BSD License

import BaseXClient

# create session
session = BaseXClient.Session('127.0.0.1', 1984, 'admin', 'admin')

try:
    # create new database
    session.create("database", "<x>Hello World!</x>")
    print(session.info())

    # run query on database
    print("\n" + session.execute("xquery doc('database')"))

    # drop database
    session.execute("drop db database")
    print(session.info())

finally:
    # close session
    if session:
        session.close()