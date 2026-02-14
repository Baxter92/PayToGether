import React, { lazy, Suspense } from "react";

const lazyIconCache = new Map<string, React.LazyExoticComponent<any>>();

export function getLazyIcon(name: string) {
  if (lazyIconCache.has(name)) return lazyIconCache.get(name)!;

  const LazyComp = lazy(() =>
    import("lucide-react").then((mod: any) => {
      const Icon = mod[name];
      if (!Icon) {
        // fallback to a simple empty component to avoid crash
        return {
          default: () => (
            <span style={{ display: "inline-block", width: 24, height: 24 }} />
          ),
        };
      }
      return { default: Icon };
    }),
  );

  lazyIconCache.set(name, LazyComp);
  return LazyComp;
}

export function LazyIcon({
  name,
  size = 20,
}: {
  name: string;
  size?: number;
  className?: string;
}) {
  const IconComp = getLazyIcon(name);
  return (
    <Suspense
      fallback={
        <span style={{ display: "inline-block", width: size, height: size }} />
      }
    >
      <IconComp />
    </Suspense>
  );
}
