# To install: pip install tavily-python
#hym00@tempumail.cc
import datetime
import json

from tavily import TavilyClient
client = TavilyClient("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")
response = client.search(
    query="crawl  job portals and find jobs in germany \nwith Keywords \nTest Automation \nSelenium \nPlaywright\nAppium\nCypress \nTestNG / JUnit\nCucumber / BDD / Gherkin\n",
    search_depth="advanced",
    max_results=50,
    time_range="day"
)

if __name__=='__main__':
    #print(f"\n=== Test Automation Jobs in Germany – {datetime.now():%Y-%m-%d %H:%M} ===\n")

    jobs = []
    for r in response.get("results", []):
        title = r.get("title", "").lower()
        if any(w in title for w in ["job", "stelle", "hiring", "position", "qa", "test", "automation", "(m/w/d)"]):
            jobs.append(r)
            print(f"{r['title']}")
            print(f"{r['url']}")
            print(f"Score: {r.get('score', '–'):.2f}")
            print(f"{r.get('content', '')[:180]}...\n")

    if not jobs:
        print("No clear job matches in last 24h → try time_range='week' or check LinkedIn/StepStone directly.")

    # Optional: save
    with open("jobs.json", "w", encoding="utf-8") as f:
        json.dump(jobs, f, indent=2, ensure_ascii=False)





