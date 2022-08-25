#!/usr/bin/env bash

# https://aws.amazon.com/premiumsupport/knowledge-center/ec2-linux-log-user-data/
exec > >(tee /var/log/user-data.log|logger -t user-data -s 2>/dev/console) 2>&1

set -exo pipefail
source /root/.env

# Schema bootstrap
psql postgresql://$ENDPOINT/postgres -U $ADMIN_USERNAME -v ON_ERROR_STOP=1 -f /root/bootstrap_db.sql

# App user/auth bootstrap
psql postgresql://$ENDPOINT/swodlr -U $ADMIN_USERNAME -v ON_ERROR_STOP=1 -f /root/bootstrap_user.sql

shutdown
