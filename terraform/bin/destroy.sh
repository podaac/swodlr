#!/usr/bin/env bash
set -eo pipefail

source "$(dirname $BASH_SOURCE)/config.sh"
terraform apply
