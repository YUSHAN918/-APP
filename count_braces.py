with open('app/src/main/java/com/example/ui/MaterialStudyScreen.kt', 'r') as f:
    lines = f.readlines()

depth = 0
for i, line in enumerate(lines):
    depth += line.count('{')
    depth -= line.count('}')
    if depth < 0:
        print(f"Negative depth at line {i+1}: {line.strip()}")
    if i+1 in [636, 829, 831, 958, 1239, 1343]:
        print(f"Line {i+1} depth: {depth}")
print(f"Final depth: {depth}")
