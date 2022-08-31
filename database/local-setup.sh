#!/usr/bin/env zsh

DATABASE_NAME="swodlr"

psql postgres $USER \
    -a \
    -v ON_ERROR_STOP=1 \
    -c "DROP DATABASE IF EXISTS ${DATABASE_NAME};" \
    -c "CREATE DATABASE ${DATABASE_NAME};" \
    -c "\connect ${DATABASE_NAME}" \
    -f schema.sql \
    -f local_data.sql
