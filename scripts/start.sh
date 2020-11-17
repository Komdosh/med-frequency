#!/bin/bash -

export ACCEPTED_HOSTS="['127.0.0.1', '192.168.25.225']"

"$METAMAP_PATH"/bin/wsdserverctl start
"$METAMAP_PATH"/bin/skrmedpostctl start
"$METAMAP_PATH"/mmserver
