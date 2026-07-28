import JSZip from "jszip";
import type { OfficialTemplate, StyleDefinition } from "../templates/template-registry";

export interface StudioProject {
  templateId: string;
  pack: { format: number; id: string; name: string; author: string; styles: string[] };
  style: StyleDefinition;
  root: string;
  files: string[];
  overrides: Map<string, Blob>;
}

const idPattern = /^[a-z0-9_.-]+:[a-z0-9_./-]+$/;

export function validateProject(project: StudioProject, template: OfficialTemplate) {
  const errors: string[] = [];
  if (!idPattern.test(project.pack.id)) errors.push("样式包 ID 不是合法的 ResourceLocation。");
  if (!idPattern.test(project.style.id)) errors.push("样式 ID 不是合法的 ResourceLocation。");
  if (project.style.id.startsWith("totemdoll:") || project.style.id.startsWith("template:")) errors.push("样式 ID 必须使用自己的命名空间。");
  const known = new Set(template.files);
  for (const path of Object.values(project.style.textures)) {
    if (path.includes("..") || path.startsWith("/") || /^[A-Za-z]:/.test(path)) errors.push(`纹理路径不安全：${path}`);
    if (!known.has(path) && !project.overrides.has(path)) errors.push(`缺少纹理文件：${path}`);
  }
  for (const [name, animation] of Object.entries(project.style.texture_animations ?? {})) {
    for (const frame of animation.frames) {
      const path = project.style.textures[frame];
      if (!path) errors.push(`纹理动画 ${name} 缺少帧槽 ${frame}。`);
      else if (!known.has(path) && !project.overrides.has(path)) errors.push(`纹理动画 ${name} 缺少文件 ${path}。`);
    }
  }
  return [...new Set(errors)];
}

export async function exportProject(project: StudioProject, template: OfficialTemplate) {
  const errors = validateProject(project, template);
  if (errors.length) throw new Error(errors.join("\n"));
  const zip = new JSZip();
  const dir = project.pack.styles[0].replace(/\/style\.json$/, "");
  const style = structuredClone(project.style);
  if (style.animations) for (const value of Object.values(style.animations)) delete value.enabled;
  if (style.texture_animations) for (const value of Object.values(style.texture_animations)) delete value.enabled;
  zip.file("pack.json", JSON.stringify(project.pack, null, 2));
  zip.file(`${dir}/style.json`, JSON.stringify(style, null, 2));
  const assetPaths = [...new Set([
    ...template.files,
    ...Object.values(style.textures),
    ...project.overrides.keys()
  ])].filter((path) => !path.includes(":") && !path.includes("..") && !path.startsWith("/"));
  await Promise.all(assetPaths.map(async (path) => {
    const blob = project.overrides.get(path) ?? await fetch(`${project.root}/${path}`).then((response) => {
      if (!response.ok) throw new Error(`无法读取模板文件：${path}`);
      return response.blob();
    });
    zip.file(`${dir}/${path}`, blob);
  }));
  const output = await zip.generateAsync({ type: "blob" });
  const url = URL.createObjectURL(output);
  const anchor = document.createElement("a");
  anchor.href = url;
  anchor.download = `${project.pack.id.replace(":", "-")}.zip`;
  anchor.click();
  URL.revokeObjectURL(url);
}
