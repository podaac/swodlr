#!/usr/bin/env bash
set -eo pipefail

if [ ! $# -eq 1 ]
then
    echo "destroy.sh venue"
    exit
fi

VENUE=$1
source "environments/$VENUE.env"

TF_IN_AUTOMATION=true  # https://www.terraform.io/cli/config/environment-variables#tf_in_automation
TF_INPUT=false  # https://www.terraform.io/cli/config/environment-variables#tf_input

terraform init -reconfigure -backend-config="bucket=$BUCKET" -backend-config="region=$REGION"
terraform destroy -var-file "environments/$VENUE.tfvars"
