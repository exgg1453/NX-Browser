import json
import os
import urllib.request

text = ""
for name in ("build-debug.log", "build-release.log"):
    if os.path.exists(name):
        with open(name, errors="replace") as handle:
            text += handle.read()

lines = text.splitlines()
picked = []
capture = 0
for line in lines:
    stripped = line.strip()
    if line.startswith("e: ") or stripped.startswith("ERROR:") or stripped.startswith("Caused by:"):
        picked.append(line)
    elif "FAILURE: Build failed" in line or stripped.startswith("* What went wrong"):
        capture = 40
        picked.append(line)
    elif capture > 0:
        picked.append(line)
        capture -= 1

if not picked:
    picked = lines[-250:]

body = "\n".join(picked[:500])
body = body.replace("/home/runner/work/NX-Browser/NX-Browser/", "")

payload = json.dumps({
    "title": "Build failure " + os.environ["GITHUB_RUN_ID"],
    "body": "```\n" + body[:60000] + "\n```",
}).encode()

request = urllib.request.Request(
    "https://api.github.com/repos/" + os.environ["GITHUB_REPOSITORY"] + "/issues",
    data=payload,
    headers={
        "Authorization": "Bearer " + os.environ["GH_TOKEN"],
        "Accept": "application/vnd.github+json",
        "Content-Type": "application/json",
    },
)

with urllib.request.urlopen(request) as response:
    print(response.status)
