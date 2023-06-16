FROM python:3.9.6
WORKDIR /app
COPY requirements.txt requirements.txt
RUN pip3 install -r requirements.txt
COPY . .

EXPOSE 5000
ENV PORT 5000

CMD exec gunicorn --bind :$PORT main:app --workers 1 --threads 1
