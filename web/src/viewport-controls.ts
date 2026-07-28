import * as THREE from "three";

export interface ViewportControls {
  reset(): void;
  setView(target: THREE.Vector3, position: THREE.Vector3): void;
  dispose(): void;
}

export function installViewportControls(
  camera: THREE.PerspectiveCamera,
  dom: HTMLElement,
  pickRoot: THREE.Object3D,
  initialTarget = new THREE.Vector3()
): ViewportControls {
  const target = initialTarget.clone();
  const initialPosition = camera.position.clone();
  const initialLookAt = initialTarget.clone();
  const raycaster = new THREE.Raycaster();
  const pointer = new THREE.Vector2();
  const spherical = new THREE.Spherical();
  let dragging = false;
  let panning = false;
  let lastX = 0;
  let lastY = 0;

  const syncSpherical = () => spherical.setFromVector3(camera.position.clone().sub(target));
  const updateCamera = () => {
    spherical.phi = THREE.MathUtils.clamp(spherical.phi, 0.05, Math.PI - 0.05);
    spherical.radius = THREE.MathUtils.clamp(spherical.radius, 0.15, 20);
    camera.position.copy(target).add(new THREE.Vector3().setFromSpherical(spherical));
    camera.lookAt(target);
  };
  const raycast = (event: PointerEvent | WheelEvent) => {
    const rect = dom.getBoundingClientRect();
    pointer.set(
      ((event.clientX - rect.left) / Math.max(1, rect.width)) * 2 - 1,
      -((event.clientY - rect.top) / Math.max(1, rect.height)) * 2 + 1
    );
    raycaster.setFromCamera(pointer, camera);
    return raycaster.intersectObject(pickRoot, true)[0]?.point;
  };
  const focusAtPointer = (event: PointerEvent | WheelEvent) => {
    const point = raycast(event);
    if (!point) return false;
    target.copy(point);
    syncSpherical();
    camera.lookAt(target);
    return true;
  };
  const down = (event: PointerEvent) => {
    if (event.button !== 0 && event.button !== 1 && event.button !== 2) return;
    dragging = true;
    panning = event.button === 1 || event.button === 2 || event.shiftKey;
    lastX = event.clientX;
    lastY = event.clientY;
    if (!panning) focusAtPointer(event);
    dom.setPointerCapture(event.pointerId);
  };
  const move = (event: PointerEvent) => {
    if (!dragging) return;
    const dx = event.clientX - lastX;
    const dy = event.clientY - lastY;
    if (panning) {
      const distance = camera.position.distanceTo(target);
      const right = new THREE.Vector3(1, 0, 0).applyQuaternion(camera.quaternion);
      const up = new THREE.Vector3(0, 1, 0).applyQuaternion(camera.quaternion);
      const offset = right.multiplyScalar(-dx * distance * 0.0015)
        .add(up.multiplyScalar(dy * distance * 0.0015));
      target.add(offset);
      camera.position.add(offset);
    } else {
      spherical.theta -= dx * 0.008;
      spherical.phi -= dy * 0.008;
      updateCamera();
    }
    lastX = event.clientX;
    lastY = event.clientY;
  };
  const up = (event: PointerEvent) => {
    dragging = false;
    if (dom.hasPointerCapture(event.pointerId)) dom.releasePointerCapture(event.pointerId);
  };
  const wheel = (event: WheelEvent) => {
    event.preventDefault();
    focusAtPointer(event);
    spherical.radius *= Math.exp(event.deltaY * 0.001);
    updateCamera();
  };
  const doubleClick = (event: MouseEvent) => {
    focusAtPointer(event as unknown as PointerEvent);
  };
  const contextMenu = (event: MouseEvent) => event.preventDefault();
  const reset = () => {
    target.copy(initialLookAt);
    camera.position.copy(initialPosition);
    syncSpherical();
    camera.lookAt(target);
  };
  const setView = (nextTarget: THREE.Vector3, nextPosition: THREE.Vector3) => {
    target.copy(nextTarget);
    camera.position.copy(nextPosition);
    syncSpherical();
    camera.lookAt(target);
  };

  syncSpherical();
  camera.lookAt(target);
  dom.addEventListener("pointerdown", down);
  dom.addEventListener("pointermove", move);
  dom.addEventListener("pointerup", up);
  dom.addEventListener("pointercancel", up);
  dom.addEventListener("wheel", wheel, { passive: false });
  dom.addEventListener("dblclick", doubleClick);
  dom.addEventListener("contextmenu", contextMenu);

  return {
    reset,
    setView,
    dispose() {
      dom.removeEventListener("pointerdown", down);
      dom.removeEventListener("pointermove", move);
      dom.removeEventListener("pointerup", up);
      dom.removeEventListener("pointercancel", up);
      dom.removeEventListener("wheel", wheel);
      dom.removeEventListener("dblclick", doubleClick);
      dom.removeEventListener("contextmenu", contextMenu);
    }
  };
}
