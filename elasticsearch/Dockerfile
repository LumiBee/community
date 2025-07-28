FROM docker.elastic.co/elasticsearch/elasticsearch:8.18.4

COPY ./elasticsearch-analysis-ik-8.18.4.zip /tmp/analysis-ik.zip

RUN ./bin/elasticsearch-plugin install --batch file:///tmp/analysis-ik.zip