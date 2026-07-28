import { useEffect, useRef, useState } from "react";
import { Icon } from "./Icon";

type TextureTool = "pencil" | "eraser" | "picker" | "fill";

export function TextureEditor({ blob, onChange }: { blob?: Blob; onChange: (blob: Blob) => void }) {
  const canvasRef = useRef<HTMLCanvasElement>(null);
  const loadedBlob = useRef<Blob>();
  const commitFrame = useRef<number>();
  const [tool, setTool] = useState<TextureTool>("pencil");
  const [color, setColor] = useState("#ffffff");
  const [scale, setScale] = useState(8);
  const [history, setHistory] = useState<ImageData[]>([]);
  const [redoHistory, setRedoHistory] = useState<ImageData[]>([]);
  const [drawing, setDrawing] = useState(false);

  useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas || blob === loadedBlob.current) return;
    let cancelled = false;
    canvas.width = 64;
    canvas.height = 64;
    const ctx = canvas.getContext("2d")!;
    ctx.imageSmoothingEnabled = false;
    ctx.clearRect(0, 0, 64, 64);
    loadedBlob.current = blob;
    setHistory([]);
    setRedoHistory([]);
    if (blob) createImageBitmap(blob).then((image) => {
      if (!cancelled) ctx.drawImage(image, 0, 0, 64, 64);
      image.close();
    });
    return () => { cancelled = true; };
  }, [blob]);

  const emit = () => {
    const canvas = canvasRef.current;
    if (!canvas) return;
    if (commitFrame.current) cancelAnimationFrame(commitFrame.current);
    commitFrame.current = requestAnimationFrame(() => {
      canvas.toBlob((next) => {
        if (next) {
          loadedBlob.current = next;
          onChange(next);
        }
      }, "image/png");
    });
  };

  useEffect(() => () => {
    if (commitFrame.current) cancelAnimationFrame(commitFrame.current);
  }, []);

  const snapshot = () => {
    const ctx = canvasRef.current?.getContext("2d");
    if (!ctx) return;
    setHistory((items) => [...items.slice(-31), ctx.getImageData(0, 0, 64, 64)]);
    setRedoHistory([]);
  };

  const pixel = (event: React.PointerEvent<HTMLCanvasElement>) => {
    const canvas = canvasRef.current;
    if (!canvas) return;
    const rect = canvas.getBoundingClientRect();
    const x = Math.floor((event.clientX - rect.left) / rect.width * 64);
    const y = Math.floor((event.clientY - rect.top) / rect.height * 64);
    if (x < 0 || y < 0 || x >= 64 || y >= 64) return;
    const ctx = canvas.getContext("2d")!;
    const activeTool = event.altKey && tool === "pencil" ? "picker" : tool;
    if (activeTool === "picker") {
      const value = ctx.getImageData(x, y, 1, 1).data;
      setColor(`#${[value[0], value[1], value[2]].map((v) => v.toString(16).padStart(2, "0")).join("")}`);
      if (!event.altKey) setTool("pencil");
      return;
    }
    ctx.globalCompositeOperation = activeTool === "eraser" ? "destination-out" : "source-over";
    ctx.fillStyle = color;
    if (activeTool === "fill") ctx.fillRect(0, 0, 64, 64);
    else ctx.fillRect(x, y, 1, 1);
    ctx.globalCompositeOperation = "source-over";
    emit();
  };

  const undo = () => {
    const canvas = canvasRef.current;
    const ctx = canvas?.getContext("2d");
    const previous = history.at(-1);
    if (!canvas || !ctx || !previous) return;
    setRedoHistory((items) => [...items, ctx.getImageData(0, 0, 64, 64)]);
    setHistory((items) => items.slice(0, -1));
    ctx.putImageData(previous, 0, 0);
    emit();
  };

  const redo = () => {
    const canvas = canvasRef.current;
    const ctx = canvas?.getContext("2d");
    const next = redoHistory.at(-1);
    if (!canvas || !ctx || !next) return;
    setHistory((items) => [...items, ctx.getImageData(0, 0, 64, 64)]);
    setRedoHistory((items) => items.slice(0, -1));
    ctx.putImageData(next, 0, 0);
    emit();
  };

  return <div className="texture-editor" onKeyDown={(event) => {
    if (event.target instanceof HTMLInputElement || event.target instanceof HTMLSelectElement) return;
    if (event.ctrlKey && event.key.toLowerCase() === "z") { event.preventDefault(); event.shiftKey ? redo() : undo(); return; }
    if (event.ctrlKey && event.key.toLowerCase() === "y") { event.preventDefault(); redo(); return; }
    if (event.key.toLowerCase() === "b") setTool("pencil");
    if (event.key.toLowerCase() === "e") setTool("eraser");
    if (event.key.toLowerCase() === "i") setTool("picker");
    if (event.key.toLowerCase() === "f") setTool("fill");
  }} tabIndex={0}>
    <div className="texture-toolbar">
      <ToolButton name="pencil" label="铅笔 (B)" active={tool === "pencil"} onClick={() => setTool("pencil")} />
      <ToolButton name="eraser" label="橡皮 (E)" active={tool === "eraser"} onClick={() => setTool("eraser")} />
      <ToolButton name="picker" label="取色器 (I)" active={tool === "picker"} onClick={() => setTool("picker")} />
      <ToolButton name="fill" label="填充 (F)" active={tool === "fill"} onClick={() => setTool("fill")} />
      <input aria-label="前景色" type="color" value={color} onChange={(event) => setColor(event.target.value)} />
      <ToolButton name="undo" label="撤销 Ctrl+Z" disabled={!history.length} onClick={undo} />
      <ToolButton name="redo" label="重做 Ctrl+Y" disabled={!redoHistory.length} onClick={redo} />
      <button type="button" title="缩放画布" aria-label="缩放画布" onClick={() => setScale((value) => value === 16 ? 4 : value * 2)}><Icon name="settings" /> {scale}×</button>
    </div>
    <div className="texture-editor-hint">按住 Alt 使用取色器 · 拖动连续绘制 · 当前帧独立保存</div>
    <div className="texture-canvas-wrap" style={{ width: 64 * scale, height: 64 * scale }}>
      <canvas
        ref={canvasRef}
        style={{ width: 64 * scale, height: 64 * scale }}
        onPointerDown={(event) => { snapshot(); setDrawing(true); event.currentTarget.setPointerCapture(event.pointerId); pixel(event); }}
        onPointerMove={(event) => { if (drawing) pixel(event); }}
        onPointerUp={(event) => { setDrawing(false); event.currentTarget.releasePointerCapture(event.pointerId); }}
      />
    </div>
  </div>;
}

function ToolButton({ name, label, active, disabled, onClick }: { name: "pencil" | "eraser" | "picker" | "fill" | "undo" | "redo"; label: string; active?: boolean; disabled?: boolean; onClick: () => void }) {
  return <button type="button" className={active ? "selected" : ""} title={label} aria-label={label} disabled={disabled} onClick={onClick}><Icon name={name} /></button>;
}
