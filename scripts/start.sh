#!/bin/bash -

export ACCEPTED_HOSTS="['127.0.0.1']"

"$MED_FREQUENCY_WORKDIR"/public_mm/bin/skrmedpostctl start
"$MED_FREQUENCY_WORKDIR"/public_mm/mmserver
