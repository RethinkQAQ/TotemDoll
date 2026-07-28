import { describe, expect, it } from "vitest";
import { interpolateBoneFrames, normalizeBoneFrames, resolveAnimationName } from "./animation-utils";

describe("animation normalization", () => {
  it("accepts a single keyframe object", () => {
    expect(normalizeBoneFrames({ time: 0, value: [1, 2, 3] })).toHaveLength(1);
  });
  it("resolves struggle binding to the actual animation id", () => {
    expect(resolveAnimationName("totem_struggle", "struggle", { struggle: {} })).toBe("struggle");
  });
  it("handles step and linear interpolation", () => {
    expect(interpolateBoneFrames([{ time: 0, value: [0, 0, 0], interpolation: "step" }, { time: 10, value: [10, 10, 10] }], 5, [0, 0, 0])).toEqual([0, 0, 0]);
    expect(interpolateBoneFrames([{ time: 0, value: [0, 0, 0] }, { time: 10, value: [10, 10, 10] }], 5, [0, 0, 0])).toEqual([5, 5, 5]);
  });
});
