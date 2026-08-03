import { describe, expect, it } from "vitest";
import { validateProject } from "./exporter";
import { createProject, officialTemplates } from "../templates/template-registry";

describe("project validation", () => {
  it("accepts a normalized user project", () => {
    const template = officialTemplates[0];
    expect(validateProject(createProject(template, 123456), template)).toEqual([]);
  });

  it("rejects reserved ids and unsafe paths", () => {
    const template = officialTemplates[0];
    const project = createProject(template, 123456);
    project.style.id = "totemdoll:stolen";
    project.style.textures.base = "../secret.png";
    expect(validateProject(project, template).join(" ")).toMatch(/命名空间|不安全/);
  });
  it("rejects non-format-3 projects and unsafe model paths", () => {
    const template = officialTemplates[0];
    const project = createProject(template, 123456);
    (project.style as { format: number }).format = 2;
    project.style.model.geometry = "../native-model.json";
    const errors = validateProject(project, template).join(" ");
    expect(errors).toMatch(/format:3/);
    expect(errors).toMatch(/模型路径不安全/);
  });
});
