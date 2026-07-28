export type Trigger = "loop" | "random_idle" | "on_screen_open" | "on_totem_activate" | "manual";

export interface ActionBinding {
  animation: string;
  trigger: Trigger;
  priority: number;
  enabled?: boolean;
}

export interface TextureAnimation {
  type: "frame_sequence";
  frames: string[];
  frame_duration: number;
  trigger: Trigger;
  interval?: { min: number; max: number };
  enabled?: boolean;
}

export interface StyleDefinition {
  format: 2;
  id: string;
  name: string;
  model:
    | { type: "minecraft_item"; file: string }
    | { type: "minecraft_bone"; geometry: string; animations?: string };
  textures: Record<string, string>;
  skin?: {
    supported: boolean;
    format: "minecraft_64x64";
    target: string;
    mapping: "minecraft_player";
  };
  features: { animations: boolean; dynamic_textures: boolean };
  animations?: Record<string, ActionBinding>;
  texture_animations?: Record<string, TextureAnimation>;
  [key: string]: unknown;
}

export interface OfficialTemplate {
  id: string;
  name: string;
  character: "Steve" | "Alex";
  description: string;
  source: "builtin" | "style-template";
  preview: "item" | "bone";
  root: string;
  files: string[];
  editable: ("skin" | "actions" | "texture_frames")[];
  style: StyleDefinition;
}

const skin = {
  supported: true,
  format: "minecraft_64x64" as const,
  target: "base",
  mapping: "minecraft_player" as const
};

const actions: Record<string, ActionBinding> = {
  idle_head_shake: { animation: "idle_head_shake", trigger: "loop", priority: 20, enabled: true },
  screen_wave: { animation: "screen_wave", trigger: "on_screen_open", priority: 60, enabled: true },
  totem_struggle: { animation: "struggle", trigger: "on_totem_activate", priority: 100, enabled: true }
};

const blink: TextureAnimation = {
  type: "frame_sequence",
  frames: ["open", "half", "close", "half", "open"],
  frame_duration: 3,
  trigger: "random_idle",
  interval: { min: 80, max: 180 },
  enabled: true
};

const activate: TextureAnimation = {
  type: "frame_sequence",
  frames: ["activate"],
  frame_duration: 40,
  trigger: "on_totem_activate",
  interval: { min: 0, max: 0 },
  enabled: true
};

function item(id: "alex" | "steve", character: "Alex" | "Steve"): OfficialTemplate {
  return {
    id,
    name: `${character} Doll`,
    character,
    description: "经典静态模型，可直接替换 64×64 玩家皮肤。",
    source: "builtin",
    preview: "item",
    root: `templates/${id}`,
    files: ["models/main.json", "textures/base.png"],
    editable: ["skin"],
    style: {
      format: 2,
      id: `template:${id}`,
      name: `${character} Doll`,
      model: { type: "minecraft_item", file: "models/main.json" },
      textures: { base: "textures/base.png" },
      skin,
      features: { animations: false, dynamic_textures: false }
    }
  };
}

function animated(id: "animated_alex" | "animated_steve", character: "Alex" | "Steve"): OfficialTemplate {
  return {
    id,
    name: `Animated ${character} Doll`,
    character,
    description: "内置骨骼模型，包含待机摇头、界面挥手和图腾挣扎动作。",
    source: "builtin",
    preview: "bone",
    root: `templates/${id}`,
    files: ["models/geometry.json", "models/animations.json", "textures/base.png"],
    editable: ["skin", "actions"],
    style: {
      format: 2,
      id: `template:${id}`,
      name: `Animated ${character} Doll`,
      model: {
        type: "minecraft_bone",
        geometry: "models/geometry.json",
        animations: "models/animations.json"
      },
      textures: { base: "textures/base.png" },
      skin,
      features: { animations: true, dynamic_textures: false },
      animations: structuredClone(actions)
    }
  };
}

export const officialTemplates: OfficialTemplate[] = [
  item("steve", "Steve"),
  item("alex", "Alex"),
  {
    id: "blink_alex",
    name: "Blinking Alex Doll",
    character: "Alex",
    description: "静态模型配合睁眼、半闭、闭眼和图腾激活纹理帧。",
    source: "builtin",
    preview: "item",
    root: "templates/blink_alex",
    files: [
      "models/main.json",
      "textures/open.png",
      "textures/half.png",
      "textures/close.png",
      "textures/activate.png"
    ],
    editable: ["texture_frames"],
    style: {
      format: 2,
      id: "template:blink_alex",
      name: "Blinking Alex Doll",
      model: { type: "minecraft_item", file: "models/main.json" },
      textures: {
        base: "textures/open.png",
        open: "textures/open.png",
        half: "textures/half.png",
        close: "textures/close.png",
        activate: "textures/activate.png"
      },
      features: { animations: true, dynamic_textures: true },
      texture_animations: {
        blink: structuredClone(blink),
        activate: structuredClone(activate)
      }
    }
  },
  animated("animated_steve", "Steve"),
  animated("animated_alex", "Alex"),
  {
    ...animated("animated_alex", "Alex"),
    id: "animated_blink_alex",
    name: "Animated Blinking Alex Doll",
    description: "骨骼动作与动态眼睛纹理组合的完整高级模板。",
    root: "templates/animated_blink_alex",
    files: [
      "models/geometry.json",
      "models/animations.json",
      "textures/open.png",
      "textures/half.png",
      "textures/close.png",
      "textures/activate.png"
    ],
    editable: ["actions", "texture_frames"],
    style: {
      format: 2,
      id: "template:animated_blink_alex",
      name: "Animated Blinking Alex Doll",
      model: {
        type: "minecraft_bone",
        geometry: "models/geometry.json",
        animations: "models/animations.json"
      },
      textures: {
        base: "textures/open.png",
        open: "textures/open.png",
        half: "textures/half.png",
        close: "textures/close.png",
        activate: "textures/activate.png"
      },
      features: { animations: true, dynamic_textures: true },
      animations: structuredClone(actions),
      texture_animations: { blink: structuredClone(blink) }
    }
  }
];

export function createProject(template: OfficialTemplate, sequence = Date.now()) {
  const suffix = String(sequence).slice(-6);
  const style = structuredClone(template.style);
  style.id = `player:${template.id}_${suffix}`;
  style.name = `${template.name} Copy`;
  delete style.origin;
  return {
    templateId: template.id,
    pack: {
      format: 1,
      id: `player:totem_pack_${suffix}`,
      name: "My Totem Doll Pack",
      author: "Player",
      styles: [`styles/${template.id}_${suffix}/style.json`]
    },
    style,
    root: template.root,
    files: [...template.files],
    overrides: new Map<string, Blob>()
  };
}
