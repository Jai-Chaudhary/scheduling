#!/bin/bash

(cd frontend && npm run build)
mkdir -p backend/server/src/main/resources/public/static
cp frontend/index.html backend/server/src/main/resources/public
cp frontend/static/bundle.js backend/server/src/main/resources/public/static
(cd backend/server && gradle installDist)
# docker build -t tongcx/schedule-demo .
