import requests
import pyrebase
from datetime import datetime
import time

# Bingqi Xia
# 22300549

def initialize_firebase(config_file):
    with open(config_file) as f:
        config = f.readlines()

    firebase = pyrebase.initialize_app(config)
    return firebase

def request_noise_data():
    # current noise data for monitors
    url = "https://dublincityairandnoise.ie/assets/php/get-monitors.php"
    # url = "https://dublincityairandnoise.ie/readings?monitor=DCC-008&dateFrom=06+Nov+2022&dateTo=06+Nov+2022"
    my_referer = "https://dublincityairandnoise.ie/readings?monitor=DCC-008&dateFrom=06+Nov+2022&dateTo=06+Nov+2022"
    user_agent = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/107.0.0.0 Safari/537.36"
    accept = "application/json, text/javascript, */*; q=0.01"

    headers={'referer': my_referer, "accept": accept, "user_agent": user_agent}
   
    r = requests.get(url, headers)
    noise_data = []
    if r.status_code == 200:
        ret = r.json()
        noise_data = [x for x in ret if x['monitor_type']['category']=='noise']
    return noise_data

def push_noise_data():
    noise_data = request_noise_data()
    firebase = initialize_firebase()

    db = firebase.database()

    for data in noise_data:
        print(data)
        results = db.child("noise_open_data") \
                    .child(data['location']) \
                    .child(data['latest_reading']['recorded_at']).set(data)


if __name__ == '__main__':
    push_noise_data()
    # while (datetime.now() <= datetime(2022, 11, 6, 17, 30)):
    #     print(datetime.now())
    #     push_noise_data()
    #     time.sleep(300)