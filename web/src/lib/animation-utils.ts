export interface BoneFrame {
  time: number;
  value: [number, number, number];
  interpolation?: string;
}

export type RawBoneFrames = BoneFrame[] | BoneFrame | undefined;

export function normalizeBoneFrames(frames: RawBoneFrames): BoneFrame[] {
  if (!frames) return [];
  return (Array.isArray(frames) ? frames : [frames])
    .filter((frame) => frame && Array.isArray(frame.value) && frame.value.length >= 3)
    .map((frame) => ({
      time: Number(frame.time) || 0,
      value: [Number(frame.value[0]) || 0, Number(frame.value[1]) || 0, Number(frame.value[2]) || 0] as [number, number, number],
      interpolation: frame.interpolation ?? "linear"
    }))
    .sort((a, b) => a.time - b.time);
}

export function interpolateBoneFrames(
  frames: RawBoneFrames,
  time: number,
  fallback: [number, number, number]
): [number, number, number] {
  const ordered = normalizeBoneFrames(frames);
  if (!ordered.length) return fallback;
  const right = ordered.findIndex((frame) => frame.time >= time);
  if (right <= 0) return ordered[0].value;
  if (right < 0) return ordered.at(-1)!.value;
  const leftFrame = ordered[right - 1];
  const rightFrame = ordered[right];
  if (leftFrame.interpolation === "step") return leftFrame.value;
  const amount = (time - leftFrame.time) / Math.max(0.001, rightFrame.time - leftFrame.time);
  // catmullrom is intentionally mapped to a stable linear fallback until a
  // four-point sampler is needed by the model format.
  return leftFrame.value.map((value, index) =>
    value + (rightFrame.value[index] - value) * amount
  ) as [number, number, number];
}

export function resolveAnimationName(
  bindingName: string,
  bindingAnimation: string | undefined,
  available: Record<string, unknown>
): string | undefined {
  const candidates = [bindingAnimation, bindingName].filter(Boolean) as string[];
  return candidates.find((name) => name in available);
}
