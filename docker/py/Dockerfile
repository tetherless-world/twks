FROM python:3.7-stretch

COPY /py /twks/py
WORKDIR /twks/py
RUN mv .cache /root/
RUN pip3 install -r requirements.txt
RUN python3 setup.py install
