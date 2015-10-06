#!/bin/bash

(cd frontend && npm run build)
mkdir -p backend/server/src/main/resources/public
cp -r frontend/index.html frontend/static backend/server/src/main/resources/public
(cd backend/server && gradle installDist)
# docker build -t tongcx/schedule-demo .
