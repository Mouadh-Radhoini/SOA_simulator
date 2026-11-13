#!/bin/sh
set -eu
cat > /usr/share/nginx/html/config.js <<EOF
window.SOA_WS_CONFIG = {
  username: "${DASHBOARD_WS_USERNAME:-}",
  password: "${DASHBOARD_WS_PASSWORD:-}",
  apiUsername: "${DASHBOARD_API_USERNAME:-}",
  apiPassword: "${DASHBOARD_API_PASSWORD:-}"
};
EOF
