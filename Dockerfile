FROM java
COPY backend/server/build/install/server  /opt/
WORKDIR /opt/
EXPOSE 4567
CMD ["bin/server"]
