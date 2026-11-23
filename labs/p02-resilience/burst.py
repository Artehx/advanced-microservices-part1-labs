import concurrent.futures
import threading
import time
import urllib.request
import urllib.error
import json
import sys
from collections import Counter

URL = "http://127.0.0.1:8080/api/orders/preview?sku=SKU-001&quantity=2"
barrier = threading.Barrier(9)
def request(i):
    barrier.wait(timeout=5)
    start = time.monotonic()
    try:
        with urllib.request.urlopen(URL, timeout=12) as resp:
            code, body = resp.status, resp.read().decode()
    except urllib.error.HTTPError as e:
        code, body = e.code, e.read().decode()
    return (i, code, round(time.monotonic()-start, 3), json.loads(body))
with concurrent.futures.ThreadPoolExecutor(max_workers=8) as pool:
    futures = [pool.submit(request, i) for i in range(8)]
    barrier.wait(timeout=5)
    time.sleep(0.2)
    start = time.monotonic()
    with urllib.request.urlopen("http://127.0.0.1:8080/api/orders/training/ping", timeout=5) as resp:
        ping = json.loads(resp.read().decode())
    print("Independent ping during burst:", ping, round(time.monotonic()-start, 3), "s")
    results = [f.result() for f in futures]
for i, code, elapsed, body in results:
    print(i, code, elapsed, body.get("code", "OK"))
counts = Counter(r[1] for r in results)
print("HTTP counts:", dict(counts))
if "--assert-bulkhead" in sys.argv:
    assert counts[200] >= 1 and counts[503] >= 1 and counts[200]+counts[503]==8, results
    assert all(body.get("code")=="BULKHEAD_FULL" for _,code,_,body in results if code==503), results
    assert ping["status"]=="ok", ping
