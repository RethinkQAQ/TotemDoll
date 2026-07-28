import { useEffect, useMemo, useState } from "react";
import JSZip from "jszip";
import { BonePreview } from "./BonePreview";
import { ItemModelRenderer } from "./ItemModelRenderer";
import { TextureEditor } from "./TextureEditor";
import { TextureTimeline } from "./TextureTimeline";
import { Icon } from "./Icon";
import { assetUrl, httpAssets, type AssetStore } from "./lib/asset-store";
import { exportProject, type StudioProject, validateProject } from "./lib/exporter";
import { readPng } from "./lib/skin";
import {
  minecraftRemoteAssets,
  type MinecraftRemoteAssetStatus
} from "./lib/minecraft-remote-assets";
import {
  createProject,
  officialTemplates,
  type OfficialTemplate,
  type StyleDefinition,
  type Trigger
} from "./templates/template-registry";

const triggers: Trigger[] = ["loop", "random_idle", "on_screen_open", "on_totem_activate", "manual"];

function projectAssets(project: StudioProject): AssetStore {
  return {
    async read(path) {
      const local = project.overrides.get(path);
      if (local) return local;
      const response = await fetch(`${project.root}/${path}`);
      if (!response.ok) throw new Error(`资源不存在：${path}`);
      return response.blob();
    },
    has(path) {
      return project.overrides.has(path) || project.files.includes(path);
    },
    list() {
      return [...new Set([...project.files, ...project.overrides.keys()])];
    }
  };
}

function LicenseNotice({ onClose }: { onClose: () => void }) {
  return (
    <div className="license-backdrop" role="presentation" onClick={onClose}>
      <section className="license-dialog" role="dialog" aria-modal="true" onClick={(event) => event.stopPropagation()}>
        <header>
          <h2>灵感来源与许可证</h2>
          <button type="button" onClick={onClose}>关闭</button>
        </header>
        <p>Totem Doll 工坊的模型读取、UV 映射和编辑器代码均为独立实现。</p>
        <p>
          界面信息架构参考了开源模型编辑器 Blockbench，但没有复制其 GPLv3
          源代码、图标、样式文件或其他资源。
        </p>
        <p>
          <a href="https://github.com/JannisX11/blockbench" target="_blank" rel="noreferrer">Blockbench 项目</a>
          {" · "}
          <a href="https://github.com/JannisX11/blockbench/blob/master/LICENSE.MD" target="_blank" rel="noreferrer">GPLv3 许可证</a>
        </p>
        <p>用户在本工具中创建或导入的模型、纹理和动画仍归用户所有。</p>
      </section>
    </div>
  );
}

export function App() {
  const [project, setProject] = useState<StudioProject | null>(null);
  const [template, setTemplate] = useState<OfficialTemplate | null>(null);
  const [tab, setTab] = useState<"templates" | "convert">("templates");
  const [notice, setNotice] = useState("");
  const [textureAnimation, setTextureAnimation] = useState("");
  const [frame, setFrame] = useState(0);
  const [action, setAction] = useState("");
  const [selectedTextureSlot, setSelectedTextureSlot] = useState("base");
  const [inspector, setInspector] = useState<"project" | "texture" | "action">("project");
  const [playing, setPlaying] = useState(true);
  const [showLicense, setShowLicense] = useState(false);
  const [leftWidth, setLeftWidth] = useState(() => Number(localStorage.getItem("totemdoll.leftWidth")) || 220);
  const [rightWidth, setRightWidth] = useState(() => Number(localStorage.getItem("totemdoll.rightWidth")) || 330);

  const store = useMemo(() => project ? projectAssets(project) : null, [project]);

  useEffect(() => {
    localStorage.setItem("totemdoll.leftWidth", String(leftWidth));
    localStorage.setItem("totemdoll.rightWidth", String(rightWidth));
  }, [leftWidth, rightWidth]);

  useEffect(() => {
    const onKeyDown = (event: KeyboardEvent) => {
      const target = event.target as HTMLElement;
      if (target.matches("input, textarea, select, [contenteditable=true]")) return;
      if (event.code === "Space") { event.preventDefault(); setPlaying((value) => !value); }
      if (event.key.toLowerCase() === "r") document.querySelector<HTMLElement>(".model-viewport")?.dispatchEvent(new Event("model-preview-reset"));
    };
    document.addEventListener("keydown", onKeyDown);
    return () => document.removeEventListener("keydown", onKeyDown);
  }, []);

  const mutate = (fn: (draft: StudioProject) => void) => {
    if (!project) return;
    const draft = {
      ...project,
      pack: { ...project.pack },
      style: structuredClone(project.style),
      overrides: new Map(project.overrides)
    };
    fn(draft);
    setProject(draft);
  };

  const openProject = (nextTemplate: OfficialTemplate, nextProject: StudioProject) => {
    setTemplate(nextTemplate);
    setProject(nextProject);
    setAction(Object.keys(nextProject.style.animations ?? {})[0] ?? "");
    setTextureAnimation(Object.keys(nextProject.style.texture_animations ?? {})[0] ?? "");
    setFrame(0);
    setSelectedTextureSlot(nextProject.style.textures.base ? "base" : Object.keys(nextProject.style.textures)[0] ?? "");
    setInspector("project");
  };

  const useTemplate = async (value: OfficialTemplate) => {
    const next = createProject(value);
    for (const path of value.files) {
      try {
        const response = await fetch(`${value.root}/${path}`);
        if (response.ok) next.overrides.set(path, await response.blob());
      } catch {
        // The preview will report the exact missing resource.
      }
    }
    openProject(value, next);
  };

  const importPack = async (file?: File) => {
    if (!file) return;
    try {
      const zip = await JSZip.loadAsync(file);
      const entry = Object.values(zip.files).find((item) => item.name.endsWith("style.json"));
      if (!entry) throw new Error("ZIP 中没有 style.json");
      const style = JSON.parse(await entry.async("text")) as StyleDefinition;
      const prefix = entry.name.slice(0, -10);
      const files = new Map<string, Blob>();
      for (const item of Object.values(zip.files)) {
        if (!item.dir && item.name.startsWith(prefix) && item.name !== entry.name) {
          files.set(item.name.slice(prefix.length), await item.async("blob"));
        }
      }
      const local: OfficialTemplate = {
        id: "local",
        name: style.name ?? "本地样式",
        character: "Alex",
        description: "从本地 ZIP 导入的样式",
        source: "builtin",
        preview: style.model?.type === "minecraft_bone" ? "bone" : "item",
        root: "local",
        files: [...files.keys()],
        editable: ["skin", "actions", "texture_frames"],
        style
      };
      const next = createProject(local);
      next.style = style;
      next.overrides = files;
      next.files = [...files.keys()];
      openProject(local, next);
    } catch (error) {
      setNotice(error instanceof Error ? error.message : "导入失败");
    }
  };

  if (!project || !template || !store) {
    return (
      <>
        <Library
          tab={tab}
          setTab={setTab}
          onUse={useTemplate}
          onImport={importPack}
          notice={notice}
          setNotice={setNotice}
          onLicense={() => setShowLicense(true)}
        />
        {showLicense && <LicenseNotice onClose={() => setShowLicense(false)} />}
      </>
    );
  }

  const animations = project.style.texture_animations ?? {};
  const currentTextureAnimation = animations[textureAnimation];
  const basePath = project.style.textures.base ?? project.style.textures.open ?? "";
  const activeTextureSlot = selectedTextureSlot;
  const currentPath = project.style.textures[activeTextureSlot] ?? basePath;
  const currentBlob = project.overrides.get(currentPath);
  const model = project.style.model;
  const errors = validateProject(project, template);

  const upload = async (path: string, file?: File) => {
    if (!file) return;
    try {
      const result = await readPng(file);
      mutate((draft) => draft.overrides.set(path, result.blob));
      setNotice(result.converted ? "旧版皮肤已转换为 64×64" : "纹理已替换");
    } catch (error) {
      setNotice(error instanceof Error ? error.message : "纹理读取失败");
    }
  };

  return (
    <main className="editor-shell">
      <header className="editor-top">
        <button type="button" title="返回资源库" aria-label="返回资源库" onClick={() => { setProject(null); setTemplate(null); }}><Icon name="back" /> 资源库</button>
        <strong>{project.style.name}</strong>
        <span className="template-name">{template.name}</span>
        <div className="top-actions">
          <button type="button" title="许可证" aria-label="许可证" onClick={() => setShowLicense(true)}><Icon name="settings" /></button>
          <button type="button" title="保存草稿" aria-label="保存草稿" onClick={() => setNotice("草稿仅保存在当前浏览器页面")}><Icon name="save" /></button>
          <button
            type="button"
            className="primary"
            disabled={errors.length > 0}
            title={errors.join("\n")}
            onClick={() => exportProject(project, template).catch((error) => setNotice(error.message))}
          >
            <Icon name="download" /> 导出 ZIP
          </button>
        </div>
      </header>

      <div className="editor-grid" style={{ "--left-width": `${leftWidth}px`, "--right-width": `${rightWidth}px` } as React.CSSProperties}>
        <aside className="editor-left">
          <h3>资源</h3>
          <button className="tree-item selected">▾ 模型 / {model.type}</button>
          <h3>纹理槽</h3>
          {Object.keys(project.style.textures).map((slot) => (
            <button
              className={inspector === "texture" && activeTextureSlot === slot ? "tree-item selected" : "tree-item"}
              key={slot}
              onClick={() => {
                setTextureAnimation("");
                setFrame(0);
                setSelectedTextureSlot(slot);
                setInspector("texture");
              }}
            >
              ▧ {slot}
            </button>
          ))}
          <h3>纹理动画</h3>
          {Object.keys(animations).map((name) => (
            <button
              className={textureAnimation === name ? "tree-item selected" : "tree-item"}
              key={name}
              onClick={() => {
                setTextureAnimation(name);
                setFrame(0);
                setInspector("texture");
              }}
            >
              ▶ {name}
            </button>
          ))}
          <h3>骨骼动作</h3>
          {Object.keys(project.style.animations ?? {}).map((name) => (
            <button
              className={action === name ? "tree-item selected" : "tree-item"}
              key={name}
              onClick={() => {
                setAction(name);
                setInspector("action");
              }}
            >
              ◇ {name}
            </button>
          ))}
        </aside>
        <ResizeHandle side="left" onResize={(delta) => setLeftWidth((value) => Math.min(360, Math.max(160, value + delta)))} />

        <section className="editor-center">
          <div className="viewport-title">
            <span>实时预览</span>
            <span className="viewport-help">在模型上拖动：围绕光标位置旋转 · 右键/Shift：平移 · 滚轮：缩放</span>
            <button type="button" title={playing ? "暂停" : "播放"} aria-label={playing ? "暂停" : "播放"} onClick={() => setPlaying((value) => !value)}>
              <Icon name={playing ? "pause" : "play"} />
            </button>
          </div>
          {model.type === "minecraft_item" ? (
            <ItemModelRenderer
              store={store}
              modelPath={model.file}
              texturePath={currentPath}
              textureSlots={project.style.textures}
              displayTransform="editor"
            />
          ) : (
            <BonePreview
              store={store}
              geometryPath={model.geometry}
              animationsPath={model.animations}
              texturePath={basePath}
              action={project.style.animations?.[action]?.animation ?? action}
              playing={playing}
              mode="combined"
              displayTransform="editor"
              textureSlots={project.style.textures}
              textureAnimation={currentTextureAnimation}
            />
          )}
          <div className="editor-bottom">
            {currentTextureAnimation && (
              <TextureTimeline animation={currentTextureAnimation} selected={frame} onSelect={setFrame} />
            )}
            <div className="scrub">
              <span>0 tick</span>
              <input type="range" min="0" max="100" defaultValue="0" />
              <span>100 tick</span>
            </div>
          </div>
        </section>
        <ResizeHandle side="right" onResize={(delta) => setRightWidth((value) => Math.min(460, Math.max(260, value - delta)))} />

        <aside className="editor-right">
          <div className="inspector-tabs">
            <button className={inspector === "project" ? "selected" : ""} onClick={() => setInspector("project")}>项目</button>
            <button className={inspector === "texture" ? "selected" : ""} onClick={() => setInspector("texture")}>纹理</button>
            <button className={inspector === "action" ? "selected" : ""} onClick={() => setInspector("action")}>动作</button>
          </div>
          <h3>
            {inspector === "texture"
              ? `编辑纹理 · ${activeTextureSlot || "base"}`
              : inspector === "action" ? `动作 · ${action || "未选择"}` : "项目属性"}
          </h3>
          {inspector === "texture" ? (
            <>
              {currentTextureAnimation && <DynamicTexturePanel
                animation={currentTextureAnimation}
                textures={project.style.textures}
                store={store}
                selected={frame}
                onSelect={(index) => { setFrame(index); setSelectedTextureSlot(currentTextureAnimation.frames[index] ?? selectedTextureSlot); }}
                onUpload={(slot, file) => upload(project.style.textures[slot] ?? currentPath, file)}
                onBaseUpload={(file) => upload(basePath, file)}
                onBaseEdit={() => { setTextureAnimation(""); setSelectedTextureSlot("base"); setInspector("texture"); }}
                onDuplicate={() => mutate((draft) => {
                  if (!textureAnimation || !draft.style.texture_animations) return;
                  const animation = draft.style.texture_animations[textureAnimation];
                  const slot = `${textureAnimation}_frame_${Date.now()}`;
                  const sourceSlot = animation.frames[frame];
                  const sourcePath = draft.style.textures[sourceSlot];
                  const sourceBlob = draft.overrides.get(sourcePath);
                  draft.style.textures[slot] = `textures/${slot}.png`;
                  animation.frames[frame] = slot;
                  if (sourceBlob) draft.overrides.set(draft.style.textures[slot], sourceBlob);
                })}
                onDelete={() => mutate((draft) => {
                  const animation = textureAnimation && draft.style.texture_animations?.[textureAnimation];
                  if (animation && animation.frames.length > 1) animation.frames.splice(frame, 1);
                })}
              />}
              <TextureEditor blob={currentBlob} onChange={(blob) => mutate((draft) => draft.overrides.set(currentPath, blob))} />
            </>
          ) : inspector === "action" ? (
            <>
              <label>当前动作
                <select value={action} onChange={(event) => setAction(event.target.value)}>
                  {Object.keys(project.style.animations ?? {}).map((name) => <option key={name}>{name}</option>)}
                </select>
              </label>
              <label>触发器
                <select
                  value={project.style.animations?.[action]?.trigger ?? "manual"}
                  onChange={(event) => mutate((draft) => {
                    if (action && draft.style.animations) {
                      draft.style.animations[action].trigger = event.target.value as Trigger;
                    }
                  })}
                >
                  {triggers.map((trigger) => <option key={trigger}>{trigger}</option>)}
                </select>
              </label>
              <label>优先级
                <input
                  type="number"
                  value={project.style.animations?.[action]?.priority ?? 0}
                  onChange={(event) => mutate((draft) => {
                    if (action && draft.style.animations) {
                      draft.style.animations[action].priority = Number(event.target.value);
                    }
                  })}
                />
              </label>
            </>
          ) : (
            <>
              <label>样式名称
                <input value={project.style.name} onChange={(event) => mutate((draft) => { draft.style.name = event.target.value; })} />
              </label>
              <label>作者
                <input value={project.pack.author} onChange={(event) => mutate((draft) => { draft.pack.author = event.target.value; })} />
              </label>
              <label className="upload">替换基础纹理
                <input type="file" accept="image/png" onChange={(event) => upload(basePath, event.target.files?.[0])} />
              </label>
            </>
          )}
        </aside>
      </div>
      {notice && <div className="toast" onClick={() => setNotice("")}>{notice}</div>}
      {showLicense && <LicenseNotice onClose={() => setShowLicense(false)} />}
    </main>
  );
}

interface LibraryProps {
  tab: "templates" | "convert";
  setTab: (value: "templates" | "convert") => void;
  onUse: (value: OfficialTemplate) => void;
  onImport: (file?: File) => void;
  notice: string;
  setNotice: (value: string) => void;
  onLicense: () => void;
}

function Library({ tab, setTab, onUse, onImport, notice, setNotice, onLicense }: LibraryProps) {
  return (
    <main className="library">
      <header className="library-bar">
        <MinecraftAssetBadge />
        <strong>Totem Doll / 工坊</strong>
        <button type="button" className="text-button" onClick={onLicense}>灵感来源与许可证</button>
      </header>
      <nav className="tabs">
        <button className={tab === "templates" ? "active" : ""} onClick={() => setTab("templates")}>官方模板</button>
        <button className={tab === "convert" ? "active" : ""} onClick={() => setTab("convert")}>BBModel 转换</button>
      </nav>
      {tab === "templates" ? (
        <section className="library-content">
          <div className="section-title">
            <h1>选择模板</h1>
            <span>{officialTemplates.length} 个官方模板</span>
          </div>
          <div className="template-grid">
            {officialTemplates.map((value) => <TemplateCard key={value.id} template={value} onUse={onUse} />)}
            <label className="template-card add-card">
              <input type="file" accept=".zip,application/zip" onChange={(event) => onImport(event.target.files?.[0])} />
              <span>＋</span>
              <strong>导入本地样式包</strong>
              <small>ZIP · 文件只在浏览器内处理</small>
            </label>
          </div>
        </section>
      ) : <Converter setNotice={setNotice} />}
      {notice && <div className="toast" onClick={() => setNotice("")}>{notice}</div>}
    </main>
  );
}

function MinecraftAssetBadge() {
  const [status, setStatus] = useState<MinecraftRemoteAssetStatus>({
    version: "",
    loading: true,
    cachedAssets: 0,
    error: ""
  });
  useEffect(() => minecraftRemoteAssets.subscribe(setStatus), []);
  useEffect(() => {
    minecraftRemoteAssets.getLatestRelease().catch(() => undefined);
  }, []);

  const label = status.loading
    ? "正在获取最新 Minecraft 资源…"
    : status.version
      ? `Minecraft ${status.version} · 已缓存 ${status.cachedAssets} 个贴图`
      : "Minecraft 原版资源不可用";
  return (
    <span
      className={`minecraft-assets-status ${status.error ? "warning" : ""}`}
      title={status.error || "来源：mcasset.cloud"}
    >
      {label}
    </span>
  );
}

function ResizeHandle({ side, onResize }: { side: "left" | "right"; onResize: (delta: number) => void }) {
  const start = useRefValue();
  return <div
    className={`resize-handle ${side}`}
    role="separator"
    tabIndex={0}
    aria-label={`${side === "left" ? "左侧" : "右侧"}面板宽度`}
    onPointerDown={(event) => {
      start.current = event.clientX;
      event.currentTarget.setPointerCapture(event.pointerId);
    }}
    onPointerMove={(event) => {
      if (start.current === null) return;
      const delta = event.clientX - start.current;
      if (delta) { onResize(delta); start.current = event.clientX; }
    }}
    onPointerUp={() => { start.current = null; }}
    onKeyDown={(event) => {
      if (event.key === "ArrowLeft") onResize(-8);
      if (event.key === "ArrowRight") onResize(8);
    }}
  />;
}

function useRefValue() {
  const ref = useMemo(() => ({ current: null as number | null }), []);
  return ref;
}

function DynamicTexturePanel({
  animation,
  textures,
  store,
  selected,
  onSelect,
  onUpload,
  onBaseUpload,
  onBaseEdit,
  onDuplicate,
  onDelete
}: {
  animation: NonNullable<StyleDefinition["texture_animations"]>[string];
  textures: Record<string, string>;
  store: AssetStore;
  selected: number;
  onSelect: (index: number) => void;
  onUpload: (slot: string, file?: File) => void;
  onBaseUpload: (file?: File) => void;
  onBaseEdit: () => void;
  onDuplicate: () => void;
  onDelete: () => void;
}) {
  return <section className="dynamic-texture-panel">
    <div className="dynamic-texture-heading"><strong>动态纹理</strong><span>{animation.trigger} · {animation.frame_duration} tick</span></div>
    <div className="dynamic-texture-meta">每一帧都可以单独上传、预览和编辑；相同槽位会同步更新。</div>
    <div className="base-texture-row"><span>基础纹理 · base</span><button type="button" title="编辑基础纹理" aria-label="编辑基础纹理" onClick={onBaseEdit}><Icon name="pencil" /></button><label title="上传基础纹理"><Icon name="upload" /><input type="file" accept="image/png" onChange={(event) => onBaseUpload(event.target.files?.[0])} /></label></div>
    <div className="dynamic-frame-list">
      {[...new Set(animation.frames)].map((slot) => <DynamicTextureResource
        key={slot}
        slot={slot}
        path={textures[slot]}
        store={store}
        selected={animation.frames[selected] === slot}
        references={animation.frames.filter((item) => item === slot).length}
        onSelect={() => onSelect(animation.frames.indexOf(slot))}
        onUpload={(file) => onUpload(slot, file)}
      />)}
    </div>
    <div className="dynamic-frame-actions">
      <button type="button" title="复制为独立帧" aria-label="复制为独立帧" onClick={onDuplicate}><Icon name="copy" /> 独立复制</button>
      <button type="button" title="删除当前帧" aria-label="删除当前帧" onClick={onDelete}><Icon name="trash" /> 删除帧</button>
    </div>
  </section>;
}

function DynamicTextureResource({ slot, path, store, index = 0, selected, references, onSelect, onUpload }: {
  slot: string;
  path?: string;
  store: AssetStore;
  index?: number;
  selected: boolean;
  references: number;
  onSelect: () => void;
  onUpload: (file?: File) => void;
}) {
  return <div className={`dynamic-frame-card ${selected ? "selected" : ""}`}>
    <button type="button" className="dynamic-frame-select" onClick={onSelect} title={`选择第 ${index + 1} 帧`}>
      <TextureThumbnail store={store} path={path} />
      <span className="frame-slot">{slot}</span>
      <span className="frame-path">{path ?? "缺少文件"}</span>
    </button>
    <label className="frame-upload" title={`上传 ${slot} 纹理`}>
      <Icon name="upload" />
      <input type="file" accept="image/png" onChange={(event) => onUpload(event.target.files?.[0])} />
    </label>
  </div>;
}

function TextureThumbnail({ store, path }: { store: AssetStore; path?: string }) {
  const [url, setUrl] = useState("");
  useEffect(() => {
    let active = true;
    if (!path) { setUrl(""); return () => { active = false; }; }
    assetUrl(store, path).then((value) => { if (active) setUrl(value); }).catch(() => { if (active) setUrl(""); });
    return () => { active = false; if (url.startsWith("blob:")) URL.revokeObjectURL(url); };
  }, [store, path]);
  return url ? <img className="dynamic-frame-thumb" src={url} alt="" /> : <span className="dynamic-frame-thumb missing">?</span>;
}

function TemplateCard({ template, onUse }: { template: OfficialTemplate; onUse: (value: OfficialTemplate) => void }) {
  const store = useMemo(() => httpAssets(template.root), [template.root]);
  const model = template.style.model;
  const textureAnimation = Object.values(template.style.texture_animations ?? {})[0];
  const texturePath = template.style.textures.base ?? template.style.textures.open;
  const hasAnimation = Boolean(template.style.features?.animations || template.style.features?.dynamic_textures);
  const defaultAction = Object.keys(template.style.animations ?? {}).find((name) => template.style.animations?.[name]?.animation === "idle_head_shake")
    ?? Object.keys(template.style.animations ?? {})[0] ?? "";
  return (
    <article className="template-card model-card">
      <div className="card-model">
        {model.type === "minecraft_bone" ? (
          <BonePreview
            store={store}
            geometryPath={model.geometry}
            animationsPath={model.animations}
            texturePath={texturePath}
            action={template.style.animations?.[defaultAction]?.animation ?? defaultAction}
            playing={hasAnimation}
            mode="bone"
            interactive={false}
            displayTransform="game"
            fitScale={1.25}
            textureSlots={template.style.textures}
            textureAnimation={textureAnimation}
            previewYaw={-18}
          />
        ) : (
          <ItemModelRenderer
            store={store}
            modelPath={model.file}
            texturePath={texturePath}
            textureSlots={template.style.textures}
            textureAnimation={textureAnimation}
            interactive={false}
            displayTransform="game"
            fitScale={1.25}
            previewYaw={-18}
          />
        )}
      </div>
      <div className="card-body">
        <div className="tags">
          <span>{template.character}</span>
          <span>{template.preview === "bone" ? "骨骼" : "Item"}</span>
        </div>
        <h3>{template.name}</h3>
        <p>{template.description}</p>
        <button onClick={() => onUse(template)}>使用模板 →</button>
      </div>
    </article>
  );
}

function Converter({ setNotice }: { setNotice: (value: string) => void }) {
  return (
    <section className="empty-state">
      <h1>BBModel 转换</h1>
      <p>选择 .bbmodel 文件，在浏览器中生成 geometry.json 和 animations.json。</p>
      <label className="dropzone">选择 .bbmodel
        <input
          type="file"
          accept=".bbmodel,.json"
          onChange={async (event) => {
            const file = event.target.files?.[0];
            if (!file) return;
            try {
              const { convertBbmodel } = await import("./lib/bbmodel");
              const result = convertBbmodel(JSON.parse(await file.text()));
              for (const [name, text] of Object.entries(result)) {
                const url = URL.createObjectURL(new Blob([text], { type: "application/json" }));
                const anchor = document.createElement("a");
                anchor.href = url;
                anchor.download = `${name}.json`;
                anchor.click();
                URL.revokeObjectURL(url);
              }
            } catch (error) {
              setNotice(error instanceof Error ? error.message : "转换失败");
            }
          }}
        />
      </label>
    </section>
  );
}
