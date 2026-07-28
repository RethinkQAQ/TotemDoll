type Vec = [number, number, number];
type AnyRecord = Record<string, any>;

const vec = (value: any, fallback: Vec = [0, 0, 0]): Vec => Array.isArray(value) && value.length >= 3 ? [Number(value[0]), Number(value[1]), Number(value[2])] : fallback;
const frameValue = (point: AnyRecord): Vec => [Number(point?.x ?? 0), Number(point?.y ?? 0), Number(point?.z ?? 0)];

export function convertBbmodel(project: AnyRecord, animationProject?: AnyRecord): { geometry: string; animations: string } {
  const geometry = project.geometry ?? project;
  const animation = animationProject ?? project;
  const elements = geometry.elements ?? [];
  const index = new Map<string, AnyRecord>();
  const groups = geometry.groups ?? [];
  const indexGroups = (group: AnyRecord) => { if (group.name) index.set(group.name, group); for (const child of group.children ?? []) if (typeof child === "object") indexGroups(child); };
  for (const group of groups) indexGroups(group);
  const convertCube = (element: AnyRecord) => {
    const from = vec(element.from), to = vec(element.to);
    const faces: AnyRecord = {};
    for (const direction of ["down", "up", "north", "south", "west", "east"]) {
      const face = element.faces?.[direction];
      if (!Array.isArray(face?.uv)) continue;
      faces[direction] = { uv: face.uv.map(Number), rotation: Number(face.rotation ?? 0) };
    }
    return { origin: from, size: [to[0] - from[0], to[1] - from[1], to[2] - from[2]], uv: faces.north?.uv?.slice(0, 2) ?? [0, 0], faces };
  };
  const convertBone = (group: AnyRecord): AnyRecord => {
    const origin = vec(group.origin), pivot: Vec = [origin[0] + 8, origin[1], origin[2] + 8];
    const cubes: AnyRecord[] = [], children: AnyRecord[] = [];
    for (const child of group.children ?? []) {
      if (typeof child === "number") cubes.push(convertCube(elements[child]));
      else if (typeof child === "object") children.push(convertBone(child));
    }
    return { name: String(group.name ?? "root"), pivot, rotation: [0, 0, 0], cubes, children };
  };
  const root = groups[0] ? convertBone(groups[0]) : { name: "root", pivot: [0, 0, 0], rotation: [0, 0, 0], cubes: [], children: [] };
  const geometryOutput = { format: 1, texture_width: 64, texture_height: 64, display: geometry.display ?? {}, bones: [root] };
  const animations: AnyRecord = {};
  for (const item of animation.animations ?? []) {
    const bones: AnyRecord = {};
    for (const animator of Object.values(item.animators ?? {}) as AnyRecord[]) {
      if (!animator.name) continue;
      const timeline: AnyRecord = {};
      for (const channel of ["rotation", "position", "scale"]) {
        const frames = (animator.keyframes ?? []).filter((key: AnyRecord) => key.channel === channel).map((key: AnyRecord) => ({ time: Number(key.time) * 20, value: frameValue(key.data_points?.[0]), interpolation: key.interpolation ?? "linear" }));
        if (frames.length) timeline[channel] = frames;
      }
      if (Object.keys(timeline).length) bones[animator.name] = timeline;
    }
    animations[item.name] = { loop: item.loop === "loop", length: Math.round(Number(item.length ?? 0) * 20), bones };
  }
  return { geometry: JSON.stringify(geometryOutput, null, 2), animations: JSON.stringify({ format: 1, animations }, null, 2) };
}
