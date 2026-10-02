FROM jsreport/jsreport:latest AS jsreport

COPY export.jsrexport /opt/jsreport/config/export.jsrexport

RUN jsreport import /opt/jsreport/config/export.jsrexport

CMD [ "jsreport", "start" ]

EXPOSE 5488