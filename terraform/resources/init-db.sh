#!/usr/bin/env bash

# https://aws.amazon.com/premiumsupport/knowledge-center/ec2-linux-log-user-data/
exec > >(tee /var/log/user-data.log|logger -t user-data -s 2>/dev/console) 2>&1

set -exo pipefail
source /root/.env

psql postgresql://$ENDPOINT/postgres \
    -U $ADMIN_USERNAME  \
    -v ON_ERROR_STOP=1  \
    -f /root/init.sql   \
    -f /root/schema.sql \
    -f /root/app_user.sql

shutdown
