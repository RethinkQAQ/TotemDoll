export async function readPng(file: File): Promise<{ blob: Blob; width: number; height: number; converted: boolean }> {
  if (file.type !== "image/png") throw new Error("只支持 PNG 纹理。");
  const bitmap = await createImageBitmap(file);
  if (!((bitmap.width === 64 && bitmap.height === 64) || (bitmap.width === 64 && bitmap.height === 32))) {
    bitmap.close();
    throw new Error("纹理必须是 64×64 或旧版 64×32。");
  }
  if (bitmap.height === 64) {
    bitmap.close();
    return { blob: file, width: 64, height: 64, converted: false };
  }
  const canvas = document.createElement("canvas");
  canvas.width = 64;
  canvas.height = 64;
  const context = canvas.getContext("2d")!;
  context.imageSmoothingEnabled = false;
  context.clearRect(0, 0, 64, 64);
  context.drawImage(bitmap, 0, 0);
  // Minecraft 1.8+ legacy conversion: mirror right limbs into left limb base areas.
  mirrorRegion(context, canvas, 0, 16, 16, 16, 16, 48);
  mirrorRegion(context, canvas, 40, 16, 16, 16, 32, 48);
  bitmap.close();
  const blob = await new Promise<Blob>((resolve, reject) =>
    canvas.toBlob((value) => value ? resolve(value) : reject(new Error("PNG 转换失败。")), "image/png")
  );
  return { blob, width: 64, height: 64, converted: true };
}

function mirrorRegion(context: CanvasRenderingContext2D, canvas: HTMLCanvasElement, sx: number, sy: number, width: number, height: number, dx: number, dy: number) {
  context.save();
  context.translate(dx + width, dy);
  context.scale(-1, 1);
  context.drawImage(canvas, sx, sy, width, height, 0, 0, width, height);
  context.restore();
}
