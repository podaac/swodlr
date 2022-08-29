#!/usr/bin/env bash
set -eo pipefail

if [ ! $# -eq 1 ]
then
    echo "destroy.sh venue"
    exit
fi

VENUE=$1
source "environments/$VENUE.env"

export TF_IN_AUTOMATION=true  # https://www.terraform.io/cli/config/environment-variables#tf_in_automation
export TF_INPUT=false  # https://www.terraform.io/cli/config/environment-variables#tf_input

export TF_VAR_region="$REGION"
export TF_VAR_stage="$VENUE"

terraform init -reconfigure -backend-config="bucket=$BUCKET" -backend-config="region=$REGION"
terraform destroy
