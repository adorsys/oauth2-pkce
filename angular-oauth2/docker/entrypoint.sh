#!/bin/sh -e

sed -i -e \
  's#___OAUT2_APP_BACKEND_URL___#'"$BACKEND_URL"'#g' \
  /usr/share/nginx/html/main.*.bundle.js

exec "$@"
