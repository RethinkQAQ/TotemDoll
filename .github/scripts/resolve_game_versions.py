"""Resolve Minecraft compatibility ranges into exact supported game versions."""

import re


def _family(version: str) -> tuple[int, ...]:
    parts = tuple(int(part) for part in version.split(".") if part.isdigit())
    if len(parts) < 2:
        raise ValueError(f"Invalid Minecraft version: {version}")
    return (parts[0], parts[1])


def _compare(left: str, right: str) -> int:
    a = tuple(int(part) for part in left.split(".") if part.isdigit())
    b = tuple(int(part) for part in right.split(".") if part.isdigit())
    width = max(len(a), len(b))
    a += (0,) * (width - len(a))
    b += (0,) * (width - len(b))
    return (a > b) - (a < b)


def _constraints(expression: str) -> list[tuple[str, str]]:
    expression = expression.strip()
    bracket = re.fullmatch(r"([[(])\s*([^,]+)\s*,\s*([^\])]+)\s*([)])", expression)
    if bracket:
        return [(">=" if bracket.group(1) == "[" else ">", bracket.group(2).strip()),
                ("<=" if bracket.group(4) == "]" else "<", bracket.group(3).strip())]
    tokens = re.findall(r"(>=|<=|>|<|=)?\s*(\d+(?:\.\d+)+)", expression)
    if not tokens:
        raise ValueError(f"Unsupported Minecraft version range: {expression}")
    return [(operator or "=", version) for operator, version in tokens]


def resolve_versions(expression: str, candidates: list[str], target: str) -> list[str]:
    constraints = _constraints(expression)
    families = {_family(version) for _, version in constraints}
    if len(families) != 1 or _family(target) != next(iter(families)):
        raise ValueError(f"Cross-family or invalid Minecraft range: {expression}")
    result = []
    def matches(candidate: str, operator: str, version: str) -> bool:
        comparison = _compare(candidate, version)
        return {
            ">": comparison > 0,
            ">=": comparison >= 0,
            "=": comparison == 0,
            "<": comparison < 0,
            "<=": comparison <= 0,
        }[operator]

    for candidate in candidates:
        if _family(candidate) != _family(target):
            continue
        if all(matches(candidate, operator, version)
               for operator, version in constraints):
            result.append(candidate)
    if target not in result:
        raise ValueError(f"Target {target} is not included in range {expression}")
    return result
