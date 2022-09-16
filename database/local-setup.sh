#!/usr/bin/env zsh

DATABASE_NAME="swodlr"
SCRIPT_DIR=$(dirname "$0")

psql postgres $USER \
    -a \
    -v ON_ERROR_STOP=1 \
    -c "DROP DATABASE IF EXISTS ${DATABASE_NAME};" \
    -c "CREATE DATABASE ${DATABASE_NAME};" \
    -c "\connect ${DATABASE_NAME}" \
    -f "$SCRIPT_DIR/schema.sql" \
    -f "$SCRIPT_DIR/local_data.sql"
