import { useEffect, useState } from 'react';
import { createPortal } from 'react-dom';
import { useMap } from '@vis.gl/react-google-maps';

interface OverlayMarkerProps {
  position: google.maps.LatLngLiteral;
  children: React.ReactNode;
  zIndex?: number;
}

export function OverlayMarker({ position, children, zIndex = 0 }: OverlayMarkerProps) {
  const map = useMap();
  const [container] = useState(document.createElement('div'));
  const [overlay, setOverlay] = useState<google.maps.OverlayView | null>(null);

  useEffect(() => {
    if (!map) return;

    // Create OverlayView instance
    class CustomOverlay extends google.maps.OverlayView {
      onAdd() {
        // floatPane is above overlayLayer but below map controls
        // overlayMouseTarget allows pointer events
        this.getPanes()?.overlayMouseTarget.appendChild(container);
      }
      
      draw() {
        const projection = this.getProjection();
        if (!projection) return;
        
        const point = projection.fromLatLngToDivPixel(new google.maps.LatLng(position));
        if (point) {
          container.style.position = 'absolute';
          container.style.left = `${point.x}px`;
          container.style.top = `${point.y}px`;
          container.style.transform = 'translate(-50%, -50%)'; // Center the marker
          container.style.zIndex = String(zIndex);
          container.style.cursor = 'pointer';
        }
      }
      
      onRemove() {
        if (container.parentElement) {
          container.parentElement.removeChild(container);
        }
      }
    }

    const overlayInstance = new CustomOverlay();
    overlayInstance.setMap(map);
    setOverlay(overlayInstance);

    return () => {
      overlayInstance.setMap(null);
    };
  }, [map, container]); // Re-create overlay if map changes

  // Update overlay when position changes
  useEffect(() => {
    if (overlay) {
      overlay.draw();
    }
  }, [position, overlay]);

  return createPortal(children, container);
}
