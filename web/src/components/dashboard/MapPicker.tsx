"use client";

import { useEffect, useRef } from "react";

interface MapPickerProps {
  latitude: number;
  longitude: number;
  onChange: (lat: number, lng: number) => void;
}

export default function MapPicker({ latitude, longitude, onChange }: MapPickerProps) {
  const mapRef = useRef<L.Map | null>(null);
  const markerRef = useRef<L.Marker | null>(null);
  const containerRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (!containerRef.current || mapRef.current) return;

    let cancelled = false;

    (async () => {
      const L = (await import("leaflet")).default;

      if (cancelled || !containerRef.current) return;

      const icon = L.divIcon({
        html: `<svg xmlns="http://www.w3.org/2000/svg" width="32" height="40" viewBox="0 0 24 30" fill="none">
          <path d="M12 0C5.37 0 0 5.37 0 12c0 9 12 18 12 18s12-9 12-18c0-6.63-5.37-12-12-12z" fill="#4FC3F7"/>
          <circle cx="12" cy="12" r="5" fill="#1C1C2E"/>
        </svg>`,
        iconSize: [32, 40],
        iconAnchor: [16, 40],
        className: "",
      });

      const map = L.map(containerRef.current, {
        center: [latitude, longitude],
        zoom: 13,
        zoomControl: true,
      });

      const streets = L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
        attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OSM</a>',
        maxZoom: 19,
      });

      const satellite = L.tileLayer("https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}", {
        attribution: '&copy; Esri, Maxar, Earthstar Geographics',
        maxZoom: 19,
      });

      const hybrid = L.layerGroup([
        L.tileLayer("https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}", {
          maxZoom: 19,
        }),
        L.tileLayer("https://server.arcgisonline.com/ArcGIS/rest/services/Reference/World_Boundaries_and_Places/MapServer/tile/{z}/{y}/{x}", {
          attribution: '&copy; Esri',
          maxZoom: 19,
        }),
      ]);

      streets.addTo(map);

      L.control.layers({
        "Streets": streets,
        "Satellite": satellite,
        "Hybrid": hybrid,
      }, {}, { position: "topright" }).addTo(map);

      const marker = L.marker([latitude, longitude], {
        icon,
        draggable: true,
      }).addTo(map);

      marker.on("dragend", () => {
        const pos = marker.getLatLng();
        onChange(
          Math.round(pos.lat * 1000000) / 1000000,
          Math.round(pos.lng * 1000000) / 1000000
        );
      });

      map.on("click", (e: L.LeafletMouseEvent) => {
        const { lat, lng } = e.latlng;
        marker.setLatLng([lat, lng]);
        onChange(
          Math.round(lat * 1000000) / 1000000,
          Math.round(lng * 1000000) / 1000000
        );
      });

      mapRef.current = map;
      markerRef.current = marker;
    })();

    return () => {
      cancelled = true;
      if (mapRef.current) {
        mapRef.current.remove();
        mapRef.current = null;
        markerRef.current = null;
      }
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  // Sync marker when lat/lng change externally
  useEffect(() => {
    if (markerRef.current && mapRef.current) {
      const currentPos = markerRef.current.getLatLng();
      if (
        Math.abs(currentPos.lat - latitude) > 0.000001 ||
        Math.abs(currentPos.lng - longitude) > 0.000001
      ) {
        markerRef.current.setLatLng([latitude, longitude]);
        mapRef.current.panTo([latitude, longitude]);
      }
    }
  }, [latitude, longitude]);

  return (
    <div
      ref={containerRef}
      className="w-full h-[350px] rounded-lg overflow-hidden border border-dark-border"
      style={{ zIndex: 0 }}
    />
  );
}
