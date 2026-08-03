import { describe, expect, it } from "vitest";
import { createProject, officialTemplates } from "./template-registry";

describe("official template registry", () => {
  it("contains all six built-in styles", () => {
    expect(officialTemplates.map((template) => template.id)).toEqual([
      "steve", "alex", "blink_alex", "animated_steve", "animated_alex", "animated_blink_alex"
    ]);
  });

  it("uses one version-independent mesh format", () => {
    expect(officialTemplates.every((template) => template.style.format === 3)).toBe(true);
    expect(officialTemplates.every((template) => template.style.model.type === "mesh")).toBe(true);
    expect(officialTemplates.every((template) => template.style.model.geometry.endsWith("geometry.json"))).toBe(true);
    expect(officialTemplates.every((template) => !template.files.some((path) => path.endsWith("models/main.json")))).toBe(true);
  });

  it("creates an independent user-owned project", () => {
    const template = officialTemplates[1];
    const project = createProject(template, 123456);
    project.style.name = "Changed";
    expect(project.style.id).toBe("player:alex_123456");
    expect(project.style.skin?.target).toBe("base");
    expect(template.style.name).toBe("Alex Doll");
    expect(project.style).not.toHaveProperty("origin");
  });

  it("keeps blink and activation triggers", () => {
    const blink = officialTemplates.find((template) => template.id === "blink_alex")!;
    expect(blink.style.texture_animations?.blink.frames).toEqual(["open", "half", "close", "half", "open"]);
    expect(blink.style.texture_animations?.activate.trigger).toBe("on_totem_activate");
  });
});
