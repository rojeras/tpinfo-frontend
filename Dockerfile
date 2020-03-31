FROM httpd:2.4-alpine

LABEL maintainer="SLL-IT suupport@sll.se"

#ENV VIRTUAL_HOST=tpinfo.se

#RUN apt-get update && apt-get install -y python

EXPOSE 80 443

#COPY ./src/hippo/all.min.js /usr/local/apache2/htdocs/hippo/
COPY ./build/libs/showcase-1.0.0-SNAPSHOT/* /usr/local/apache2/htdocs/hippo7/


# Then we need a way to redirect all calls:
# /var/www/html/tpdb/ --> background-containder:...tpdb/

#ADD apache-config.conf /etc/apache2/sites-enabled/000-default.conf
