import type { SVGProps } from "react";

const paths: Record<string, string> = {
  back: "M19 12H5m7-7-7 7 7 7",
  save: "M5 4h11l3 3v13H5zM8 4v6h8V4M8 20v-6h8v6",
  download: "M12 3v11m0 0 4-4m-4 4-4-4M4 18v3h16v-3",
  undo: "M9 7 4 12l5 5M5 12h8a5 5 0 0 1 5 5",
  redo: "m15 7 5 5-5 5m4-5h-8a5 5 0 0 0-5 5",
  play: "m8 5 11 7-11 7z",
  pause: "M8 5v14M16 5v14",
  reset: "M4 12a8 8 0 1 0 3-6m-3-3v5h5",
  pencil: "m4 20 4-1 10-10-3-3L5 16zM14 7l3 3",
  eraser: "m4 16 8-9 7 7-5 5H7zM14 18h6",
  picker: "m4 20 6-2 9-9-4-4-9 9zM13 6l5 5",
  fill: "m5 19 7-7 5 5-7 3zM11 5l8 8",
  upload: "M12 16V4m0 0L8 8m4-4 4 4M4 15v5h16v-5",
  copy: "M8 8h11v12H8zM5 16H4V4h12v1",
  trash: "M5 7h14M10 11v5m4-5v5M8 7l1-3h6l1 3-1 13H9z",
  grid: "M4 4h16v16H4zM4 10h16M10 4v16",
  settings: "M12 8a4 4 0 1 0 0 8 4 4 0 0 0 0-8zm0-5v2m0 10v2m0 5v2M3 12h2m14 0h2M5.6 5.6 7 7m10 10 1.4 1.4M18.4 5.6 17 7M7 17l-1.4 1.4"
};

export function Icon({ name, size = 16, ...props }: { name: keyof typeof paths; size?: number } & SVGProps<SVGSVGElement>) {
  return <svg {...props} width={size} height={size} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true"><path d={paths[name]} /></svg>;
}
