"""
Parses Maven Surefire XML reports and writes SUMMARY and FAILURES
as GitHub Actions multiline step outputs to stdout.
"""
import os
import sys
import xml.etree.ElementTree as ET

reports_dir = "target/surefire-reports"
total = 0
failed = 0
skipped = 0
failed_tests = []

if os.path.isdir(reports_dir):
    for fname in sorted(os.listdir(reports_dir)):
        if not fname.startswith("TEST-") or not fname.endswith(".xml"):
            continue
        fpath = os.path.join(reports_dir, fname)
        try:
            tree = ET.parse(fpath)
            root = tree.getroot()
            suite = root if root.tag == "testsuite" else root.find("testsuite")
            if suite is None:
                suite = root
            total   += int(suite.get("tests",    0) or 0)
            failed  += int(suite.get("failures", 0) or 0) + int(suite.get("errors", 0) or 0)
            skipped += int(suite.get("skipped",  0) or 0)
            for tc in tree.findall(".//testcase"):
                if tc.find("failure") is not None or tc.find("error") is not None:
                    classname = tc.get("classname", "")
                    name = tc.get("name", "")
                    failed_tests.append(f"{classname}.{name}")
        except Exception as exc:
            print(f"WARN: could not parse {fpath}: {exc}", file=sys.stderr)

passed = total - failed - skipped

summary_lines = [
    f"* Total: {total}",
    f"* Passed: {passed}",
    f"* Failed: {failed}",
    f"* Skipped: {skipped}",
]
summary = "\n".join(summary_lines)

if failed_tests:
    failures = "\n".join(f"* {t}" for t in failed_tests)
else:
    failures = "* (no test names found — check run logs)"

# Write as GitHub Actions multiline outputs
print(f"SUMMARY<<ENDOFVAR\n{summary}\nENDOFVAR")
print(f"FAILURES<<ENDOFVAR\n{failures}\nENDOFVAR")
